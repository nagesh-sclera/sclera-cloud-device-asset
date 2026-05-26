# Design: Java 21 + Spring Boot 4.0.6 Upgrade and MySQL → PostgreSQL Migration

- **Date:** 2026-05-22
- **Status:** Approved (brainstorming) — plan written; retargeted from Boot 3.4.x to **Boot 4.0.6** at user request
- **Scope:** All 11 Maven modules (2 data services, 8 skeletons, 1 gateway)
- **Author:** platform

> **Version note:** This design originally targeted Spring Boot 3.4.x. The user requires the latest line, so the target is now **Spring Boot 4.0.6** (GA Nov 2025, built on Spring Framework 7 + Jakarta EE 11). The 4.0 line is a materially bigger jump than 3.4.x — it adds **Jackson 3**, **Hibernate 7**, **JUnit 6**, and **Servlet 6.1** on top of everything 3.x already required. Those deltas are called out explicitly below.

## 1. Goal & non-goals

### Goal
Modernize the entire `sclera-cloud-device-asset` workspace onto a current runtime, then move the two data services off MySQL:

1. **Java 11 → Java 21** across all modules.
2. **Spring Boot 2.6.5 → 4.0.6** across all modules (`javax.*` → `jakarta.*` / Jakarta EE 11, Hibernate 5 → 7, Jackson 2 → 3, JUnit 5 → 6).
3. **MySQL → PostgreSQL 16** for `sclera-cloud-device-asset` and `sclera-vdms-service`.

### Non-goals
- No production data migration (greenfield dev only — schema recreates, no data copy).
- No schema redesign — port the existing JPA-defined schema as-is.
- No business-logic changes — behavior is preserved.
- No per-service database carve-out — one PostgreSQL instance, schema-per-service.
- No Boot 4.1.x+ — pin to `4.0.6`.

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
- `javax.websocket-api` — becomes `jakarta.websocket-api` (Jakarta EE 11).
- `com.vladmihalcea:hibernate-types-52:2.21.1` — Hibernate 5 only. Replace with `io.hypersistence:hypersistence-utils-hibernate-70` for **Hibernate 7** (the version the Boot 4 BOM manages).
- `springdoc-openapi-ui:1.6.4` — Boot 2 only. Replace with `springdoc-openapi-starter-webmvc-ui:2.8.17` (the springdoc release validated against Boot 4.0).
- `caffeine:2.9.3` — bump to 3.1.x (Java 17+ baseline).
- **Hardcoded Jackson 2 (`com.fasterxml.jackson.core:jackson-core/databind` + `dataformat-yaml`, 2.11/2.12)** — under Boot 4 these must be **removed** (not just unversioned): Boot 4 manages **Jackson 3** under the new `tools.jackson` group, so the old `com.fasterxml` 2.x coordinates are no longer what the BOM manages.
- micrometer-prometheus (1.9.7) — drop version, inherit from parent.
- `mysql-connector-java` (legacy coordinates) — Phase 3 swaps to `org.postgresql:postgresql`.

## 3. Phasing

Three sequential phases. **Each phase must be GREEN (build + tests pass, containers boot) before the next begins.** Each phase is its own commit, tagged for rollback.

| Phase | What | Gate |
|---|---|---|
| 1 | Java 11 → 21 everywhere; keep Boot version per module. Fix api-gateway's illegal Boot 3.2.5 / Java 11 combo by raising its Java to 21. | All 11 modules compile + test on JDK 21. Containers boot. |
| 2 | Boot → 4.0.6 everywhere; `javax→jakarta` (Jakarta EE 11); Hibernate 7; **Jackson 2→3**; JUnit 6; security DSL fix; springdoc/caffeine/hibernate-types swaps; Spring Cloud train alignment for the gateway. | All modules build + test. E2E smoke through gateway → Dapr sidecar → sclera-audit subscriber. |
| 3 | MySQL → PostgreSQL 16 for the 2 data services. Driver swap, URL change, dialect, Flyway baseline, PG compose container. | Both data services boot against PG; JPA repos pass; E2E smoke clean. |

### Why this order
- Java 21 first is low-risk and de-risks Boot 4 (which **requires** Java 17+; Java 21 is in range, supported through Java 26).
- Boot 4 second is the heavy lift; doing it on a known-good Java 21 base isolates failures to the framework upgrade, not the JDK.
- Postgres last means the DB swap happens **once**, on the final modernized stack — no rework when the namespace / Hibernate version changes underneath it.

## 4. Phase 1 — Java 11 → 21

**Per module:** set `<java.version>21</java.version>` (each module has its own parent; edit each pom).

**Workspace:** ensure JDK 21 is the build JVM (`java -version`, `JAVA_HOME`). Update each module's Dockerfile base image to a JDK 21 runtime (`eclipse-temurin:21-jre-jammy`).

**api-gateway fix:** it is Boot 3.2.5 on Java 11 — illegal. Phase 1 raises it to Java 21, which also makes it legal. (Its Boot version is aligned to 4.0.6 in Phase 2.)

**Expected friction:** strong encapsulation (JPMS) may break reflection-heavy libs (older jackson, lombok). Bump lombok to a 1.18.34+ release that supports 21. Caffeine 2.x is fine on 21 at runtime but is bumped in Phase 2.

**Gate:** `mvn -q clean test` per module on JDK 21; `docker compose build` succeeds; containers reach actuator health.

## 5. Phase 2 — Spring Boot 2.6.5 → 4.0.6 (the heavy lift)

Boot 4.0.6 is built on **Spring Framework 7** and **Jakarta EE 11**, and ships **Hibernate 7, Jackson 3, JUnit 6, Servlet 6.1**. It also removes ~88% of the APIs deprecated across the 2.x/3.x lines. Java baseline is 17 (our Java 21 from Phase 1 is in range).

### 5.1 Parent + properties
- Bump `spring-boot-starter-parent` to `4.0.6` in every module.
- Align api-gateway from 3.2.5 → 4.0.6; if it uses Spring Cloud, move its BOM to the release train that targets Boot 4.0 / Framework 7 (the 2024.0.x/2025.0.x trains target Boot 3.4/3.5 and are NOT compatible — verify on the Spring Cloud compatibility matrix).
- Drop hardcoded versions the 4.0.6 parent now manages (security, micrometer, flyway). **Remove** the hardcoded Jackson 2 deps entirely (see §5.7).

### 5.2 Namespace migration `javax.*` → `jakarta.*` (Jakarta EE 11)
Mechanical but wide. Apply per module:
- `javax.persistence.*` → `jakarta.persistence.*` (all 86 entities)
- `javax.servlet.*` → `jakarta.servlet.*` (Servlet 6.1)
- `javax.validation.*` → `jakarta.validation.*`
- `javax.websocket.*` → `jakarta.websocket.*` (+ dependency coordinate swap)
- `javax.annotation.*` → `jakarta.annotation.*`
Tooling: OpenRewrite recipe `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` as a first pass, then manual sweep for stragglers. Verify with a repo-wide grep for `import javax.(persistence|servlet|validation|websocket|annotation)`. (Leave JDK packages `javax.crypto`/`javax.net`/`javax.imageio` untouched.)

### 5.3 Hibernate 5 → 7
- `hibernate-types-52` → `hypersistence-utils-hibernate-70`; update `@TypeDef`/`@Type` usages to `@Type(JsonType.class)` (the API form is identical across hypersistence's H6/H7 artifacts — the difference is the coordinate).
- Review custom dialects / `@SQLInsert` / naming-strategy beans.
- Check `GenerationType` and identifier generators (Hibernate 7 default sequence/identity behavior differs — relevant to Phase 3 on PG; note any surprises for the schema baseline).

### 5.4 Spring Security 7
- `WebSecurityConfigurerAdapter` is gone → `SecurityFilterChain` bean style. (`WebSecurityConfig` is **already** in this style in the codebase.)
- `antMatchers`/`authorizeRequests` are **removed** in Security 7 (not just deprecated) → must use `requestMatchers`/`authorizeHttpRequests`. This is a hard compile failure until fixed.
- oauth2-resource-server config in the lambda DSL; drop the pinned 3.0.6 coordinate (inherit from parent).

### 5.5 Other removed/changed APIs
- `springdoc-openapi-ui:1.6.4` → `springdoc-openapi-starter-webmvc-ui:2.8.17`.
- Actuator endpoint exposure properties — verify `management.endpoints.web.exposure.include`.
- `spring.redis.*` → `spring.data.redis.*` properties.
- Trailing-slash matching default changed — audit controllers if any rely on it.
- Caffeine → 3.1.x.

### 5.6 Skeletons
Skeletons are thin; the same namespace + parent bump applies, but they have little/no JPA or security. Re-run the verb-aware scaffold tests after the bump to confirm generated controllers still compile under jakarta; update the scaffold template if it emits `javax.*`.

### 5.7 Jackson 2 → 3 (unique to the 4.x line)
The single highest-risk delta vs a 3.4.x upgrade, because the dangerous part fails **silently**.
- **Coordinates/packages:** JSON moves `com.fasterxml.jackson` → `tools.jackson`. The hardcoded Jackson 2 deps in the pom are deleted; the starters pull Jackson 3 transitively. **Exception:** `jackson-annotations` (`@JsonProperty`, `@JsonIgnore`, `@JsonFormat`, …) keeps the `com.fasterxml.jackson.annotation` package/group — do not rename those imports.
- **Entry point:** `new ObjectMapper()` → `tools.jackson.databind.json.JsonMapper.builder()…build()` (or inject the Boot-provided `JsonMapper`); `Jackson2ObjectMapperBuilderCustomizer` → `org.springframework.boot.jackson.JsonMapperBuilderCustomizer`.
- **Silent default flip:** Jackson 3 defaults `WRITE_DATES_AS_TIMESTAMPS` to `false`, so dates serialize as ISO-8601 strings (`"2025-11-06T05:30:00"`) instead of epoch millis (`1699257000000`). Any client/test depending on the old shape breaks at runtime with no compile error. Decision: if a contract needs the old shape, re-enable the feature explicitly; otherwise accept ISO-8601 and record the contract change.

**Gate:** all modules `mvn clean test`; bring up full `docker compose`; trigger an E2E request through `sclera-api-gateway` → Dapr sidecar → publish `device.audit-recorded` → `sclera-audit` subscriber consumes (idempotency store). Confirm a trace appears in Jaeger.

## 6. Phase 3 — MySQL → PostgreSQL 16

Applies only to `sclera-cloud-device-asset` and `sclera-vdms-service`.

### 6.1 Dependency + driver
- Remove `mysql:mysql-connector-java`.
- Add `org.postgresql:postgresql` (version managed by the 4.0.6 parent), `runtime` scope.

### 6.2 Connection + dialect
- `DB_URL`: `jdbc:mysql://host.docker.internal:3306/vdms` → `jdbc:postgresql://host.docker.internal:5432/vdms`.
- Driver class → `org.postgresql.Driver` (Boot autodetects; explicit if set).
- Hibernate dialect → `org.hibernate.dialect.PostgreSQLDialect` (or let Boot autodetect).
- Update `dapr/secrets.local.json` `db.url` and any `application*.yml`/`.properties`.

### 6.3 Schema strategy (greenfield)
The repo has Flyway on the classpath but **zero migration files**. Chosen approach (confirmed in brainstorming):
- **A (chosen): JPA generates, Flyway baselines.** Set `spring.jpa.hibernate.ddl-auto=create` once against an empty PG to materialize the schema, dump it, check that dump in as `V1__baseline.sql`, then switch to `ddl-auto=validate` + `flyway.enabled=true`. Gives a real, version-controlled baseline.
- (Rejected) B: `ddl-auto=update` only — no migration history.

vdms-service shares the `vdms` database; it gets its own schema (`vdms_svc`) to avoid table collisions with cloud-device-asset.

### 6.4 SQL-dialect differences to watch (86 entities, 35 native-query repositories)
- MySQL `AUTO_INCREMENT` (`GenerationType.IDENTITY`) → PG `SERIAL`/`IDENTITY`; verify generators on each `@Id` (compounded by the Hibernate 7 generator-default change from §5.3).
- `tinyint(1)`/boolean handling, `DATETIME` → `timestamp`, backtick identifiers → none in PG (case-folding).
- JSON column types (the `hypersistence` usages) — PG uses native `jsonb`; confirm column definitions.
- Native queries (`@Query(nativeQuery=true)`) with MySQL-specific functions — audit and port: `IFNULL`→`COALESCE`, `GROUP_CONCAT`→`STRING_AGG`, `DATE_FORMAT`→`TO_CHAR`, `STR_TO_DATE`→`TO_TIMESTAMP`, `LIMIT n,m`→`LIMIT m OFFSET n`, `UNIX_TIMESTAMP`→`EXTRACT(EPOCH …)`.

### 6.5 Compose
- Add a `postgres:16` service to `docker-compose.yml` (port 5432, volume, env for db/user/pass aligned with `secrets.local.json`).
- Keep MySQL service definition commented for rollback during the transition, then remove once green.

**Gate:** both data services boot against PG; JPA repository smoke (CRUD on a representative entity per service) passes; full E2E smoke clean; trace visible in Jaeger.

## 7. Testing & verification strategy

- **Per-module unit/integration:** `mvn clean test` is the baseline gate at every phase.
- **Containerized boot:** `docker compose up` — every service reaches `/actuator/health` UP; the test-ui "Skeletons N/8" pill goes green.
- **E2E smoke (Phases 2 & 3):** drive a request through the gateway that exercises Dapr service-invocation, pub/sub (`device.audit-recorded` → sclera-audit), and a JPA write; confirm idempotency store and a Jaeger trace.
- **DB smoke (Phase 3):** CRUD against PG for one entity in each data service.
- **Jackson serialization (Phase 2):** because Jackson 3 changes JSON shape silently, watch any serialization assertions — a green build with a changed shape is the failure mode.
- Test framework moves from JUnit 5 to **JUnit 6** (carried by `spring-boot-starter-test`); no manual test rewrite expected, but flag any JUnit 4 vintage usage.

## 8. Risk register & rollback

| Risk | Likelihood | Mitigation / rollback |
|---|---|---|
| Boot 4 namespace sweep misses an import | Med | OpenRewrite + grep verification; compile fails fast and points at the file |
| **Jackson 3 silent date/shape change breaks a client** | **Med-High** | Dedicated Jackson task; explicitly decide ISO-8601 vs epoch-millis per contract; watch serialization tests |
| Security 7 removed `.authorizeRequests()` | Med | Hard compile failure (not silent) → caught immediately; isolated commit; test gateway auth path |
| Hibernate 7 generator change alters PK behavior on PG | Med | Caught in Phase 3 schema baseline review; pin generators explicitly |
| MySQL-specific native SQL fails on PG (35 repos) | Med | Audit `nativeQuery=true` usages up front (§6.4); port functions; runtime-verify |
| `hypersistence` jsonb mapping drift | Low | Verify each JSON column maps to `jsonb` with a smoke read/write |
| Spring Cloud train incompatible with Boot 4 (gateway) | Med | Verify train on compatibility matrix before bumping the parent |
| api-gateway already on Boot 3.2.5 — partial state | Low | Phase 1 fixes Java; Phase 2 aligns to 4.0.6 with the rest |
| Each phase is an independent commit/tag | — | Rollback = revert the phase commit / reset to prior `phaseN-…-green` tag |

## 9. Out of scope / follow-ups
- Production data migration (separate effort with a real ETL/dump-restore plan).
- Boot 4.1.x+ adoption.
- Per-service physical database separation.
- Replacing AWS SDK v1 (still present alongside v2) — tech-debt cleanup, not blocking.
