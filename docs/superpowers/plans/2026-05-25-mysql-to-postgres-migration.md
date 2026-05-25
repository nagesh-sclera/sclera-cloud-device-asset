# MySQL → PostgreSQL Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all MySQL usage with PostgreSQL across `sclera-cloud-device-asset` and `sclera-vdms-service`, including a full-fledged local dev environment with pgAdmin.

**Architecture:** Direct swap — Maven driver, JDBC URLs, Hibernate dialect, and all native SQL upsert syntax changed in one pass. No Flyway, no data migration. Hibernate `ddl-auto` manages schema as before.

**Tech Stack:** Spring Boot 2.6.5, JPA/Hibernate, PostgreSQL 16 (postgres:16-alpine), pgAdmin 4, Docker Compose

---

## File Map

| File | Action |
|---|---|
| `docker-compose.yml` | Add postgres + pgAdmin services; update app/vdms-service DB env vars |
| `.env` | New — docker-compose env source (gitignored) |
| `.env.example` | New — committed template |
| `pgadmin/servers.json` | New — pgAdmin auto-connects to postgres |
| `.gitignore` | Add `.env` |
| `db-init/init.sql` | Full rewrite for PostgreSQL syntax |
| `sclera-cloud-device-asset/docker-compose.yml` | Swap mysql:8 → postgres:16-alpine |
| `sclera-cloud-device-asset/pom.xml` | Swap mysql-connector-java → postgresql |
| `sclera-cloud-device-asset/src/main/resources/application.yml` | URL + driver + dialect |
| `sclera-cloud-device-asset/src/main/resources/application-local.yml` | URL + driver + dialect |
| `sclera-cloud-device-asset/src/main/resources/application-docker.yml` | URL + driver + dialect |
| `sclera-cloud-device-asset/src/main/resources/application-qa.yml` | URL + driver + dialect |
| `sclera-vdms-service/pom.xml` | Swap mysql-connector-java → postgresql |
| `sclera-vdms-service/src/main/resources/application.yml` | URL + driver + dialect |
| `sclera-vdms-service/src/main/resources/application-local.yml` | URL + driver + dialect |
| `sclera-vdms-service/src/main/resources/application-docker.yml` | URL + driver + dialect |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/AssetFieldQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/BuildingQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/FloorQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/LocationQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/MeasuringInstrumentsQueryRepository.java` | No SQL change needed |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientBarCodeQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientNfcQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientQrCodeQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceQueryRepository.java` | ON CONFLICT + EXCLUDED |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceTypeQueryRepository.java` | ON CONFLICT + table-ref |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DocumentQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/MediaQueryRepository.java` | ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/models/ApplicationUser.java` | ON CONFLICT in NamedNativeQuery |
| `sclera-cloud-device-asset/src/main/java/io/sclera/models/System_interface.java` | VALUE→VALUES + ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/models/TechnicianAvailability.java` | Backticks→unquoted + ON CONFLICT |
| `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/ApplicationUserRepository.java` | ON CONFLICT in @Query |
| `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/AssetDeviceMappingRepository.java` | ON CONFLICT in @Query |

---

## Task 1: PostgreSQL Dev Environment (root docker-compose + env files)

**Files:**
- Modify: `docker-compose.yml`
- Create: `.env`
- Create: `.env.example`
- Create: `pgadmin/servers.json`
- Modify: `.gitignore`

- [ ] **Step 1: Add `.env` to `.gitignore`**

Open `.gitignore` and add after the last line:
```
# Local dev secrets
.env
```

- [ ] **Step 2: Create `.env.example`**

Create file `.env.example` at the repo root:
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

- [ ] **Step 3: Create `.env`**

Copy `.env.example` to `.env` (this file is gitignored, will be picked up by docker-compose):
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

- [ ] **Step 4: Create `pgadmin/servers.json`**

Create the directory `pgadmin/` at the repo root and create `pgadmin/servers.json`:
```json
{
  "Servers": {
    "1": {
      "Name": "Sclara PostgreSQL",
      "Group": "Servers",
      "Host": "postgres",
      "Port": 5432,
      "MaintenanceDB": "vdms",
      "Username": "sclera",
      "SSLMode": "prefer"
    }
  }
}
```

- [ ] **Step 5: Update `docker-compose.yml` — add postgres service**

In `docker-compose.yml`, add the postgres service block **before** the `app:` service (after the `redis:` block):
```yaml
  postgres:
    image: postgres:16-alpine
    container_name: sclera-postgres
    env_file: .env
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./db-init/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - sclera-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 5s
      timeout: 3s
      retries: 10
```

- [ ] **Step 6: Update `docker-compose.yml` — add pgadmin service**

Add the pgadmin service block **after** the postgres service:
```yaml
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: sclera-pgadmin
    env_file: .env
    ports:
      - "5050:80"
    volumes:
      - ./pgadmin/servers.json:/pgadmin4/servers.json
    networks:
      - sclera-net
    depends_on:
      postgres:
        condition: service_healthy
```

- [ ] **Step 7: Update `docker-compose.yml` — `app` service (sclera-cloud-device-asset)**

In the `app:` service block, make these changes:

Change `depends_on` to add postgres health check:
```yaml
    depends_on:
      redis:
        condition: service_healthy
      postgres:
        condition: service_healthy
```

Change the `environment` block — replace the DB env vars:
```yaml
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_URL: jdbc:postgresql://postgres:5432/vdms
      DB_USER: sclera
      DB_PASS: sclera123
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
      SPRING_FLYWAY_ENABLED: "false"
```

Remove the `extra_hosts` block entirely (it was only needed to reach the host's MySQL from inside a container).

- [ ] **Step 8: Update `docker-compose.yml` — `vdms-service` service**

In the `vdms-service:` service block, change `depends_on`:
```yaml
    depends_on:
      redis:
        condition: service_healthy
      postgres:
        condition: service_healthy
```

Change `environment`:
```yaml
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_URL: jdbc:postgresql://postgres:5432/vdms
      DB_USER: sclera
      DB_PASS: sclera123
```

Remove the `extra_hosts` block.

- [ ] **Step 9: Add `postgres_data` volume to `docker-compose.yml`**

At the bottom of `docker-compose.yml`, find the `networks:` block. Add a `volumes:` block if not present (or add to existing):
```yaml
volumes:
  postgres_data:

networks:
  sclera-net:
    driver: bridge
```

- [ ] **Step 10: Commit**

```bash
git add docker-compose.yml .env.example pgadmin/servers.json .gitignore
git commit -m "feat(infra): replace MySQL with PostgreSQL 16 + pgAdmin dev environment"
```

---

## Task 2: Rewrite `db-init/init.sql` for PostgreSQL

**Files:**
- Modify: `db-init/init.sql`

- [ ] **Step 1: Replace `db-init/init.sql` entirely**

The existing file uses `INSERT IGNORE` and `UNIX_TIMESTAMP()` which are MySQL-only. Replace the entire contents:
```sql
-- Sclara 2.0 Demo Seed Data
-- Runs automatically on first PostgreSQL container start

CREATE TABLE IF NOT EXISTS vdms (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    property_name VARCHAR(128),
    activation_status VARCHAR(16),
    status INTEGER,
    location TEXT,
    timezone VARCHAR(128),
    address TEXT,
    city VARCHAR(64),
    country VARCHAR(64),
    state VARCHAR(64),
    zip INTEGER,
    image_url VARCHAR(128),
    latitude TEXT,
    longitude TEXT,
    region VARCHAR(32),
    customer_org_id VARCHAR(64),
    adc_configuration_id VARCHAR(64),
    is_master INTEGER,
    has_secondary_device INTEGER,
    secondary_device_id TEXT,
    master_ip TEXT,
    slave_ip TEXT,
    activation_timestamp BIGINT,
    deployment_type VARCHAR(16)
);

INSERT INTO vdms (
    id, property_name, activation_status, status,
    address, city, country, state, zip,
    timezone, region, customer_org_id, adc_configuration_id,
    is_master, has_secondary_device, deployment_type,
    latitude, longitude, activation_timestamp
) VALUES (
    'demo-vdms-001',
    'Sclara HQ — Demo Building',
    'ACTIVE', 1,
    '101 Innovation Drive, Suite 500',
    'San Francisco', 'USA', 'CA', 94105,
    'America/Los_Angeles',
    'West Coast',
    'org-demo-sclara',
    'adc-config-001',
    1, 0, 'cloud',
    '37.7749', '-122.4194',
    EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
) ON CONFLICT (id) DO NOTHING;
```

Changes made:
- `INT` → `INTEGER` (standard SQL, PostgreSQL prefers this)
- `INSERT IGNORE INTO` → `INSERT INTO ... ON CONFLICT (id) DO NOTHING`
- `UNIX_TIMESTAMP() * 1000` → `EXTRACT(EPOCH FROM NOW())::BIGINT * 1000`
- Removed `ENGINE=InnoDB` / `DEFAULT CHARSET` (PostgreSQL has no equivalent)

- [ ] **Step 2: Commit**

```bash
git add db-init/init.sql
git commit -m "feat(db): rewrite init.sql for PostgreSQL syntax"
```

---

## Task 3: Standalone `sclera-cloud-device-asset/docker-compose.yml`

**Files:**
- Modify: `sclera-cloud-device-asset/docker-compose.yml`

- [ ] **Step 1: Replace the `mysql` service block**

Find:
```yaml
  mysql:
    image: mysql:8
    container_name: sclera-cloud-device-asset-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: sclera_cloud_device_asset
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
```

Replace with:
```yaml
  postgres:
    image: postgres:16-alpine
    container_name: sclera-cloud-device-asset-postgres
    environment:
      POSTGRES_USER: sclera
      POSTGRES_PASSWORD: sclera123
      POSTGRES_DB: vdms
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sclera -d vdms"]
      interval: 5s
      timeout: 3s
      retries: 10
```

- [ ] **Step 2: Update the `app` service**

Change `depends_on: - mysql` to:
```yaml
    depends_on:
      postgres:
        condition: service_healthy
```

Change:
```yaml
      SPRING_DATASOURCE_URL: "jdbc:mysql://sclera-cloud-device-asset-mysql:3306/sclera_cloud_device_asset"
      SPRING_DATASOURCE_USERNAME: "root"
      SPRING_DATASOURCE_PASSWORD: "root"
```
to:
```yaml
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/vdms"
      SPRING_DATASOURCE_USERNAME: "sclera"
      SPRING_DATASOURCE_PASSWORD: "sclera123"
```

- [ ] **Step 3: Update the `volumes` block at the bottom**

Change `mysql_data:` → `postgres_data:`:
```yaml
volumes:
  postgres_data:
```

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/docker-compose.yml
git commit -m "feat(device-asset): swap standalone MySQL container for PostgreSQL 16"
```

---

## Task 4: `sclera-cloud-device-asset` Maven Dependency

**Files:**
- Modify: `sclera-cloud-device-asset/pom.xml` (lines 111–115)

- [ ] **Step 1: Replace MySQL driver with PostgreSQL driver**

Find in `pom.xml` (around line 112):
```xml
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
```

Replace with:
```xml
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

No version tag — Spring Boot 2.6.5 BOM manages the PostgreSQL driver version (42.x).

- [ ] **Step 2: Verify no other mysql references in pom.xml**

Run:
```bash
grep -i mysql sclera-cloud-device-asset/pom.xml
```
Expected: no output (zero matches).

- [ ] **Step 3: Commit**

```bash
git add sclera-cloud-device-asset/pom.xml
git commit -m "feat(device-asset): swap mysql-connector-java for postgresql driver"
```

---

## Task 5: `sclera-cloud-device-asset` Application YAMLs

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/resources/application.yml`
- Modify: `sclera-cloud-device-asset/src/main/resources/application-local.yml`
- Modify: `sclera-cloud-device-asset/src/main/resources/application-docker.yml`
- Modify: `sclera-cloud-device-asset/src/main/resources/application-qa.yml`

> All 6 profile YAMLs must be updated: `application.yml`, `application-local.yml`, `application-docker.yml`, `application-qa.yml`, `application-dev.yml`, `application-development.yml`, `application-uat.yml`. The steps below show the pattern; apply identically to `application-dev.yml`, `application-development.yml`, and `application-uat.yml` — same URL, same driver, same dialect changes as `application-qa.yml`.

- [ ] **Step 1: Update `application.yml` (base / server profile)**

Find and replace the `spring.datasource` block (around lines 12–27):
```yaml
  datasource:
    url: jdbc:postgresql://postgres:5432/vdms
    username: sclerauser
    password: WySq1@Sclera
    driver-class-name: org.postgresql.Driver
    hikari:
      minimum-idle: 1
      maximum-pool-size: 500
      idle-timeout: 200000
      pool-name: SpringBootJPAHikariCP
      max-lifetime: 2000000
      connection-timeout: 30000
      leak-detection-threshold: 10000
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
```

- [ ] **Step 2: Update `application-local.yml`**

Find the `datasource.url` line (around line 21):
```yaml
    url: ${DB_URL:jdbc:mysql://localhost:3306/vdms}
```
Replace with:
```yaml
    url: ${DB_URL:jdbc:postgresql://localhost:5432/vdms}
    driver-class-name: org.postgresql.Driver
```

Add `database-platform` under `jpa:` (after the `jpa:` key, before `hibernate:`):
```yaml
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

- [ ] **Step 3: Update `application-docker.yml`**

Find:
```yaml
    url: ${DB_URL:jdbc:mysql://scleravdmsdatabase:3306/vdms}
```
Replace with:
```yaml
    url: ${DB_URL:jdbc:postgresql://postgres:5432/vdms}
    driver-class-name: org.postgresql.Driver
```

Add `database-platform` under `jpa:`:
```yaml
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

- [ ] **Step 4: Update `application-qa.yml`**

Find:
```yaml
    url: jdbc:mysql://scleravdmsdatabase:3306/vdms
    username: sclerauser
    password: WySq1@Sclera
```
Replace with:
```yaml
    url: jdbc:postgresql://postgres:5432/vdms
    username: sclerauser
    password: WySq1@Sclera
    driver-class-name: org.postgresql.Driver
```

Add `database-platform` under `jpa:`:
```yaml
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

- [ ] **Step 5: Verify no MySQL URLs remain in this service**

```bash
grep -ri "mysql" sclera-cloud-device-asset/src/main/resources/
```
Expected: no output.

- [ ] **Step 6: Commit**

```bash
git add sclera-cloud-device-asset/src/main/resources/
git commit -m "feat(device-asset): update datasource to PostgreSQL across all profiles"
```

---

## Task 5b: Rename `sclera.mysql-container-name` → `sclera.db-container-name`

**Files:**
- Modify: All 7 profile YAMLs in `sclera-cloud-device-asset/src/main/resources/`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/utils/ResourceUrlConfig.java`

> `ResourceUrlConfig.java` injects this property with `@Value("${sclera.mysql-container-name}")`. Renaming requires updating both the YAML key and the Java `@Value` reference simultaneously.

- [ ] **Step 1: Rename the property key in all 7 profile YAMLs**

In each of these files, find:
```yaml
  mysql-container-name: <value>
```
Replace with:
```yaml
  db-container-name: <value>
```

Apply to all 7 files:
- `application.yml` (value: `sclera_database`)
- `application-local.yml` (value: `localhost`)
- `application-docker.yml` (value: `scleravdmsdatabase` → also change value to `postgres`)
- `application-qa.yml` (value: `sclera_database`)
- `application-dev.yml` (value: `sclera_database`)
- `application-development.yml` (value: `sclera_database`)
- `application-uat.yml` (value: `sclera_database`)

- [ ] **Step 2: Update the `@Value` reference in `ResourceUrlConfig.java`**

Open `sclera-cloud-device-asset/src/main/java/io/sclera/utils/ResourceUrlConfig.java`.

Find (around line 25):
```java
    @Value("${sclera.mysql-container-name}")
```
Replace with:
```java
    @Value("${sclera.db-container-name}")
```

- [ ] **Step 3: Verify no remaining references**

```bash
grep -ri "mysql-container-name" sclera-cloud-device-asset/src/
```
Expected: zero output.

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/
git commit -m "feat(device-asset): rename sclera.mysql-container-name to sclera.db-container-name"
```

---

## Task 6: `sclera-vdms-service` Maven Dep + All YAMLs

**Files:**
- Modify: `sclera-vdms-service/pom.xml`
- Modify: `sclera-vdms-service/src/main/resources/application.yml`
- Modify: `sclera-vdms-service/src/main/resources/application-local.yml`
- Modify: `sclera-vdms-service/src/main/resources/application-docker.yml`

- [ ] **Step 1: Swap Maven driver in `sclera-vdms-service/pom.xml`**

Find (around line 28):
```xml
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
```
Replace with:
```xml
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

- [ ] **Step 2: Update `sclera-vdms-service/src/main/resources/application.yml`**

Current content:
```yaml
server:
  port: 8089

spring:
  application:
    name: sclera-vdms-service
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/vdms}
    username: ${DB_USER:root}
    password: ${DB_PASS:mypass123}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
```

Replace the entire datasource + jpa block with:
```yaml
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/vdms}
    username: ${DB_USER:sclera}
    password: ${DB_PASS:sclera123}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
    show-sql: false
```

- [ ] **Step 3: Update `application-local.yml`**

Find:
```yaml
    url: ${DB_URL:jdbc:mysql://localhost:3306/vdms}
    username: ${DB_USER:root}
    password: ${DB_PASS:mypass123}
    driver-class-name: com.mysql.cj.jdbc.Driver
```
Replace with:
```yaml
    url: ${DB_URL:jdbc:postgresql://localhost:5432/vdms}
    username: ${DB_USER:sclera}
    password: ${DB_PASS:sclera123}
    driver-class-name: org.postgresql.Driver
```

Add `database-platform` under `jpa:`:
```yaml
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: false
```

- [ ] **Step 4: Update `application-docker.yml`**

Find:
```yaml
    url: ${DB_URL:jdbc:mysql://scleravdmsdatabase:3306/vdms}
    username: ${DB_USER:root}
    password: ${DB_PASS:mypass123}
    driver-class-name: com.mysql.cj.jdbc.Driver
```
Replace with:
```yaml
    url: ${DB_URL:jdbc:postgresql://postgres:5432/vdms}
    username: ${DB_USER:sclera}
    password: ${DB_PASS:sclera123}
    driver-class-name: org.postgresql.Driver
```

Add `database-platform` under `jpa:`:
```yaml
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: false
```

- [ ] **Step 5: Verify no MySQL references remain in vdms-service**

```bash
grep -ri "mysql" sclera-vdms-service/
```
Expected: no output.

- [ ] **Step 6: Commit**

```bash
git add sclera-vdms-service/
git commit -m "feat(vdms-service): swap MySQL driver and datasource config for PostgreSQL"
```

---

## Task 7: Simple Query Repos — AssetField, Building, Floor, Location

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/AssetFieldQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/BuildingQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/FloorQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/LocationQueryRepository.java`

> `MeasuringInstrumentsQueryRepository` has no upsert — it's already PostgreSQL-compatible, skip it.

**Translation rule for all tasks below:**
- `ON DUPLICATE KEY UPDATE col = VALUES(col)` → `ON CONFLICT (id) DO UPDATE SET col = EXCLUDED.col`
- `ON DUPLICATE KEY UPDATE col = ?N` → `ON CONFLICT (id) DO UPDATE SET col = ?N` (literal parameter ref stays unchanged)

- [ ] **Step 1: Replace `AssetFieldQueryRepository.java`**

Replace the entire `getQueryForUpsertAssetField()` method body:
```java
    public String getQueryForUpsertAssetField() {
        return "INSERT INTO asset_field(" +
                "id, name, type, tool_tip, default_value, is_active, options, is_deleted, show_in_section, created_at " +
                ") VALUES (?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, type = EXCLUDED.type, tool_tip = EXCLUDED.tool_tip, " +
                "default_value = EXCLUDED.default_value, is_active = EXCLUDED.is_active, " +
                "options = EXCLUDED.options, is_deleted = EXCLUDED.is_deleted, " +
                "show_in_section = EXCLUDED.show_in_section, created_at = EXCLUDED.created_at";
    }
```

- [ ] **Step 2: Replace `BuildingQueryRepository.java`**

Replace the entire `getQueryForUpsertBuilding()` method body:
```java
    public String getQueryForUpsertBuilding() {
        return "INSERT INTO building(" +
                "id, name, vdms_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, vdms_id = EXCLUDED.vdms_id, " +
                "updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";
    }
```

- [ ] **Step 3: Replace `FloorQueryRepository.java`**

Replace the entire `getQueryForUpsertFloor()` method body:
```java
    public String getQueryForUpsertFloor() {
        return "INSERT INTO floor(" +
                "id, name, building_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, building_id = EXCLUDED.building_id, " +
                "updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";
    }
```

- [ ] **Step 4: Replace `LocationQueryRepository.java`**

Replace the entire `getQueryForUpsertLocation()` method body:
```java
    public String getQueryForUpsertLocation() {
        return "INSERT INTO location(" +
                "id, name, code, floor_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, code = EXCLUDED.code, floor_id = EXCLUDED.floor_id, " +
                "updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";
    }
```

(`getQueryForUpdateLocationRecordChecklistStatus()` uses plain `UPDATE` — no change needed.)

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/AssetFieldQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/BuildingQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/FloorQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/LocationQueryRepository.java
git commit -m "feat(device-asset): convert AssetField/Building/Floor/Location upserts to PostgreSQL ON CONFLICT"
```

---

## Task 8: Scanner Code Query Repos — ClientBarCode, ClientNfc, ClientQrCode

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientBarCodeQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientNfcQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientQrCodeQueryRepository.java`

> These queries insert 10–11 columns. The last column is hardcoded `false` (not a `?` param). PostgreSQL allows literal values in the INSERT list, so `false` stays as-is.
> Note: the trailing `;` inside the Java string is harmless in MySQL but will cause a syntax error via JDBC in PostgreSQL. Remove it.

- [ ] **Step 1: Replace `ClientBarCodeQueryRepository.java`**

```java
    public String getQueryForUpsertClientBarCode() {
        return "INSERT INTO client_bar_code (id, added_at, added_by, client_bar_code_id, device_id, location_id, updated_at, updated_by, vdms_id, batch_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,false) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "added_at = EXCLUDED.added_at, "
                + "added_by = EXCLUDED.added_by, "
                + "client_bar_code_id = EXCLUDED.client_bar_code_id, "
                + "device_id = EXCLUDED.device_id, "
                + "location_id = EXCLUDED.location_id, "
                + "updated_at = EXCLUDED.updated_at, "
                + "updated_by = EXCLUDED.updated_by, "
                + "vdms_id = EXCLUDED.vdms_id, "
                + "batch_id = EXCLUDED.batch_id, "
                + "is_deleted = false";
    }
```

- [ ] **Step 2: Replace `ClientNfcQueryRepository.java`**

```java
    public String getQueryForUpsertClientNfc() {
        return "INSERT INTO client_nfc (id, batch_id, created_by, creation_time, device_id, location_id, nfc_id, uuid, vdms_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?,false) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "batch_id = EXCLUDED.batch_id, "
                + "created_by = EXCLUDED.created_by, "
                + "creation_time = EXCLUDED.creation_time, "
                + "device_id = EXCLUDED.device_id, "
                + "location_id = EXCLUDED.location_id, "
                + "nfc_id = EXCLUDED.nfc_id, "
                + "uuid = EXCLUDED.uuid, "
                + "vdms_id = EXCLUDED.vdms_id, "
                + "is_deleted = false";
    }
```

- [ ] **Step 3: Replace `ClientQrCodeQueryRepository.java`**

```java
    public String getQueryForUpsertClientQrcodes() {
        return "INSERT INTO client_qr_code (id, added_at, added_by, client_qr_code_id, device_id, location_id, updated_at, updated_by, vdms_id, batch_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,false) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "added_at = EXCLUDED.added_at, "
                + "added_by = EXCLUDED.added_by, "
                + "client_qr_code_id = EXCLUDED.client_qr_code_id, "
                + "device_id = EXCLUDED.device_id, "
                + "location_id = EXCLUDED.location_id, "
                + "updated_at = EXCLUDED.updated_at, "
                + "updated_by = EXCLUDED.updated_by, "
                + "vdms_id = EXCLUDED.vdms_id, "
                + "batch_id = EXCLUDED.batch_id, "
                + "is_deleted = false";
    }
```

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientBarCodeQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientNfcQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/ClientQrCodeQueryRepository.java
git commit -m "feat(device-asset): convert ClientBarCode/Nfc/QrCode upserts to PostgreSQL ON CONFLICT"
```

---

## Task 9: Device Query Repos — Device + DeviceType

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceTypeQueryRepository.java`

- [ ] **Step 1: Replace `DeviceQueryRepository.java` — `getQueryForUpsertCollection()`**

This is a 33-column upsert. Replace the entire `getQueryForUpsertCollection()` method:
```java
    public String getQueryForUpsertCollection() {
        return "INSERT INTO device (" +
                "id, system_type_name, asset_type_name, asset_sub_type_name, adc_json, created_email, assigned_user_email, system_type_id, asset_type_id, asset_sub_type_id, " +
                "location_id, docker_name, type, monitor, docker_vdms_id, virtual_device_type, asset_match_status, created_timestamp, asset_group, onboard_status, category, sub_category, " +
                "location_status, source_type, display_name, model, vendor, serial_number, warranty, description, user_data_name, user_data_model, user_data_vendor " +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "system_type_name = EXCLUDED.system_type_name, " +
                "asset_type_name = EXCLUDED.asset_type_name, " +
                "asset_sub_type_name = EXCLUDED.asset_sub_type_name, " +
                "adc_json = EXCLUDED.adc_json, " +
                "created_email = EXCLUDED.created_email, " +
                "assigned_user_email = EXCLUDED.assigned_user_email, " +
                "system_type_id = EXCLUDED.system_type_id, " +
                "asset_type_id = EXCLUDED.asset_type_id, " +
                "asset_sub_type_id = EXCLUDED.asset_sub_type_id, " +
                "location_id = EXCLUDED.location_id, " +
                "docker_name = EXCLUDED.docker_name, " +
                "type = EXCLUDED.type, " +
                "monitor = EXCLUDED.monitor, " +
                "docker_vdms_id = EXCLUDED.docker_vdms_id, " +
                "virtual_device_type = EXCLUDED.virtual_device_type, " +
                "asset_match_status = EXCLUDED.asset_match_status, " +
                "created_timestamp = EXCLUDED.created_timestamp, " +
                "asset_group = EXCLUDED.asset_group, " +
                "onboard_status = EXCLUDED.onboard_status, " +
                "category = EXCLUDED.category, " +
                "sub_category = EXCLUDED.sub_category, " +
                "location_status = EXCLUDED.location_status, " +
                "source_type = EXCLUDED.source_type, " +
                "display_name = EXCLUDED.display_name, " +
                "model = EXCLUDED.model, " +
                "vendor = EXCLUDED.vendor, " +
                "serial_number = EXCLUDED.serial_number, " +
                "warranty = EXCLUDED.warranty, " +
                "description = EXCLUDED.description, " +
                "user_data_name = EXCLUDED.user_data_name, " +
                "user_data_model = EXCLUDED.user_data_model, " +
                "user_data_vendor = EXCLUDED.user_data_vendor";
    }
```

(`getQueryForUpdateDeviceRecordChecklistStatus()` and `getQueryForUpdateImage()` use plain `UPDATE` — no change needed.)

- [ ] **Step 2: Replace `DeviceTypeQueryRepository.java` — `getQueryForUpsertDeviceTypesInBatch()`**

The original query captures the old name with `old_name = name` (referencing the table's current value). In PostgreSQL this becomes `old_name = device_types.name` (table-qualified reference to current row value):

```java
    public String getQueryForUpsertDeviceTypesInBatch() {
        return "INSERT INTO device_types (id, name, updated_timestamp) " +
                "VALUES (?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "old_name = device_types.name, " +
                "name = EXCLUDED.name, " +
                "updated_timestamp = EXCLUDED.updated_timestamp";
    }
```

- [ ] **Step 3: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceTypeQueryRepository.java
git commit -m "feat(device-asset): convert Device and DeviceType upserts to PostgreSQL ON CONFLICT"
```

---

## Task 10: Document + Media Query Repos

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DocumentQueryRepository.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/MediaQueryRepository.java`

> `device_document` and `device_media` are junction tables. Their "upsert" just re-inserts the same composite key — `DO NOTHING` is the correct PostgreSQL equivalent since the intent is idempotent linking.

- [ ] **Step 1: Replace `DocumentQueryRepository.java`**

```java
    public String getQueryForUpsertDocument() {
        return "INSERT INTO document (id, name, category, description, link, created_email, created_timestamp, encrypted_type, source_type) " +
                "VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, category = EXCLUDED.category, " +
                "description = EXCLUDED.description, link = EXCLUDED.link, " +
                "encrypted_type = EXCLUDED.link";
    }

    public String getQueryForTagDocument() {
        return "INSERT INTO device_document (document_id, device_id) VALUES (?,?) " +
                "ON CONFLICT (document_id, device_id) DO NOTHING";
    }

    public String getCountUpdateQuery() {
        return "UPDATE device SET document_count = (SELECT COUNT(*) FROM device_document WHERE device_id = ?), " +
                "media_count = (SELECT COUNT(*) FROM device_media WHERE device_id = ?) WHERE id = ?";
    }
```

- [ ] **Step 2: Replace `MediaQueryRepository.java`**

```java
    public String getQueryForUpsertMedia() {
        return "INSERT INTO media (id, name, category, description, link, created_email, created_timestamp, extension, source_type) " +
                "VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, category = EXCLUDED.category, " +
                "description = EXCLUDED.description, link = EXCLUDED.link";
    }

    public String getQueryForTagDocument() {
        return "INSERT INTO device_media (media_id, device_id) VALUES (?,?) " +
                "ON CONFLICT (media_id, device_id) DO NOTHING";
    }
```

- [ ] **Step 3: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DocumentQueryRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/MediaQueryRepository.java
git commit -m "feat(device-asset): convert Document and Media upserts to PostgreSQL ON CONFLICT"
```

---

## Task 11: Entity Model — `ApplicationUser.java`

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/models/ApplicationUser.java`

- [ ] **Step 1: Fix the `upsertApplicationUsers` NamedNativeQuery (line 7)**

Find:
```java
    @NamedNativeQuery(name = "ApplicationUser.upsertApplicationUsers", query = "INSERT INTO application_user (id, technician_id, email, type) VALUES (?1, ?2, ?3, ?4) ON DUPLICATE KEY UPDATE technician_id = ?2, email = ?3, type = ?4", resultClass = ApplicationUser.class),
```

Replace with:
```java
    @NamedNativeQuery(name = "ApplicationUser.upsertApplicationUsers", query = "INSERT INTO application_user (id, technician_id, email, type) VALUES (?1, ?2, ?3, ?4) ON CONFLICT (id) DO UPDATE SET technician_id = ?2, email = ?3, type = ?4", resultClass = ApplicationUser.class),
```

All other `@NamedNativeQuery` entries in this file use plain `UPDATE`, `SELECT`, or `DELETE` — no changes needed.

- [ ] **Step 2: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/ApplicationUser.java
git commit -m "feat(device-asset): fix ApplicationUser upsert NamedNativeQuery for PostgreSQL"
```

---

## Task 12: Entity Model — `System_interface.java`

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/models/System_interface.java`

> This file has two issues: `VALUE` (MySQL synonym for `VALUES`) and `ON DUPLICATE KEY UPDATE`.

- [ ] **Step 1: Fix the `upsertInterfaceStatus` NamedNativeQuery (line 45)**

Find:
```java
    @NamedNativeQuery(name = "System_interface.upsertInterfaceStatus", query = "INSERT INTO system_interface(interface_name, status) VALUE (?1, ?2) ON DUPLICATE KEY UPDATE status = ?2", resultClass = System_interface.class),
```

Replace with:
```java
    @NamedNativeQuery(name = "System_interface.upsertInterfaceStatus", query = "INSERT INTO system_interface(interface_name, status) VALUES (?1, ?2) ON CONFLICT (interface_name) DO UPDATE SET status = ?2", resultClass = System_interface.class),
```

Changes: `VALUE` → `VALUES`, `ON DUPLICATE KEY UPDATE` → `ON CONFLICT (interface_name) DO UPDATE SET`. The PK of `system_interface` is `interface_name` (the `@Id` field).

All other queries in this file use plain `SELECT`, `UPDATE`, `DELETE` — no changes needed.

- [ ] **Step 2: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/System_interface.java
git commit -m "feat(device-asset): fix System_interface upsert NamedNativeQuery for PostgreSQL"
```

---

## Task 13: Entity Model — `TechnicianAvailability.java`

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/models/TechnicianAvailability.java`

> This file has three issues: (1) backtick-quoted `` `condition` `` in SQL queries — PostgreSQL uses double quotes for identifiers, but `condition` is not a reserved word in PostgreSQL SQL so no quoting is needed at all. (2) `@Column(name = "`condition`")` — same fix. (3) `ON DUPLICATE KEY UPDATE` in the upsert query. (4) `@ColumnResult(name = "`condition`")` in the `@SqlResultSetMapping`.

- [ ] **Step 1: Fix `@SqlResultSetMapping` — `@ColumnResult(name = "`condition`")`**

On line 7, find:
```java
@ColumnResult(name = "`condition`", type = String.class)
```
Replace with:
```java
@ColumnResult(name = "condition", type = String.class)
```

- [ ] **Step 2: Fix all NamedNativeQueries — replace backtick `` `condition` `` with `condition`**

In lines 9–18, every query that references `` `condition` `` must have the backticks removed. Go through each `@NamedNativeQuery` and apply the following:

Line 9 (`getAllTechnicianAvailability`):
```
`condition` → condition
```

Line 10 (`getTechnicianAvailabilityById`):
```
`condition` → condition
```

Line 11 (`getTechnicianAvailabilityInRange`):
```
`condition` → condition
```

Line 12 (`createTechnicianAvailability`):
```
`condition` → condition
```

Line 13 (`updateTechnicianAvailability`):
```
`condition` → condition
```

Line 18 (`upsertTechnicianAvailability`):
```
`condition` → condition
```
Also change `ON DUPLICATE KEY UPDATE` → `ON CONFLICT (id) DO UPDATE SET` in this query. Full replacement for line 18:
```java
    @NamedNativeQuery(name = "TechnicianAvailability.upsertTechnicianAvailability", query = "INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9) ON CONFLICT (id) DO UPDATE SET start_date = ?2, end_date = ?3, start_time = ?4, end_time = ?5, is_all_day = ?6, frequency = ?7, condition = ?8", resultClass = TechnicianAvailability.class)
```

- [ ] **Step 3: Fix `@Column` annotation on the `condition` field (line 33)**

Find:
```java
    @Column(name = "`condition`")
    private String condition;
```
Replace with:
```java
    @Column(name = "condition")
    private String condition;
```

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/TechnicianAvailability.java
git commit -m "feat(device-asset): fix TechnicianAvailability - remove MySQL backticks and convert upsert to PostgreSQL"
```

---

## Task 14: Repository — `ApplicationUserRepository.java`

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/ApplicationUserRepository.java`

- [ ] **Step 1: Fix the `upsertApplicationUsers` @Query (lines 20–24)**

Find:
```java
    @Query(value = "INSERT INTO application_user (id, technician_id, email, type) " +
            "VALUES (?1, ?2, ?3, ?4) " +
            "ON DUPLICATE KEY UPDATE " +
            "technician_id = ?2, email = ?3, type = ?4", nativeQuery = true)
    Integer upsertApplicationUsers(String id, String technicianId, String email, String type);
```

Replace with:
```java
    @Query(value = "INSERT INTO application_user (id, technician_id, email, type) " +
            "VALUES (?1, ?2, ?3, ?4) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "technician_id = ?2, email = ?3, type = ?4", nativeQuery = true)
    Integer upsertApplicationUsers(String id, String technicianId, String email, String type);
```

All other `@Query` methods in this file use plain `UPDATE`, `SELECT`, or `DELETE` — no changes needed.

- [ ] **Step 2: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/ApplicationUserRepository.java
git commit -m "feat(device-asset): fix ApplicationUserRepository upsert @Query for PostgreSQL"
```

---

## Task 15: Repository — `AssetDeviceMappingRepository.java`

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/AssetDeviceMappingRepository.java`

- [ ] **Step 1: Fix the `saveNewAssetMapping` @Query (line 19)**

Find:
```java
  @Query(value = "INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id) VALUES(?1,?2,?3,?4) ON DUPLICATE KEY UPDATE match_score=?2", nativeQuery = true)
  void saveNewAssetMapping(String id, Integer match_score, Asset asset, Device device);
```

Replace with:
```java
  @Query(value = "INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id) VALUES(?1,?2,?3,?4) ON CONFLICT (id) DO UPDATE SET match_score=?2", nativeQuery = true)
  void saveNewAssetMapping(String id, Integer match_score, Asset asset, Device device);
```

- [ ] **Step 2: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/AssetDeviceMappingRepository.java
git commit -m "feat(device-asset): fix AssetDeviceMappingRepository upsert @Query for PostgreSQL"
```

---

## Task 16: Smoke Test — Verify Full Dev Stack

- [ ] **Step 1: Verify no MySQL references remain anywhere**

```bash
grep -ri "mysql" \
  sclera-cloud-device-asset/src \
  sclera-cloud-device-asset/pom.xml \
  sclera-vdms-service/src \
  sclera-vdms-service/pom.xml \
  docker-compose.yml \
  db-init/init.sql
```
Expected: **zero output**.

- [ ] **Step 2: Build both services locally (no DB needed)**

```bash
cd sclera-cloud-device-asset && mvn clean compile -q && cd ..
cd sclera-vdms-service && mvn clean compile -q && cd ..
```
Expected: `BUILD SUCCESS` for both. No `ClassNotFoundException: com.mysql.cj.jdbc.Driver`.

- [ ] **Step 3: Bring up the full dev stack**

```bash
docker-compose up --build -d postgres pgadmin
```
Wait ~10 seconds for postgres health check to pass, then:
```bash
docker-compose up --build -d app vdms-service
```

- [ ] **Step 4: Verify postgres is healthy**

```bash
docker-compose ps postgres
```
Expected: `Status: healthy`

- [ ] **Step 5: Verify pgAdmin is reachable**

Open `http://localhost:5050` in a browser.
- Login with `admin@sclera.com` / `admin123`
- Expand Servers → "Sclara PostgreSQL" → should connect without password prompt
- Navigate to `vdms` database → Tables → confirm `vdms` table exists with seed row

- [ ] **Step 6: Verify app service started**

```bash
docker-compose logs app | grep -E "Started|ERROR|mysql"
```
Expected: `Started Sclera in X seconds` with no MySQL driver errors.

- [ ] **Step 7: Verify vdms-service started**

```bash
docker-compose logs vdms-service | grep -E "Started|ERROR|mysql"
```
Expected: `Started` with no MySQL driver errors.

- [ ] **Step 8: Final commit**

```bash
git add .
git commit -m "feat: complete MySQL → PostgreSQL migration with full dev environment"
```
