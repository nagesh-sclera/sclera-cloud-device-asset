# Phase 3 — Original Monolith Entity Definitions

> Monolith repo: `C:/Users/DhanushVasanth/Desktop/AssetManagement POD/sclera-vdms-edge-server`
> Extracted service: `C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice123/sclera-cloud-device-asset`
> Surveyed: 2026-05-26

---

## 1. `Conditions` (table: `conditions`)

### Monolith path
`src/main/java/io/sclera/models/Conditions.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `id` | `String` | `@Id` |
| `name` | `String` | `@Column(length=128)` |
| `value` | `String` | `@Column(length=128)` |
| `second_value` | `String` | `@Column(length=128)` |
| `alert_message` | `String` | (none) |
| `start_time` | `String` | `@Column(length=64)` |
| `end_time` | `String` | `@Column(length=64)` |
| `alert_condition` | `String` | `@Column(length=64)` |
| `alert` | `Boolean` | `@Column(columnDefinition="boolean default false")` |
| `show_alert` | `Boolean` | `@Column(columnDefinition="boolean default false")` |
| `show_alert_message_as_value` | `Boolean` | `@Column(columnDefinition="boolean default false")` |
| `schedule` | `Integer` | `@Column(length=8, columnDefinition="integer default 0")` |
| `schedule_conditions` | `String` | (none) |
| `max_alert_count` | `Integer` | `@Column(columnDefinition="integer default 0")` |
| `alert_count` | `Integer` | `@Column(columnDefinition="integer default 0")` |
| `alert_count_enabled` | `Integer` | `@Column(columnDefinition="integer default 0")` |
| `last_alerted_timestamp` | `BigInteger` | (none) |
| `alert_time` | `Integer` | (none) |
| `priority` | `String` | `@Column(length=128)` |
| `last_alerted` | `Boolean` | `@Column(columnDefinition="boolean default false")` |
| `alert_count_time` | `Integer` | `@Column` |
| `enable_threshold_line_onchart` | `Integer` | `@Column` |
| `color_of_threshold_line_onchart` | `String` | `@Column` |
| `daintree_device_id` | `String` | (none — plain FK string, not a relation) |

### Original relationships (`@ManyToOne`)

| Field | Target entity |
|---|---|
| `bacnet_object` | `Bacnet_Object` |
| `lorawan_sensor_attributes` | `Lorawan_Sensor_Attributes` |
| `snmp_device` | `Snmp_Device` |
| `disruptive_sensor` | `DisruptiveSensor` |
| `my_devices_sensor_attributes` | `MyDevicesSensorAttributes` |
| `monnit_sensor` | `Monnit_Sensor` |
| `pelican_sensor_attributes` | `PelicanSensorAttributes` |
| `knx_group` | `KNXGroup` |
| `snmp_object` | `SnmpObject` |
| `measuring_instrument` | `MeasuringInstrument` |
| `daintree_point` | `DaintreePoint` |
| `alert_profile` | `AlertProfile` |
| `ecobee_sensor_attributes` | `EcobeeSensorAttributes` |
| `modbus_register` | `ModbusRegister` |

**Total columns: 24 + 14 FK relations = 38 mapped elements**

### Current extracted service state

`sclera-cloud-device-asset/src/main/java/io/sclera/models/Conditions.java` is a stub with:
- `@Id Long id` (wrong type: original is `String`)
- `@ManyToOne Bacnet_Object bacnet_object` (only 1 of 14 relations)
- All 23 scalar columns are missing

### Trimmed columns/relations

**Trimmed scalar columns (23):**
`name`, `value`, `second_value`, `alert_message`, `start_time`, `end_time`, `alert_condition`, `alert`, `show_alert`, `show_alert_message_as_value`, `schedule`, `schedule_conditions`, `max_alert_count`, `alert_count`, `alert_count_enabled`, `last_alerted_timestamp`, `alert_time`, `priority`, `last_alerted`, `alert_count_time`, `enable_threshold_line_onchart`, `color_of_threshold_line_onchart`, `daintree_device_id`

**Trimmed relations (13 of 14):**
`lorawan_sensor_attributes`, `snmp_device`, `disruptive_sensor`, `my_devices_sensor_attributes`, `monnit_sensor`, `pelican_sensor_attributes`, `knx_group`, `snmp_object`, `measuring_instrument`, `daintree_point`, `alert_profile`, `ecobee_sensor_attributes`, `modbus_register`

**Also: `id` type is wrong** — monolith uses `String`, stub uses `Long`.

> **Restore cost note:** Scalar columns are all plain primitives/Strings — trivial to add. Restoring all 14 FK relations pulls in 13 Bucket-C entities (`Snmp_Device`, `DisruptiveSensor`, `MyDevicesSensorAttributes`, `Monnit_Sensor`, `PelicanSensorAttributes`, `KNXGroup`, `SnmpObject`, `MeasuringInstrument`, `DaintreePoint`, `AlertProfile`, `EcobeeSensorAttributes`, `ModbusRegister`, `Lorawan_Sensor_Attributes`). These can be stubbed as `@Entity` shells with only `@Id`. The `alert_profile` FK specifically requires the `AlertProfile` table entity (see #4 below).

---

## 2. `Product_Details` (table: `product_details`)

### Monolith path
`src/main/java/io/sclera/models/Product_Details.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `product_id` | `String` | `@Id` |
| `image_url_1` | `String` | (none) |
| `image_url_2` | `String` | (none) |
| `image_url_3` | `String` | (none) |
| `global_image_url_1` | `String` | (none) |
| `global_image_url_2` | `String` | (none) |
| `global_image_url_3` | `String` | (none) |
| `end_of_life` | `Boolean` | `@Column(length=1)` |

### Original relationships

| Field | Target entity | Type |
|---|---|---|
| `devices` | `Device` | `@OneToMany(mappedBy="product_details")` |

### Current extracted service state

`sclera-cloud-device-asset/src/main/java/io/sclera/models/Product_Details.java` is a stub with:
- `@Id Long id` (wrong column name and type: original PK is `String product_id`)
- All 7 scalar columns are missing
- `@OneToMany devices` relation is missing
- Comment says: `/** STUB: non-AP-C1 entity (no @Entity to keep out of schema) */` but `@Entity` is actually present

### Trimmed columns

`image_url_1`, `image_url_2`, `image_url_3`, `global_image_url_1`, `global_image_url_2`, `global_image_url_3`, `end_of_life`

**Also: PK field is wrong** — original is `String product_id`, stub uses `Long id`.

### Trimmed relations

`@OneToMany Set<Device> devices`

> **Restore cost note:** Image columns are all plain `String`; `end_of_life` is a `Boolean` — all trivial to restore. `@OneToMany devices` just references `Device` which already exists in the extracted service. PK rename from `id` → `product_id` is required. Low overall restore cost.

---

## 3. `User` (table: `user`)

### Monolith path
`src/main/java/io/sclera/models/User.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `email` | `String` | `@Id @Column(length=255)` |
| `company_name` | `String` | `@Column(length=255)` |
| `creation_timestamp` | `BigInteger` | (none) |
| `name` | `String` | `@Column(length=255)` |
| `phone` | `String` | `@Column(length=255)` |
| `phone_type` | `String` | `@Column(length=255)` |
| `value` | `String` | `@Column(length=255)` |
| `website` | `String` | `@Column(length=255)` |
| `created_by` | `String` | `@Column(length=255)` |
| `image_url` | `String` | `@Column(columnDefinition="TEXT")` |
| `language` | `String` | `@Column(columnDefinition="varchar(16) default 'EN'")` |
| `role` | `String` | `@Column(length=255)` |

### Original relationships

| Field | Target entity | Type |
|---|---|---|
| `address` | `Address` | `@OneToOne(cascade=ALL)` |
| `customer_org` | `Customer_Organisation` | `@ManyToOne` — FK column is `customer_org_id` |
| `profile_user` | `ProfileUser` | `@OneToMany(mappedBy="user", cascade=ALL)` |
| `user_settings` | `UserSettings` | `@OneToOne(mappedBy="user", cascade=ALL)` |

> **Confirmation:** `customer_org_id` IS present in the original entity as the FK column backing `@ManyToOne Customer_Organisation customer_org`. Queries alias it as `u.customer_org_id as organisation_id`.

### Current extracted service state

`sclera-cloud-device-asset/src/main/java/io/sclera/models/User.java` retains:
- All 12 scalar columns (email, company_name, creation_timestamp, name, phone, phone_type, value, website, created_by, image_url, language, role)
- `@OneToOne Address address` (retained)
- `@OneToMany List<Device> devices` (new addition not in monolith)

Explicitly removed (with comments):
- `@ManyToOne Customer_Organisation customer_org` — FK column `customer_org_id` is not a declared `@Column` field, so it is inaccessible as a typed field; the FK exists only in the DB via the relation
- `@OneToMany Set<ProfileUser> profile_user`
- `@OneToOne UserSettings user_settings`
- `@OneToMany Set<Ticket> ...` (was `AP-C3`)

### Trimmed columns/relations

**No scalar columns were trimmed.** The native queries in the extracted service still reference `u.customer_org_id` (correctly, as it is a DB column from the FK), so the column itself exists in the DB.

**Trimmed relations (3):**
- `@ManyToOne Customer_Organisation customer_org` (effective FK `customer_org_id` in DB)
- `@OneToMany Set<ProfileUser> profile_user`
- `@OneToOne UserSettings user_settings`

> **Restore cost note:** `customer_org_id` FK is still referenced in native queries — restoring the `@ManyToOne` relation requires a stub `Customer_Organisation` entity (see #8 below). `ProfileUser` and `UserSettings` are Bucket-C entities not needed by the extracted service's queries. Cost: low if stubbing Customer_Organisation; medium if full fidelity needed.

---

## 4. `alert_profile` (entity: `AlertProfile`)

### Monolith path
`src/main/java/io/sclera/models/AlertProfile.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `id` | `String` | `@Id` |
| `name` | `String` | `@Column(length=128)` |
| `description` | `String` | `@Column(columnDefinition="LONGTEXT")` |
| `sms_alert` | `Integer` | `@Column(columnDefinition="integer default 0")` |
| `email_alert` | `Integer` | `@Column(columnDefinition="integer default 0")` |
| `ioc` | `Integer` | `@Column(columnDefinition="integer default 0")` |
| `ioc_popup_notification` | `Integer` | `@Column(columnDefinition="integer default 0")` |

### Original relationships

| Field | Target entity | Type |
|---|---|---|
| `profile` | `Profile` | `@ManyToOne` — FK: `profile_id` |
| `workorder_template` | `WorkorderTemplate` | `@ManyToOne` — FK: `workorder_template_id` |
| `conditions` | `Conditions` | `@OneToMany(mappedBy="alert_profile", cascade=ALL)` |
| `global_checklist_conditions` | `GlobalChecklistConditions` | `@OneToMany(mappedBy="alert_profile", cascade=ALL)` |
| `device_conditions` | `DeviceConditions` | `@OneToMany(mappedBy="alert_profile", cascade=ALL)` |
| `report_conditions` | `ReportConditions` | `@OneToMany(mappedBy="alert_profile", cascade=ALL)` |
| `siemens_asset_conditions` | `SiemensAssetConditions` | `@OneToMany(mappedBy="alert_profile", cascade=ALL)` |

### Current extracted service state

No `AlertProfile` entity exists in the extracted service. The table is referenced in:
- `Conditions.getConditionsForAdvanceExcelExport` — `LEFT JOIN alert_profile ap ON ap.id = c.alert_profile_id` selecting `ap.id`, `ap.name`, `ap.ioc`
- The `alert_profile_id` FK column in the `conditions` table

> **Restore cost note:** Scalar columns are 7 plain primitives. Restoring as a read-only stub needs only `id`, `name`, `ioc` to satisfy current queries. Full restore pulls in 2 Bucket-C parent relations (`Profile`, `WorkorderTemplate`) and 5 `@OneToMany` children. A minimal stub with only `@Id String id`, `String name`, `Integer ioc` covers all current query needs. Cost: **low** (stub); **medium** (full).

---

## 5. `vendor` (entity: `Vendor`)

### Monolith path
`src/main/java/io/sclera/models/Vendor.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `email` | `String` | `@Id @Column(length=255)` |
| `name` | `String` | `@Column(length=255)` |
| `phone` | `String` | `@Column(length=255)` |
| `phone_type` | `String` | `@Column(length=255)` |
| `value` | `String` | `@Column(length=256)` |
| `company_name` | `String` | `@Column(length=255)` |
| `website` | `String` | `@Column(length=255)` |
| `address` | `String` | `@Column(length=255)` |
| `city` | `String` | `@Column(length=64)` |
| `country` | `String` | `@Column(length=64)` |
| `state` | `String` | `@Column(length=64)` |
| `zip` | `Integer` | `@Column(length=255)` |
| `street` | `String` | `@Column(length=64)` |
| `role` | `String` | `@Column(length=255)` |
| `image_url` | `String` | `@Column(length=255)` |

### Original relationships

| Field | Target entity | Type |
|---|---|---|
| `vendor_org` | `Vendor_Organisation` | `@ManyToOne` — FK: `vendor_org_id` |

### Current extracted service state

No `Vendor` entity exists in the extracted service. The table is referenced in native queries via JOINs (e.g. in `Device` queries selecting `vendor` as a plain string column, and `Vendor.getVendorByNetworkNameAndVdmsId` which also joins `docker`).

> **Restore cost note:** 15 plain scalar columns; 1 `@ManyToOne` to `Vendor_Organisation` which in turn references `Docker` and `Integration`. A minimal stub with just `@Id String email` + relevant queried columns (`name`, `role`, `vendor_org_id`) is sufficient. `Vendor_Organisation` already exists in the extracted service (`Vendor_Organisation.java` is present). Cost: **low** (stub with queried columns); **low-medium** if full fidelity needed.

---

## 6. `report_attributes` (entity: `ReportAttributes`)

### Monolith path
`src/main/java/io/sclera/models/ReportAttributes.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `id` | `String` | `@Id` |
| `primary_id` | `String` | (none) |
| `secondary_id` | `String` | (none) |
| `protocol` | `String` | (none) |
| `is_deleted` | `Integer` | (none) |

### Original relationships

| Field | Target entity | Type |
|---|---|---|
| `report_template` | `ReportTemplate` | `@ManyToOne` — FK: `report_template_id` |

### Current extracted service state

No `ReportAttributes` entity exists in the extracted service. The table is referenced in:
- `ReportAttributes.getReportAttributesByTemplateId` — selects `ra.id`, `ra.primary_id`, `ra.secondary_id`, `ra.protocol`, `ra.report_template_id` with a LEFT JOIN to `report_template`

> **Restore cost note:** 5 simple scalar columns. The `@ManyToOne ReportTemplate` dependency requires a `ReportTemplate` stub (not currently in extracted service). Restoring both as minimal `@Id`-only stubs is straightforward. Cost: **low**.

---

## 7. `location_global_checklist` (NOT a standalone entity)

### Monolith path
`src/main/java/io/sclera/models/Location.java` (defined as `@ManyToMany` `@JoinTable`)

### Definition

`location_global_checklist` is **not a JPA `@Entity`** in the monolith. It is a pure join table created implicitly by:

```java
// In Location.java:
@ManyToMany()
@JoinTable(
    name = "location_global_checklist",
    joinColumns = @JoinColumn(name = "location_id"),
    inverseJoinColumns = @JoinColumn(name = "global_checklist_id")
)
private Set<GlobalChecklist> global_checklist;
```

### Actual table schema (inferred from queries and annotations)

| Column | Type | Notes |
|---|---|---|
| `location_id` | String/FK | FK → `location.id` |
| `global_checklist_id` | String/FK | FK → `global_checklist.id` |
| `is_removed` | Integer | Soft-delete flag (used in queries: `WHERE is_removed = 0`) |

> **Note:** The `is_removed` column is referenced in queries but is NOT declared in the `@JoinTable` annotation — it was added directly to the DB and manipulated only via native queries in `GlobalChecklistRepository`.

### Current extracted service state

No entity and no `@JoinTable` definition for this table exists in the extracted service. Both `Location` and `GlobalChecklist` entities exist in the extracted service, but the `@ManyToMany` link between them is not declared.

> **Restore cost note:** No new entity needed. Restore by adding `@ManyToMany @JoinTable(name="location_global_checklist", ...)` to the `Location` entity in the extracted service, mirroring the monolith. The `is_removed` column requires native-query handling only (already present in `GlobalChecklistRepository` queries). Cost: **low**.

---

## 8. `customer_organisation` (entity: `Customer_Organisation`)

### Monolith path
`src/main/java/io/sclera/models/Customer_Organisation.java`

### Full original column/field list

| Field | Java type | JPA annotation |
|---|---|---|
| `id` | `String` | `@Id` |
| `status` | `String` | `@Column(length=32)` |

### Original relationships

| Field | Target entity | Type |
|---|---|---|
| `users` | `User` | `@OneToMany(mappedBy="customer_org", cascade=ALL)` |
| `vdms` | `Vdms` | `@OneToOne(cascade=ALL, mappedBy="customer_org")` |
| `integrations` | `Integration` | `@OneToMany(mappedBy="customer_org", cascade=ALL)` |

### Current extracted service state

No `Customer_Organisation` entity exists in the extracted service. The table is referenced in:
- Multiple `User` native queries: `LEFT JOIN customer_organisation co on co.id = u.customer_org_id`
- The `User` entity's removed `@ManyToOne customer_org` relation used `customer_org_id` as FK
- `Conditions.getConditionsForAdvanceExcelExport` does NOT directly join `customer_organisation`, but `AlertProfile.ioc` query indirectly relates

### Columns queried in extracted service

Only `co.id` is used in the native queries (JOIN on identity). No `co.status` or relation columns are selected.

> **Restore cost note:** Only 2 scalar columns (`id`, `status`). As a stub, only `@Id String id` is needed to satisfy current JPA FK references. Restoring `@OneToMany users` re-links `User`, `Vdms`, and `Integration` which all exist in the extracted service. Full restore is low cost; the 3 `@OneToMany` relations are inverses (no extra DB columns). Cost: **low**.

---

## Recommendation Summary

| Entity / Table | Trimmed elements | Restore cost | Notes |
|---|---|---|---|
| `Conditions` | 23 scalar cols + 13 FK relations; `id` type wrong (Long→String) | **Medium** | Scalars trivial; 13 FK stubs needed. `alert_profile` FK needs AlertProfile stub. Fix `id` type to String. |
| `Product_Details` | 7 scalar cols + 1 `@OneToMany`; PK name/type wrong | **Low** | All plain Strings/Boolean; `Device` already exists. Rename PK to `product_id : String`. |
| `User` | 3 relations removed (`customer_org`/`profile_user`/`user_settings`); no scalar cols dropped | **Low** | `customer_org_id` FK still in DB/queries. Add Customer_Organisation stub to restore ManyToOne. ProfileUser/UserSettings are Bucket-C, skip. |
| `alert_profile` | Entire entity missing | **Low (stub)** | Needs `id`, `name`, `ioc` for current queries. Full restore adds Profile+WorkorderTemplate deps. |
| `vendor` | Entire entity missing | **Low** | 15 plain scalar cols; `Vendor_Organisation` already present. Stub with `email`+`name`+`role`. |
| `report_attributes` | Entire entity missing | **Low** | 5 simple cols; needs `ReportTemplate` stub too. |
| `location_global_checklist` | Join table missing from `@ManyToMany` on `Location` | **Low** | Not a standalone entity; add `@JoinTable` to `Location`. Note `is_removed` col is native-query-only. |
| `customer_organisation` | Entire entity missing | **Low** | Only 2 cols; only `id` needed for FK joins. |
