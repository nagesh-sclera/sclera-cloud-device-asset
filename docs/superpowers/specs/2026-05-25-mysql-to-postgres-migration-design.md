# MySQL → PostgreSQL Migration Design

**Date:** 2026-05-25
**Branch:** feature/microservice-sclera2.0
**Approach:** Direct Swap (Approach A)

---

## 1. Scope

Two services are affected. All other services in the monorepo are untouched.

| Service | Nature of change |
|---|---|
| `sclera-cloud-device-asset` | Maven dep, all profile configs, 12 query repos, 3–4 entity models |
| `sclera-vdms-service` | Maven dep, all profile configs |
| `db-init/init.sql` | Full rewrite for PostgreSQL syntax |
| Root `docker-compose.yml` | MySQL container → postgres:16, pgAdmin added, .env wired |
| `sclera-cloud-device-asset/docker-compose.yml` | MySQL container → postgres:16 |

**Not changing:**
- Flyway stays disabled in all profiles
- Hibernate `ddl-auto` values unchanged per profile (`update` / `none`)
- No schema versioning introduced
- All other microservices untouched

---

## 2. PostgreSQL Dev Environment

### Docker Compose additions (root `docker-compose.yml`)

**PostgreSQL 16 container:**
- Image: `postgres:16-alpine`
- Named volume `postgres_data` for persistence across restarts
- Health check via `pg_isready` — dependent services use `depends_on: condition: service_healthy`
- Environment sourced from `.env`: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
- Init script mounted at `/docker-entrypoint-initdb.d/init.sql`
- Host port: `5432`

**pgAdmin 4 container:**
- Image: `dpage/pgadmin4`
- Pre-configured server connection via mounted `pgadmin/servers.json`
- Host port: `5050` → `http://localhost:5050`
- Credentials from `.env`: `PGADMIN_DEFAULT_EMAIL`, `PGADMIN_DEFAULT_PASSWORD`

**Service startup order:**
```
postgres (health_check: healthy)
  └── sclera-cloud-device-asset
  └── sclera-vdms-service
pgadmin (independent)
```

### .env file

New file `.env` (gitignored) sourced by docker-compose:

```
POSTGRES_USER=sclera
POSTGRES_PASSWORD=sclera123
POSTGRES_DB=vdms
PGADMIN_DEFAULT_EMAIL=admin@sclera.com
PGADMIN_DEFAULT_PASSWORD=admin123
DB_URL=jdbc:postgresql://postgres:5432/vdms
DB_USER=sclera
DB_PASS=sclera123
```

`.env.example` committed to repo with placeholder values.

### pgAdmin server pre-configuration

File `pgadmin/servers.json` mounted into pgAdmin container so the PostgreSQL server appears automatically on first login — no manual setup needed.

---

## 3. Maven Dependency Changes

Both `sclera-cloud-device-asset/pom.xml` and `sclera-vdms-service/pom.xml`:

**Remove:**
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Add:**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Version is managed by the Spring Boot parent BOM — no explicit version needed.

---

## 4. Application Configuration Changes

All profile YAML files in both services (`application.yml`, `application-local.yml`, `application-docker.yml`, `application-development.yml`, `application-dev.yml`, `application-qa.yml`, `application-uat.yml`):

| Property | Old value | New value |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://host:3306/db` | `jdbc:postgresql://host:5432/db` |
| `spring.datasource.driver-class-name` | `com.mysql.cj.jdbc.Driver` | `org.postgresql.Driver` |
| `spring.jpa.database-platform` | _(absent or MySQL dialect)_ | `org.hibernate.dialect.PostgreSQLDialect` |
| `sclera.mysql-container-name` | `sclera_database` / `localhost` | renamed to `sclera.db-container-name` |

Hostname mapping per profile:

| Profile | Old host | New host |
|---|---|---|
| local | `localhost` | `localhost` |
| docker | `scleravdmsdatabase` | `postgres` (service name in compose) |
| development / dev / qa / uat | `scleravdmsdatabase` | `postgres` |

---

## 5. SQL Syntax Rewrites

### 5a. ON DUPLICATE KEY UPDATE → ON CONFLICT

This is the largest change. ~153 occurrences across 12 query repository classes and 4 entity models.

**MySQL:**
```sql
INSERT INTO device (id, name, system_type_name)
VALUES (?1, ?2, ?3)
ON DUPLICATE KEY UPDATE system_type_name = VALUES(system_type_name)
```

**PostgreSQL:**
```sql
INSERT INTO device (id, name, system_type_name)
VALUES (?1, ?2, ?3)
ON CONFLICT (id) DO UPDATE SET system_type_name = EXCLUDED.system_type_name
```

Rules:
- `VALUES(col)` → `EXCLUDED.col`
- `ON DUPLICATE KEY UPDATE col = ?N` → `ON CONFLICT (pk_col) DO UPDATE SET col = ?N`
- The conflict target `(pk_col)` must name the primary key or unique constraint column(s) for that table

### 5b. INSERT IGNORE → ON CONFLICT DO NOTHING

**MySQL:** `INSERT IGNORE INTO table (...) VALUES (...)`
**PostgreSQL:** `INSERT INTO table (...) VALUES (...) ON CONFLICT DO NOTHING`

### 5c. Backtick identifiers → double quotes

**MySQL:** `` `condition` ``
**PostgreSQL:** `"condition"`

Affected: `TechnicianAvailability.java` (3 occurrences in `@NamedNativeQuery`).

### 5d. UNIX_TIMESTAMP() → EXTRACT EPOCH

**MySQL:** `UNIX_TIMESTAMP() * 1000`
**PostgreSQL:** `EXTRACT(EPOCH FROM NOW())::BIGINT * 1000`

Affected: `db-init/init.sql`.

### 5e. VALUE typo → VALUES

MySQL tolerates `VALUE (?1, ?2)` as a synonym for `VALUES`. PostgreSQL does not.
All occurrences changed to `VALUES`.

---

## 6. db-init/init.sql Rewrite

The existing file uses MySQL-specific syntax. The rewritten file:
- Uses standard `INTEGER`, `BOOLEAN`, `BIGINT` types (no `TINYINT` for booleans)
- Replaces `INSERT IGNORE` with `INSERT ... ON CONFLICT DO NOTHING`
- Replaces `UNIX_TIMESTAMP() * 1000` with `EXTRACT(EPOCH FROM NOW())::BIGINT * 1000`
- Removes `ENGINE=InnoDB` and `DEFAULT CHARSET` clauses (PostgreSQL has no equivalent)
- Creates the `vdms` table with PostgreSQL-compatible DDL

---

## 7. Files Changed Summary

### sclera-cloud-device-asset
- `pom.xml` — dep swap
- `src/main/resources/application.yml` — datasource + dialect
- `src/main/resources/application-local.yml` — datasource + dialect
- `src/main/resources/application-docker.yml` — datasource + dialect
- `src/main/resources/application-development.yml` — datasource + dialect
- `src/main/resources/application-dev.yml` — datasource + dialect
- `src/main/resources/application-qa.yml` — datasource + dialect
- `src/main/resources/application-uat.yml` — datasource + dialect
- `src/main/java/io/sclera/queryrepository/DeviceQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/LocationQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/FloorQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/BuildingQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/AssetFieldQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/ClientBarCodeQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/ClientNfcQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/ClientQrCodeQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/DeviceTypeQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/DocumentQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/MediaQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/queryrepository/MeasuringInstrumentsQueryRepository.java` — SQL rewrite
- `src/main/java/io/sclera/models/ApplicationUser.java` — SQL rewrite
- `src/main/java/io/sclera/models/System_interface.java` — SQL rewrite
- `src/main/java/io/sclera/models/TechnicianAvailability.java` — SQL rewrite + backtick fix
- `src/main/java/io/sclera/Repository/ApplicationUserRepository.java` — SQL rewrite
- `src/main/java/io/sclera/Repository/AssetDeviceMappingRepository.java` — SQL rewrite
- `sclera-cloud-device-asset/docker-compose.yml` — MySQL → postgres:16

### sclera-vdms-service
- `pom.xml` — dep swap
- `src/main/resources/application.yml` — datasource + dialect
- `src/main/resources/application-local.yml` — datasource + dialect
- `src/main/resources/application-docker.yml` — datasource + dialect

### Root
- `docker-compose.yml` — MySQL → postgres:16, pgAdmin added, depends_on, env vars
- `db-init/init.sql` — full PostgreSQL rewrite
- `.env.example` — new file
- `.env` — new file (gitignored)
- `.gitignore` — add `.env`
- `pgadmin/servers.json` — new file (pgAdmin server config)

---

## 8. Testing Checklist

- [ ] `docker-compose up` starts postgres and both services without errors
- [ ] pgAdmin accessible at `http://localhost:5050`, shows `vdms` database
- [ ] Hibernate schema created automatically on first boot (ddl-auto: update)
- [ ] `db-init/init.sql` seed data inserts without error
- [ ] All upsert paths exercised (device sync, user upsert, asset field upsert)
- [ ] No MySQL driver class not found errors in logs
- [ ] Services reach `UP` health state
