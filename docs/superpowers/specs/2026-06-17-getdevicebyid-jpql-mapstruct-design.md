# Eliminate `getDeviceByDeviceId` Native Query → JPQL + MapStruct

**Date:** 2026-06-17
**Module:** `sclera-cloud-device-asset` (only)
**Status:** Approved design — pending implementation plan

## Problem

The module relies on ~1,057 native-query sites (404 `@NamedNativeQuery` + 653 inline
`@Query(nativeQuery = true)`). Response DTOs are populated by Hibernate
`@SqlResultSetMapping` / `@ConstructorResult` projections — a mapping style that is invisible to
the compiler (column/constructor mismatches fail only at runtime) and couples every read to
hand-written SQL.

The goal is to begin eliminating `@NamedNativeQuery` usage and introduce **MapStruct** for
entity→DTO mapping of responses. This spec covers the **first, safest vertical slice**: the
single-device read path `DeviceRepository.getDeviceByDeviceId(String device_id)`.

## Hard constraint — response contract is frozen

The frontend is already built against the exact response keys and shapes. **Every response key,
type, and value must remain byte-identical.** This is non-negotiable and drives every design
choice below.

`getDeviceByDeviceId` is additionally called from ~40 sites inside `DeviceService`, so its
`DeviceDTO` output is an internal contract as well. The conversion therefore happens **in place**:
the repository method keeps its signature (`DeviceDTO getDeviceByDeviceId(String device_id)`) and
return type, so no caller and no endpoint observes any change.

## Scope

In scope:
- Convert `DeviceRepository.getDeviceByDeviceId(String device_id)` from native +
  `@SqlResultSetMapping` to **JPQL + a projection + a MapStruct mapper**.
- Remove **only** the `@NamedNativeQuery(name = "Device.getDeviceByDeviceId")` declaration in
  `Device.java`.
- Add MapStruct to the module build.

Explicitly out of scope (this round):
- `@SqlResultSetMapping("devicedtomapping")` is **kept** — the filtered/paginated list queries
  (`listAllDevicebyVdmsidAndDockerName`, `getfilterdevices`, and many others) still use it.
- The filtered-list multi-join projections stay native, per the existing repo strategy
  (comments tag them `NOT CONVERTED — stays native (PG-translation track)`).
- Write-side native queries (INSERT/UPDATE/DELETE/`ON CONFLICT`) — they produce no response.
- All other entities/DTOs.

## Current behaviour (baseline)

`Device.getDeviceByDeviceId` (in `Device.java`) selects ~120 columns into `DeviceDTO` via
`resultSetMapping = "devicedtomapping"`:

- The bulk are plain `device` columns `d.*` — including the denormalised count columns
  (`d.snmp_count`, `d.notes_count`, `d.ticket_count`, `d.bacnet_count`, …). These are **stored
  columns, not aggregations**, which is what makes this path cleanly mappable.
- `display_name`, `vendor`, `model` are selected **plain** (`d.display_name`, not the
  `COALESCE(user_data_*, …)` form used by the list queries) — so no computed fallback logic is
  required here.
- Joined scalars from 5 LEFT JOINs:
  - `location`        ← `l.name`            (`LEFT JOIN location l ON d.location_id = l.id`)
  - `location_id`     ← `l.id`
  - `floor`           ← `f.name`            (`LEFT JOIN floor f ON l.floor_id = f.id`)
  - `floor_id`        ← `f.id`
  - `building`        ← `b.name`            (`LEFT JOIN building b ON f.building_id = b.id`)
  - `building_id`     ← `b.id`
  - onboard status    ← `dos.id`, `dos.assignee_email`, `dos.image_status`,
                          `dos.geolocation_status`, `dos.tag_status`, `dos.field_status`
                          (`LEFT JOIN device_onboard_status dos ON d.id = dos.device_id`)
  - inventory         ← `ind.tracking_id` → `inventory_tracking_id`
                          (`LEFT JOIN inventory_device ind ON d.id = ind.device_id`)
- A few aliased remaps: `vdms_id` ← `d.docker_vdms_id`, `system_type` ← `d.type`.
- DTO fields **not** selected by this query are left `null` today.

All 5 join targets exist as JPA entities: `Location`, `Floor`, `Building`,
`DeviceOnboardStatus`, `InventoryDevice`. `DeviceDTO` is a flat bean with full getters/setters.

## Target design (Approach A — projection record + MapStruct)

### Query
Replace the native query with JPQL on the repository method using ad-hoc entity joins
(supported by Hibernate ORM 6+/HQL):

```
@Query("""
  SELECT d, l.name, l.id, f.name, f.id, b.name, b.id,
         dos.id, dos.assigneeEmail, dos.imageStatus, dos.geolocationStatus,
         dos.tagStatus, dos.fieldStatus, ind.trackingId
  FROM Device d
  LEFT JOIN Location l ON l.id = d.locationId
  LEFT JOIN Floor f ON f.id = l.floorId
  LEFT JOIN Building b ON b.id = f.buildingId
  LEFT JOIN DeviceOnboardStatus dos ON dos.deviceId = d.id
  LEFT JOIN InventoryDevice ind ON ind.deviceId = d.id
  WHERE d.id = :deviceId
""")
List<Object[]> findDeviceDetailRow(@Param("deviceId") String deviceId);
```

(Exact JPA property names for the join `ON` columns and the `dos.*`/`ind.*` fields are confirmed
against each entity during implementation. Element `[0]` is the managed `Device` entity; the rest
are the joined scalars in order.)

### Projection record
```
public record DeviceDetailRow(
    Device device,
    String location, String locationId,
    String floor, String floorId,
    String building, String buildingId,
    String onboardStatusId, String assigneeEmail,
    Integer imageStatus, Integer geolocationStatus, Integer tagStatus, Integer fieldStatus,
    String inventoryTrackingId) {}
```
A thin assembler converts each `Object[]` row into a `DeviceDetailRow` (carrying the entity +
scalars), avoiding a 120-component constructor expression in JPQL.

### MapStruct mapper
```
@Mapper(componentModel = "spring")
public interface DeviceDtoMapper {
    @BeanMapping(ignoreByDefault = true)           // PARITY LOCK: map ONLY listed targets
    @Mapping(target = "id",            source = "device.id")
    @Mapping(target = "display_name",  source = "device.displayName")
    @Mapping(target = "vdms_id",       source = "device.dockerVdmsId")
    @Mapping(target = "system_type",   source = "device.type")
    // … one explicit @Mapping per device.* column the old SELECT populated …
    @Mapping(target = "location",      source = "location")
    @Mapping(target = "location_id",   source = "locationId")
    @Mapping(target = "floor",         source = "floor")
    @Mapping(target = "floor_id",      source = "floorId")
    @Mapping(target = "building",      source = "building")
    @Mapping(target = "building_id",   source = "buildingId")
    @Mapping(target = "inventory_tracking_id", source = "inventoryTrackingId")
    // … onboard-status targets …
    DeviceDTO toDto(DeviceDetailRow row);
}
```

`@BeanMapping(ignoreByDefault = true)` is the core parity mechanism: MapStruct maps **only** the
targets we explicitly list, so it can never populate a `DeviceDTO` field that the old query left
`null`. The explicit `@Mapping` list is a machine-checkable mirror of the old SELECT list. The
mapper is intentionally verbose (~110 lines); that verbosity *is* the guarantee.

### Wiring
The repository method `getDeviceByDeviceId(String device_id)` keeps its signature. Its body
(via a `default` method or a thin service-layer wrapper) calls `findDeviceDetailRow`, assembles
the first row into a `DeviceDetailRow`, maps it with `DeviceDtoMapper.toDto`, and returns it (or
`null` when no row) — matching today's single-result / null behaviour.

## Build / config changes

In `sclera-cloud-device-asset/pom.xml`:
- Add `<mapstruct.version>` property (latest stable for Java 21 / Spring Boot 4).
- Add dependencies `org.mapstruct:mapstruct` (compile) and `org.mapstruct:mapstruct-processor`
  (provided/annotation-processor).
- Configure `maven-compiler-plugin` `annotationProcessorPaths` in the required order:
  **lombok → lombok-mapstruct-binding → mapstruct-processor** (Lombok must generate
  getters/setters before MapStruct reads them).

## Verification

1. **Baseline capture (before):** from the running container, capture `GET
   /device/{id}/getdevice` JSON for representative devices: (a) device with location/floor/
   building, (b) device with onboard status + inventory tracking, (c) a bare device with nulls.
2. **Diff (after):** the new response must be byte-identical to the baseline (modulo JSON key
   order). Any field that gains or loses a value is a defect.
3. **Integration test:** a focused JPA test (following existing `*IT` / Testcontainers patterns,
   e.g. `DeviceRepositoryIT`) seeds a device + relations and asserts the mapped `DeviceDTO`
   field-by-field, locking parity in CI.
4. **Build:** `mvn -q -pl sclera-cloud-device-asset compile` succeeds (MapStruct generates the
   mapper impl); full module build + the container rebuild verify wiring.

## Rollout

This slice proves the pattern: JPQL + projection record + `ignoreByDefault` MapStruct mapper,
with parity verified by diff + test. Once green, the same shape templates to sibling single-row
read paths (`getDeviceByDeviceIdNew`, `getDeviceAndOnboardStatusByDeviceId`, …). Filtered-list
projections remain native per the existing PG-translation strategy. Each subsequent slice is its
own small, independently verifiable change.

## Risks & mitigations

- **Over-mapping changes the response** → `@BeanMapping(ignoreByDefault = true)` + explicit
  per-field `@Mapping`; parity diff + field-by-field test.
- **JPQL ad-hoc `JOIN ... ON` unsupported** → confirmed supported in Hibernate ORM 6+/HQL;
  validated by the integration test at build time.
- **Annotation-processor ordering (Lombok vs MapStruct)** → explicit `annotationProcessorPaths`
  with `lombok-mapstruct-binding`.
- **Property-name drift** (JPQL uses entity property names, not DB columns) → confirmed against
  each entity during implementation; a wrong name fails the build, not silently at runtime.

## Notes

- Per the standing project rule, changes are left in the working tree; the user commits manually.
- `sclera-vdms-edge-server` (root/reference repo) is read-only and untouched.
