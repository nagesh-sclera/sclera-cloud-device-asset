# Design: Java 21 + Spring Boot 3.4.x Upgrade and MySQL → PostgreSQL Migration

- **Date:** 2026-05-22
- **Status:** Approved (brainstorming) — ready for plan
- **Scope:** All 11 Maven modules (2 data services, 8 skeletons, 1 gateway)
- **Author:** platform

## 1. Goal & non-goals

### Goal
Modernize the entire `sclera-cloud-device-asset` workspace onto a current runtime, then move the two data services off MySQL:

1. **Java 11 → Java 21** across all modules.
2. **Spring Boot 2.6.5 → 3.4.x** across all modules (`javax.*` → `jakarta.*`, Hibernate 5 → 6).
3. **MySQL → PostgreSQL 16** for `sclera-cloud-device-asset` and `sclera-vdms-service`.

### Non-goals
- No production data migration (greenfield dev only — schema recreates, no data copy).
- No schema redesign — port the existing JPA-defined schema as-is.
- No business-logic changes — behavior is preserved.
- No per-service database carve-out — one PostgreSQL instance, schema-per-service.
- No Boot 3.5.x+ — pin to the latest 3.4.x patch.

## 2. Current state (verified)

| Module | Boot | Java | DB | Notes |
|---|---|---|---|---|
| sclera-cloud-device-asset | 2.6.5 | 11 | MySQL | Heavy: 60+ deps, websocket/thymeleaf, **Boot-3 artifacts already wedged in** |
| sclera-vdms-service | 2.6.5 | 11 | MySQL | Trivial: web + jpa + mysql only |
| sclera-audit | 2.6.5 | 11 | — | Skeleton; Dapr subscriber |
| sclera-identity | 2.6.5 | 11 | — | Skeleton |
| sclera-alerts | 2.6.5 | 11 | — | Skeleton |
| sclera-inventory | 2.6.5 | 11 | — | Skeleton |
| sclera-workorders | 2.6.5 | 11 | — | Skeleton |
| sclera-inspection | 2.6.5 | 11 | — | Skeleton |
| sclera-integrations | 2.6.5 | 11 | — | Skeleton |
| sclera-edge | 2.6.5 | 17 | — | Skeleton; 10 controllers |
| sclera-api-gateway | **3.2.5** | **11** | — | **Mismatched** — Boot 3 on Java 11 (illegal) |

### Landmines found in `sclera-cloud-device-asset/pom.xml`
- `spring-boot-starter-oauth2-resource-server:3.0.6` — a Boot 3 artifact pinned inside a Boot 2.6.5 build. Must drop the version and inherit from parent.
- `spring-security-config:5.7.3` + `spring-security-web:5.7.3` — hardcoded; must drop versions, inherit from parent.
- `javax.websocket-api` — becomes `jakarta.websocket-api` under Boot 3.
- `com.vladmihalcea:hibernate-types-52:2.21.1` — Hibernate 5 only. Replace with `io.hypersistence:hypersistence-utils-hibernate-63` for Hibernate 6.
- `springdoc-openapi-ui:1.6.4` — Boot 2 only. Replace with `springdoc-openapi-starter-webmvc-ui:2.6.x`.
- `caffeine:2.9.3` — bump to 3.1.x (Java 17+ baseline).
- Hardcoded jackson (2.11/2.12) + micrometer-prometheus (1.9.7) — drop versions, inherit from parent.
- `mysql-connector-java` (legacy coordinates) — Phase 3 swaps to `org.postgresql:postgresql`.

## 3. Phasing

Three sequential phases. **Each phase must be GREEN (build + tests pass, containers boot) before the next begins.** Each phase is its own commit.

| Phase | What | Gate |
|---|---|---|
| 1 | Java 11 → 21 everywhere; keep Boot version per module. Fix api-gateway's illegal Boot 3.2.5 / Java 11 combo by raising its Java to 21. | All 11 modules compile + test on JDK 21. Containers boot. |
| 2 | Boot → 3.4.x everywhere; `javax→jakarta`; Hibernate 6; security rewrite; removed-API audit; springdoc/caffeine/hibernate-types swaps. | All modules build + test. E2E smoke through gateway → Dapr sidecar → sclera-audit subscriber. |
| 3 | MySQL → PostgreSQL 16 for the 2 data services. Driver swap, URL change, dialect, Flyway baseline, PG compose container. | Both data services boot against PG; JPA repos pass; E2E smoke clean. |

### Why this order
- Java 21 first is low-risk and de-risks Boot 3 (which **requires** Java 17+).
- Boot 3 second is the heavy lift; doing it on a known-good Java 21 base isolates failures to the framework upgrade, not the JDK.
- Postgres last means the DB swap happens **once**, on the final modernized stack — no rework when the namespace/Hibernate version changes underneath it.

## 4. Phase 1 — Java 11 → 21

**Per module:** set `<java.version>21</java.version>` (skeletons already have a parent; edit each pom).

**Workspace:** ensure JDK 21 is the build JVM (`java -version`, `JAVA_HOME`). Update each module's Dockerfile base image to a JDK 21 runtime (e.g. `eclipse-temurin:21-jre`).

**api-gateway fix:** it is Boot 3.2.5 on Java 11 — illegal. Phase 1 raises it to Java 21, which also makes it legal. (Its Boot version is handled in Phase 2 alignment to 3.4.x.)

**Expected friction:** strong encapsulation (JPMS) may break reflection-heavy libs (older jackson, lombok). Bump lombok to a 1.18.30+ release that supports 21. Caffeine 2.x is fine on 21 at runtime but is bumped in Phase 2.

**Gate:** `mvn -q clean test` per module on JDK 21; `docker compose build` succeeds; containers reach actuator health.

## 5. Phase 2 — Spring Boot 2.6.5 → 3.4.x (the heavy lift)

### 5.1 Parent + properties
- Bump `spring-boot-starter-parent` to the latest 3.4.x in every module.
- Align api-gateway from 3.2.5 → 3.4.x.
- Drop all hardcoded versions that the 3.4.x parent now manages (security, jackson, micrometer, flyway).

### 5.2 Namespace migration `javax.*` → `jakarta.*`
Mechanical but wide. Apply per module:
- `javax.persistence.*` → `jakarta.persistence.*` (all 86 entities)
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.websocket.*` → `jakarta.websocket.*` (+ dependency coordinate swap)
- `javax.annotation.*` → `jakarta.annotation.*`
Tooling: OpenRewrite recipe `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` as a first pass, then manual sweep for stragglers. Verify with a repo-wide grep for `import javax.(persistence|servlet|validation|websocket|annotation)`.

### 5.3 Hibernate 5 → 6
- `hibernate-types-52` → `hypersistence-utils-hibernate-63`; update `@TypeDef`/`@Type` usages to the Hibernate 6 form (`@Type(JsonType.class)` style).
- Review custom dialects / `@SQLInsert` / naming-strategy beans.
- Check `GenerationType` and identifier generators (Hibernate 6 changed default sequence behavior — relevant to Phase 3 on PG).

### 5.4 Spring Security rewrite
- `WebSecurityConfigurerAdapter` is gone → `SecurityFilterChain` bean style.
- `antMatchers`/`authorizeRequests` → `requestMatchers`/`authorizeHttpRequests`.
- oauth2-resource-server config moves to the new DSL; drop the pinned 3.0.6 coordinate (inherit from parent).

### 5.5 Other removed/changed APIs
- `springdoc-openapi-ui:1.6.4` → `springdoc-openapi-starter-webmvc-ui:2.6.x`.
- Actuator endpoint exposure properties unchanged but verify `management.endpoints.web.exposure.include`.
- `spring.redis.*` → `spring.data.redis.*` properties.
- Trailing-slash matching default changed — audit controllers if any rely on it.
- Caffeine → 3.1.x.

### 5.6 Skeletons
Skeletons are thin; the same namespace + parent bump applies, but they have little/no JPA or security. Re-run the verb-aware scaffold tests after the bump to confirm generated controllers still compile under jakarta.

**Gate:** all modules `mvn clean test`; bring up full `docker compose`; trigger an E2E request through `sclera-api-gateway` → Dapr sidecar → publish `device.audit-recorded` → `sclera-audit` subscriber consumes (idempotency store). Confirm a trace appears in Jaeger.

## 6. Phase 3 — MySQL → PostgreSQL 16

Applies only to `sclera-cloud-device-asset` and `sclera-vdms-service`.

### 6.1 Dependency + driver
- Remove `mysql:mysql-connector-java`.
- Add `org.postgresql:postgresql` (version managed by the 3.4.x parent), `runtime` scope.

### 6.2 Connection + dialect
- `DB_URL`: `jdbc:mysql://host.docker.internal:3306/vdms` → `jdbc:postgresql://host.docker.internal:5432/vdms`.
- Driver class → `org.postgresql.Driver` (Boot autodetects; explicit if set).
- Hibernate dialect → `org.hibernate.dialect.PostgreSQLDialect` (or let Boot 3 autodetect).
- Update `dapr/secrets.local.json` `db.url` and any `application*.yml`/`.properties`.

### 6.3 Schema strategy (greenfield)
The repo has Flyway on the classpath but **zero migration files**. Two options:
- **A (recommended): JPA generates, Flyway baselines.** Set `spring.jpa.hibernate.ddl-auto=create` once against an empty PG to materialize the schema, dump it, then check that dump in as `V1__baseline.sql` and switch to `ddl-auto=validate` + `flyway.enabled=true`. Gives a real, version-controlled baseline.
- **B: `ddl-auto=update` only.** Faster but no migration history; not recommended past the first sprint.

Decision pending in the plan, but A is the target.

### 6.4 SQL-dialect differences to watch (86 entities)
- MySQL `AUTO_INCREMENT` (`GenerationType.IDENTITY`) → PG `SERIAL`/`IDENTITY`; verify generators on each `@Id`.
- `tinyint(1)`/boolean handling, `DATETIME` → `timestamp`, backtick identifiers → none in PG (case-folding).
- JSON column types (the `hibernate-types`/`hypersistence` usages) — PG uses native `jsonb`; confirm column definitions.
- Any native queries (`@Query(nativeQuery=true)`) with MySQL-specific functions (`IFNULL`, `NOW()`, `GROUP_CONCAT`, `LIMIT` syntax) — audit and port to PG equivalents.

### 6.5 Compose
- Add a `postgres:16` service to `docker-compose.yml` (port 5432, volume, env for db/user/pass aligned with `secrets.local.json`).
- Keep MySQL service definition commented for rollback during the transition, then remove once green.

**Gate:** both data services boot against PG; JPA repository smoke (CRUD on a representative entity per service) passes; full E2E smoke clean; trace visible in Jaeger.

## 7. Testing & verification strategy

- **Per-module unit/integration:** `mvn clean test` is the baseline gate at every phase.
- **Containerized boot:** `docker compose up` — every service reaches `/actuator/health` UP; the test-ui "Skeletons N/8" pill goes green.
- **E2E smoke (Phases 2 & 3):** drive a request through the gateway that exercises Dapr service-invocation, pub/sub (`device.audit-recorded` → sclera-audit), and a JPA write; confirm idempotency store and a Jaeger trace.
- **DB smoke (Phase 3):** CRUD against PG for one entity in each data service.
- No new test framework introduced; existing `spring-boot-starter-test` (JUnit 5) carries over.

## 8. Risk register & rollback

| Risk | Likelihood | Mitigation / rollback |
|---|---|---|
| Boot 3 namespace sweep misses an import | Med | OpenRewrite + grep verification; compile fails fast and points at the file |
| Security rewrite breaks auth | Med | Isolate to its own commit; test gateway auth path before merging |
| Hibernate 6 generator change alters PK behavior on PG | Med | Caught in Phase 3 schema baseline review; pin generators explicitly |
| MySQL-specific native SQL fails on PG | Med | Audit `nativeQuery=true` usages up front (§6.4); port functions |
| `hibernate-types` → `hypersistence` mapping drift on jsonb | Low | Verify each JSON column maps to `jsonb` with a smoke read/write |
| api-gateway already on Boot 3.2.5 — partial state | Low | Phase 1 fixes Java; Phase 2 aligns to 3.4.x with the rest |
| Each phase is an independent commit | — | Rollback = revert the phase commit; prior phase stays green |

## 9. Out of scope / follow-ups
- Production data migration (separate effort with a real ETL/dump-restore plan).
- Boot 3.5.x adoption.
- Per-service physical database separation.
- Replacing AWS SDK v1 (still present alongside v2) — tech-debt cleanup, not blocking.
