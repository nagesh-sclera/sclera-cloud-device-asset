# Java 21 + Spring Boot 3.4.x + PostgreSQL Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move all 11 Maven modules to Java 21 + Spring Boot 3.4.x, then migrate the two data services from MySQL to PostgreSQL 16 (greenfield dev — schema recreates, no data copy).

**Architecture:** Three strictly-sequential phases, each ending GREEN (build + tests pass, containers boot) before the next starts. Phase 1 = Java 21 everywhere. Phase 2 = Boot 3.4.x everywhere (`javax→jakarta`, Hibernate 6, security/springdoc/caffeine/hibernate-types swaps). Phase 3 = PostgreSQL for `sclera-cloud-device-asset` + `sclera-vdms-service`. The codebase is **partially pre-migrated** (e.g. `WebSecurityConfig` already uses `SecurityFilterChain`, Dockerfiles on temurin:17), so each phase begins with a discovery/audit step that produces the true work list before any edit.

**Tech Stack:** Java 21 (eclipse-temurin), Spring Boot 3.4.x, Hibernate 6, Maven, Dapr 1.12, Docker Compose, PostgreSQL 16, Flyway, OpenRewrite (migration tooling).

**Spec:** `docs/superpowers/specs/2026-05-22-java21-springboot3-postgres-migration-design.md`

**Module inventory (verified):**

| Module | Boot | Java | DB | Risk |
|---|---|---|---|---|
| sclera-cloud-device-asset | 2.6.5 | 11 | MySQL | High (60+ deps, security, websocket, JPA) |
| sclera-vdms-service | 2.6.5 | 11 | MySQL | Low (web+jpa+mysql only) |
| sclera-audit | 2.6.5 | 11 | — | Low (Dapr subscriber) |
| sclera-identity / -alerts / -inventory / -workorders / -inspection / -integrations | 2.6.5 | 11 | — | Low (skeletons) |
| sclera-edge | 2.6.5 | 17 | — | Low (skeleton, 10 controllers) |
| sclera-api-gateway | 3.2.5 | 11 | — | Medium (mismatched — already Boot 3, illegal on Java 11) |

**Conventions used in this plan:**
- All `mvn` commands are run from each module's own directory (each module has its own `pom.xml`; there is no aggregator parent). Where "for each module" appears, run the command once per module directory.
- The repo is on Windows / PowerShell. Bash tool commands use forward slashes and the absolute repo root `C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice123/sclera-cloud-device-asset`.
- `$REPO` below means that absolute path.
- Source repo `sclera-vdms-edge-server` is read-only — never edit it.

---

## PHASE 0 — Pre-flight & baseline

### Task 0.1: Confirm toolchain and capture a green baseline

**Files:** none (verification only)

- [ ] **Step 1: Confirm JDK 21 is installed and is the build JVM**

Run:
```bash
java -version
mvn -version
```
Expected: `java version "21..."` and Maven reporting `Java version: 21`. If Maven shows a different JDK, set `JAVA_HOME` to the JDK 21 install before continuing. **Do not proceed past Phase 1 on a non-21 JDK.**

- [ ] **Step 2: Record the current branch and confirm clean tree**

Run:
```bash
cd "$REPO" && git status && git rev-parse --abbrev-ref HEAD
```
Expected: clean working tree on `feature/microservice-sclera2.0`.

- [ ] **Step 3: Capture which modules build today (baseline)**

Run, for each of the 11 module directories:
```bash
cd "$REPO/<module>" && mvn -q -o clean test 2>&1 | tail -20 || echo "BASELINE-FAIL: <module>"
```
Record the result of each in a scratch note. This is the reference: a module that fails baseline is not a regression we introduced.

- [ ] **Step 4: Commit a baseline marker (notes only)**

```bash
cd "$REPO" && git add migration-notes/ 2>/dev/null; git commit --allow-empty -m "chore(migration): baseline marker before Java 21 / Boot 3.4 / PG migration

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## PHASE 1 — Java 11 → Java 21 (keep Boot version per module)

Goal of phase: every module compiles + tests on JDK 21 and its container boots. Boot versions are NOT changed here (except none — api-gateway stays 3.2.5 for now; only its Java moves to 21, which makes its existing Boot 3.2.5 legal).

### Task 1.1: Bump `<java.version>` to 21 in every pom

**Files:**
- Modify: `sclera-cloud-device-asset/pom.xml:20` (`<java.version>11</java.version>`)
- Modify: `sclera-vdms-service/pom.xml:16`
- Modify: `sclera-api-gateway/pom.xml` (java.version 11 → 21)
- Modify: `sclera-audit/pom.xml`, `sclera-identity/pom.xml`, `sclera-alerts/pom.xml`, `sclera-inventory/pom.xml`, `sclera-workorders/pom.xml`, `sclera-inspection/pom.xml`, `sclera-integrations/pom.xml` (each `<java.version>11</java.version>`)
- Leave: `sclera-edge/pom.xml` (already 17 → set to 21 for consistency)

- [ ] **Step 1: Find every java.version declaration**

Run:
```bash
cd "$REPO" && grep -rn "<java.version>" --include=pom.xml .
```
Expected: 11 hits (10 showing `11`, edge showing `17`).

- [ ] **Step 2: Set each to 21**

In every `pom.xml` listed above, change the property to:
```xml
<java.version>21</java.version>
```

- [ ] **Step 3: Verify no `11`/`17` java.version remains**

Run:
```bash
cd "$REPO" && grep -rn "<java.version>" --include=pom.xml . | grep -vc ">21<"
```
Expected: `0`.

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add **/pom.xml && git commit -m "build(java21): set <java.version>21</java.version> in all 11 modules

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 1.2: Bump JDK-sensitive libraries for Java 21 compatibility

**Files:**
- Modify: `sclera-cloud-device-asset/pom.xml` — `caffeine` 2.9.3 → 3.1.8; ensure lombok resolves to a 21-capable version.

Rationale: Caffeine 2.x and old Lombok fail under JDK 21's stronger encapsulation. Other heavy libs (POI, PDFBox, AWS SDK) are already recent enough.

- [ ] **Step 1: Bump Caffeine**

In `sclera-cloud-device-asset/pom.xml`, change the caffeine dependency (currently `2.9.3`) to:
```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>
```

- [ ] **Step 2: Ensure Lombok is 21-capable**

Lombok is inherited from the Boot 2.6.5 parent (1.18.22), which does NOT support JDK 21. Pin it explicitly in `sclera-cloud-device-asset/pom.xml`:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
</dependency>
```

- [ ] **Step 3: Compile cloud-device-asset on JDK 21**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o clean compile 2>&1 | tail -30
```
Expected: BUILD SUCCESS. If lombok-generated accessors fail, the lombok bump in Step 2 was not picked up — re-check.

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/pom.xml && git commit -m "build(java21): bump caffeine 3.1.8 + lombok 1.18.34 for JDK 21

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 1.3: Build + test all modules on JDK 21

**Files:** none (verification)

- [ ] **Step 1: Build/test each module**

Run, per module:
```bash
cd "$REPO/<module>" && mvn -q -o clean test 2>&1 | tail -30
```
Expected: BUILD SUCCESS for every module that passed the Phase-0 baseline. Any module that newly fails (passed baseline, fails now) is a Java-21 regression — fix it before continuing (typically a transitive dep needing a bump; record the bump in the module's pom).

- [ ] **Step 2: Commit any fixes**

```bash
cd "$REPO" && git add -A && git commit -m "build(java21): fix Java 21 build regressions across modules

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 1.4: Bump all Dockerfiles to a JDK 21 runtime

**Files:**
- Modify: `sclera-cloud-device-asset/Dockerfile:1` (`FROM eclipse-temurin:17-jre-jammy`)
- Modify: the other 10 module Dockerfiles + `tools/skeleton-template/Dockerfile.template`

- [ ] **Step 1: Find current base images**

Run:
```bash
cd "$REPO" && grep -rn "^FROM eclipse-temurin" --include=Dockerfile* .
```
Expected: a mix of `17-jre-*` / `11-jre-*` base images across the 12 Dockerfiles.

- [ ] **Step 2: Set every base image to temurin 21**

In each Dockerfile and `tools/skeleton-template/Dockerfile.template`, change the `FROM` line to:
```dockerfile
FROM eclipse-temurin:21-jre-jammy
```

- [ ] **Step 3: Verify**

Run:
```bash
cd "$REPO" && grep -rn "^FROM eclipse-temurin" --include=Dockerfile* . | grep -vc "21-jre"
```
Expected: `0`.

- [ ] **Step 4: Build the two data-service images (smoke)**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o clean package -DskipTests && docker build -t sclera-cloud-device-asset:phase1 .
cd "$REPO/sclera-vdms-service" && mvn -q -o clean package -DskipTests && docker build -t sclera-vdms-service:phase1 .
```
Expected: both images build.

- [ ] **Step 5: Commit**

```bash
cd "$REPO" && git add **/Dockerfile* tools/skeleton-template/Dockerfile.template && git commit -m "build(java21): base all Dockerfiles on eclipse-temurin:21-jre-jammy

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 1.5: PHASE 1 GATE — full compose boot

**Files:** none (verification)

- [ ] **Step 1: Bring up the stack**

Run:
```bash
cd "$REPO" && docker compose up -d --build 2>&1 | tail -30
```

- [ ] **Step 2: Confirm health**

Run (after ~60s):
```bash
cd "$REPO" && docker compose ps
curl -s http://localhost:8085/actuator/health
curl -s http://localhost:8089/actuator/health
```
Expected: all containers `running`/`healthy`; both data services return `{"status":"UP"}`. Open `test-ui/index.html` and confirm the "Skeletons N/8" pill is green (8/8).

- [ ] **Step 3: Tear down**

```bash
cd "$REPO" && docker compose down
```

- [ ] **Step 4: Tag the phase**

```bash
cd "$REPO" && git tag phase1-java21-green && git commit --allow-empty -m "chore(migration): PHASE 1 GREEN — all modules on Java 21

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## PHASE 2 — Spring Boot 2.6.5 → 3.4.x

Goal of phase: every module on Boot 3.4.x, all `javax.*` jakarta-ized, Hibernate 6, security/springdoc/hibernate-types swapped, E2E smoke green. **DB stays MySQL in this phase.**

> **Pin the exact version once.** Use the latest 3.4.x patch available in the local Maven repo / Maven Central. Throughout this phase, `3.4.x` means that one chosen patch (e.g. `3.4.5`). Pick it in Task 2.1 Step 1 and use it verbatim everywhere.

### Task 2.1: Discovery — measure the true migration surface

**Files:** Create `migration-notes/phase2-surface.md`

- [ ] **Step 1: Choose the Boot 3.4.x patch**

Run:
```bash
cd "$REPO/sclera-vdms-service" && mvn -o help:evaluate -Dexpression=spring-boot.version -q -DforceStdout 2>/dev/null; echo
```
If offline resolution is unavailable, pick the latest 3.4.x from Maven Central (e.g. `3.4.5`). Write the chosen value at the top of `migration-notes/phase2-surface.md` as `BOOT_VERSION=3.4.x`.

- [ ] **Step 2: Inventory remaining `javax.*` imports (the jakarta surface)**

Run:
```bash
cd "$REPO" && grep -rln "import javax\.\(persistence\|servlet\|validation\|websocket\|annotation\|transaction\)" --include=*.java . | grep -v sclera-vdms-edge-server
```
Paste the file list into `migration-notes/phase2-surface.md` under "## javax imports". (We already know `WebSecurityConfig.java` uses `javax.servlet.http`.)

- [ ] **Step 3: Inventory Boot-2-only / pinned dependencies**

Run:
```bash
cd "$REPO" && grep -rn "hibernate-types-52\|springdoc-openapi-ui\|spring-security-config\|spring-security-web\|oauth2-resource-server\|mysql-connector-java\|javax.websocket-api" --include=pom.xml .
```
Record each hit and its required replacement in `migration-notes/phase2-surface.md` under "## dependency swaps".

- [ ] **Step 4: Inventory deprecated security DSL usage**

Run:
```bash
cd "$REPO" && grep -rn "authorizeRequests\|WebSecurityConfigurerAdapter\|antMatchers\|EnableGlobalMethodSecurity" --include=*.java . | grep -v "//"
```
Record live (non-commented) hits. Known: `WebSecurityConfig.java` uses `.authorizeRequests()`; check `DockerSecurityConfig.java` too.

- [ ] **Step 5: Inventory `spring.redis.*` property usage**

Run:
```bash
cd "$REPO" && grep -rn "spring.redis\|spring:\s*$" --include=*.yml --include=*.properties . | grep -i redis
```
Record any `spring.redis.*` that must become `spring.data.redis.*`.

- [ ] **Step 6: Commit the surface note**

```bash
cd "$REPO" && git add migration-notes/phase2-surface.md && git commit -m "docs(migration): Phase 2 Boot 3.4 migration surface inventory

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.2: Bump the parent + drop now-managed pinned versions (low-risk modules first)

Order: do `sclera-vdms-service` first (trivial), then the skeletons, then api-gateway, then cloud-device-asset last (Task 2.5).

**Files:**
- Modify: `sclera-vdms-service/pom.xml:9` (parent version)

- [ ] **Step 1: Bump vdms-service parent**

In `sclera-vdms-service/pom.xml`, change:
```xml
<version>2.6.5</version>
```
to (using the chosen patch):
```xml
<version>3.4.x</version>
```

- [ ] **Step 2: Jakarta-ize vdms-service entities**

Run:
```bash
cd "$REPO" && grep -rln "import javax\." sclera-vdms-service/src
```
For each file, replace `import javax.persistence.` → `import jakarta.persistence.` (and `validation`/`servlet` similarly).

- [ ] **Step 3: Build + test**

Run:
```bash
cd "$REPO/sclera-vdms-service" && mvn -q -o clean test 2>&1 | tail -30
```
Expected: BUILD SUCCESS. (vdms-service still points at MySQL — that's fine; Phase 3 swaps it.)

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add sclera-vdms-service && git commit -m "feat(boot3): migrate sclera-vdms-service to Spring Boot 3.4.x + jakarta

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.3: Migrate the 8 skeleton modules to Boot 3.4.x

**Files:**
- Modify each: `sclera-audit/pom.xml`, `sclera-identity/pom.xml`, `sclera-alerts/pom.xml`, `sclera-inventory/pom.xml`, `sclera-workorders/pom.xml`, `sclera-inspection/pom.xml`, `sclera-integrations/pom.xml`, `sclera-edge/pom.xml` (parent version → 3.4.x)
- Modify: any `import javax.*` under each module's `src` (skeletons are thin; expect few/none)
- Modify: `sclera-audit` Dapr subscriber if it imports `javax.*`

- [ ] **Step 1: Bump each skeleton's parent to 3.4.x**

In each of the 8 skeleton poms, set the `spring-boot-starter-parent` `<version>` to the chosen `3.4.x`.

- [ ] **Step 2: Jakarta-ize per skeleton**

Run per module:
```bash
cd "$REPO" && grep -rln "import javax\." sclera-audit/src sclera-identity/src sclera-alerts/src sclera-inventory/src sclera-workorders/src sclera-inspection/src sclera-integrations/src sclera-edge/src
```
Replace `javax.` → `jakarta.` for `persistence`/`servlet`/`validation`/`annotation`/`websocket` imports in each hit.

- [ ] **Step 3: Build + test each skeleton**

Run per module:
```bash
cd "$REPO/<skeleton>" && mvn -q -o clean test 2>&1 | tail -20
```
Expected: BUILD SUCCESS each.

- [ ] **Step 4: Re-run the scaffold verb-aware tests (sclera-edge generator)**

Run:
```bash
cd "$REPO" && bash tools/skeleton-template/tests/test-scaffold.sh 2>&1 | tail -20
```
Expected: all assertions pass (PostMapping/RequestBody for writes, GetMapping for reads). If the generated source references `javax.*`, update the template in `tools/skeleton-template/scaffold-skeleton.sh` to emit `jakarta.*`, then re-run.

- [ ] **Step 5: Commit**

```bash
cd "$REPO" && git add sclera-audit sclera-identity sclera-alerts sclera-inventory sclera-workorders sclera-inspection sclera-integrations sclera-edge tools/skeleton-template && git commit -m "feat(boot3): migrate 8 skeleton modules + scaffold template to Boot 3.4.x + jakarta

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.4: Align sclera-api-gateway to Boot 3.4.x

**Files:**
- Modify: `sclera-api-gateway/pom.xml` (parent 3.2.5 → 3.4.x)
- Modify: any `import javax.*` under `sclera-api-gateway/src`

- [ ] **Step 1: Bump parent**

In `sclera-api-gateway/pom.xml` set the parent `<version>` from `3.2.5` to the chosen `3.4.x`. If the gateway uses Spring Cloud Gateway, also bump the `spring-cloud.version` property to the release train matching Boot 3.4.x (`2024.0.x`).

- [ ] **Step 2: Find the Spring Cloud train if present**

Run:
```bash
grep -n "spring-cloud" "$REPO/sclera-api-gateway/pom.xml"
```
If a `spring-cloud-dependencies` BOM is imported, set its version to `2024.0.0` (the train for Boot 3.4.x).

- [ ] **Step 3: Jakarta-ize gateway sources**

Run:
```bash
cd "$REPO" && grep -rln "import javax\." sclera-api-gateway/src
```
Replace any hits `javax.` → `jakarta.`.

- [ ] **Step 4: Build + test**

Run:
```bash
cd "$REPO/sclera-api-gateway" && mvn -q -o clean test 2>&1 | tail -30
```
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
cd "$REPO" && git add sclera-api-gateway && git commit -m "feat(boot3): align sclera-api-gateway to Boot 3.4.x + Spring Cloud 2024.0.x

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.5: Migrate sclera-cloud-device-asset — pom modernization

**Files:**
- Modify: `sclera-cloud-device-asset/pom.xml` extensively

- [ ] **Step 1: Bump parent to 3.4.x**

Change the `spring-boot-starter-parent` `<version>` from `2.6.5` to the chosen `3.4.x`.

- [ ] **Step 2: Drop pinned versions now managed by the 3.4.x parent**

Remove the `<version>` element (let it inherit) from these dependencies in `sclera-cloud-device-asset/pom.xml`:
- `spring-security-config` (was 5.7.3)
- `spring-security-web` (was 5.7.3)
- `jackson-core` (was 2.12.0)
- `jackson-databind` (was 2.11.3)
- `jackson-dataformat-yaml` (was 2.12.0)
- `micrometer-registry-prometheus` (was 1.9.7)

And replace the pinned Boot-3 oauth2 starter:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    <version>3.0.6</version>
</dependency>
```
with (version inherited):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

- [ ] **Step 3: Swap `javax.websocket-api` for the jakarta artifact**

Replace:
```xml
<dependency>
    <groupId>javax.websocket</groupId>
    <artifactId>javax.websocket-api</artifactId>
</dependency>
```
with:
```xml
<dependency>
    <groupId>jakarta.websocket</groupId>
    <artifactId>jakarta.websocket-api</artifactId>
</dependency>
```

- [ ] **Step 4: Swap springdoc for the Boot 3 starter**

Replace:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.6.4</version>
</dependency>
```
with:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

- [ ] **Step 5: Swap hibernate-types for hypersistence-utils**

Replace:
```xml
<dependency>
    <groupId>com.vladmihalcea</groupId>
    <artifactId>hibernate-types-52</artifactId>
    <version>2.21.1</version>
</dependency>
```
with:
```xml
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.8.3</version>
</dependency>
```

- [ ] **Step 6: Commit the pom (will not yet compile — that's expected)**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/pom.xml && git commit -m "build(boot3): cloud-device-asset pom -> Boot 3.4.x, drop pinned versions, swap jakarta/springdoc/hypersistence

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.6: cloud-device-asset — jakarta namespace sweep

**Files:** all `*.java` under `sclera-cloud-device-asset/src` importing `javax.persistence|servlet|validation|websocket|annotation|transaction` (includes all 86-entity subset that live here, plus `WebSecurityConfig.java`, `DockerSecurityConfig.java`)

- [ ] **Step 1: Run OpenRewrite jakarta recipe (first pass)**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -o org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta 2>&1 | tail -30
```
Expected: plugin reports files changed. If the plugin can't be resolved offline, fall back to a manual sweep (Step 2).

- [ ] **Step 2: Manual sweep for stragglers**

Run:
```bash
cd "$REPO" && grep -rln "import javax\.\(persistence\|servlet\|validation\|websocket\|annotation\|transaction\)" sclera-cloud-device-asset/src
```
For each remaining file, replace the `javax.` prefix with `jakarta.` on those imports (and the matching usages). Specifically fix `WebSecurityConfig.java:100` `import javax.servlet.http.HttpServletRequest;` → `import jakarta.servlet.http.HttpServletRequest;`.

- [ ] **Step 3: Verify zero remaining javax persistence/servlet/etc**

Run:
```bash
cd "$REPO" && grep -rlc "import javax\.\(persistence\|servlet\|validation\|websocket\|annotation\|transaction\)" sclera-cloud-device-asset/src | grep -v ":0" || echo "CLEAN"
```
Expected: `CLEAN`. (Note: `javax.crypto`, `javax.net`, `javax.imageio` are JDK packages and stay — do not touch them.)

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/src && git commit -m "refactor(boot3): javax->jakarta namespace sweep in cloud-device-asset

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.7: cloud-device-asset — Hibernate 6 + security DSL fixes

**Files:**
- Modify: every `@Type`/`@TypeDef` JSON-mapped entity (hypersistence 3.x API)
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/config/WebSecurityConfig.java:156` (`.authorizeRequests()` → `.authorizeHttpRequests()`)
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/config/DockerSecurityConfig.java` (same DSL fix if present)

- [ ] **Step 1: Find hibernate-types annotations to port**

Run:
```bash
cd "$REPO" && grep -rln "com.vladmihalcea\|@TypeDef\|@Type(type" sclera-cloud-device-asset/src
```
For each file: replace `import com.vladmihalcea.hibernate.type...` with `io.hypersistence.utils.hibernate.type...`, and convert `@Type(type = "jsonb")` / `@TypeDef(...)` to the Hibernate 6 form `@Type(JsonType.class)` (import `io.hypersistence.utils.hibernate.type.json.JsonType`). Remove now-unused `@TypeDef` class-level annotations.

- [ ] **Step 2: Fix the security DSL deprecation**

In `WebSecurityConfig.java`, change line 156 area:
```java
.authorizeRequests()
.requestMatchers(this::allowAccess)
.permitAll()
.anyRequest()
.authenticated()
.and()
```
to the Boot 3 lambda DSL:
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(this::allowAccess).permitAll()
        .anyRequest().authenticated())
```
Apply the equivalent change in `DockerSecurityConfig.java` if it uses `.authorizeRequests()`/`antMatchers`.

- [ ] **Step 3: Compile**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o clean compile 2>&1 | tail -40
```
Expected: BUILD SUCCESS. Common remaining failures and fixes:
- `RequestMatcher` ambiguity on `.requestMatchers(this::allowAccess)` — the predicate overload still exists in Boot 3; if the compiler complains, wrap as `.requestMatchers(new RequestMatcher() { public boolean matches(HttpServletRequest r){ return allowAccess(r);} })`.
- Hibernate `NoSuchMethodError`/mapping errors surface at test time, not compile — caught in Step 4.

- [ ] **Step 4: Run tests**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o test 2>&1 | tail -40
```
Expected: BUILD SUCCESS (still against MySQL config). Fix any Hibernate mapping failures (usually a JSON column still on the old `@Type(type=...)` form).

- [ ] **Step 5: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/src && git commit -m "refactor(boot3): Hibernate 6 (hypersistence) JSON types + authorizeHttpRequests DSL

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.8: cloud-device-asset — property + redis namespace fixes

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/resources/application*.yml` (the `spring.redis.*` → `spring.data.redis.*` rename, and any deprecated keys)

- [ ] **Step 1: Find spring.redis usage**

Run:
```bash
cd "$REPO" && grep -rln "spring:" sclera-cloud-device-asset/src/main/resources --include=*.yml | xargs grep -l "redis" 2>/dev/null
```

- [ ] **Step 2: Rename redis keys**

In each yml that has it, move `spring.redis.*` under `spring.data.redis.*`. (If none use redis config blocks, this is a no-op — note that in the commit.)

- [ ] **Step 3: Boot the app locally to surface deprecated-property warnings**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o spring-boot:run -Dspring-boot.run.profiles=test 2>&1 | tail -40
```
Watch for `Property '...' is deprecated` / failed-to-bind messages. Fix each, Ctrl-C to stop.

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/src/main/resources && git commit -m "fix(boot3): rename spring.redis->spring.data.redis and clear deprecated properties

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 2.9: PHASE 2 GATE — full build + E2E smoke

**Files:** none (verification)

- [ ] **Step 1: Build/test every module**

Run per module:
```bash
cd "$REPO/<module>" && mvn -q -o clean test 2>&1 | tail -20
```
Expected: BUILD SUCCESS for all 11.

- [ ] **Step 2: Bring up the full stack**

Run:
```bash
cd "$REPO" && docker compose up -d --build 2>&1 | tail -30
```

- [ ] **Step 3: E2E smoke through gateway + Dapr pub/sub**

Run (after ~60s warmup):
```bash
curl -s http://localhost:8085/actuator/health
curl -s http://localhost:8089/actuator/health
# Trigger a request that publishes device.audit-recorded, consumed by sclera-audit
curl -s -X POST http://localhost:8080/<a known device-event endpoint> -H "Content-Type: application/json" -d '{}'
```
Then confirm: (a) `sclera-audit` logs show it consumed `device.audit-recorded` (idempotency store hit), (b) a trace for the request appears in Jaeger at `http://localhost:16686`.

- [ ] **Step 4: Tear down + tag**

```bash
cd "$REPO" && docker compose down && git tag phase2-boot34-green && git commit --allow-empty -m "chore(migration): PHASE 2 GREEN — all modules on Spring Boot 3.4.x

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## PHASE 3 — MySQL → PostgreSQL 16 (data services only)

Goal of phase: `sclera-cloud-device-asset` + `sclera-vdms-service` run against PostgreSQL 16 with a version-controlled Flyway baseline; E2E smoke green. Greenfield — no data copy.

### Task 3.1: Audit MySQL-specific native SQL (35 repositories)

**Files:** Create `migration-notes/phase3-native-sql.md`

- [ ] **Step 1: Extract every native query**

Run:
```bash
cd "$REPO" && grep -rn "nativeQuery\s*=\s*true" --include=*.java sclera-cloud-device-asset/src sclera-vdms-service/src
```

- [ ] **Step 2: Flag MySQL-only constructs**

Run:
```bash
cd "$REPO" && grep -rniE "IFNULL|GROUP_CONCAT|\bNOW\(\)|DATE_FORMAT|STR_TO_DATE|LIMIT [0-9]+\s*,|`[a-z_]+`|UNIX_TIMESTAMP|CONCAT_WS" --include=*.java sclera-cloud-device-asset/src sclera-vdms-service/src
```
For each hit, record file:line + the PostgreSQL replacement in `migration-notes/phase3-native-sql.md`:
- `IFNULL(a,b)` → `COALESCE(a,b)`
- `GROUP_CONCAT(x)` → `STRING_AGG(x::text, ',')`
- `NOW()` → `NOW()` (OK) / `CURRENT_TIMESTAMP`
- `DATE_FORMAT(d,fmt)` → `TO_CHAR(d, fmt)` (format string differs — translate)
- `STR_TO_DATE(s,fmt)` → `TO_TIMESTAMP(s, fmt)`
- `LIMIT n, m` → `LIMIT m OFFSET n`
- backtick `` `col` `` identifiers → remove backticks (PG lowercases unquoted)
- `UNIX_TIMESTAMP(d)` → `EXTRACT(EPOCH FROM d)`

- [ ] **Step 3: Commit the audit note**

```bash
cd "$REPO" && git add migration-notes/phase3-native-sql.md && git commit -m "docs(migration): Phase 3 MySQL-specific native SQL audit

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 3.2: Swap driver dependency in both data services

**Files:**
- Modify: `sclera-cloud-device-asset/pom.xml` (mysql-connector-java → postgresql)
- Modify: `sclera-vdms-service/pom.xml` (same)

- [ ] **Step 1: Replace the driver dependency**

In both poms, replace:
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```
with (version managed by the 3.4.x parent):
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 2: Verify resolution**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o dependency:get -Dartifact=org.postgresql:postgresql:42.7.4 2>&1 | tail -5; mvn -q -o clean compile 2>&1 | tail -10
cd "$REPO/sclera-vdms-service" && mvn -q -o clean compile 2>&1 | tail -10
```
Expected: BUILD SUCCESS both.

- [ ] **Step 3: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/pom.xml sclera-vdms-service/pom.xml && git commit -m "build(postgres): swap mysql-connector-java -> org.postgresql:postgresql

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 3.3: Add a PostgreSQL 16 service to docker-compose

**Files:**
- Modify: `docker-compose.yml`
- Modify: `dapr/secrets.local.json` (`db.url`)

- [ ] **Step 1: Add the postgres service**

In `docker-compose.yml`, add (keep the existing MySQL service block but commented for rollback):
```yaml
  postgres:
    image: postgres:16
    container_name: scleravdmsdatabase-pg
    environment:
      POSTGRES_DB: vdms
      POSTGRES_USER: root
      POSTGRES_PASSWORD: mypass123
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U root -d vdms"]
      interval: 5s
      timeout: 5s
      retries: 10
```
And add to the top-level `volumes:` block:
```yaml
  pgdata:
```

- [ ] **Step 2: Point both data services' DB env at postgres**

In `docker-compose.yml`, for the `sclera-cloud-device-asset` and `sclera-vdms-service` service definitions, set:
```yaml
      DB_URL: jdbc:postgresql://postgres:5432/vdms
      DB_USER: root
      DB_PASS: mypass123
```
and add `depends_on: { postgres: { condition: service_healthy } }` to each.

- [ ] **Step 3: Update the dev secret**

In `dapr/secrets.local.json`, change `db.url` to `jdbc:postgresql://host.docker.internal:5432/vdms`.

- [ ] **Step 4: Bring up just postgres and confirm health**

Run:
```bash
cd "$REPO" && docker compose up -d postgres && sleep 8 && docker compose ps postgres
```
Expected: `healthy`.

- [ ] **Step 5: Commit**

```bash
cd "$REPO" && git add docker-compose.yml dapr/secrets.local.json && git commit -m "feat(postgres): add postgres:16 compose service + repoint data services

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 3.4: Switch datasource + dialect in application configs

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/resources/application-docker.yml:21` and the other active profiles (`application.yml`, `application-dev.yml`, `application-local.yml`)
- Modify: `sclera-vdms-service/src/main/resources/application-docker.yml:8,11`, `application.yml`, `application-local.yml`

- [ ] **Step 1: Update vdms-service datasource**

In each vdms-service profile yml, set:
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/vdms}
    username: ${DB_USER:root}
    password: ${DB_PASS:mypass123}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

- [ ] **Step 2: Update cloud-device-asset datasource**

In each active cloud-device-asset profile yml, change the `url` from `jdbc:mysql://...:3306/vdms` to `jdbc:postgresql://...:5432/vdms` (preserve the `${DB_URL:...}` env-default pattern), and add under `spring.jpa.properties.hibernate`: `dialect: org.hibernate.dialect.PostgreSQLDialect`. Leave `ddl-auto` as-is for now (changed in Task 3.5).

- [ ] **Step 3: Verify no jdbc:mysql remains in active configs**

Run:
```bash
cd "$REPO" && grep -rn "jdbc:mysql\|com.mysql.cj.jdbc.Driver" sclera-cloud-device-asset/src/main/resources sclera-vdms-service/src/main/resources
```
Expected: no hits (or only in clearly-unused profiles like `-qa`/`-uat`/`-test` you choose to leave; if so, update those too for consistency).

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/src/main/resources sclera-vdms-service/src/main/resources && git commit -m "feat(postgres): repoint datasource URLs + PostgreSQLDialect in both data services

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 3.5: Generate the Flyway baseline (schema-from-JPA)

**Files:**
- Create: `sclera-cloud-device-asset/src/main/resources/db/migration/V1__baseline.sql`
- Create: `sclera-vdms-service/src/main/resources/db/migration/V1__baseline.sql`
- Modify: the two data services' `application*.yml` (ddl-auto + flyway flags)

Strategy: let Hibernate materialize the schema once against empty PG, dump it, check it in as the Flyway baseline, then switch to `validate` + Flyway-managed.

- [ ] **Step 1: One-shot schema generation for cloud-device-asset**

Temporarily set `spring.jpa.hibernate.ddl-auto: create` and `spring.flyway.enabled: false` in `application-docker.yml`, then:
```bash
cd "$REPO" && docker compose up -d postgres && docker compose up -d --build sclera-cloud-device-asset
# wait for boot, then dump the generated schema:
docker exec scleravdmsdatabase-pg pg_dump -U root -d vdms --schema-only --no-owner --no-privileges > "$REPO/sclera-cloud-device-asset/src/main/resources/db/migration/V1__baseline.sql"
```
Expected: a non-empty `V1__baseline.sql` containing `CREATE TABLE` for the cloud-device-asset entities.

- [ ] **Step 2: One-shot schema generation for vdms-service**

vdms-service shares the same `vdms` database. Generate its tables the same way but dump only tables not already in V1 (or use a separate schema). Simplest greenfield approach: give vdms-service its own schema. Set in vdms `application-docker.yml`:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: vdms_svc
```
Pre-create the schema and generate:
```bash
docker exec scleravdmsdatabase-pg psql -U root -d vdms -c "CREATE SCHEMA IF NOT EXISTS vdms_svc;"
# set vdms ddl-auto: create, flyway off, boot it, then dump:
docker compose up -d --build sclera-vdms-service
docker exec scleravdmsdatabase-pg pg_dump -U root -d vdms --schema=vdms_svc --schema-only --no-owner --no-privileges > "$REPO/sclera-vdms-service/src/main/resources/db/migration/V1__baseline.sql"
```

- [ ] **Step 3: Switch both services to validate + Flyway**

In both services' `application*.yml`, set:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
```

- [ ] **Step 4: Recreate DB clean and let Flyway apply the baseline**

Run:
```bash
cd "$REPO" && docker compose down -v && docker compose up -d postgres && sleep 8
docker compose up -d --build sclera-cloud-device-asset sclera-vdms-service 2>&1 | tail -10
```
Then confirm Flyway applied V1:
```bash
docker exec scleravdmsdatabase-pg psql -U root -d vdms -c "SELECT version, description, success FROM flyway_schema_history;"
```
Expected: a row `1 | baseline | t`. Both services log `Validated ... migrations` and start with `ddl-auto=validate` (no schema mismatch).

- [ ] **Step 5: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/src/main/resources sclera-vdms-service/src/main/resources && git commit -m "feat(postgres): Flyway V1 baseline from JPA schema + switch to ddl-auto=validate

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 3.6: Fix native-SQL dialect breakages found in 3.1

**Files:** the repository files listed in `migration-notes/phase3-native-sql.md`

- [ ] **Step 1: Apply each PG translation**

For every file:line recorded in `migration-notes/phase3-native-sql.md`, edit the `@Query(value = "...", nativeQuery = true)` string to the PostgreSQL equivalent (per the mapping in Task 3.1 Step 2). Tick each item off in the note as you go.

- [ ] **Step 2: Compile**

Run:
```bash
cd "$REPO/sclera-cloud-device-asset" && mvn -q -o clean compile 2>&1 | tail -10
cd "$REPO/sclera-vdms-service" && mvn -q -o clean compile 2>&1 | tail -10
```
Expected: BUILD SUCCESS (native SQL is not validated at compile time — runtime check is Step 3).

- [ ] **Step 3: Runtime check — exercise the repositories**

Run the data services against PG and hit endpoints that execute the previously-MySQL queries:
```bash
cd "$REPO" && docker compose up -d --build sclera-cloud-device-asset sclera-vdms-service
# tail logs while exercising endpoints; watch for org.postgresql.util.PSQLException
docker compose logs -f sclera-cloud-device-asset | grep -i "PSQLException\|ERROR" &
```
For each `PSQLException: function ... does not exist` / syntax error, return to Step 1, fix that query, redeploy. Repeat until clean.

- [ ] **Step 4: Commit**

```bash
cd "$REPO" && git add sclera-cloud-device-asset/src sclera-vdms-service/src migration-notes/phase3-native-sql.md && git commit -m "fix(postgres): port MySQL-specific native queries to PostgreSQL syntax

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

### Task 3.7: PHASE 3 GATE — both services on PG, E2E smoke

**Files:** none (verification)

- [ ] **Step 1: Clean full stack against PostgreSQL**

Run:
```bash
cd "$REPO" && docker compose down -v && docker compose up -d --build 2>&1 | tail -30 && sleep 75
docker compose ps
curl -s http://localhost:8085/actuator/health
curl -s http://localhost:8089/actuator/health
```
Expected: all containers healthy; both data services `UP`; no MySQL container running.

- [ ] **Step 2: CRUD smoke per service**

Hit one create + one read endpoint per data service (a representative entity each), confirming a row lands in PostgreSQL:
```bash
docker exec scleravdmsdatabase-pg psql -U root -d vdms -c "\dt"   # tables exist
# after a create call:
docker exec scleravdmsdatabase-pg psql -U root -d vdms -c "SELECT count(*) FROM <table written by the create call>;"
```
Expected: table list non-empty; count reflects the write.

- [ ] **Step 3: E2E + trace**

Repeat the Phase 2 gate E2E (gateway → Dapr pub/sub → sclera-audit) and confirm a trace in Jaeger (`http://localhost:16686`). Expected: clean, with the JPA write now hitting PG.

- [ ] **Step 4: Remove the commented MySQL compose block + connector references**

Delete the commented-out MySQL service from `docker-compose.yml` now that PG is green. Confirm no `mysql` remains:
```bash
cd "$REPO" && grep -rn "mysql" docker-compose.yml dapr/ || echo "NO MYSQL REFS"
```

- [ ] **Step 5: Tear down + tag**

```bash
cd "$REPO" && docker compose down && git add docker-compose.yml && git commit -m "chore(postgres): remove MySQL compose fallback now that PG is green" && git tag phase3-postgres-green
```

---

## Final verification (all phases)

- [ ] **Step 1: Update APP_IDS / docs**

Update `dapr/APP_IDS.md` observability/notes if any ports or DB facts changed (PG on 5432). Update `sclera-cloud-device-asset/CLAUDE.md` "Project identity" line — it currently says "Spring Boot 2.6.5, Java 11. Do not modernize without an explicit Phase-2 plan." Change to "Spring Boot 3.4.x, Java 21, PostgreSQL 16."

- [ ] **Step 2: Final full build**

Run per module: `mvn -q -o clean test`. Expected: all 11 GREEN.

- [ ] **Step 3: Commit docs**

```bash
cd "$REPO" && git add dapr/APP_IDS.md sclera-cloud-device-asset/CLAUDE.md migration-notes/ && git commit -m "docs(migration): record Java 21 + Boot 3.4 + PostgreSQL final state

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Self-review notes (spec coverage)

- Spec §3 Phase 1 (Java 21) → Tasks 1.1–1.5 ✔
- Spec §5.1 parent/properties → Tasks 2.2–2.5 ✔
- Spec §5.2 javax→jakarta → Tasks 2.2/2.3/2.4 Step 2, 2.6 ✔
- Spec §5.3 Hibernate 6 (hibernate-types→hypersistence) → Tasks 2.5 Step 5, 2.7 Step 1 ✔
- Spec §5.4 security rewrite → Task 2.7 Step 2 (note: WebSecurityConfig already SecurityFilterChain; only DSL deprecation + javax import remained) ✔
- Spec §5.5 springdoc/redis/caffeine/trailing-slash → Tasks 1.2 (caffeine), 2.5 Step 4 (springdoc), 2.8 (redis) ✔
- Spec §5.6 skeletons + scaffold tests → Task 2.3 ✔
- Spec §6.1 driver swap → Task 3.2 ✔
- Spec §6.2 connection/dialect → Task 3.4 ✔
- Spec §6.3 schema strategy (Option A) → Task 3.5 ✔
- Spec §6.4 SQL-dialect differences → Tasks 3.1 + 3.6 ✔
- Spec §6.5 compose PG service → Task 3.3 ✔
- Spec §7 testing gates → Tasks 1.5, 2.9, 3.7 ✔
- Spec §8 rollback (per-phase commits/tags) → tags at each gate ✔
