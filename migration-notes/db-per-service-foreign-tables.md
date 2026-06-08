# DB-per-Service Foreign-Table Register

> Audit trail for the DB-per-service separation of sclera-cloud-device-asset.
> Update this file as each domain is completed.

---

## Foreign-Table Inventory

| Table | Owner service | Owner app-id | Local entity | Local repo | Scalar FK kept |
|---|---|---|---|---|---|
| `product_details` | sclera-inventory | `sclera-inventory` | `Product_Details` (deleted) | `Product_DetailsRepository` (deleted) | `device.product_id` |
| `customer_organisation` | sclera-identity | `sclera-identity` | `CustomerOrganisation` (TBD) | TBD | `user.customer_org_id` |
| `alert_profile` | sclera-alerts | `sclera-alerts` | `AlertProfile` (TBD) | TBD | `conditions.alert_profile_id` |
| `report_attributes` | sclera-reports | `sclera-reports` | `ReportAttributes` (TBD) | TBD | `report_template_id` |
| `location_global_checklist` | sclera-inspection | `sclera-inspection` | `LocationGlobalChecklist` (TBD) | TBD | TBD |
| `vendor` | sclera-integrations | `sclera-integrations` | `Vendor` (TBD) | TBD | `device.vendor_org_id` |
| sensor-attributes family | sclera-integrations | `sclera-integrations` | none | none | varies |

---

## Discovered Entity/Repo Paths

| Table | Entity file | Repo file |
|---|---|---|
| `product_details` | `models/Product_Details.java` | `Repository/Product_DetailsRepository.java` |
| `customer_organisation` | `models/CustomerOrganisation.java` | (TBD) |
| `alert_profile` | `models/AlertProfile.java` | (TBD) |
| `report_attributes` | `models/ReportAttributes.java` | (TBD) |
| `location_global_checklist` | `models/LocationGlobalChecklist.java` | (TBD) |
| `vendor` | `models/Vendor.java` | (TBD) |
| sensor-attributes | no local entity | no local repo |

---

## Filter/Sort Dependencies (Hard Cases)

> Any `p.<col>` used in WHERE/ORDER BY/GROUP BY — these require special handling.

None found for `product_details`. All `p.image_url_*` and `p.global_image_url_1` usages were in SELECT only.

**Note on `Device.getDeviceAlertInfoById`:** The original query had a split string join `"LEFT JOIN produc" + "t_details p ON d.product_id = p.product_id"` — this used `p.product_id` as the join key (incorrect; the table only has `id`). This was a pre-existing bug. The JOIN was removed cleanly (column `p.global_image_url_1 as image_url` replaced with CAST(NULL AS varchar)).

---

## Writes Converted to Dapr / No-Op

| Method | File | Original action | New action |
|---|---|---|---|
| `deleteProductDetailsById` | `service/Product_DetailsService.java` | delete from product_details + file cleanup | WARN no-op (owner handles deletion) |
| `addProductImages` | `service/Product_DetailsService.java` | already no-op | unchanged no-op |
| `upsertProductDetail` | `service/Product_DetailsService.java` | already no-op | unchanged no-op |
| `checkProductId` | `service/Product_DetailsService.java` | already no-op (returns 0) | unchanged |

---

## Task Status

### product_details -> sclera-inventory: DONE (2026-06-08)

**Client:** `io.sclera.stubs.InventoryClient` + `InventoryClientStub`
**DTO:** `io.sclera.dto.ProductImagesDTO` (record: image_url_1, image_url_2, image_url_3, global_image_url_1)
**Test:** `InventoryClientStubTest` — PASS

**Queries rewritten (Device.java):**

| Query name | p.* columns replaced | JOIN removed |
|---|---|---|
| `Device.listDevicesTs` | `p.image_url_1` → `CAST(NULL AS varchar) AS image_url_1` | YES |
| `Device.listDevicesByPaginationTs` | `p.image_url_1` → `CAST(NULL AS varchar) AS image_url_1` | YES |
| `Device.getDeviceInfoById` | `p.image_url_1`, `p.image_url_2`, `p.image_url_3` → CAST(NULL AS varchar) | YES |
| `Device.listofflinedeviceByParentTs` | `p.image_url_1` → `CAST(NULL AS varchar) AS image_url_1` | YES |
| `Device.listofflinedeviceByParentByPaginationTs` | `p.image_url_1` → `CAST(NULL AS varchar) AS image_url_1` | YES |
| `Device.DeviceInfoById` | `p.image_url_1` → `CAST(NULL AS varchar) AS image_url_1` | YES |
| `Device.getDeviceAlertInfoById` | `p.global_image_url_1 as image_url` → `CAST(NULL AS varchar) AS image_url` | YES |
| `Device.getDeviceConditionAlertInfoById` | `p.global_image_url_1 as image_url` → `CAST(NULL AS varchar) AS image_url` | YES |
| `Device.getDeviceById` | no p.* in SELECT (JOIN was dead) | YES |

**Queries rewritten (MeasuringInstrument.java):**

| Query name | p.* columns replaced | JOIN removed |
|---|---|---|
| `MeasuringInstrument.getMeasuringInstrumentSensorDetailsById` | `p.image_url_1 as device_image_url_1`, `p.global_image_url_1 as device_global_image_url_1` → CAST(NULL AS varchar) | YES |

**Enrichment wiring:**
- `DeviceService.listDevicesTs` and `listDevicesByPaginationTs` — enriched via `enrichDeviceListImages()` private helper
- Helper uses `DeviceRepository.findDeviceProductIdRows(Set<String> ids)` (native query: `SELECT id, product_id FROM device WHERE id IN (:ids)`)
- Maps: device_id → product_id → `InventoryClientStub.getProductImages()` → sets `image_url_1` on `DeviceListDTO`
- Stub returns `Map.of()` (no data) — URLs remain null until sclera-inventory is wired

**Entity/Repo deleted:**
- `models/Product_Details.java` (git rm)
- `Repository/Product_DetailsRepository.java` (git rm)
- `@ManyToOne` + `@JoinColumn(name = "product_id")` removed from `Device.java`

**Hard cases:** None.

---

### customer_organisation -> sclera-identity: TODO
### alert_profile -> sclera-alerts: TODO
### report_attributes -> sclera-reports: TODO
### location_global_checklist -> sclera-inspection: TODO
### vendor -> sclera-integrations: TODO
### sensor-attributes -> sclera-integrations: TODO

---

## Schema Regen Requirement

After all domains are complete, run:
```bash
docker compose down -v
docker compose up -d postgres redis app app-dapr
```
`ddl-auto=update` will regen the schema without the deleted stub tables (Product_Details entity gone → `product_details` table no longer managed by Hibernate but still exists in DB until schema drop).
