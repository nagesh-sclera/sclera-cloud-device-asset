# Self-Contained QR Code Feature in `sclera-cloud-device-asset`

- **Date:** 2026-06-22
- **Status:** Design — pending user review
- **Owner:** device-asset team
- **Goal:** Make `sclera-cloud-device-asset` the sole owner of all QR functionality (generation, storage, lookup, validation, scan, lifecycle) and remove every dependency on `sclera-cloud-vdms` / `sclera-integrations` for QR.

---

## 1. Objective & scope

### 1.1 Objective
Today QR is **not** owned by device-asset. The service holds QR *entities* and *DTOs* but delegates all behaviour through four Dapr stub-clients (`QrCodeClient`, `ClientQrCodeClient`, `GlobalQrcodeClient`, `PropertyQrcodeClient`) to `sclera-integrations`, whose `QrNfcStubController` returns empty/zero for everything. QR counts, lookups, and scans are therefore silent no-ops.

This design brings QR **fully in-process**: local JPA repositories + services + controllers backed by the local PostgreSQL `sclera_assets` database, with image generation (ZXing), PDF export (iText), Excel import (POI), and image storage on **AWS S3**.

### 1.2 Scope (confirmed with stakeholder)
**In scope — full feature parity from BOTH references:**
- **Edge-server subsystem** (`sclera-vdms-edge-server`, the read-only reference): `QrCode`, `ClientQrCode`, `GlobalQrcode`, `PropertyQrcode` entities; their repositories, query repositories, services and `GlobalQrcodeContoller`; local ZXing generation; iText PDF export; scan/lookup/lifecycle.
- **Cloud-vdms feature set** (`sclera-cloud-vdms`): `QrCodeTemplate` (CRUD + branded PDF templates), client-QR **Excel import/preview** (POI), **ADC tagging**, **bulk generation** (1–50 000) with ZIP/PDF/TXT export and email delivery, untagged-QR listings, QR-counts-by-vdms.
- **Storage:** AWS S3 for all generated images / exports (with a filesystem fallback for local/test profiles so the build runs without AWS credentials).
- **Dependency removal:** delete the four Dapr clients; rewire `DeviceService` / `LocationService` / `EssentialService`; remove QR routing through `sclera-integrations`.

**Out of scope / explicitly removed (this is the VDMS coupling being eliminated):**
- Cloud-sync code: `APICallService` QR REST sync methods, the scheduled `scheduleQrcodeNFCBarcodeSync` job, multi-tenant `WebClientService.multiTenantSyncApiCall`, and the cloud WebSocket push (`/topic/vdms/{vdmsId}/sync/data`, `socketPropertyServiceValueUpdate`).
- VDMS API calls, VDMS DB access, VDMS event subscriptions, VDMS repositories/entities/services.
- The `qr_code_sync` / `client_qr_code_sync` sync-state machinery (replaced by simple local lifecycle; see §6).

**Rationale for removing sync while claiming "parity":** the sync methods exist *only* to mirror cloud state into an edge replica. Once device-asset *owns* the data, there is nothing to sync from. The active consumers (`DeviceService`, `LocationService`) call only local read/count/lookup methods; the sync call-sites in `EssentialService` are already commented out. Keeping sync would re-introduce exactly the coupling the task forbids.

---

## 2. Current-state findings (repository analysis)

### 2.1 Three repositories
| Repo | Role re: QR |
|---|---|
| `sclera-vdms-edge-server` | Original monolith; **read-only reference** (per `CLAUDE.md`). Complete in-process QR layer: 4 services, 4 repos, 2 query repos, `GlobalQrcodeContoller`, entities, DTOs, ZXing generation, iText PDF, local-filesystem images. `qr_code`/`client_qr_code` are cloud-generated and only synced/read here; `global_qrcode`/`property_qrcode` are generated locally. |
| `sclera-cloud-vdms` | Cloud authority. Full QR generation (ZXing, customizable size), `QrCodeTemplate`, Excel import, ADC tagging, S3, multi-tenant WebSocket sync, bulk async email. Controllers: `QRCodeController`, `ClientQrCodeController`, `QrCodeTemplateController`, `AdcTaggingTouchscreenController`. |
| `sclera-cloud-device-asset` | **Target.** Has QR entities + DTOs + 4 Dapr stub-clients, **no** QR repo/service/controller. Tables auto-created by Hibernate `ddl-auto: update`. |

### 2.2 QR artifacts currently in device-asset
- **Entities:** `models/QrCode.java` (`qr_code`), `models/ClientQrCode.java` (`client_qr_code`), `models/GlobalQrcode.java` (`global_qrcode`). `GlobalQrcode` already has PostgreSQL-adapted `@NamedNativeQuery` (LIMIT/OFFSET, CONCAT_WS).
- **DTOs:** `QrCodeDTO`, `ClientQrCodeDTO`, `GlobalQrcodeDTO`, `PropertyQrcodeDTO`.
- **Query repo:** `queryrepository/ClientQrCodeQueryRepository.java` — already Postgres `ON CONFLICT`.
- **Dapr clients (to delete):** `client/QrCodeClient.java`, `client/ClientQrCodeClient.java`, `client/GlobalQrcodeClient.java`, `client/PropertyQrcodeClient.java` + their tests under `src/test/java/io/sclera/client/`.
- **No** `PropertyQrcode` entity, **no** `QrCodeTemplate` entity, **no** QR repositories/services/controllers, **no** `QrCodeQueryRepository`.

### 2.3 Consumers (the coupling to redirect)
| Consumer | Methods called (active) |
|---|---|
| `DeviceService` | `getQrCodeCountByDeviceId`, `getClientQrCodeCountByDeviceId`, `getDeviceQrcodeCountByDeviceId`, `getQrCodesByDeviceIds`, `getDeviceIdsTaggedTo[Client]QrCode`, `getMaxUpdatedQrCodeTimeStamp`, `maxUpdatedClientQrCodeTimeStamp`, `countByDeviceId` (×2 bean aliases: `qrCodeService`/`qrCodeRepository`, `clientQrCodeService`/`clientQrCodeRepository`) |
| `LocationService` | `getQrCodesByLocationIds`, `getLocationIdsTaggedTo[Client]QrCode`, `deleteGlobalQRCodeByLocationId`, `updatePropertyServiceLocations` |
| `EssentialService` | All QR sync calls — **already commented out** (dead) |

### 2.4 Environment facts
- **Runtime DB:** PostgreSQL 16, database `sclera_assets` (docker-compose `DB_URL=jdbc:postgresql://postgres:5432/sclera_assets`). The MySQL URL in `application.yml` is dead (overridden by env).
- **Schema management:** Hibernate `ddl-auto: update`; **Flyway disabled**. DB init seeds live in `infra/postgres-init/` (`01-schemas.sql`, `20-workorder-db.sql`).
- **Classpath:** ZXing (`com.google.zxing:core`,`:javase`), iText (`com.itextpdf:itextpdf`), PDFBox 3.0.3, POI + poi-ooxml, AWS SDK (`com.amazonaws:aws-java-sdk-s3` and `software.amazon.awssdk:s3`) — all present. **No existing S3 helper code** in device-asset; AWS is currently unused.
- **Image config present:** `sclera.server-qrcode-images-url`, `sclera.server-qrcode-images-absolute-path`, `sclera.global-qrcode-server-url` (via `ResourceUrlConfig`).
- **No QR seed data** in `infra/postgres-init` → greenfield; "migration" = schema/constraint correctness, no cross-DB data copy.

---

## 3. Target architecture

### 3.1 Ownership
```
            ┌──────────────────────────────────────────────┐
            │            sclera-cloud-device-asset          │
            │                                               │
 UI ──REST──►  QR Controllers ─► QR Services ─► QR Repos ─► PostgreSQL (sclera_assets)
            │        │               │                       (qr_code, client_qr_code,
            │        │               ├─ ZXing (generate)      global_qrcode, property_qrcode,
            │        │               ├─ iText (PDF export)    qr_code_template, property_service*)
            │        │               ├─ POI  (Excel import)
            │        │               └─ QrImageStorageService ─► AWS S3 (images / exports)
            └──────────────────────────────────────────────┘
   NO Dapr · NO sclera-integrations · NO sclera-cloud-vdms · NO sync jobs
```

### 3.2 Package layout (follows existing conventions)
- `io.sclera.models` — entities (note: `Repository` package uses a capital R in this codebase).
- `io.sclera.Repository` — Spring Data JPA repositories (`@Repository`).
- `io.sclera.queryrepository` — native-SQL string providers (`@Component`).
- `io.sclera.service` — business logic (`@Service`).
- `io.sclera.controller.admin` — REST controllers.
- `io.sclera.utils` — `QrImageStorageService` + S3 impl, `ResourceUrlConfig` (extend).

### 3.3 Bean-name compatibility
New services are injected into existing consumers under the **same field/bean names** currently used for the Dapr clients (`qrCodeService`, `clientQrCodeService`, `globalQrcodeService`, `propertyQrcodeService`, and the `qrCodeRepository`/`clientQrCodeRepository` aliases in `DeviceService`). This keeps consumer edits to import swaps + autowire-type changes, minimizing blast radius.

### 3.4 Storage abstraction
```
interface QrImageStorageService {
    String store(byte[] bytes, String key, String ext);   // returns public/presigned URL
    byte[] fetch(String key);                              // for PDF export reads
    void delete(String key);
}
```
- `S3QrImageStorageService` (default / `docker`,`qa`,`uat`,`prod` profiles) — AWS SDK; bucket/region/prefix from config; URL via presign or public path.
- `FilesystemQrImageStorageService` (`local`,`test`,`dev` profiles) — writes to `server-qrcode-images-absolute-path`, returns `server-qrcode-images-url + key`. Lets the build/tests run with no AWS creds.
- Selected via Spring `@Profile` / `@ConditionalOnProperty`.

---

## 4. Data model & schema

### 4.1 Reconciliation decision (cloud entity FKs → String columns)
The cloud `QrCode`/`ClientQrCode`/`QrCodeTemplate` reference `Vdms` and `Customer_Organisation` JPA entities. **device-asset has neither** (it is an edge service; `vdmsId` is a plain `String` everywhere). Therefore cloud entity-FKs become **lightweight `String` columns** in device-asset: `vdms_id`, `customer_org_id` (nullable String). This matches device-asset's existing model and avoids importing the cloud's org/vdms aggregate.

### 4.2 Tables (all in `sclera_assets`, managed by `ddl-auto: update`)

**`qr_code`** (extend existing entity)
| Column | Type | Notes |
|---|---|---|
| `id` | varchar PK | UUID |
| `image_url` | varchar | S3 URL |
| `qr_code_link` | varchar | encoded payload URL |
| `device_id` | varchar FK → `device(id)` | `@ManyToOne` |
| `location_id` | varchar FK → `location(id)` | `@ManyToOne` |
| `vdms_id` | varchar | String (not FK) |
| `customer_org_id` | varchar | **new** (ADC org context) |
| `batch_id` | varchar | bulk cohort |
| `adc_qr_code_check` | int default 0 | **new** (ADC tagging) |
| `created_by`,`updated_by` | varchar | audit |
| `creation_time` | bigint | audit |
| `updated_time` | varchar | audit (kept as String for parity) |
| `is_deleted` | boolean default false | soft delete |

**`client_qr_code`** (extend existing entity) — as above but with `client_qr_code_id` (varchar, unique business key), `added_at`/`added_by`, `updated_at` (bigint), `adc_client_qr_code_check` (int).

**`global_qrcode`** (existing) — `id` PK, `image_url`, `device_id` (`@OneToOne`, unique), `location_id` (`@OneToOne`, unique). Device XOR location.

**`property_qrcode`** (new) — `id` PK, `image_url`, `property_service_id` FK → `property_service`, `location_id` FK → `location`, `@OneToMany` responses.

**`property_service`, `property_service_request`, `property_service_response`** (new) — the property-service graph required by `PropertyQrcodeService` (service definition, its request templates, and per-location/per-request response values).

**`qr_code_template`** (new) — `id` PK, `name`, `qr_template_json` (text), `qr_code_template_url`, `qr_code_logo_url` (S3), `customer_org_id` (String), `in_use` (int), `is_default` (int), `creation_timestamp`/`updated_timestamp` (bigint), `added_by`/`updated_by`.

**`device`** — add `qrcode_count` (int) if not already present (denormalized count maintained on tag/untag/delete).

### 4.3 Constraints & indexes
- **Unique:** `qr_code.id`, `client_qr_code.client_qr_code_id` (duplicate prevention), `global_qrcode.device_id`, `global_qrcode.location_id`.
- **Indexes:** `qr_code(device_id)`, `qr_code(location_id)`, `qr_code(vdms_id)`, `client_qr_code(device_id)`, `client_qr_code(location_id)`, `property_qrcode(location_id)`, `property_qrcode(property_service_id)`, `qr_code_template(customer_org_id)`.
- **FK integrity:** all QR→`device`/`location` FKs are intra-DB. No references to any VDMS table.
- **Soft delete:** `is_deleted` on `qr_code`/`client_qr_code`; queries filter `is_deleted = false`.

### 4.4 FK safety
No VDMS-owned FK exists today (the coupling is service-invocation, not DB). All new FKs target local `device`/`location`/`property_service`. Greenfield ⇒ no orphan risk. `ddl-auto: update` is additive (never drops). Migration scripts (§7) are `IF NOT EXISTS`-guarded.

---

## 5. Components to build

### 5.1 Repositories (`io.sclera.Repository`)
- `QrCodeRepository extends JpaRepository<QrCode,String>` — `getQrCodesByDeviceIds`, `getQrCodesByLocationIds`, `getQrCodeDetailsByIds`, `getClientQrCodeDetailsByIds` (named native, UNION across both tables), `getQrCodeCountByDeviceId`, `getDeviceIdsTaggedToQrCode`, `getLocationIdsTaggedToQrCode`, `getMaxUpdatedQrCodeTimeStamp`, `countByDeviceId`, plus cloud-parity: `getUnTaggedQrCode`, `checkQrCodeId`, `getAdcCheckByQrCodeId`, `getIsManagedAssetsTagged`, `getQrCodeCountsByVdmsId`, `updateQrcode`/`tagAdcQrCode`/`updateQrCodeById` (modifying).
- `ClientQrCodeRepository` — analogous (`client_qr_code`), incl. `getClientQrCodeId` (dup check), `getUnTaggedClientQrCode`, ADC checks.
- `GlobalQrcodeRepository` — full set from edge inventory (CRUD, `upsertGlobalQrcode`, `getGlobalQrCodeLocation/Device`, `getQrcodeDetail`, `getUntaggedGlobalQrcodes`, `getDeviceQrcodeCountByDeviceId`, `getImageurlByID`, `deleteGlobalQrcodeById`, id-by-location/device lookups).
- `PropertyQrCodeRepository` + `PropertyServiceRepository`, `PropertyServiceRequestRepository`, `PropertyServiceResponseRepository`.
- `QrCodeTemplateRepository` — add/update/delete, `getAllByOrgId`, `getInUseUrlByOrgId`, `getDefaultTemplate`, `updateInUseByUrl`, `getDataByIds`.

### 5.2 Query repositories (`io.sclera.queryrepository`)
- `QrCodeQueryRepository` (new) — Postgres upsert (convert edge MySQL):
  `INSERT INTO qr_code (...) VALUES (...) ON CONFLICT (id) DO UPDATE SET col = EXCLUDED.col, ..., is_deleted = false`.
- `ClientQrCodeQueryRepository` (existing, reuse).

### 5.3 Services (`io.sclera.service`) — replace the 4 Dapr clients
- `QrCodeService` — local read/count/lookup; **+ cloud-parity generation**: `generateQRCode` (single/batch, customizable width/height, ZXing H, → S3), `generateBulkQRCode` (1–50 000 async, ZIP/PDF/TXT → S3, email link), `upsertQrcode`, `getQrCodeDetailsByQrCodeId`, `getQrCodeDetailsByVdmsIdAndDeviceId/LocationId`, `tagQrCodeByVdmsId`, `updateQrcodeById`, `getUnTaggedQrCode`, `getQrCodeCounts`, `getAdcCheckByQrCodeId`. Generation persists records locally — **no cloud sync**.
- `ClientQrCodeService` — local read/count/lookup + `tagClientQrCode`, `importClientQrCode`/`previewExcelSheet` (POI; validate headers `client_qr_code_id,device_id,location_id,vdms_id`; device-XOR-location), `getClientQrCodeDetailsBy*`, `getUnTaggedClientQrCode`, ADC checks.
- `GlobalQrcodeService` — `createGlobalQrcode`, `addGlobalQrcode`/`upsertGlobalQrcode` (ZXing→S3), `getGlobalQrCode` (typed/paged/filtered), `getQrcodeDetail` (scan resolution), `deleteGlobalQrcode` (S3 cleanup + `qrcode_count` update), `exportGlobalQrCodes` (iText PDF, branded), `getDeviceQrcodeCountByDeviceId`. Maintains `device.qrcode_count`.
- `PropertyQrcodeService` — `upsertPropertyServiceDetails`, `addPropertyServiceLocations` (generate QR per location), `generateQrcode`, response CRUD, `getZoneMap`, `getPropertyServiceLocationsById`, deletes with S3 cleanup, `updatePropertyServiceLocations` (location-delete cascade). **WebSocket-to-cloud removed** (local-only; optional local broadcast via existing device-asset websocket infra if needed).
- `QrCodeTemplateService` — template CRUD; uploads template image + logo to S3; org default/in-use management.
- `QrImageStorageService` (+ S3 / filesystem impls) and an `EmailService` hook for bulk-export delivery (reuse existing device-asset mail infra if present; otherwise a thin, profile-gated sender — bulk email link generation must not block generation).

### 5.4 Controllers (`io.sclera.controller.admin`)
- `GlobalQrcodeController` — port `GlobalQrcodeContoller` endpoints (getqrcodes, deleteqrcodes, exportqrcodes [PDF], upsertglobalqrcode, createqrcode, getqrcodedetail [scan]).
- `QrCodeController` — generate / bulk-generate / upsert / lookup-by-id / by-vdms+device / by-vdms+location / untagged / counts / ADC-check (cloud-parity paths).
- `ClientQrCodeController` — tag / lookup / import / preview / untagged / ADC-check.
- `QrCodeTemplateController` — template CRUD + in-use.
- `AdcTaggingController` — `tagQrCodeByVdmsId`, `tagClientQrCodeByVdmsId`.
- Endpoint **paths preserved** from the references for API compatibility; auth/path-prefix follows device-asset's existing `controller/admin` conventions.

### 5.5 Generation details (faithful port)
- ZXing `QRCodeWriter` / `MultiFormatWriter`, `BarcodeFormat.QR_CODE`, `ErrorCorrectionLevel.H`. Global/property: 500×500. Cloud-parity single/bulk: customizable width/height (inches→points for PDF).
- Payloads (from edge):
  - generic: `{global-qrcode-server-url}/qrcode?vdms={vdmsId}&id={id}`
  - location: `…/qrcode?vdms={vdmsId}&type=location&location_id={locationId}`
  - device: `…/qrcode?vdms={vdmsId}&type=device&docker_name={dockerName}&device_id={deviceId}`
  - property: `{services-cloud-server-url}/services?property_qrcode_id={id}` → repointed to a device-asset-owned base URL config.
- PDF via iText (branded layout, logos from `resources/images`). Bulk ZIP/TXT assembled in-memory/temp → uploaded to S3.

---

## 6. Lifecycle, validation, scan

- **Lifecycle states:** Active (record present, `is_deleted=false`), Tagged (device_id or location_id set), Untagged (both null), Soft-deleted (`is_deleted=true`), Regenerated (image re-created, same id, `image_url` replaced), Hard-deleted (global/property on explicit delete + S3 cleanup). The cloud `qr_code_sync` state is **dropped** (no sync).
- **Validation:** duplicate prevention via PK + unique `client_qr_code_id` and `ON CONFLICT`; device-XOR-location rule on tagging and Excel import; Excel header/row validation (POI) with per-row `is_validated`/`validationMessage`; status checks before tag (already-tagged / ADC-managed via `getIsManagedAssetsTagged`, `getAdcCheckBy*`).
- **Scan workflow:** scan endpoint resolves a QR `id` / link / `client_qr_code_id` → `getQrcodeDetail` / `getQrCodeDetailsByQrCodeId` → returns device/location (+ hierarchy: floor/building) for navigation. Pure local DB resolution.

---

## 7. Migration (greenfield, production-safe)

- **Mechanism:** primary schema creation remains Hibernate `ddl-auto: update` (consistent with the rest of the service). In addition, ship an **idempotent Postgres DDL script** in `infra/postgres-init/` (e.g. `30-qr-schema.sql`) so fresh environments and rolling deploys have deterministic constraints/indexes that `ddl-auto` does not create:
  - `CREATE TABLE IF NOT EXISTS` for `property_qrcode`, `property_service*`, `qr_code_template`.
  - `ALTER TABLE … ADD COLUMN IF NOT EXISTS` for `qr_code.customer_org_id`, `qr_code.adc_qr_code_check`, `client_qr_code.adc_client_qr_code_check`, `device.qrcode_count`.
  - `CREATE UNIQUE INDEX IF NOT EXISTS` / `CREATE INDEX IF NOT EXISTS` per §4.3.
- **SQL dialect conversion:** every ported query changes MySQL→Postgres: `ON DUPLICATE KEY UPDATE … VALUES(col)` → `ON CONFLICT (id) DO UPDATE SET col = EXCLUDED.col`; `INSERT … VALUE (…)` → `VALUES (…)`; `IFNULL` → `COALESCE`; backticks removed; `LIMIT ?, ?` → `LIMIT ? OFFSET ?`.
- **No data copy:** greenfield. If production VDMS QR data must be imported later, a separate one-off backfill (source dump → `ON CONFLICT` upsert + `qrcode_count` recompute) can be added; not part of this work.
- **Backward compatibility / rolling deploy:** additive only — no column drops/renames, no destructive changes. Old instances keep working (Dapr stubs returned empty anyway); new instances serve real data. Endpoint paths preserved.

---

## 8. Dependency removal plan

**Delete:**
- `client/QrCodeClient.java`, `client/ClientQrCodeClient.java`, `client/GlobalQrcodeClient.java`, `client/PropertyQrcodeClient.java`
- `src/test/java/io/sclera/client/QrCodeClientTest.java`, `ClientQrCodeClientTest.java`, `GlobalQrcodeClientTest.java`, `PropertyQrcodeClientTest.java`

**Modify:**
- `service/DeviceService.java` — swap autowired types from `client.*Client` to `service.*Service`; keep field names; verify the 11 call-sites compile against new signatures.
- `service/LocationService.java` — same for `qrCodeService`, `clientQrCodeService`, `globalQrcodeService`, `propertyQrcodeService` (≈ 20 call-sites).
- `service/EssentialService.java` — leave the sync block deleted/commented (no reactivation).
- `scheduler/DeviceAssetJobHandlers.java` — `qrcodeNfcBarcodeSync` stays a no-op or is removed (no sync).
- `utils/ResourceUrlConfig.java` — add accessors for QR image base/abs path + S3 settings as needed.
- `pom.xml` — confirm ZXing/iText/POI/AWS already present (they are); add nothing unless a transitive gap appears.
- `application*.yml` — add S3 (bucket/region/prefix/credentials source) and any new base-URL keys; document env injection.
- `migration-notes/` — record each decision (per `CLAUDE.md` audit-trail rule).

**Create:** entities (`PropertyQrcode`, `PropertyService*`, `QrCodeTemplate`), repositories, `QrCodeQueryRepository`, services, controllers, `QrImageStorageService` (+ impls) per §5.

**Net result:** zero QR references to `sclera-integrations` / Dapr / VDMS. `sclera-integrations` `QrNfcStubController` QR routes become dead (no caller) — left as-is (not our module) or noted for cleanup.

---

## 9. Testing & validation checklist

- [ ] **Build:** module compiles; Docker-free unit tests pass (JBR 21 `JAVA_HOME`, `mvnw`; Testcontainers ITs skipped per build-test env note).
- [ ] **Generation:** `generateQRCode`, `createGlobalQrcode`, property-QR generation produce valid scannable PNGs; image stored (S3 in prod profile, filesystem in test) and `image_url`/`qr_code_link` persisted.
- [ ] **Bulk:** bulk generation produces ZIP/PDF/TXT; export uploaded; email-link path exercised (mock).
- [ ] **Lookup:** by QR value/id, by device, by location, untagged lists, counts — return correct local rows.
- [ ] **Scan:** scan endpoint resolves id/link → correct device/location + hierarchy.
- [ ] **Validation:** duplicate id/`client_qr_code_id` rejected/upserted; device-XOR-location enforced; Excel preview/import validation works; ADC/managed checks correct.
- [ ] **Lifecycle:** tag, untag, soft-delete, regenerate, hard-delete (with S3 cleanup) all behave; `device.qrcode_count` stays accurate.
- [ ] **Consumers:** `DeviceService`/`LocationService` now get real counts/lookups (no longer 0/empty).
- [ ] **No VDMS dependency:** grep shows no `QrCodeClient`/`ClientQrCodeClient`/`GlobalQrcodeClient`/`PropertyQrcodeClient`, no `dapr.invokeMethod(... "sclera-integrations" ... qrCode ...)`, no QR `APICallService`/sync/scheduled-sync/cloud-WebSocket.
- [ ] **No FK violations:** schema applies cleanly; FKs resolve within `sclera_assets`.
- [ ] **Data intact:** existing rows unaffected (additive schema).

---

## 10. Risks & open items

1. **S3 credentials/bucket** are not configured in the seed. The filesystem-fallback profile keeps local/test green; prod requires real S3 config (env). Documented, not blocking.
2. **Email infra** for bulk delivery may not exist in the seed — bulk generation must degrade gracefully (generate + store + return link even if email send is stubbed).
3. **Scope size** — this is large (5 services, ~6 repos, 6+ entities, 5 controllers, S3, iText, POI). The implementation plan will sequence it: data layer → storage → services → generation/export → controllers → consumer rewire → cleanup → tests.
4. **`property_service*` graph** must be ported carefully from the edge reference (request/response cardinality) to keep `getZoneMap` and response CRUD correct.
5. **API path parity** — controllers reuse reference paths; if device-asset has a global path prefix/security filter, paths are adapted consistently and documented.

---

## 11. Deliverables mapping (task → this design)
- Repository findings → §2; Dependency analysis → §2.3, §8; Migration design → §3, §4; DB impact → §4, §7; Code changes (create/modify/delete) → §5, §8; Migration scripts → §7; Validation checklist → §9.
