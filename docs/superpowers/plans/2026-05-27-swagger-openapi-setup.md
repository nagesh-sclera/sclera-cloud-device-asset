# Swagger / OpenAPI Setup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give `sclera-cloud-device-asset`'s Swagger UI a basic API identity (title/version/description) via a single OpenAPI config bean, verified by a focused unit test.

**Architecture:** springdoc-openapi auto-detects an `OpenAPI` `@Bean` and merges it into the generated spec served at `/v3/api-docs` (rendered by Swagger UI at `/swagger-ui/index.html`). We add one `@Configuration` bean in `io.sclera.config` and unit-test the bean's `Info` metadata directly — no Spring context boot. Security is unchanged (docker/dev-only reachability; the docker profile is already `permitAll`).

**Tech Stack:** Java 21 (Amazon Corretto 21.0.8), Spring Boot 4.0.6, `springdoc-openapi-starter-webmvc-ui:3.0.3` (already in the pom), swagger-models v2 (`io.swagger.v3.oas.models.*`), JUnit 5 + AssertJ (both already used by the module's tests).

Spec: `docs/superpowers/specs/2026-05-27-swagger-openapi-setup-design.md`

---

## Testing approach (why a unit test, not an endpoint test)

The spec proposed asserting `GET /v3/api-docs` returns 200. Exploration of the module's test
suite shows there is **no full-web-context test** to extend: tests are either Dapr-client slices
(`@SpringBootTest(classes = DaprClientConfig.class)`) or Testcontainers JPA slices that use a
hand-rolled `JpaTestConfig` (`@ImportAutoConfiguration({...})`). Booting the full app context for
an HTTP test pulls in DB + Dapr + Redis + AMQP and is disproportionate/fragile for a one-bean
change; a springdoc "slice" requires pinning exact springdoc-3.0.3 auto-config class names, which
is brittle. The durable, codebase-consistent test is therefore a **pure unit test of the
`OpenApiConfig` bean** — it verifies exactly the metadata we add, with zero boot risk. Endpoint
reachability is a springdoc framework guarantee (it auto-detects the `OpenAPI` bean) and is
confirmed by the manual boot step in Task 2.

## File structure

- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/config/OpenApiConfig.java`
  — one `@Configuration` with a single `@Bean OpenAPI customOpenAPI()`. Sole responsibility:
  define the OpenAPI document's top-level `Info` metadata.
- Create: `sclera-cloud-device-asset/src/test/java/io/sclera/config/OpenApiConfigTest.java`
  — unit test asserting the bean's `Info` fields.
- No other files change (no security config, no controllers, no pom — springdoc 3.0.3 is present).

## Build/run conventions (this machine)

- No global `mvn`. Build via the module's wrapper from inside the module dir.
- Set `JAVA_HOME` to Corretto 21 every invocation (env does not persist across calls).
- All commands below assume PowerShell, run from the module directory
  `C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\sclera-cloud-device-asset`.

---

### Task 1: OpenApiConfig bean + unit test (TDD)

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/config/OpenApiConfig.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/config/OpenApiConfigTest.java`

- [ ] **Step 1: Write the failing test**

Create `sclera-cloud-device-asset/src/test/java/io/sclera/config/OpenApiConfigTest.java`:

```java
package io.sclera.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void customOpenAPI_definesApiMetadata() {
        OpenAPI openAPI = new OpenApiConfig().customOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo("Sclera Cloud Device Asset API");
        assertThat(openAPI.getInfo().getVersion())
                .isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("REST API for the Sclera cloud device/asset management service.");
    }
}
```

- [ ] **Step 2: Run the test to verify it fails (does not compile)**

Run (PowerShell, from the module dir):

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; .\mvnw.cmd test "-Dtest=OpenApiConfigTest"
```

Expected: BUILD FAILURE — compilation error, `cannot find symbol: class OpenApiConfig`
(the class does not exist yet).

- [ ] **Step 3: Write the minimal implementation**

Create `sclera-cloud-device-asset/src/main/java/io/sclera/config/OpenApiConfig.java`:

```java
package io.sclera.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines the OpenAPI document metadata (title/version/description) for this service.
 * springdoc auto-detects this {@link OpenAPI} bean and merges it into the spec served at
 * /v3/api-docs (rendered by Swagger UI at /swagger-ui/index.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Sclera Cloud Device Asset API")
                .version("1.0.0")
                .description("REST API for the Sclera cloud device/asset management service."));
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run:

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; .\mvnw.cmd test "-Dtest=OpenApiConfigTest"
```

Expected: BUILD SUCCESS — `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.

- [ ] **Step 5: Run the full test suite to confirm nothing broke**

Run:

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; .\mvnw.cmd test
```

Expected: BUILD SUCCESS — `Tests run: 191, Failures: 0, Errors: 0, Skipped: 0`
(190 existing + the 1 new test).

- [ ] **Step 6: Commit**

Run (from the repo root or module dir; paths are repo-relative):

```powershell
git add sclera-cloud-device-asset/src/main/java/io/sclera/config/OpenApiConfig.java sclera-cloud-device-asset/src/test/java/io/sclera/config/OpenApiConfigTest.java
git commit -m "feat(swagger): add OpenApiConfig bean with API metadata"
```

(The commit message body should end with the standard `Co-Authored-By` trailer used in this repo.)

---

### Task 2: Manual endpoint verification (one-time, not automated)

**Files:** none (verification only).

This confirms the live endpoints in the docker profile, which the unit test does not cover.
Optional but recommended once.

- [ ] **Step 1: Boot the service in the docker profile**

Bring up (or restart) the cloud-device-asset container via the existing compose stack so it runs
under `SPRING_PROFILES_ACTIVE=docker` (where `DockerSecurityConfig` already permits all requests).

- [ ] **Step 2: Verify the spec endpoint returns 200 with the title**

Run:

```powershell
(Invoke-WebRequest -UseBasicParsing http://localhost:8085/v3/api-docs).Content | Select-String "Sclera Cloud Device Asset API"
```

Expected: HTTP 200 and the matched line containing `"title":"Sclera Cloud Device Asset API"`.

- [ ] **Step 3: Verify Swagger UI renders**

Open `http://localhost:8085/swagger-ui/index.html` in a browser.
Expected: the Swagger UI loads, shows the title/version, and lists the controllers' endpoints.

---

## Self-Review

**1. Spec coverage:**
- "One new file `io.sclera.config.OpenApiConfig` with `@Bean OpenAPI` (title/version/description)" → Task 1, Step 3. ✓
- "No security change (docker/dev-only)" → no task touches `WebSecurityConfig`/`DockerSecurityConfig`; stated in File structure. ✓
- "No per-endpoint annotations / no pom change" → not in any task; stated. ✓
- "Slice test asserting /v3/api-docs returns 200 with the title" → resolved per the spec's own
  risk note ("falls back to the lightest context… resolved during implementation against the
  existing test harness"): automated as a bean unit test (Task 1) + manual endpoint 200/title
  check (Task 2). The "Testing approach" section documents the rationale. ✓
- "Verification: compile green, suite green, optional manual boot" → Task 1 Steps 4–5, Task 2. ✓

**2. Placeholder scan:** No TBD/TODO; all code blocks complete; all commands have expected output. ✓

**3. Type consistency:** `OpenApiConfig.customOpenAPI()` (test Step 1) matches the method defined
in Step 3; `io.swagger.v3.oas.models.OpenAPI` / `io.swagger.v3.oas.models.info.Info` imports are
consistent across test and impl; the three metadata string literals match exactly between the test
assertions, the impl, and the spec. ✓
