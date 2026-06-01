# Swagger / OpenAPI Setup — sclera-cloud-device-asset

**Date:** 2026-05-27
**Status:** Approved (design)
**Module:** `sclera-cloud-device-asset`
**Branch:** `feature/postgres-migration`

## Context

The PostgreSQL migration's core work is complete (Boot 4.0.6 + Java 21 + PostgreSQL
boot, full native-query dialect port, jsonb column migration; 190 tests green). The
remaining PG items are deferred, non-blocking hardening (seed-data round-trip ITs,
Flyway V1 baseline, a one-time `docker compose down -v` schema regen). With the
migration effectively done, the next piece of work is Swagger/OpenAPI.

Current Swagger state:
- `springdoc-openapi-starter-webmvc-ui:3.0.3` is already in `sclera-cloud-device-asset/pom.xml`
  (the Boot-4-compatible version; 2.x references the relocated `WebMvcProperties` and breaks).
- 72 controllers exist, so Swagger UI auto-generates a full API surface out of the box.
- There is **no** customization: no `OpenAPI` config bean, no API metadata (title/version/
  description), no `@Operation`/`@Tag` annotations, and no `springdoc.*` properties.
- Two security configs gate access by Spring profile:
  - `DockerSecurityConfig` (`@Profile("docker")`): `anyRequest().permitAll()` — Swagger is
    **already fully reachable** when the stack runs under the docker profile (the normal run mode).
  - `WebSecurityConfig` (`@Profile("!docker")`): OAuth2 resource server (JWT). Permits
    localhost/allowed-subnet via `allowAccess(...)`, otherwise `anyRequest().authenticated()`.
    There is no path rule for Swagger, so a remote client in this profile gets 401 on
    `/swagger-ui` and `/v3/api-docs` (localhost still works).

## Goal

Smoke-level: make Swagger reachable and give it a basic API identity. Explicitly **out of
scope**: per-endpoint `@Operation`/`@ApiResponse`/`@Parameter` annotations, auth-scheme
documentation / the "Authorize" button, and controller grouping/tagging.

## Decisions

1. **Scope = "make it work + reachable" + basic metadata.** No per-endpoint annotation work.
2. **Reachability = docker/dev only.** `WebSecurityConfig` (the secured, non-docker profile)
   is left unchanged — Swagger must NOT be publicly exposed in prod-like deployments. It
   stays reachable in the docker profile (already open) and from localhost in the secured
   profile. No `permitAll` rule is added for Swagger paths.
3. **Approach A — dedicated `@Configuration` bean** (chosen over the `@OpenAPIDefinition`
   annotation on the boot class, and over `springdoc.*` properties). Most idiomatic, lives
   with the existing `io.sclera.config` classes, and is trivially extensible later.

## Design

### Component: `io.sclera.config.OpenApiConfig` (new file)

A single `@Configuration` class exposing one `@Bean OpenAPI` that sets the `Info` block:

```java
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

- **What it does:** supplies the OpenAPI document's top-level metadata.
- **How it's used:** springdoc auto-detects the `OpenAPI` bean and merges it into the generated
  spec served at `/v3/api-docs`, rendered by Swagger UI at `/swagger-ui/index.html`.
- **Depends on:** the springdoc starter (already present). Nothing else.

Metadata values (title / `1.0.0` / description) are the approved defaults.

### Component: `test-ui/index.html` (modify)

Add one `Swagger ↗` anchor to the topbar `.svc-strip`, beside the existing `Traces ↗` link,
pointing at the device-asset Swagger UI (`http://localhost:8085/swagger-ui/index.html`). The
test-ui is the static dashboard served by the nginx `ui` service on :3000. Static edit, no test
(no HTML test harness exists); verified by loading the page. Direct `:8085` link because the
gateway has no `/swagger-ui` route and hardcoded `localhost` matches the UI's existing
`http://localhost:8080` gateway default.

### What does NOT change

- No edits to `WebSecurityConfig` or `DockerSecurityConfig` (docker/dev-only decision).
- No edits to controllers (no per-endpoint annotations).
- No pom change (springdoc 3.0.3 already present).
- No `springdoc.*` properties (defaults are fine: docs at `/v3/api-docs`, UI at `/swagger-ui/index.html`).

## Testing

A durable, CI-friendly slice test asserting the doc endpoint resolves and the metadata is wired:

- Test: `GET /v3/api-docs` returns **200** and the response body contains the title
  `"Sclera Cloud Device Asset API"` (and optionally version `1.0.0`).
- **Important harness note:** springdoc's `/v3/api-docs` endpoint is registered by springdoc
  auto-configuration, which a bare `@WebMvcTest` slice does **not** load. The test must use a
  context that loads springdoc auto-config — i.e. the same full-context `@SpringBootTest` +
  MockMvc setup the existing controller tests already use (Boot 4: `@AutoConfigureMockMvc`
  lives in `org.springframework.boot.webmvc.test.autoconfigure` and needs the test-scope
  `spring-boot-webmvc-test` dependency). The implementation plan resolves the exact harness
  by mirroring an existing passing controller test in this module.

### Verification steps

1. `mvnw compile` — green.
2. `mvnw test` — full suite green, including the new slice test.
3. Optional manual confirm: boot in docker profile, open `http://localhost:8085/swagger-ui/index.html`
   (all 72 controllers listed), and `http://localhost:8085/v3/api-docs` (spec JSON returns).

## Risks / Notes

- If the existing module test setup cannot cheaply boot a full web context (DB/context cost),
  the slice test falls back to the lightest context that still registers springdoc's endpoint;
  resolved during implementation against the existing test harness.
- This is intentionally a minimal footprint: one new class + one test. Grouping, auth-scheme
  docs, and per-endpoint annotations are deferred and can build on `OpenApiConfig` later.
