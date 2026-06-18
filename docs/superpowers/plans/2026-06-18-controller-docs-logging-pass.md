# Controller Documentation + Logging Pass Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enrich the 23 REST controllers in `sclera-cloud-device-asset` with Swagger/OpenAPI annotations + JavaDoc, route all controller errors through one shared `@RestControllerAdvice`, and replace 619 `System.out.println`/`printStackTrace` calls across 37 service/util files with SLF4J logging.

**Architecture:** Each controller method carries a JavaDoc block whose wording is reused in `@Operation(summary, description)`; args get `@Parameter`; methods get `@ApiResponses`; the class gets `@Tag`. Per-method `try/catch`-log-rethrow is deleted and replaced by a single `GlobalExceptionHandler` that logs once and returns the existing `ResponseDTO` error shape. Service/util logging cleanup is a separate mechanical sweep (no JavaDoc, no behavior change).

**Tech Stack:** Java 21 (Corretto 21), Spring Boot 4.0.6, springdoc-openapi-starter-webmvc-ui 3.0.3 (already on classpath — brings `io.swagger.v3.oas.annotations.*`), SLF4J, JUnit 5 + spring-test MockMvc.

**Spec:** `docs/superpowers/specs/2026-06-18-controller-docs-logging-pass-design.md`

---

## Build & environment (read before any task)

All commands run from the **module dir** `sclera-cloud-device-asset/` (the inner one), using the module's own Maven wrapper. There is **no global `mvn`** and **no parent/aggregator POM**, and only JDK 17 is on PATH — so `JAVA_HOME` must be set to Corretto 21 every invocation, and the shared lib must be installed once.

**One-time setup (Task 0).** **Per-task build** (PowerShell):

```powershell
$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
cd "C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\sclera-cloud-device-asset"
.\mvnw.cmd -q test
```

To compile only (faster during iteration): `.\mvnw.cmd -q compile`. To run a single test class: `.\mvnw.cmd -q -Dtest=GlobalExceptionHandlerTest test`.

## File structure

- **New:** `src/main/java/io/sclera/exception/GlobalExceptionHandler.java` — generic `@RestControllerAdvice` (logging + `ResponseDTO` mapping). Sits beside the existing `MaximoExceptionHandler` (which is left unchanged).
- **New:** `src/test/java/io/sclera/exception/GlobalExceptionHandlerTest.java` — standalone-MockMvc test, no Spring context, no DB.
- **Modified:** 23 controllers in `src/main/java/io/sclera/controller/admin/` — annotations + JavaDoc + logging + try/catch removal.
- **Modified:** `src/main/java/io/sclera/scheduler/TriggerDispatchSubscriber.java` — JavaDoc + logging only.
- **Modified:** 37 service/util/config files — `println`/`printStackTrace` → SLF4J.

## Testing note (why few new tests)

There are **no existing controller tests** in this module (only service/client/config/IT tests). The controller edits are **non-behavioral** — annotations, JavaDoc, an entry log line, and deletion of try/catch that only logged-and-rethrew. The one genuinely behavioral change is the new `GlobalExceptionHandler`, so that gets real TDD. Controller and service edits are verified by **compilation + the existing suite staying green + a manual Swagger spot-check**. We do not add 23 MockMvc controller tests — that cost is not justified for documentation/logging changes and conflicts with the project's token/time-efficiency preference.

---

## Task 0: Baseline green build

**Files:** none (verification only).

- [ ] **Step 1: Install the shared lib to the local repo** (required — no aggregator POM)

```powershell
$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
cd "C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\dapr-commons"
.\mvnw.cmd -q install -DskipTests
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Confirm the module suite is green before any change**

```powershell
$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
cd "C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\sclera-cloud-device-asset"
.\mvnw.cmd -q test
```

Expected: `BUILD SUCCESS`. If red here, stop and fix the environment before proceeding — later "green" claims depend on this baseline.

---

## Task 1: GlobalExceptionHandler (TDD)

**Files:**
- Create: `src/main/java/io/sclera/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/io/sclera/exception/GlobalExceptionHandlerTest.java`

Behavior: log every unhandled exception once, return the existing `ResponseDTO` shape (`new ResponseDTO(message, status, requestUri, success=false, timestamp)` — the same 5-arg ctor `MaximoExceptionHandler` uses), and map status by type: `IllegalArgumentException` → 400, `java.util.NoSuchElementException` → 404, any other `Exception` → 500. `MaximoException` is **not** handled here — its existing, more-specific handler wins automatically.

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class BoomController {
        @GetMapping("/boom") String boom() { throw new RuntimeException("kaboom"); }
        @GetMapping("/bad")  String bad()  { throw new IllegalArgumentException("nope"); }
        @GetMapping("/missing") String missing() { throw new NoSuchElementException("gone"); }
    }

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new BoomController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void genericException_maps500_withResponseDtoShape() throws Exception {
        mvc.perform(get("/boom").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kaboom"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void illegalArgument_maps400() throws Exception {
        mvc.perform(get("/bad").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void noSuchElement_maps404() throws Exception {
        mvc.perform(get("/missing").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
```

- [ ] **Step 2: Run the test, verify it fails**

```powershell
.\mvnw.cmd -q -Dtest=GlobalExceptionHandlerTest test
```

Expected: FAIL — compilation error `cannot find symbol GlobalExceptionHandler`.

- [ ] **Step 3: Implement the handler**

```java
package io.sclera.exception;

import io.sclera.integration.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * Catch-all controller advice that logs any otherwise-unhandled exception once and
 * returns a standardized {@link ResponseDTO} error body. Status is derived from the
 * exception type (400 for bad input, 404 for missing resources, 500 otherwise).
 *
 * <p>{@link MaximoException} is intentionally not handled here: its dedicated, more
 * specific {@link MaximoExceptionHandler} takes precedence.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps invalid-input exceptions to HTTP 400.
     *
     * @param e   the thrown exception
     * @param req current request, used for the logged/returned path
     * @return a {@link ResponseDTO} body with status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO> handleBadRequest(IllegalArgumentException e, HttpServletRequest req) {
        return build(e, req, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maps missing-resource lookups to HTTP 404.
     *
     * @param e   the thrown exception
     * @param req current request, used for the logged/returned path
     * @return a {@link ResponseDTO} body with status 404
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseDTO> handleNotFound(NoSuchElementException e, HttpServletRequest req) {
        return build(e, req, HttpStatus.NOT_FOUND);
    }

    /**
     * Maps any other unhandled exception to HTTP 500.
     *
     * @param e   the thrown exception
     * @param req current request, used for the logged/returned path
     * @return a {@link ResponseDTO} body with status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleGeneric(Exception e, HttpServletRequest req) {
        return build(e, req, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ResponseDTO> build(Exception e, HttpServletRequest req, HttpStatus status) {
        log.error("{} failed [{}]: {}", req.getRequestURI(), status.value(), e.getMessage(), e);
        ResponseDTO body = new ResponseDTO(
                e.getMessage(),
                status.value(),
                req.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(body);
    }
}
```

- [ ] **Step 4: Run the test, verify it passes**

```powershell
.\mvnw.cmd -q -Dtest=GlobalExceptionHandlerTest test
```

Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/io/sclera/exception/GlobalExceptionHandler.java src/test/java/io/sclera/exception/GlobalExceptionHandlerTest.java
git commit -m "feat(error): central GlobalExceptionHandler returning standardized ResponseDTO"
```

---

## Task 2: Pilot — BuildingController full treatment  ⏸ REVIEW GATE

**Files:**
- Modify: `src/main/java/io/sclera/controller/admin/BuildingController.java`

This is the reference implementation. It locks the exact conventions (tag naming, summary wording, parameter descriptions, response codes, logging, try/catch removal) that every later controller copies. **Stop for user review after this task.**

- [ ] **Step 1: Replace the file with the annotated version**

```java
package io.sclera.controller.admin;

import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST endpoints for managing buildings, floors and floor maps for a VDMS.
 * Delegates all persistence and business logic to {@link BuildingService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Buildings", description = "Create, read, delete and sync buildings, floors and floor maps for a VDMS.")
public class BuildingController {

    private static final Logger log = LoggerFactory.getLogger(BuildingController.class);

    @Autowired
    private BuildingService buildingService;

    /**
     * Creates or updates the given buildings for the tenant.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param buildings          set of buildings to upsert
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return set of upserted buildings
     */
    @Operation(summary = "Upsert buildings for a VDMS",
            description = "Creates or updates the given set of buildings for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buildings upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertbuildings")
    public Set<BuildingDTO> upsertBuildingsByVdmsId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody Set<BuildingDTO> buildings, HttpServletRequest httpServletRequest) {
        log.info("upsertBuildingsByVdmsId username={} vdms_id={}", username, vdms_id);
        return buildingService.upsertBuildingsByVdmsId(username, vdms_id, buildings, httpServletRequest);
    }

    /**
     * Returns the building that contains the given location.
     *
     * @param username    owning user
     * @param vdms_id     owning VDMS id
     * @param location_id location whose building is requested
     * @return building containing the location
     */
    @Operation(summary = "Get building by location",
            description = "Returns the building that contains the given location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Building found"),
            @ApiResponse(responseCode = "404", description = "No building for the location"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/building/floor/location/{location_id}/getbuildingbylocation")
    public BuildingDTO getBuildingByLocationId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Location whose building is requested") @PathVariable String location_id) {
        log.info("getBuildingByLocationId username={} vdms_id={} location_id={}", username, vdms_id, location_id);
        return buildingService.getBuildingByLocationId(username, vdms_id, location_id);
    }

    /**
     * Returns the buildings for the tenant, optionally filtered by a field and value.
     *
     * @param vdms_id  owning VDMS id
     * @param field    optional field name to filter on
     * @param field_id optional field value to match
     * @return set of matching buildings
     */
    @Operation(summary = "Get buildings for a VDMS",
            description = "Returns the buildings for the tenant, optionally filtered by a field name and value.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buildings returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getbuildingsbyvdmsid")
    public Set<BuildingDTO> getBuildingsByVdmsId(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Optional field name to filter on") @RequestParam(required = false) String field,
            @Parameter(description = "Optional field value to match") @RequestParam(required = false) String field_id) {
        log.info("getBuildingsByVdmsId vdms_id={}", vdms_id);
        return buildingService.getBuildingsByVdmsId(vdms_id, field, field_id);
    }

    /**
     * Deletes the buildings identified by the given ids.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param building_ids       set of building ids to delete
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete buildings by ids",
            description = "Deletes the buildings identified by the given ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buildings deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deletebuildings")
    public void deleteBuildingsByIds(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<String> building_ids, HttpServletRequest httpServletRequest) {
        log.info("deleteBuildingsByIds username={} vdmsid={}", username, vdmsid);
        buildingService.deleteBuildingsByIds(username, vdmsid, building_ids, httpServletRequest);
    }

    /**
     * Synchronizes building locations from the backend (temporary sync endpoint).
     *
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return map describing the sync result
     */
    @Operation(summary = "Sync buildings from backend",
            description = "Synchronizes building locations from the backend. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncbuildings")
    public Map<String, Object> syncLocationsFromBackend(HttpServletRequest httpServletRequest) {
        log.info("syncLocationsFromBackend called");
        return buildingService.syncLocationsFromBackend(httpServletRequest);
    }

    /**
     * Synchronizes floor maps for the given VDMS from the backend (temporary sync endpoint).
     *
     * @param vdms_id owning VDMS id
     * @return set of synchronized floors
     */
    @Operation(summary = "Sync floor maps from backend",
            description = "Synchronizes floor maps for the given VDMS from the backend. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor maps synced"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncfloormaps")
    public Set<FloorDTO> syncFloorMaps(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id) {
        log.info("syncFloorMaps vdms_id={}", vdms_id);
        return buildingService.syncFloorMaps(vdms_id);
    }

    /**
     * Updates floor map images for the given VDMS (temporary sync endpoint).
     *
     * @param vdms_id     owning VDMS id
     * @param floorImages floor map images to apply
     * @return list of updated floors
     */
    @Operation(summary = "Update floor maps",
            description = "Updates floor map images for the given VDMS. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor maps updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/updatefloormaps")
    public List<FloorDTO> updateFloorMaps(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody List<FloorDTO> floorImages) {
        log.info("updateFloorMaps vdms_id={} count={}", vdms_id, floorImages == null ? 0 : floorImages.size());
        return buildingService.updateFloorMaps(vdms_id, floorImages);
    }

    /**
     * Synchronizes floor map tiles from the backend (temporary sync endpoint).
     *
     * @return list of floors whose map tiles were synchronized
     */
    @Operation(summary = "Sync floor map tiles",
            description = "Synchronizes floor map tiles from the backend. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor map tiles synced"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncfloormapstiles")
    public List<FloorDTO> syncFloorMapsTiles() {
        log.info("syncFloorMapsTiles called");
        return buildingService.syncFloorMapsTiles();
    }
}
```

Note what changed vs. the original: added `@Tag` + swagger imports; added `@Operation`/`@ApiResponses`/`@Parameter`; normalized `@RequestMapping(method=...)` to `@GetMapping`/`@PostMapping`/`@DeleteMapping` (paths unchanged); deleted every `try/catch`-log-rethrow; deleted the `System.out.println("******...Floor Images...")` and replaced its intent with a `count=` field on the existing entry log.

- [ ] **Step 2: Compile**

```powershell
.\mvnw.cmd -q compile
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Run the full suite (guards against an accidental behavior change)**

```powershell
.\mvnw.cmd -q test
```

Expected: `BUILD SUCCESS`, same test count as the Task 0 baseline.

- [ ] **Step 4: Commit**

```powershell
git add src/main/java/io/sclera/controller/admin/BuildingController.java
git commit -m "docs(buildings): Swagger annotations + JavaDoc + central error handling"
```

- [ ] **Step 5: ⏸ STOP — request user review.** Show the diff and ask the user to confirm the pilot conventions (tag naming, summary/description wording, parameter descriptions, response-code choices, logging style) before applying them to the remaining 22 controllers. Optional: have the user boot the app in the docker profile and open `http://localhost:8085/swagger-ui/index.html` → the **Buildings** tag should list all 8 operations with summaries. Do not start Task 3+ until approved.

---

## Task 3: The transformation recipe (reference — apply to every remaining controller)

This is the exact, mechanical procedure locked by the Task 2 pilot. Tasks 4–7 apply it file-by-file. It is deliberately not re-pasted per controller; follow these rules precisely. Per-method **summary/description text is derived by reading what the method does** (its name, route, params, and the service call it delegates to) — write the human-readable "what it does" first as JavaDoc, then reuse that wording in `@Operation`.

For each controller file:

1. **Imports** — add:
   ```java
   import io.swagger.v3.oas.annotations.Operation;
   import io.swagger.v3.oas.annotations.Parameter;
   import io.swagger.v3.oas.annotations.responses.ApiResponse;
   import io.swagger.v3.oas.annotations.responses.ApiResponses;
   import io.swagger.v3.oas.annotations.tags.Tag;
   ```
   Add `org.slf4j.Logger` / `LoggerFactory` only if the class has no logger yet.
2. **Class** — add a class JavaDoc (one or two sentences: what this controller manages, "delegates to {@link XxxService}") if missing, and a `@Tag(name = "<Domain>", description = "<one line>")`. Tag name = the domain noun (e.g. `Devices`, `Locations`, `Floors`, `Documents`, `Media`, `Notes`, `Specifications`, `Networks`, `Managed Software`, `Asset Onboarding`, `Asset Mapper`, `Asset Fields`, `Property Services`, `Device Conditions`, `Device Metadata`, `Device Specifications`, `Device Lifecycle History`, `Location History`, `AI Call Log`, `ChatGPT`, `Device Onboard Actions`, `Device Technician AI Suggestions`).
3. **Logger** — ensure `private static final Logger log = LoggerFactory.getLogger(<Class>.class);` exists.
4. **Each endpoint method:**
   - Add/keep a JavaDoc block with a one-line summary, `@param` for every argument **except** `HttpServletRequest` is described as "current request, used to resolve tenant/VDMS context", and `@return` when non-void.
   - Add `@Operation(summary = "<imperative phrase>", description = "<the JavaDoc summary sentence>")`.
   - Add `@ApiResponses` with: always `200`; add `400` if the method has a `@RequestBody` or required input that can be invalid; add `404` if it fetches a single entity by id; always `500`. Use the descriptions from the pilot as the wording model.
   - Add `@Parameter(description = "...")` to every `@RequestParam`/`@PathVariable` argument. Do **not** annotate `@RequestBody` or `HttpServletRequest` with `@Parameter`.
   - Normalize `@RequestMapping(method = RequestMethod.X, value = "...")` → `@XMapping("...")` (GET/POST/PUT/DELETE/PATCH). **Never change the path string or HTTP method.**
   - **Delete** any `try { ... } catch (Exception e) { log.error(...); throw e; }` wrapper, leaving the body as the direct delegate call (errors now flow to `GlobalExceptionHandler`). Keep the single entry `log.info("<methodName> key=val ...", ...)` — log only identifying scalars (ids, usernames), **never** request bodies or secrets. If the method had no entry log, add one.
   - Remove any `System.out.println`/`printStackTrace` in the method (fold useful detail into the entry log, e.g. `count=`).
5. **Do not** change method signatures, return types, routes, or business logic.

**Per-controller checklist (used by Tasks 4–7):** compile after each file (`.\mvnw.cmd -q compile`), and commit per batch (not per file) with message `docs(<area>): Swagger annotations + JavaDoc + central error handling`.

---

## Task 4: DeviceController (78 endpoints)

**Files:** Modify `src/main/java/io/sclera/controller/admin/DeviceController.java`

The largest controller — apply the Task 3 recipe. Because of its size, work in sub-chunks and commit progressively so a mistake is easy to bisect.

- [ ] **Step 1:** Apply the recipe to the class header (`@Tag(name = "Devices", ...)`, class JavaDoc, confirm logger exists).
- [ ] **Step 2:** Apply the recipe to endpoints 1–26 (first third). Run `.\mvnw.cmd -q compile` → `BUILD SUCCESS`.
- [ ] **Step 3:** Apply to endpoints 27–52. Run `.\mvnw.cmd -q compile` → `BUILD SUCCESS`.
- [ ] **Step 4:** Apply to endpoints 53–78. Run `.\mvnw.cmd -q compile` → `BUILD SUCCESS`.
- [ ] **Step 5:** Run the full suite: `.\mvnw.cmd -q test` → `BUILD SUCCESS`, same test count as baseline.
- [ ] **Step 6: Commit**

```powershell
git add src/main/java/io/sclera/controller/admin/DeviceController.java
git commit -m "docs(devices): Swagger annotations + JavaDoc + central error handling"
```

---

## Task 5: Controller batch A — large controllers (57 endpoints)

**Files (modify, apply Task 3 recipe to each):**
- `ManagedSoftwareController.java` (17) — `@Tag(name = "Managed Software")`
- `LocationController.java` (16) — `@Tag(name = "Locations")`
- `AiCallLogController.java` (13) — `@Tag(name = "AI Call Log")`
- `PropertyServiceController.java` (11) — `@Tag(name = "Property Services")`

- [ ] **Step 1:** Apply the recipe to `ManagedSoftwareController.java`, then `.\mvnw.cmd -q compile`.
- [ ] **Step 2:** Apply to `LocationController.java`, then compile.
- [ ] **Step 3:** Apply to `AiCallLogController.java` (also remove its `System.out.println`/`printStackTrace`), then compile.
- [ ] **Step 4:** Apply to `PropertyServiceController.java`, then compile.
- [ ] **Step 5:** Full suite `.\mvnw.cmd -q test` → green.
- [ ] **Step 6: Commit**

```powershell
git add src/main/java/io/sclera/controller/admin/ManagedSoftwareController.java src/main/java/io/sclera/controller/admin/LocationController.java src/main/java/io/sclera/controller/admin/AiCallLogController.java src/main/java/io/sclera/controller/admin/PropertyServiceController.java
git commit -m "docs(controllers): Swagger annotations + JavaDoc + central error handling (batch A)"
```

---

## Task 6: Controller batch B — medium controllers (50 endpoints)

**Files (modify, apply Task 3 recipe to each):**
- `SpecificationsController.java` (11) — `@Tag(name = "Specifications")`
- `AssetOnboardController.java` (10) — `@Tag(name = "Asset Onboarding")`
- `DeviceSpecificationController.java` (10) — `@Tag(name = "Device Specifications")`
- `FloorController.java` (10) — `@Tag(name = "Floors")`
- `DocumentController.java` (9) — `@Tag(name = "Documents")`

- [ ] **Step 1:** Apply the recipe to each of the five files, compiling (`.\mvnw.cmd -q compile`) after each.
- [ ] **Step 2:** Full suite `.\mvnw.cmd -q test` → green.
- [ ] **Step 3: Commit**

```powershell
git add src/main/java/io/sclera/controller/admin/SpecificationsController.java src/main/java/io/sclera/controller/admin/AssetOnboardController.java src/main/java/io/sclera/controller/admin/DeviceSpecificationController.java src/main/java/io/sclera/controller/admin/FloorController.java src/main/java/io/sclera/controller/admin/DocumentController.java
git commit -m "docs(controllers): Swagger annotations + JavaDoc + central error handling (batch B)"
```

---

## Task 7: Controller batch C — remaining small controllers (46 endpoints)

**Files (modify, apply Task 3 recipe to each):**
- `DeviceConditionsController.java` (8) — `@Tag(name = "Device Conditions")`
- `MediaController.java` (7) — `@Tag(name = "Media")`
- `AssetMapperController.java` (5) — `@Tag(name = "Asset Mapper")`
- `NetworkController.java` (5) — `@Tag(name = "Networks")`
- `DeviceMetadataController.java` (4) — `@Tag(name = "Device Metadata")`
- `NoteController.java` (4) — `@Tag(name = "Notes")`
- `DeviceLifecycleHistoryController.java` (3) — `@Tag(name = "Device Lifecycle History")`
- `LocationHistoryController.java` (3) — `@Tag(name = "Location History")`
- `ChatGPTController.java` (2) — `@Tag(name = "ChatGPT")`
- `DeviceOnboardActionController.java` (2) — `@Tag(name = "Device Onboard Actions")`
- `DeviceTechnicianAISuggestionController.java` (2) — `@Tag(name = "Device Technician AI Suggestions")`
- `AssetFieldController.java` (1) — `@Tag(name = "Asset Fields")`

- [ ] **Step 1:** Apply the recipe to each of the twelve files, compiling after each.
- [ ] **Step 2:** Full suite `.\mvnw.cmd -q test` → green.
- [ ] **Step 3: Commit**

```powershell
git add src/main/java/io/sclera/controller/admin/
git commit -m "docs(controllers): Swagger annotations + JavaDoc + central error handling (batch C)"
```

---

## Task 8: TriggerDispatchSubscriber — JavaDoc + logging only (no Swagger)

**Files:** Modify `src/main/java/io/sclera/scheduler/TriggerDispatchSubscriber.java`

Per the spec, this Dapr event subscriber is internal — it gets JavaDoc + proper logging but **no** `@Operation`/`@Tag`/`@ApiResponses`.

- [ ] **Step 1:** Add a class JavaDoc explaining it consumes scheduler trigger events. Ensure a logger is declared. Add a method JavaDoc + one structured entry log to each handler method. Replace any `System.out.println`/`printStackTrace` with `log.debug(...)` / `log.error("...: {}", e.getMessage(), e)`. Do **not** add Swagger annotations or a `@RestControllerAdvice`-style try/catch.
- [ ] **Step 2:** `.\mvnw.cmd -q test` → green (the module has an existing `TriggerDispatchSubscriberTest` — it must stay green).
- [ ] **Step 3: Commit**

```powershell
git add src/main/java/io/sclera/scheduler/TriggerDispatchSubscriber.java
git commit -m "docs(scheduler): JavaDoc + structured logging for TriggerDispatchSubscriber"
```

---

## Task 9: Service/util logging cleanup (println/printStackTrace → SLF4J)

**Scope:** the 37 files containing `System.out.println`/`printStackTrace`, **excluding** controllers (done in Tasks 2–7) and **excluding** `MaximoExceptionHandler.java` (its `printStackTrace(pw)` writes to a `StringWriter` to capture the trace as a string — that is intentional, not a console print; leave it).

**Mechanical rules (identical for every occurrence):**
- Ensure `private static final Logger log = LoggerFactory.getLogger(<Class>.class);` exists (add SLF4J imports if missing).
- `System.out.println(x)` → `log.debug("{}", x)` for diagnostics, or `log.info(...)` only when it is clearly a meaningful lifecycle event. Preserve the message; parameterize rather than string-concatenate.
- `System.err.println(x)` → `log.error("{}", x)`.
- `e.printStackTrace()` (and catch-block console dumps) → `log.error("<short context>: {}", e.getMessage(), e)`.
- No JavaDoc, no signature changes, no control-flow changes beyond the swap.

Batched by size so each commit is bisectable. Compile after each file, run the **per-service test** where one exists (most services have a `*ServiceTest`), and the full suite at the end of each batch.

- [ ] **Step 1 — DeviceService (252 occurrences), its own batch.** Apply the rules across the file in sub-chunks; `.\mvnw.cmd -q compile` between chunks. Then `.\mvnw.cmd -q -Dtest=DeviceServiceTest test` → green. Commit: `refactor(logging): DeviceService System.out -> SLF4J`.
- [ ] **Step 2 — Conditions/search batch (143): `ConditionsService.java` (86), `DeviceSearchService.java` (57).** Apply rules; compile; `.\mvnw.cmd -q -Dtest=ConditionsServiceTest,DeviceSearchServiceTest test` → green. Commit: `refactor(logging): conditions/search services System.out -> SLF4J`.
- [ ] **Step 3 — Remaining services batch (~119): `MeasuringInstrumentService` (27), `LocationService` (16), `EssentialService` (13), `DeviceConditionsService` (10), `AiCallService` (9), `FloorService` (9), `BuildingService` (4), `AssetOnboardService` (2), `UserService` (2), `TechnicianCertificateService` (2), `DeviceLifecycleHistoryService` (1), `TechnicianService` (1).** Apply rules; compile; run the matching `*ServiceTest`s that exist; full suite `.\mvnw.cmd -q test` → green. Commit: `refactor(logging): remaining services System.out -> SLF4J`.
- [ ] **Step 4 — Utils batch (~110): `ConditionUtils` (26), `RemoteAccessProcessParser` (10), `IPSUtils` (10), `InstrumentFormula` (10), `APIRequest` (9), `UDPReceive` (8), `FileUtils` (8), `CorrigoUtils` (8), `HeaderFooterPageEvent` (5), `SQLConnectionUtils` (5), `Utils` (4), `ChirpStackUtils` (2), `MaximoUtils` (2), `P2PActivePorts` (1), `NetworkUtils` (1), `DockerUtils` (1).** Apply rules; `.\mvnw.cmd -q compile` after each; full suite → green. Commit: `refactor(logging): util classes System.out -> SLF4J`.
- [ ] **Step 5 — Config batch: `JwtRequestFilter.java` (4), `WebSecurityConfig.java` (6).** Apply rules; compile; full suite → green. Commit: `refactor(logging): security/config classes System.out -> SLF4J`.

---

## Task 10: Final verification

**Files:** none (verification) + optional Postman regen.

- [ ] **Step 1:** Confirm no stray console prints remain in `src/main` (Grep for `System.out.println`, `System.err.println`, `printStackTrace` — only `MaximoExceptionHandler`'s `printStackTrace(pw)` should remain).
- [ ] **Step 2:** Full suite `.\mvnw.cmd -q test` → `BUILD SUCCESS`, same baseline test count plus the 3 new `GlobalExceptionHandlerTest` cases.
- [ ] **Step 3:** Manual Swagger check — boot in the docker profile, open `http://localhost:8085/swagger-ui/index.html`: each controller appears under its `@Tag`, operations show summaries/descriptions, params show descriptions, response codes are listed. `http://localhost:8085/v3/api-docs` returns 200.
- [ ] **Step 4 (optional):** Regenerate the Postman collection from the enriched spec — `curl :8085/v3/api-docs -m 40 -o spec.json` then `python tools/gen_postman.py spec.json sclera-cloud-device-asset/postman/sclera-cloud-device-asset.postman_collection.json http://localhost:8080/asset`. Commit if regenerated.

---

## Self-review notes (addressed)

- **Spec coverage:** Swagger depth (Tag/Operation/Parameter/ApiResponses) → Tasks 2–7; JavaDoc on controllers → Tasks 2–7; central handler + error-shape standardization → Task 1; per-method try/catch removal → Tasks 2–7; service logging cleanup (37 files / 619 prints) → Task 9; subscriber JavaDoc+logging-only → Task 8; pilot-then-review sequencing → Task 2 review gate; verification → Task 10.
- **Behavioral-change guard:** the only intended behavior change is error-response shape (Task 1). Each controller/service batch re-runs the suite to catch accidental changes; any test asserting the old generic-error shape is updated to the `ResponseDTO` shape when encountered.
- **Type consistency:** `GlobalExceptionHandler.build(...)` uses `ResponseDTO(String, int, Object, boolean, BigInteger)` — the real 5-arg ctor. `@Tag`/`@Operation`/`@Parameter`/`@ApiResponses` come from `io.swagger.v3.oas.annotations.*` (springdoc 3.0.3, already on classpath). Logger type is `org.slf4j.Logger` throughout.
