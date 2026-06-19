# getDeviceByDeviceId JPQL + MapStruct Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `DeviceRepository.getDeviceByDeviceId(String)` native query + `@SqlResultSetMapping` with a JPQL query, a projection record, and a MapStruct mapper — keeping the `DeviceDTO` response byte-identical.

**Architecture:** A JPQL query fetches the managed `Device` entity plus the cross-table scalars the response needs: location/floor/building names+ids (via association joins), onboard-status fields + inventory tracking id (via ad-hoc `ON` joins), and the association-FK ids the entity hides behind `@ManyToOne`/`@OneToOne` (`docker_name`, `docker_vdms_id`, the 5 `Phonebook` vendor ids, `assigned_user_email`) via association-id navigation. Each result row is wrapped in a `DeviceDetailRow` record. A MapStruct mapper (`@BeanMapping(ignoreByDefault = true)` + one explicit `@Mapping` per selected column) converts `DeviceDetailRow → DeviceDTO`. The repository method keeps its signature, so all ~40 callers and `GET /device/{id}/getdevice` are untouched.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Data JPA / Hibernate ORM 6+, MapStruct 1.6.3, Lombok, Maven, PostgreSQL 16, JUnit 5 + AssertJ + existing `*IT` Testcontainers patterns.

**Spec:** `docs/superpowers/specs/2026-06-17-getdevicebyid-jpql-mapstruct-design.md`

**Standing constraints:**
- **Do NOT `git commit` / `git push`.** The user commits manually. Every task ends "leave changes in the working tree." Do not run git commit.
- `sclera-vdms-edge-server` is read-only; do not touch it.
- Response keys/types/values must stay byte-identical. Parity is the acceptance gate.

---

## Authoritative field table (verified against source)

The `Device.getDeviceByDeviceId` SELECT, the `@SqlResultSetMapping("devicedtomapping")` `@ConstructorResult`, and the `DeviceDTO` constructor are all in the **same order** (123 columns; `getDeviceByDeviceId` does NOT include `qrcode_count`). Every column resolved to a DTO field; none indeterminate. Key facts that drive the code below:

- **`Device` entity Java fields are snake_case** (e.g. `private Integer snmp_count;`). So MapStruct source paths are `device.snmp_count`, `device.display_name`, etc. — NOT camelCase.
- **8 selected columns are association FKs, not scalar fields** — they must be pulled via JPQL association navigation, NOT `device.<field>`:
  | DTO field | JPQL source | association |
  |---|---|---|
  | `vdms_id` | `dk.vdms.id` | `device.docker` (`@ManyToOne Docker`, `@MapsId Vdms vdms`) |
  | `docker_name` | `dk.name` | `device.docker` |
  | `local_vendor_id` | `lv.id` | `device.local_vendor` (`@ManyToOne Phonebook`) |
  | `global_vendor_id` | `gv.id` | `device.global_vendor` |
  | `other_vendor_1_id` | `ov1.id` | `device.other_vendor_1` |
  | `other_vendor_2_id` | `ov2.id` | `device.other_vendor_2` |
  | `other_vendor_3_id` | `ov3.id` | `device.other_vendor_3` |
  | `assigned_user_email` | `u.email` | `device.user` (`@ManyToOne User`, `@JoinColumn(name="assigned_user_email", referencedColumnName="email")`) |
- **Join scalars:** `location`←`l.name`, `location_id`←`l.id`, `floor`←`f.name`, `floor_id`←`f.id`, `building`←`b.name`, `building_id`←`b.id` (via `d.location`→`l.floor`→`f.building`); `device_onboard_status_id`←`dos.id`, `assignee_email`←`dos.assignee_email`, `image_status`←`dos.image_status`, `geolocation_status`←`dos.geolocation_status`, `tag_status`←`dos.tag_status`, `field_status`←`dos.field_status` (`DeviceOnboardStatus`, `@OneToOne device`); `inventory_tracking_id`←`ind.tracking_id` (`InventoryDevice`, `@OneToOne device`).
- **2 type mismatches needing null-safe conversion** (the native query relied on implicit DB→String cast; MapStruct needs explicit String conversion):
  - `last_seen_on`: entity `BigInteger` → DTO `String`.
  - `alarm`: entity `Integer` → DTO `String`.
- **`d.type` is mapped twice**: to `type` and to `system_type`.
- Join-entity property names/types: `Location`(`id`:String, `name`:String, `floor`:Floor), `Floor`(`id`:String, `name`:String, `building`:Building), `Building`(`id`:String, `name`:String), `DeviceOnboardStatus`(`id`:String, `device`:Device, `assignee_email`:String, `image_status`:Integer, `geolocation_status`:Integer, `tag_status`:Integer, `field_status`:Integer), `InventoryDevice`(`tracking_id`:String `@Id`, `device`:Device).

---

## File Structure

- **Modify** `sclera-cloud-device-asset/pom.xml` — MapStruct deps + `maven-compiler-plugin` `annotationProcessorPaths` (lombok → lombok-mapstruct-binding → mapstruct-processor).
- **Create** `.../io/sclera/dto/projection/DeviceDetailRow.java` — projection record (managed `Device` + 22 scalars).
- **Create** `.../io/sclera/mapper/DeviceDtoMapper.java` — MapStruct mapper `DeviceDetailRow → DeviceDTO`.
- **Create** `.../io/sclera/mapper/DeviceRepositoryMapperHolder.java` — bridges the Spring-managed mapper into the repo default method.
- **Modify** `.../io/sclera/models/Device.java` — remove `@NamedNativeQuery(name = "Device.getDeviceByDeviceId")` (~lines 228-256). Keep `@SqlResultSetMapping("devicedtomapping")`.
- **Modify** `.../io/sclera/Repository/DeviceRepository.java` — replace native `getDeviceByDeviceId` with a JPQL projection query + a `default` assemble-and-map method.
- **Create** `.../src/test/java/io/sclera/mapper/DeviceDtoMapperTest.java` — pure mapper unit test.
- **Create** `.../src/test/java/io/sclera/Repository/DeviceByIdIT.java` — seeded parity integration test.

---

## Task 1: Add MapStruct to the build

**Files:** Modify `sclera-cloud-device-asset/pom.xml`

- [ ] **Step 1: Add version properties.** In `<properties>` (currently just `<java.version>21</java.version>`):

```xml
<properties>
    <java.version>21</java.version>
    <mapstruct.version>1.6.3</mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>
```

- [ ] **Step 2: Add the MapStruct dependency** next to the existing Lombok dependency (~lines 251-254):

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
```

- [ ] **Step 3: Add the compiler plugin** inside `<build><plugins>` (after `spring-boot-maven-plugin`, before `maven-javadoc-plugin`). Order matters — Lombok must precede MapStruct:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>${lombok-mapstruct-binding.version}</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

`${lombok.version}` is managed by the Spring Boot parent. If Maven reports it unresolved, pin the explicit version from `mvn help:evaluate -Dexpression=lombok.version -q -DforceStdout`.

- [ ] **Step 4: Compile** to confirm Lombok still runs under the new processor config.

Run (repo root): `mvn -q -pl sclera-cloud-device-asset -am compile`
Expected: BUILD SUCCESS (no mapper yet; this only proves Lombok getters/setters still generate).

- [ ] **Step 5:** Leave changes in the working tree (no commit).

---

## Task 2: Create the `DeviceDetailRow` projection record

**Files:** Create `.../io/sclera/dto/projection/DeviceDetailRow.java`

- [ ] **Step 1: Write the record** (23 components: the entity + 22 scalars, in the exact JPQL SELECT order used in Task 4):

```java
package io.sclera.dto.projection;

import io.sclera.models.Device;

/**
 * Projection for the single-device read ({@code getDeviceByDeviceId}): the managed {@link Device}
 * entity plus the cross-table scalars the DeviceDTO needs — association-FK ids the entity hides
 * behind @ManyToOne/@OneToOne, and the joined location/floor/building/onboard/inventory values.
 * Mapped to {@code DeviceDTO} by {@code DeviceDtoMapper}. Component order matches the JPQL SELECT.
 */
public record DeviceDetailRow(
        Device device,
        String dockerName,          // docker.name           -> docker_name
        String vdmsId,              // docker.vdms.id         -> vdms_id
        String location,            // location.name          -> location
        String locationId,          // location.id            -> location_id
        String floor,               // floor.name             -> floor
        String floorId,             // floor.id               -> floor_id
        String building,            // building.name          -> building
        String buildingId,          // building.id            -> building_id
        String localVendorId,       // local_vendor.id        -> local_vendor_id
        String globalVendorId,      // global_vendor.id       -> global_vendor_id
        String otherVendor1Id,      // other_vendor_1.id      -> other_vendor_1_id
        String otherVendor2Id,      // other_vendor_2.id      -> other_vendor_2_id
        String otherVendor3Id,      // other_vendor_3.id      -> other_vendor_3_id
        String assignedUserEmail,   // user.email             -> assigned_user_email
        String onboardStatusId,     // device_onboard_status.id            -> device_onboard_status_id
        String assigneeEmail,       // device_onboard_status.assignee_email -> assignee_email
        Integer imageStatus,        // device_onboard_status.image_status   -> image_status
        Integer geolocationStatus,  // device_onboard_status.geolocation_status -> geolocation_status
        Integer tagStatus,          // device_onboard_status.tag_status     -> tag_status
        Integer fieldStatus,        // device_onboard_status.field_status   -> field_status
        String inventoryTrackingId  // inventory_device.tracking_id         -> inventory_tracking_id
) {}
```

- [ ] **Step 2: Compile.** Run: `mvn -q -pl sclera-cloud-device-asset compile` → BUILD SUCCESS.
- [ ] **Step 3:** Leave in working tree (no commit).

---

## Task 3: Create the `DeviceDtoMapper` (MapStruct)

**Files:** Create `.../io/sclera/mapper/DeviceDtoMapper.java`

`@BeanMapping(ignoreByDefault = true)` is the parity lock: only listed targets are set, so no field the old query left null gets populated. Device scalar fields map `source = "device.<snake_field>"`. The 8 association-FK ids + the join scalars map from the `DeviceDetailRow` components. `device.type` maps to both `type` and `system_type`. `last_seen_on` and `alarm` use null-safe conversion methods.

- [ ] **Step 1: Write the mapper.**

```java
package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.projection.DeviceDetailRow;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigInteger;

/**
 * Maps {@link DeviceDetailRow} to {@link DeviceDTO} for the single-device read. With
 * {@code ignoreByDefault = true} only the explicitly listed targets are populated, so the output
 * matches the old {@code devicedtomapping} @ConstructorResult exactly (unselected fields stay null).
 */
@Mapper(componentModel = "spring")
public interface DeviceDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    // --- device scalar columns (entity fields are snake_case) ---
    @Mapping(target = "id",                          source = "device.id")
    @Mapping(target = "status",                       source = "device.status")
    @Mapping(target = "display_name",                 source = "device.display_name")
    @Mapping(target = "last_seen_on",                 source = "device.last_seen_on", qualifiedByName = "bigIntToStr")
    @Mapping(target = "mac_address",                  source = "device.mac_address")
    @Mapping(target = "vendor",                       source = "device.vendor")
    @Mapping(target = "model",                        source = "device.model")
    @Mapping(target = "type",                         source = "device.type")
    @Mapping(target = "ip_address",                   source = "device.ip_address")
    @Mapping(target = "monitor",                      source = "device.monitor")
    @Mapping(target = "network_layer",                source = "device.network_layer")
    @Mapping(target = "user_data_model",              source = "device.user_data_model")
    @Mapping(target = "user_data_vendor",             source = "device.user_data_vendor")
    @Mapping(target = "user_data_name",               source = "device.user_data_name")
    @Mapping(target = "parent",                       source = "device.parent")
    @Mapping(target = "snmp_parent",                  source = "device.snmp_parent")
    @Mapping(target = "system_type",                  source = "device.type")
    @Mapping(target = "remote_access",                source = "device.remote_access")
    @Mapping(target = "product_id",                   source = "device.product_id")
    @Mapping(target = "alarm",                        source = "device.alarm", qualifiedByName = "intToStr")
    @Mapping(target = "virtual_device_type",          source = "device.virtual_device_type")
    @Mapping(target = "warranty",                     source = "device.warranty")
    @Mapping(target = "quick_link_name",              source = "device.quick_link_name")
    @Mapping(target = "quick_link_url",               source = "device.quick_link_url")
    @Mapping(target = "email_alert",                  source = "device.email_alert")
    @Mapping(target = "sms_alert",                    source = "device.sms_alert")
    @Mapping(target = "popup_notification",           source = "device.popup_notification")
    @Mapping(target = "snmp_count",                   source = "device.snmp_count")
    @Mapping(target = "snmp_status",                  source = "device.snmp_status")
    @Mapping(target = "interface_count",              source = "device.interface_count")
    @Mapping(target = "notes_count",                  source = "device.notes_count")
    @Mapping(target = "ticket_count",                 source = "device.ticket_count")
    @Mapping(target = "ticket_status",                source = "device.ticket_status")
    @Mapping(target = "serial_number",                source = "device.serial_number")
    @Mapping(target = "bacnet_count",                 source = "device.bacnet_count")
    @Mapping(target = "bacnet_status",                source = "device.bacnet_status")
    @Mapping(target = "lorawan_count",                source = "device.lorawan_count")
    @Mapping(target = "lorawan_status",               source = "device.lorawan_status")
    @Mapping(target = "disruptive_count",             source = "device.disruptive_count")
    @Mapping(target = "disruptive_status",            source = "device.disruptive_status")
    @Mapping(target = "my_devices_count",             source = "device.my_devices_count")
    @Mapping(target = "my_devices_status",            source = "device.my_devices_status")
    @Mapping(target = "local_vendor_email_alert",     source = "device.local_vendor_email_alert")
    @Mapping(target = "local_vendor_sms_alert",       source = "device.local_vendor_sms_alert")
    @Mapping(target = "monnit_count",                 source = "device.monnit_count")
    @Mapping(target = "monnit_status",                source = "device.monnit_status")
    @Mapping(target = "pelican_count",                source = "device.pelican_count")
    @Mapping(target = "pelican_status",               source = "device.pelican_status")
    @Mapping(target = "knx_count",                     source = "device.knx_count")
    @Mapping(target = "knx_status",                    source = "device.knx_status")
    @Mapping(target = "subsystem_parent_id",          source = "device.subsystem_parent_id")
    @Mapping(target = "subsystem_count",              source = "device.subsystem_count")
    @Mapping(target = "custom_fields",                source = "device.custom_fields")
    @Mapping(target = "description",                   source = "device.description")
    @Mapping(target = "asset_match_status",           source = "device.asset_match_status")
    @Mapping(target = "matched_product_ids",          source = "device.matched_product_ids")
    @Mapping(target = "latitude",                      source = "device.latitude")
    @Mapping(target = "longitude",                     source = "device.longitude")
    @Mapping(target = "measuring_instrument_count",   source = "device.measuring_instrument_count")
    @Mapping(target = "document_count",               source = "device.document_count")
    @Mapping(target = "media_count",                   source = "device.media_count")
    @Mapping(target = "checklist_template_count",     source = "device.checklist_template_count")
    @Mapping(target = "snmp_object_count",            source = "device.snmp_object_count")
    @Mapping(target = "snmp_object_status",           source = "device.snmp_object_status")
    @Mapping(target = "position",                      source = "device.position")
    @Mapping(target = "measuring_instrument_status",  source = "device.measuring_instrument_status")
    @Mapping(target = "record_checklist_count",       source = "device.record_checklist_count")
    @Mapping(target = "record_checklist_status",      source = "device.record_checklist_status")
    @Mapping(target = "daintree_count",               source = "device.daintree_count")
    @Mapping(target = "daintree_status",              source = "device.daintree_status")
    @Mapping(target = "asset_image_url",              source = "device.asset_image_url")
    @Mapping(target = "created_timestamp",            source = "device.created_timestamp")
    @Mapping(target = "ecobee_count",                 source = "device.ecobee_count")
    @Mapping(target = "ecobee_status",                source = "device.ecobee_status")
    @Mapping(target = "modbus_count",                 source = "device.modbus_count")
    @Mapping(target = "modbus_status",                source = "device.modbus_status")
    @Mapping(target = "created_email",                source = "device.created_email")
    @Mapping(target = "asset_group",                   source = "device.asset_group")
    @Mapping(target = "updated_email",                source = "device.updated_email")
    @Mapping(target = "updated_timestamp",            source = "device.updated_timestamp")
    @Mapping(target = "onboard_status",               source = "device.onboard_status")
    @Mapping(target = "asset_ocr_image_url",          source = "device.asset_ocr_image_url")
    @Mapping(target = "category",                      source = "device.category")
    @Mapping(target = "sub_category",                 source = "device.sub_category")
    @Mapping(target = "location_status",              source = "device.location_status")
    @Mapping(target = "digital_twin_image_url",       source = "device.digital_twin_image_url")
    @Mapping(target = "poly_lens_count",              source = "device.poly_lens_count")
    @Mapping(target = "cost_value",                    source = "device.cost_value")
    @Mapping(target = "ai_call",                       source = "device.ai_call")
    @Mapping(target = "cost_unit",                     source = "device.cost_unit")
    @Mapping(target = "is_dnd_enabled",               source = "device.is_dnd_enabled")
    @Mapping(target = "operational_status",           source = "device.operational_status")
    @Mapping(target = "adc_json",                      source = "device.adc_json")
    @Mapping(target = "system_type_id",               source = "device.system_type_id")
    @Mapping(target = "system_type_name",             source = "device.system_type_name")
    @Mapping(target = "asset_type_id",                source = "device.asset_type_id")
    @Mapping(target = "asset_type_name",              source = "device.asset_type_name")
    @Mapping(target = "asset_sub_type_id",            source = "device.asset_sub_type_id")
    @Mapping(target = "asset_sub_type_name",          source = "device.asset_sub_type_name")
    @Mapping(target = "source_type",                  source = "device.source_type")
    @Mapping(target = "asset_tag_images_url",         source = "device.asset_tag_images_url")
    // --- association-FK ids + join scalars (from the projection record) ---
    @Mapping(target = "docker_name",                  source = "dockerName")
    @Mapping(target = "vdms_id",                       source = "vdmsId")
    @Mapping(target = "location",                      source = "location")
    @Mapping(target = "location_id",                   source = "locationId")
    @Mapping(target = "floor",                         source = "floor")
    @Mapping(target = "floor_id",                      source = "floorId")
    @Mapping(target = "building",                      source = "building")
    @Mapping(target = "building_id",                   source = "buildingId")
    @Mapping(target = "local_vendor_id",              source = "localVendorId")
    @Mapping(target = "global_vendor_id",             source = "globalVendorId")
    @Mapping(target = "other_vendor_1_id",            source = "otherVendor1Id")
    @Mapping(target = "other_vendor_2_id",            source = "otherVendor2Id")
    @Mapping(target = "other_vendor_3_id",            source = "otherVendor3Id")
    @Mapping(target = "assigned_user_email",          source = "assignedUserEmail")
    @Mapping(target = "device_onboard_status_id",     source = "onboardStatusId")
    @Mapping(target = "assignee_email",               source = "assigneeEmail")
    @Mapping(target = "image_status",                 source = "imageStatus")
    @Mapping(target = "geolocation_status",           source = "geolocationStatus")
    @Mapping(target = "tag_status",                    source = "tagStatus")
    @Mapping(target = "field_status",                 source = "fieldStatus")
    @Mapping(target = "inventory_tracking_id",        source = "inventoryTrackingId")
    DeviceDTO toDto(DeviceDetailRow row);

    /** bigint column rendered as String, null-safe — matches the native @ColumnResult(String) cast. */
    @Named("bigIntToStr")
    default String bigIntToStr(BigInteger v) { return v == null ? null : v.toString(); }

    /** integer column rendered as String, null-safe — matches the native @ColumnResult(String) cast. */
    @Named("intToStr")
    default String intToStr(Integer v) { return v == null ? null : v.toString(); }
}
```

> If MapStruct reports `Unknown property "X" in DeviceDTO`, the target field name differs from the table — fix the `target`. If it reports `Unknown property` in `DeviceDetailRow` or `Device`, fix the `source`. The DTO getters/setters confirm exact field spelling.

- [ ] **Step 2: Compile + inspect generated impl.** Run: `mvn -q -pl sclera-cloud-device-asset compile`
Expected: BUILD SUCCESS, `target/generated-sources/annotations/io/sclera/mapper/DeviceDtoMapperImpl.java` exists. No type-mismatch warnings (the two conversions are explicit; all other source/target types already agree per the table).

- [ ] **Step 3:** Leave in working tree (no commit).

---

## Task 4: Switch the repository method to JPQL + assemble + map

**Files:** Modify `.../Repository/DeviceRepository.java` (the `getDeviceByDeviceId` decl ~lines 67-69) and `.../models/Device.java` (remove the named query ~lines 228-256). Create `.../mapper/DeviceRepositoryMapperHolder.java`.

- [ ] **Step 1: Create the mapper holder** (a Spring Data repository interface can't be `@Autowired`, so bridge the bean in):

```java
package io.sclera.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bridges the Spring-managed {@link DeviceDtoMapper} into Spring Data repository default methods. */
@Component
public class DeviceRepositoryMapperHolder {
    public static DeviceDtoMapper MAPPER;

    @Autowired
    public DeviceRepositoryMapperHolder(DeviceDtoMapper mapper) {
        DeviceRepositoryMapperHolder.MAPPER = mapper;
    }
}
```

- [ ] **Step 2: Replace the native repo method.** In `DeviceRepository.java`, replace:

```java
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceByDeviceId(String device_id);
```

with (column order MUST match `DeviceDetailRow` from Task 2):

```java
    /**
     * Raw projection rows for {@link #getDeviceByDeviceId(String)}: the Device entity plus the
     * association-FK ids and LEFT-JOINed location/floor/building/onboard/inventory scalars.
     * Element [0] is the managed Device; the rest are scalars in DeviceDetailRow order.
     */
    @Query("""
            SELECT d, dk.name, v.id,
                   l.name, l.id, f.name, f.id, b.name, b.id,
                   lv.id, gv.id, ov1.id, ov2.id, ov3.id,
                   u.email,
                   dos.id, dos.assignee_email, dos.image_status, dos.geolocation_status,
                   dos.tag_status, dos.field_status, ind.tracking_id
            FROM Device d
            LEFT JOIN d.docker dk
            LEFT JOIN dk.vdms v
            LEFT JOIN d.location l
            LEFT JOIN l.floor f
            LEFT JOIN f.building b
            LEFT JOIN d.local_vendor lv
            LEFT JOIN d.global_vendor gv
            LEFT JOIN d.other_vendor_1 ov1
            LEFT JOIN d.other_vendor_2 ov2
            LEFT JOIN d.other_vendor_3 ov3
            LEFT JOIN d.user u
            LEFT JOIN DeviceOnboardStatus dos ON dos.device = d
            LEFT JOIN InventoryDevice ind ON ind.device = d
            WHERE d.id = :deviceId
            """)
    java.util.List<Object[]> findDeviceDetailRows(@org.springframework.data.repository.query.Param("deviceId") String deviceId);

    /**
     * Returns the device projection for the given id, or {@code null} if none — same contract as
     * the previous native query. Assembles the row into {@link io.sclera.dto.projection.DeviceDetailRow}
     * and maps it to {@link DeviceDTO} via {@link io.sclera.mapper.DeviceDtoMapper}.
     */
    default DeviceDTO getDeviceByDeviceId(String device_id) {
        java.util.List<Object[]> rows = findDeviceDetailRows(device_id);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        io.sclera.dto.projection.DeviceDetailRow row = new io.sclera.dto.projection.DeviceDetailRow(
                (io.sclera.models.Device) r[0],
                (String) r[1],  (String) r[2],  (String) r[3],  (String) r[4],
                (String) r[5],  (String) r[6],  (String) r[7],  (String) r[8],
                (String) r[9],  (String) r[10], (String) r[11], (String) r[12],
                (String) r[13], (String) r[14], (String) r[15], (String) r[16],
                (Integer) r[17], (Integer) r[18], (Integer) r[19], (Integer) r[20],
                (String) r[21]);
        return io.sclera.mapper.DeviceRepositoryMapperHolder.MAPPER.toDto(row);
    }
```

If `d.user` is not the exact field name for the assigned-user association (confirm in `Device.java`), adjust the `LEFT JOIN d.<field> u`. If any onboard-status scalar is a different type than `Integer` (e.g. `Short`), change both the `DeviceDetailRow` component (Task 2) and the cast here to match.

- [ ] **Step 3: Remove the now-unused named query.** In `Device.java`, delete the entire `@NamedNativeQuery(name = "Device.getDeviceByDeviceId", ... resultSetMapping = "devicedtomapping")` block (~lines 228-256). Do NOT touch `@SqlResultSetMapping("devicedtomapping")` — list queries still use it.

Run (Git Bash): `grep -rn 'name = "Device.getDeviceByDeviceId"' "sclera-cloud-device-asset/src"`
Expected: no matches remain.

- [ ] **Step 4: Compile.** Run: `mvn -q -pl sclera-cloud-device-asset compile` → BUILD SUCCESS.
- [ ] **Step 5:** Leave in working tree (no commit).

---

## Task 5: Unit-test the mapper (no DB)

**Files:** Create `.../src/test/java/io/sclera/mapper/DeviceDtoMapperTest.java`

The mapper reads only `DeviceDetailRow` scalars + the `Device` entity's own scalar fields, so the unit test needs NO association objects.

- [ ] **Step 1: Write the test.** (Use the DTO's actual accessor names — Lombok on snake_case fields generates `getDisplay_name()`, `getSnmp_count()`, etc. Confirm against `DeviceDTO.java`.)

```java
package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.projection.DeviceDetailRow;
import io.sclera.models.Device;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoMapperTest {

    private final DeviceDtoMapper mapper = org.mapstruct.factory.Mappers.getMapper(DeviceDtoMapper.class);

    private DeviceDetailRow row(Device d) {
        return new DeviceDetailRow(d,
                "docker-1", "vdms-9",
                "Room 101", "loc-1", "Floor 1", "flr-1", "Tower A", "bld-1",
                "lv-1", "gv-1", "o1", "o2", "o3",
                "tech@x.com",
                "obs-1", "assignee@x.com", 1, 0, 1, 0,
                "trk-7");
    }

    @Test
    void mapsScalars_associations_joins_andConversions() {
        Device d = new Device();
        d.setId("dev-1");
        d.setDisplay_name("Boiler-1");
        d.setType("hvac");                 // -> type AND system_type
        d.setSnmp_count(3);
        d.setMac_address("AA:BB:CC");
        d.setLast_seen_on(new BigInteger("1699999999")); // BigInteger -> String
        d.setAlarm(5);                      // Integer -> String

        DeviceDTO dto = mapper.toDto(row(d));

        assertThat(dto.getId()).isEqualTo("dev-1");
        assertThat(dto.getDisplay_name()).isEqualTo("Boiler-1");
        assertThat(dto.getType()).isEqualTo("hvac");
        assertThat(dto.getSystem_type()).isEqualTo("hvac");
        assertThat(dto.getSnmp_count()).isEqualTo(3);
        assertThat(dto.getLast_seen_on()).isEqualTo("1699999999");  // converted
        assertThat(dto.getAlarm()).isEqualTo("5");                  // converted
        assertThat(dto.getDocker_name()).isEqualTo("docker-1");
        assertThat(dto.getVdms_id()).isEqualTo("vdms-9");
        assertThat(dto.getLocation()).isEqualTo("Room 101");
        assertThat(dto.getBuilding()).isEqualTo("Tower A");
        assertThat(dto.getLocal_vendor_id()).isEqualTo("lv-1");
        assertThat(dto.getAssigned_user_email()).isEqualTo("tech@x.com");
        assertThat(dto.getInventory_tracking_id()).isEqualTo("trk-7");
    }

    @Test
    void nullConversions_stayNull() {
        Device d = new Device();
        d.setId("dev-2");                  // last_seen_on / alarm left null
        DeviceDTO dto = mapper.toDto(row(d));
        assertThat(dto.getLast_seen_on()).isNull();
        assertThat(dto.getAlarm()).isNull();
    }
}
```

- [ ] **Step 2: Run.** `mvn -q -pl sclera-cloud-device-asset test -Dtest=DeviceDtoMapperTest`
Expected: PASS. A failure is a real mapping defect — fix the mapper, not the assertion.

- [ ] **Step 3:** Leave in working tree (no commit).

---

## Task 6: Integration test — parity against a seeded device

**Files:** Create `.../src/test/java/io/sclera/Repository/DeviceByIdIT.java`

Mirror the existing `DeviceRepositoryIT` setup exactly (same base class / Testcontainers Postgres / `@DataJpaTest` or `@SpringBootTest` + the same persistence helpers).

- [ ] **Step 1: Write the test.**

```java
package io.sclera.Repository;

import io.sclera.dto.DeviceDTO;
// match imports + base class from DeviceRepositoryIT
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceByIdIT /* extends <same base as DeviceRepositoryIT> */ {

    @Autowired DeviceRepository deviceRepository;
    // @Autowired the EntityManager/helpers DeviceRepositoryIT uses to persist rows.

    @Test
    void getDeviceByDeviceId_withRelations_mapsJoinedScalars() {
        // Persist Building(bld-1,"Tower A") -> Floor(flr-1,"Floor 1",bld-1) ->
        // Location(loc-1,"Room 101",flr-1); Device(dev-1, location loc-1, type "hvac",
        // snmp_count 3); DeviceOnboardStatus(device dev-1, assignee_email "a@x.com");
        // InventoryDevice(tracking_id trk-7, device dev-1) — using DeviceRepositoryIT's helpers.

        DeviceDTO dto = deviceRepository.getDeviceByDeviceId("dev-1");

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dev-1");
        assertThat(dto.getLocation()).isEqualTo("Room 101");
        assertThat(dto.getFloor()).isEqualTo("Floor 1");
        assertThat(dto.getBuilding()).isEqualTo("Tower A");
        assertThat(dto.getLocation_id()).isEqualTo("loc-1");
        assertThat(dto.getSnmp_count()).isEqualTo(3);
        assertThat(dto.getSystem_type()).isEqualTo("hvac");
        assertThat(dto.getInventory_tracking_id()).isEqualTo("trk-7");
    }

    @Test
    void getDeviceByDeviceId_bareDevice_joinedScalarsNull() {
        // Persist Device(dev-2) with no location/onboard/inventory/vendor rows.
        DeviceDTO dto = deviceRepository.getDeviceByDeviceId("dev-2");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dev-2");
        assertThat(dto.getLocation()).isNull();
        assertThat(dto.getBuilding()).isNull();
        assertThat(dto.getLocal_vendor_id()).isNull();
        assertThat(dto.getInventory_tracking_id()).isNull();
    }

    @Test
    void getDeviceByDeviceId_unknownId_returnsNull() {
        assertThat(deviceRepository.getDeviceByDeviceId("nope")).isNull();
    }
}
```

- [ ] **Step 2: Run.** `mvn -q -pl sclera-cloud-device-asset test -Dtest=DeviceByIdIT`
Expected: PASS. If the ad-hoc `LEFT JOIN ... ON` or an association name fails at startup, check it against `Device.java`/the join entities. If a `bareDevice` row drops out, an association join is accidentally INNER — confirm every `LEFT JOIN`.

- [ ] **Step 3:** Leave in working tree (no commit).

---

## Task 7: Live parity verification (running container)

**Files:** none (manual verification against the running stack).

- [ ] **Step 1: Baseline (BEFORE deploy).** For 3 device ids — (a) with location/floor/building, (b) with onboard status + inventory tracking, (c) a bare device — capture:

```
GET http://localhost:8080/asset/api/v1/sclera-cloud-device-asset-service/docker/{docker}/device/{id}/getdevice?<scope params>
```

Save to `backups/parity/getdevice-{id}-before.json`.

- [ ] **Step 2: Rebuild + redeploy** the `app` container (project's compose rebuild flow); if `app` was recreated, recreate its sidecar: `docker compose up -d --force-recreate --no-deps app-dapr`.

- [ ] **Step 3: Capture AFTER** the same 3 endpoints to `backups/parity/getdevice-{id}-after.json`.

- [ ] **Step 4: Diff** (normalise key order):

Run (Git Bash): `for id in <id1> <id2> <id3>; do diff <(jq -S . backups/parity/getdevice-$id-before.json) <(jq -S . backups/parity/getdevice-$id-after.json) && echo "$id OK" || echo "$id DIFF"; done`
Expected: every id prints `OK`. Any diff is a parity defect — reconcile the field's `@Mapping`/type against the table and re-test.

- [ ] **Step 5: Report** the parity result to the user. Leave all changes in the working tree for their manual commit. Do NOT `git commit`.

---

## Self-Review notes (author)

- **Spec coverage:** build (T1) ✓, projection (T2) ✓, mapper + ignoreByDefault parity lock + 2 conversions (T3) ✓, JPQL + remove named query, keep devicedtomapping (T4) ✓, unit test (T5) ✓, integration parity test (T6) ✓, live byte-identical diff (T7) ✓, in-place signature so ~40 callers untouched (T4) ✓.
- **Field table verified** against `Device.java` SELECT, `devicedtomapping` `@ConstructorResult`, `DeviceDTO` constructor, and the 6 entities. Residual confirmations the compiler/tests catch: exact `d.user` association field name, and onboard-status scalar types (assumed `Integer`).
- **Known type conversions:** `last_seen_on` (BigInteger→String) and `alarm` (Integer→String), both null-safe, matching the native `@ColumnResult(String)` behaviour.
- **No git commits anywhere** per standing project rule.
```
