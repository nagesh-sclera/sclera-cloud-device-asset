# Controller Documentation + Logging Pass — sclera-cloud-device-asset

**Date:** 2026-06-18
**Status:** Approved (design)
**Module:** `sclera-cloud-device-asset` (only)
**Branch:** `feature/scheduler-vdms-management-ui`

## Context

The 2026-05-27 Swagger setup was deliberately smoke-level: a single `OpenApiConfig`
bean supplying title/version/description, with **no** per-endpoint annotations. Swagger
UI auto-lists all controllers but gives no real explanation of what each endpoint does.

Three related gaps remain, and all three require reading and understanding each method —
so they are done together in one per-method pass rather than as three separate sweeps:

1. **Swagger** — no `@Operation`/`@ApiResponse`/`@Parameter`/`@Tag` anywhere.
2. **JavaDoc** — inconsistent. Some controllers (e.g. `BuildingController`) have solid
   method JavaDoc; many of the ~72 controllers have none.
3. **Logging** — inconsistent. Only ~23 of 72 controllers declare a logger, and there
   are **619** `System.out.println` / `printStackTrace` occurrences across 37 files
   (`DeviceService` alone has 252, `ConditionsService` 86), plus a stray
   `System.out.println` inside `BuildingController.updateFloorMaps`.

Existing error handling: `io.sclera.exception.MaximoExceptionHandler` is a global
`@ControllerAdvice` that maps **only** `MaximoException` to a standardized `ResponseDTO`
(message, errorCode, path, success=false, timestamp) with HTTP 500. Generic exceptions
rethrown by controllers bypass it and fall through to Spring's default error handling.

## Scope decisions

- **Module:** `sclera-cloud-device-asset` only. Other services (alerts, audit, scheduler,
  vdms-service, integrations, gateway) are out of scope for this pass.
- **Layers:**
  - **Controllers (~72):** all three concerns — Swagger annotations + JavaDoc + logging.
  - **Services/utils (37 files):** logging cleanup **only** — replace
    `System.out.println`/`printStackTrace` with proper SLF4J logging. No JavaDoc sweep,
    no signature or control-flow changes beyond the print→log swap.
- **Swagger depth:** `@Tag` per controller + `@Operation(summary, description)` +
  `@Parameter` descriptions + `@ApiResponses` (documented status codes). No request/
  response examples, no custom schemas, no security-scheme / "Authorize" button.
- **Logging style:** one structured INFO at method entry (key=value, matching the
  existing `BuildingController` style) + a shared `@RestControllerAdvice` for errors.
  The repetitive per-method `try/catch` is removed.
- **Error shape:** generic exceptions are standardized onto the existing `ResponseDTO`
  shape (see Risks — this is a behavioral change, accepted).

## Design

### Component: per-controller-method template

Every endpoint method is rewritten to this shape (JavaDoc and annotations share wording —
the understanding is written once and surfaced in both source and Swagger UI):

```java
/**
 * Creates or updates the given buildings for the tenant.
 * @param username owning user
 * @param vdms_id  owning VDMS id
 * @return set of upserted buildings
 */
@Operation(summary = "Upsert buildings for a VDMS",
           description = "Creates or updates the given buildings for the tenant.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Buildings upserted"),
    @ApiResponse(responseCode = "400", description = "Invalid request payload"),
    @ApiResponse(responseCode = "500", description = "Unexpected server error")
})
@PostMapping("/upsertbuildings")
public Set<BuildingDTO> upsertBuildingsByVdmsId(
        @Parameter(description = "Owning user") @RequestParam String username,
        @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
        @RequestBody Set<BuildingDTO> buildings, HttpServletRequest req) {
    log.info("upsertBuildingsByVdmsId username={} vdms_id={}", username, vdms_id);
    return buildingService.upsertBuildingsByVdmsId(username, vdms_id, buildings, req);
}
```

Rules:
- Class-level `@Tag(name, description)` + class JavaDoc on each controller.
- Each controller declares `private static final Logger log = LoggerFactory.getLogger(...)`
  if it does not already.
- One entry INFO per method, structured key=value, logging identifying params only
  (ids, usernames, vdms ids) — **not** full bodies or anything sensitive.
- The per-method `try/catch`-log-rethrow is removed (including from the ~23 controllers
  that already have it). Errors are handled centrally.
- `@RequestMapping(method = ...)` may be normalized to `@GetMapping`/`@PostMapping`/etc.
  where it is a pure mechanical equivalent; route strings are never changed.
- `@ApiResponses` codes mirror the central handler's status map (200/400/404/500 as
  applicable to the method).

### Component: central exception handler (extend existing)

Add a generic exception handler in `io.sclera.exception` (extending or sitting beside
`MaximoExceptionHandler`) that:

- Logs every otherwise-unhandled exception once:
  `log.error("{} failed: {}", req.getRequestURI(), e.getMessage(), e)`.
- Returns the **same `ResponseDTO`** shape the Maximo handler uses (consistency).
- Maps status by exception type:
  - validation / `IllegalArgumentException` / bad input → **400**
  - a not-found exception type → **404**
  - fallback `Exception` → **500**
- `MaximoException` handling is preserved unchanged.

### Component: service/util logging cleanup

For the 37 files containing `System.out.println`/`printStackTrace`:
- Ensure a declared SLF4J logger exists.
- `System.out.println(...)` → `log.debug(...)` (or `log.info` where it is clearly a
  meaningful lifecycle event), preserving the message content as a parameterized log.
- `e.printStackTrace()` / catch-block prints → `log.error("<context>: {}", e.getMessage(), e)`.
- No JavaDoc added, no method signatures changed, no control flow changed beyond the swap.

## Execution sequencing

1. **Pilot:** `BuildingController` end-to-end (all three) + the central exception handler.
   Compile, run its existing test, eyeball Swagger UI / `/v3/api-docs`. **Stop for user
   review** to lock the template and wording conventions.
2. After approval, proceed controller-by-controller in batches.
3. Then the service/util print→log cleanup.

## Testing / verification

- `mvnw compile` green after each batch.
- Existing test suite green (`mvnw test`). Any test asserting the **old** generic-error
  response shape is identified and updated to the `ResponseDTO` shape.
- Spot-check `/v3/api-docs` returns 200 and Swagger UI renders the new `@Tag`/`@Operation`
  metadata for touched controllers.
- No behavioral change is intended beyond the documented exception-handling
  standardization.

## Risks / notes

- **Error-response shape change (accepted):** generic exceptions move from Spring's
  default error JSON to the standardized `ResponseDTO`. More consistent, but a contract
  change — clients/tests asserting the old shape must be updated. Surfaced during impl.
- **Volume:** ~72 controllers + 37 service/util files. The pilot-then-batch sequencing
  with a review gate keeps the pattern correct before scaling.
- **No route changes:** annotation normalization (`@RequestMapping` → `@GetMapping` etc.)
  must be a pure equivalent; URL paths and HTTP methods are never altered.
- **Logging hygiene:** entry logs must not emit request bodies or sensitive values.
