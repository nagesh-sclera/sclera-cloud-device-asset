# DeviceConditionsService Test Coverage — Design Spec

**Date:** 2026-06-03
**Status:** Approved
**Scope:** Unit-test coverage for `io.sclera.service.DeviceConditionsService` in the `sclera-cloud-device-asset` module — the proving-ground for a repeatable service-test pattern.

---

## Problem

The `sclera-cloud-device-asset` module has **49 service classes (~27k lines)** with **zero service-layer tests** (the 54 existing tests are almost all `client/` stub tests). Testing everything at once is too large and token-heavy. This spec covers **one representative service** (`DeviceConditionsService`, 272 lines) to establish a cheap, repeatable Mockito pattern that later rounds reuse. Efficiency (minimal tokens/time) is an explicit goal.

## Approach

**Pure Mockito unit tests** — no Spring context, no database. `DeviceConditionsService` is orchestration over a repository and two clients, so its collaborators are mocked and behavior is verified via return values and interaction verification.

Rejected: `@SpringBootTest`+Testcontainers (slow, token-heavy, overkill) and `@DataJpaTest` (the repo uses custom/native queries — not the unit under test).

## Unit Under Test

`DeviceConditionsService` uses field injection (`@Autowired`) of four collaborators, all mocked:
- `DeviceConditionsRepository` — persistence (custom query methods)
- `AlertProfileClient` — enriches conditions with alert-profile details
- `DeviceService` — device lookups + side-effect calls (status, delete-alert-job)
- `JobSchedulerService` — scheduled-job lookup

## Test File

`src/test/java/io/sclera/service/DeviceConditionsServiceTest.java`

**Setup:** `@ExtendWith(MockitoExtension.class)`, `@InjectMocks DeviceConditionsService`, `@Mock` for the four collaborators. Private builder helpers (`deviceConditionsDTO(...)`, `deviceDTO(...)`) reduce DTO boilerplate and are the reusable pattern for subsequent service tests.

## Coverage — main path + key branches (~16 tests)

| Method | Cases |
|--------|-------|
| `getAlertCount` | delegates to repo, returns value |
| `updateAlertProfileId` | delegates to repo |
| `getDeviceConditionsForAiCall` | returns repo result |
| `getDeviceConditionsByIdForAiCall` | returns repo result |
| `updateLastAlertedDetails` | delegates with correct args |
| `updateAlertCountByConditionId` | valid (id≠null, count>0) → updates; invalid (null id or count≤0) → no repo call |
| `getDeviceConditions` | enriches via `alertProfileClient` when `alert_profile_id` set; skips when null |
| `getDeviceConditionsById` | enriches when profile id present |
| `deleteAllDeviceConditions` | deletes each returned condition by id |
| `deleteDeviceConditions` | deletes each passed condition by id |
| `resetDeviceConditions` | resets each; calls `deviceService.getDeviceConditionStatus` only when device non-null with status |
| `upsertDeviceConditions` | (a) new insert (id null, not existing) → `addDeviceConditions`; (b) existing → `updateDeviceConditions` + reset-on-change zeroes `alert_count`/`last_alerted`; (c) trigger-time changed with scheduled job present → `deviceService.deleteDeviceAlertJob` called |
| `upsertDeviceConditionsForAiCall` | (a) new (id null) → add; (b) existing with `alertCount==3` → delete + re-add |
| `shareDeviceConditions` | `"add"` → `upsert` with mapped list; `"replace"` → delete then upsert |

**Error handling:** one test confirms `shareDeviceConditions` swallows a downstream exception (does not propagate), matching its `try/catch` behavior.

**Not covered (YAGNI for this round):** exhaustive enumeration of every nested conditional inside `upsertDeviceConditions` (alert_count_time permutations, every OR-clause in the change-detection block). The branchy method gets its main paths; full combinatorics are deferred.

## Testing / Run

`./mvnw -Dtest=DeviceConditionsServiceTest test`

Compiles the module once, then runs only this class — no Spring context, no containers; executes in seconds. Mockito's strict stubbing (default in `MockitoExtension`) keeps the tests honest.

## Out of Scope

- The other 48 service classes (later rounds, reusing this pattern).
- `DeviceService` itself (10k lines — its own decomposition/effort).
- Controller, repository, and integration testing.

## Definition of Done

- `DeviceConditionsServiceTest` exists with the cases above and passes.
- Reusable DTO-builder + Mockito setup pattern established for the next service.
