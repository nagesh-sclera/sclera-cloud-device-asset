# Phase 2 — Boot 4.0.6 Migration Surface Inventory

BOOT_VERSION=4.0.6

Date: 2026-05-25
Branch: feature/microservice-sclera2.0
Scope: repo root `sclera-cloud-device-asset/` only (sclera-vdms-edge-server excluded).

---

## Step 1 — Boot 4.0.6 Resolution + Managed Versions

### Resolution check

```
.\mvnw.cmd dependency:get -Dartifact="org.springframework.boot:spring-boot-starter-parent:4.0.6:pom"
```
Result: **BUILD SUCCESS** — artifact resolved from Maven Central.

```
.\mvnw.cmd dependency:get -Dartifact="org.springframework.boot:spring-boot-dependencies:4.0.6:pom"
```
Result: **BUILD SUCCESS** — BOM downloaded to local .m2.

### Managed versions (from spring-boot-dependencies-4.0.6.pom)

| Property | Value |
|---|---|
| `hibernate.version` | **7.2.12.Final** |
| `jackson-bom.version` (Jackson 3) | **3.1.2** (under `tools.jackson`) |
| `jackson-2-bom.version` (Jackson 2 compat) | 2.21.2 (separate compatibility BOM; legacy only) |
| `mockito.version` | 5.20.0 |
| `junit-jupiter.version` | 6.0.3 |

Key implication: Boot 4 manages **Hibernate 7.x** and **Jackson 3.x**. All pinned com.fasterxml.jackson 2.x
dependencies must be removed; all `hibernate-types-52` usages must migrate to the Hibernate 7 variant.

---

## Step 2 — javax Imports (Jakarta Surface)

Command run:
```
grep -rln "import javax\.(persistence|servlet|validation|websocket|annotation|transaction)" --include=*.java .
```

### Per-module file counts

| Module | Files with javax imports |
|---|---|
| sclera-alerts | 0 |
| sclera-api-gateway | 0 |
| sclera-audit | **1** |
| sclera-cloud-device-asset | **147** |
| sclera-edge | 0 |
| sclera-identity | 0 |
| sclera-inspection | 0 |
| sclera-integrations | 0 |
| sclera-inventory | 0 |
| sclera-vdms-service | **3** |
| sclera-workorders | 0 |
| **TOTAL** | **151 files** |

**Dominant surface:** `sclera-cloud-device-asset` module (147 files — models, repositories, services,
controllers, DTOs, configs, exceptions). This is the entire JPA/persistence layer using
`javax.persistence.*`, `javax.validation.*`, `javax.transaction.*`, and `javax.servlet.*`.

Migration action (Phase 2 execution): bulk s/javax\./jakarta./ on `*.java` across all 151 files.
Exception: `javax.websocket-api` artifact (see Step 3) must also be replaced with the Jakarta
WebSocket API artifact.

Note: `javax.crypto`, `javax.net`, `javax.imageio` (JDK builtins) are NOT counted — the grep
pattern above already excludes them.

---

## Step 3 — Boot-2-Only / Pinned Dependencies Across poms

Command:
```
grep -rn "hibernate-types-52|springdoc-openapi-ui|spring-security-config|spring-security-web|
oauth2-resource-server|mysql-connector-java|javax.websocket-api|jackson-core|jackson-databind|
jackson-dataformat|caffeine|byte-buddy.version|spring-framework.version|json-smart.version|
spring-boot-maven-plugin" --include=pom.xml .
```

### sclera-cloud-device-asset/pom.xml

| Line | Artifact / Property | Current Value | Boot-4 Action |
|---|---|---|---|
| 21 | `<byte-buddy.version>` | 1.14.18 | **REMOVABLE** under Boot 4 — Boot 4 manages byte-buddy; do not remove now, revisit after bump |
| 22 | `<json-smart.version>` | 2.5.1 | **REMOVABLE** under Boot 4 — Boot 4 manages nimbus/json-smart; do not remove now |
| 23 | `<spring-framework.version>` | 5.3.39 | **REMOVE** — this pins Spring 5.x and will block Boot 4 (which pulls Spring 6.x) |
| 74 | `javax.websocket-api` (javax.websocket:javax.websocket-api) | no version (managed by Boot 2.6.5) | **REPLACE** with `jakarta.websocket:jakarta.websocket-api` (Boot 4 manages it) |
| 116 | `mysql:mysql-connector-java` | no version (runtime) | **(Phase 3)** Replace with `org.postgresql:postgresql` OR `com.mysql:mysql-connector-j` (Boot 4 drops the old groupId) |
| 126 | `com.fasterxml.jackson.core:jackson-core` | 2.12.0 (pinned) | **REMOVE** — Boot 4 manages Jackson 3 via tools.jackson BOM |
| 131 | `com.fasterxml.jackson.core:jackson-databind` | 2.11.3 (pinned) | **REMOVE** — Boot 4 manages Jackson 3 |
| 136 | `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | 2.12.0 (pinned) | **REMOVE** — Boot 4 manages Jackson 3 dataformat |
| 172 | `org.springdoc:springdoc-openapi-ui` | 1.6.4 | **REPLACE** with `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17` |
| 246 | `spring-security-config` | 5.7.3 (pinned) | **DROP version** — inherit from Boot 4 parent (Spring Security 6.x) |
| 251 | `spring-security-web` | 5.7.3 (pinned) | **DROP version** — inherit from Boot 4 parent |
| 275 | `spring-boot-starter-oauth2-resource-server` | 3.0.6 (pinned) | **DROP version** — inherit from Boot 4 parent |
| 308 | `com.vladmihalcea:hibernate-types-52` | 2.21.1 | **REPLACE** with `io.hypersistence:hypersistence-utils-hibernate-70:3.x` (matching Hibernate 7.2.x managed by Boot 4) |
| 346-348 | `com.github.ben-manes.caffeine:caffeine` | 3.1.8 (pinned) | **DROP version** — Boot 4 manages caffeine; pin currently explicit, remove when bumping parent |
| 460 | `spring-boot-maven-plugin` | 2.7.18 (pinned) | **REMOVE explicit version** — will inherit 4.0.6 from parent once parent is bumped |

### sclera-vdms-service/pom.xml

| Line | Artifact / Property | Current Value | Boot-4 Action |
|---|---|---|---|
| 17 | `<byte-buddy.version>` | 1.14.18 | **REMOVABLE** (revisit after bump) |
| 18 | `<json-smart.version>` | 2.5.1 | **REMOVABLE** (revisit after bump) |
| 19 | `<spring-framework.version>` | 5.3.39 | **REMOVE** — blocks Boot 4 |
| 32 | `mysql:mysql-connector-java` | no version (runtime) | **(Phase 3)** Replace groupId/artifactId |
| 40 | `spring-boot-maven-plugin` | 2.7.18 (pinned) | **REMOVE explicit version** |

### sclera-identity/pom.xml

| Line | Property | Current Value | Boot-4 Action |
|---|---|---|---|
| 18 | `<byte-buddy.version>` | 1.14.18 | REMOVABLE (revisit) |
| 19 | `<json-smart.version>` | 2.5.1 | REMOVABLE (revisit) |
| 20 | `<spring-framework.version>` | 5.3.39 | **REMOVE** |
| 51 | `spring-boot-maven-plugin` | (artifact ref only, check version) | **REMOVE version** if pinned |

### sclera-audit/pom.xml

| Line | Property | Current Value | Boot-4 Action |
|---|---|---|---|
| 18 | `<byte-buddy.version>` | 1.14.18 | REMOVABLE (revisit) |
| 19 | `<json-smart.version>` | 2.5.1 | REMOVABLE (revisit) |
| 20 | `<spring-framework.version>` | 5.3.39 | **REMOVE** |
| 56 | `spring-boot-maven-plugin` | (artifact ref) | **REMOVE version** if pinned |

### sclera-integrations / sclera-alerts / sclera-inspection / sclera-inventory / sclera-workorders / sclera-edge / sclera-api-gateway — same pattern

All 8 remaining modules carry:
- `<byte-buddy.version>1.14.18</byte-buddy.version>` — REMOVABLE under Boot 4
- `<json-smart.version>2.5.1</json-smart.version>` — REMOVABLE under Boot 4
- `<spring-framework.version>5.3.39</spring-framework.version>` — **MUST REMOVE** (pins Spring 5)
- `spring-boot-maven-plugin` artifact reference (verify each has explicit version pin) — **REMOVE version**

### Summary of Boot-4 replacement mapping

| Old artifact | Replacement |
|---|---|
| `com.vladmihalcea:hibernate-types-52:2.21.1` | `io.hypersistence:hypersistence-utils-hibernate-70` (check latest for Hibernate 7.2.x) |
| `org.springdoc:springdoc-openapi-ui:1.6.4` | `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17` |
| `spring-security-config:5.7.3` | drop version (inherit Spring Security 6.x) |
| `spring-security-web:5.7.3` | drop version (inherit) |
| `spring-boot-starter-oauth2-resource-server:3.0.6` | drop version (inherit) |
| `com.fasterxml.jackson.core:jackson-core:2.12.0` | REMOVE (Boot 4 manages Jackson 3 under tools.jackson) |
| `com.fasterxml.jackson.core:jackson-databind:2.11.3` | REMOVE |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.0` | REMOVE |
| `mysql:mysql-connector-java` | (Phase 3) `com.mysql:mysql-connector-j` or `org.postgresql:postgresql` |
| `<byte-buddy.version>1.14.18` | likely REMOVABLE — Boot 4 manages; do NOT remove yet |
| `<json-smart.version>2.5.1` | likely REMOVABLE — Boot 4 manages; do NOT remove yet |
| `<spring-framework.version>5.3.39` | **REMOVE NOW** — actively blocks Boot 4 |
| `javax.websocket:javax.websocket-api` | `jakarta.websocket:jakarta.websocket-api` |
| `spring-boot-maven-plugin:2.7.18` (pinned) | remove explicit version (inherit from Boot 4 parent) |

---

## Step 4 — Deprecated/Removed Security DSL Usage (live, non-commented)

Command:
```
grep -rn "authorizeRequests|WebSecurityConfigurerAdapter|antMatchers|EnableGlobalMethodSecurity" \
  --include=*.java . | grep -v "//"
```

### Live hits

| File | Line | Usage |
|---|---|---|
| `sclera-cloud-device-asset/src/main/java/io/sclera/config/WebSecurityConfig.java` | 156 | `.authorizeRequests()` — live, active security chain |
| `sclera-cloud-device-asset/src/main/java/io/sclera/config/DockerSecurityConfig.java` | 20 | `.authorizeRequests(auth -> auth.anyRequest().permitAll())` |

**Commented-out hits (not counted as live):**
- `WebSecurityConfig.java:8,11,23,24,50,51` — all commented with `//`
- `WebSecurityConfig.java:50-51` — old `.authorizeRequests().antMatchers(...)` block, already commented

### Migration action
Both live `.authorizeRequests()` calls must be replaced with `.authorizeHttpRequests()` (Spring Security 6+
/ Boot 3+). Boot 4 removes the deprecated `authorizeRequests()` DSL entirely.

- `WebSecurityConfig.java:156`: `.authorizeRequests()` → `.authorizeHttpRequests()`
- `DockerSecurityConfig.java:20`: `.authorizeRequests(auth -> ...)` → `.authorizeHttpRequests(auth -> ...)`

`WebSecurityConfigurerAdapter`, `antMatchers`, and `@EnableGlobalMethodSecurity` are already commented out —
no live removal needed, but the commented code should be cleaned up in Phase 2 execution.

---

## Step 5 — spring.redis.* Property Usage

Command:
```
grep -rn "redis" --include=*.yml --include=*.properties .
```

### Hits requiring rename

| File | Lines | Current key | Required key (Boot 4) |
|---|---|---|---|
| `sclera-cloud-device-asset/src/main/resources/application-docker.yml` | 9-11 | `spring.redis.host` / `spring.redis.port` | `spring.data.redis.host` / `spring.data.redis.port` |

Full block in `application-docker.yml`:
```yaml
spring:
  redis:           # <-- MUST BECOME: data.redis
    host: sclera-redis
    port: 6379
```

No other `spring.redis.*` blocks found in `.yml` or `.properties` files.

Note: `spring-boot-starter-data-redis` is present as an active dependency in
`sclera-cloud-device-asset/pom.xml:433`. The Redis autoconfiguration will fail to bind at startup under
Boot 3+/4+ if the property namespace is not updated before the parent bump.

---

## Step 6 — Jackson 2 Code Usage (Jackson 3 Migration Surface)

Command:
```
grep -rln "com\.fasterxml\.jackson\.(databind|core|dataformat)|new ObjectMapper|Jackson2ObjectMapperBuilder" \
  --include=*.java .
```

### Files with genuine databind/core/dataformat or ObjectMapper usage (16 files — all in sclera-cloud-device-asset)

| File | Imports used |
|---|---|
| `controller/admin/MediaController.java` | jackson.core.JsonProcessingException, jackson.databind.{DeserializationFeature, JsonMappingException, ObjectMapper} |
| `controller/admin/DocumentController.java` | jackson.core.JsonProcessingException, jackson.databind.{DeserializationFeature, JsonMappingException, ObjectMapper} |
| `controller/admin/DeviceController.java` | jackson.core.JsonProcessingException, jackson.databind.JsonMappingException |
| `controller/admin/DeviceTechnicianAISuggestionController.java` | jackson.core.JsonProcessingException |
| `utils/Utils.java` | jackson.core.{JsonProcessingException}, jackson.databind.{DeserializationFeature, JsonMappingException, JsonNode, ObjectMapper, type.CollectionType}, jackson.dataformat.yaml.{YAMLFactory, YAMLMapper} |
| `utils/DockerUtils.java` | jackson.core.{JsonProcessingException, type.TypeReference}, jackson.databind.ObjectMapper |
| `utils/ProcessData.java` | jackson.databind.annotation.JsonSerialize |
| `utils/InstrumentFormula.java` | jackson.databind.ObjectMapper |
| `service/DeviceService.java` | jackson.core.JsonProcessingException, jackson.databind.{JsonNode, ObjectMapper} |
| `service/BuildingService.java` | jackson.core.JsonProcessingException |
| `service/FloorService.java` | jackson.core.JsonProcessingException, jackson.databind.ObjectMapper |
| `service/LocationService.java` | jackson.core.JsonProcessingException |
| `service/SystemInterfaceService.java` | jackson.core.{JsonProcessingException, type.TypeReference}, jackson.databind.ObjectMapper |
| `service/EssentialService.java` | jackson.core.JsonProcessingException (commented out — EssentialService has import commented) |
| `service/Product_DetailsService.java` | jackson.core.JsonProcessingException, jackson.databind.JsonMappingException |
| `service/DeviceTechnicianAISuggestionService.java` | jackson.core.{JsonProcessingException, type.TypeReference}, jackson.databind.ObjectMapper |

**Effective count: 15 files with live (non-commented) com.fasterxml.jackson.databind/core/dataformat
imports** (EssentialService import is commented out).

### Important: annotation-only imports EXCLUDED

Files using only `com.fasterxml.jackson.annotation.*` (`@JsonProperty`, `@JsonIgnore`,
`@JsonInclude`, etc.) are NOT counted above and are NOT included in the 16-file grep result because the
pattern already filters to `databind|core|dataformat`. These annotation imports stay on the
`com.fasterxml.jackson.annotation` package in Jackson 3 (the annotation module is preserved under the
same coordinates) — do NOT rename them.

### Migration action
Under Boot 4, `com.fasterxml.jackson.*` (Jackson 2) is replaced by `tools.jackson.*` (Jackson 3).
Package renames required in all 15 files:
- `com.fasterxml.jackson.core.*` → `tools.jackson.core.*`
- `com.fasterxml.jackson.databind.*` → `tools.jackson.databind.*`
- `com.fasterxml.jackson.dataformat.yaml.*` → `tools.jackson.dataformat.yaml.*`

The `ObjectMapper` API is largely compatible in Jackson 3, but constructors and builder patterns may
have minor changes. The `YAMLFactory` / `YAMLMapper` usage in `Utils.java` needs careful testing.

---

## Cross-Cutting Concerns / Additional Notes

1. **`spring-framework.version=5.3.39` is the most dangerous override** — it pins Spring Framework 5.x
   which is incompatible with Boot 4 (requires Spring 6.x). This must be removed from ALL 10 poms that
   carry it before or at the parent bump step.

2. **Hibernate 7 entity mapping changes**: `sclera-cloud-device-asset` has 147 files with javax.persistence
   imports. Beyond the package rename (javax→jakarta), Hibernate 7 removed several deprecated mapping
   strategies. `@Type` annotation handling via `hibernate-types-52` will break completely and requires
   migration to `hypersistence-utils-hibernate-70`.

3. **WebSocket dependency**: `javax.websocket-api` in `sclera-cloud-device-asset/pom.xml:74` has no
   explicit version (currently managed by Boot 2.6.5). After the parent bump, it must become
   `jakarta.websocket:jakarta.websocket-api` (or removed if pulled transitively via `spring-websocket`).

4. **Caffeine**: `caffeine:3.1.8` is already at a Boot-4-compatible version. The explicit version pin
   can be dropped to inherit from Boot 4 BOM; confirm Boot 4 manages a compatible version first.

5. **micrometer-registry-prometheus pinned at 1.9.7** (Java 11 safe comment in pom) — this is very old.
   Boot 4 manages a recent micrometer; the explicit version should be removed (not in scope of Step 3
   pattern but noted here as a concern).

6. **`spring.redis.*` / redis dependency**: `spring-boot-starter-data-redis` is active (line 433 of
   pom.xml). The property namespace rename (`spring.redis` → `spring.data.redis`) in
   `application-docker.yml` is mandatory before boot or the app will silently use default Redis
   connection settings (localhost:6379) in Docker where the host is `sclera-redis`.

7. **Jackson 3 is NOT backward-compatible at the package level** — `com.fasterxml` → `tools.jackson`
   is a hard rename. The 15 affected files require import updates + potential API adjustments. Boot 4
   still ships a `spring-boot-starter-jackson2` compatibility bridge (see BOM entry), but it is not
   recommended for new code; full migration is preferred.

8. **Current Boot version in all poms: 2.6.5** — all 11 modules set `spring-boot-starter-parent:2.6.5`.
   The Phase 2 execution task will bump each to 4.0.6.
