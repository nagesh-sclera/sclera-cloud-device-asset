# QR Code — self-contained ownership migration (2026-06-22)

Made `sclera-cloud-device-asset` the sole, in-process owner of QR functionality and removed all QR dependence on `sclera-cloud-vdms` / `sclera-integrations` / Dapr.

Spec: `docs/superpowers/specs/2026-06-22-qr-self-contained-device-asset-design.md`
Plan: `docs/superpowers/plans/2026-06-22-qr-self-contained-device-asset.md`

## What changed

**Removed (the VDMS coupling):**
- Deleted the 4 Dapr stub-clients `client/{QrCodeClient,ClientQrCodeClient,GlobalQrcodeClient,PropertyQrcodeClient}.java` + their tests (they delegated to `sclera-integrations`, which returned empty/zero).
- No cloud sync ported: `APICallService` QR sync, scheduled `qrcodeNfcBarcodeSync`, multi-tenant `WebClientService.multiTenantSyncApiCall`, and the cloud WebSocket push (`socketPropertyServiceValueUpdate`) are intentionally absent. `EssentialService` QR-sync block stays commented; `DeviceAssetJobHandlers.qrcodeNfcBarcodeSync` is a documented no-op.

**Added (local, in-process):**
- Services (replace the clients, same bean names): `service/{QrCodeService,ClientQrCodeService,GlobalQrcodeService,PropertyQrcodeService,QrCodeTemplateService}.java`.
- Repos: `Repository/{QrCodeRepository,ClientQrCodeRepository,GlobalQrcodeRepository,PropertyQrCodeRepository,PropertyServiceRepository,PropertyServiceRequestRepository,PropertyServiceResponseRepository,QrCodeTemplateRepository}.java` + `queryrepository/QrCodeQueryRepository.java`.
- Entities: extended `QrCode` (+`customerOrgId`,`adcQrCodeCheck`), `ClientQrCode` (+`adcClientQrCodeCheck`); new `QrCodeTemplate`, `PropertyQrcode`, `PropertyService`, `PropertyServiceRequest`, `PropertyServiceResponse`; new `QrCodeTemplateDTO`.
- Controllers: `controller/admin/{GlobalQrcodeController,QrCodeController,ClientQrCodeController,QrCodeTemplateController,AdcTaggingController}.java`.
- Storage: `utils/{QrImageStorageService,S3QrImageStorageService,FilesystemQrImageStorageService}.java` + `config/S3Config.java`. QR images → **AWS S3** (filesystem fallback for `local`/`test`/`dev` profiles); ZXing generation (500×500, error-correction H), iText 5 PDF export, POI Excel import.

**Rewired consumers:** `DeviceService`, `LocationService`, `PropertyServiceController` — field types swapped `client.*Client` → `service.*Service` (names unchanged; no call-site logic changes).

## Key decisions
- Cloud `Vdms`/`Customer_Organisation` entity FKs → plain `String` columns (`vdms_id`, `customer_org_id`); device-asset has no such entities.
- SQL converted MySQL→Postgres throughout: `ON DUPLICATE KEY UPDATE … VALUES()` → `ON CONFLICT (id) DO UPDATE SET … EXCLUDED`; `IFNULL`→`COALESCE`; `LIMIT a,b`→`LIMIT b OFFSET a`; backticks removed; `IF()`→`CASE WHEN`.
- Schema: primary creation via Hibernate `ddl-auto: update` (entities). `infra/postgres-init/30-qr-schema.sql` adds the UNIQUE constraints + indexes ddl-auto doesn't — it is **self-guarding** (`to_regclass` DO-block): a no-op at fresh DB init (base tables not yet created), idempotent, and applies the constraints/indexes when re-run after the app has started once. Greenfield — no cross-DB data copy.

## Build status at handoff
- Phase 0–2 (storage/config, entities, repos, services, dependency removal) compiled green; unit tests passed (one Mockito strictness nit fixed).
- Phase 4 (generation/bulk, templates, Excel import, controllers) authored; final `-DskipTests` compile gate run at handoff (see ledger `.superpowers/sdd/progress.md`).

## Cross-verification vs sclera-vdms-edge-server (2026-06-23)

Four parallel parity agents compared each ported service area against the edge-server reference (sync excluded by design).
- **GlobalQrcode**: PARITY OK — repo SQL, 11 named queries, ZXing(500×500/H)/iText PDF, 5 controller endpoints, storage keys all faithful.
- **PropertyQrcode**: PARITY OK — upsert/cascade/response paths, named queries, location-delete cascade (delete row + image by id) all faithful.
- **QrCode / ClientQrCode**: MINOR GAPS — two items, resolved below. Generation/tagging/ADC/template/Excel methods are cloud-vdms-origin (no edge-server counterpart) so not parity-checkable against edge; verified internally consistent instead (DTO ctor↔mapping, dedup, pagination, ADC logic all correct).

Resolutions:
1. **`vdms_id = ?1` filter on getDeviceIdsTaggedTo{Qr,ClientQr}Code / getLocationIdsTaggedTo{Qr,ClientQr}Code — INTENTIONAL divergence, KEPT.** Edge-server declares the `vdmsId` param but omits it from the SQL (returns all rows); this is safe only because edge-server is a **single-VDMS** deployment. device-asset is **multi-tenant** (one DB, many VDMS), so the param is now actually bound (`WHERE vdms_id = ?1 ...`) to prevent cross-tenant device/location ID leakage. Consumers (DeviceService/LocationService) always pass the current vdmsId. Correct adaptation, not a regression.
2. **`upsertClientQrCodesInBatch` null-fallback — FIXED to restore parity.** Re-added the edge-server fallbacks `updated_at ← new BigDecimal(addedAt)` and `updated_by ← addedBy` (was writing NULLs on the import/tag paths, which would break `MAX(updated_at)`).

## Manual test UI (2026-06-23)

Added a self-contained QR test harness for manual end-to-end testing of every QR endpoint.
- Page: `src/main/resources/static/qr-test/index.html` (vanilla HTML/JS, no build step). Covers all 32 endpoints across the 5 QR controllers — JSON, multipart upload, raw-string body, Set/List bodies, binary PDF/zip downloads, and the GlobalQrcode controller's distinct `username`/`vdmsid` casing. Has a "common values" bar (orgId/email/loggedInUser/vdmsId/username/deviceId/locationId) persisted to localStorage.
- Served same-origin at **`https://localhost:8887/qr-test/`** (accept the self-signed cert once). Same-origin ⇒ no CORS; localhost ⇒ no JWT (see WebSecurityConfig.allowAccess loopback rule).
- Wiring: `ResourceConfigs` registers `/qr-test/**` → `classpath:/static/qr-test/` (explicit handler needed because `@EnableWebMvc` disables Boot's default `static/` mapping); `WebSecurityConfig.allowAccess` permits `/qr-test` (docker profile already permits all). Both are clearly commented as developer/QA testing only.

## Runtime bugs found when deploying to the docker stack (2026-06-23)

Surfaced only at Spring context load / live calls (the compile-only gate couldn't catch these):
1. **`QrCodeTemplateRepository.getAllQrCodeTemplateByOrgId` failed to bind** → `No property 'orgId' found for type 'QrCodeTemplate'`, which broke the whole `qrCodeTemplateRepository` bean (500 on every template call). Cause: the entity's `@NamedNativeQuery` was named `QrCodeTemplate.getAllByOrgId` but the repo method is `getAllQrCodeTemplateByOrgId`; Spring Data requires the named query to be `<Entity>.<methodName>` or it falls back to deriving a query from the method name. **Fix:** renamed the `@NamedNativeQuery` to `QrCodeTemplate.getAllQrCodeTemplateByOrgId` (QrCodeTemplate.java). Swept all other QR repos/entities — every other unbound `@Query(nativeQuery=true)` method has a matching named query; this was the only mismatch.
2. **QR image storage 500 under the `docker` profile** → `S3QrImageStorageService.store` threw (no AWS S3 in the local compose stack). **Fix:** moved the `docker` profile from the S3 impl to `FilesystemQrImageStorageService` (writes to `/home/sclera/images/qrcodes`, served at `/images/**`, persisted on the `sclera-images` volume — same as asset images). qa/uat/prod/development keep S3.
3. **`/qr-test/` (trailing slash) → 500** "No static resource" — the resource handler doesn't serve a directory index. Use **`/qr-test/index.html`** (works). The React page below is the primary UI.
4. **QR image path + nested-dir bugs (generateQRCode 500):** (a) `application-docker.yml` stored QR images at `/tmp/sclera/images/qrcodes` but `/images/**` serves from `/home/sclera/images` (ResourceConfigs) and `/tmp` isn't on the `sclera-images` volume → images unservable + lost on recreate. Fixed → `server-qrcode-images-absolute-path: /home/sclera/images/qrcodes/`. (b) `FilesystemQrImageStorageService.store` only created the base dir, but `generateQRCode` stores with a nested key `<batchId>/<qrId>` → `NoSuchFileException`. Fixed → create `target.getParent()`. Verified in the running stack: PDF/ZIP/TXT generate = 200, global create = 200, and a freshly generated image fetches 200 image/png at its `image_url`. (Stale pre-fix images in `/tmp` 500 on fetch — expected.)

## sclera-ui integration — "QR Codes" page (2026-06-23)

Added a **QR Codes** page to the existing Vite+React app (`sclera-ui`), since the standalone static harness isn't reachable through the normal UI flow.
- New: `src/pages/QrCodesPage.jsx` — an interactive console covering all 32 QR endpoints (grouped QR Codes / Client QR / Global QR / Templates / ADC Tagging), with a scope bar (orgId/email/loggedInUser/vdmsId/username/deviceId/locationId) defaulted from `DEMO`. Handles JSON, multipart upload, raw-string body, list/Set bodies, and binary downloads; per-endpoint Send + Copy-URL + inline response panel. Self-contained `fetch` (the app's `api.request()` is JSON-only).
- Calls hit the gateway: `{API_CONFIG.BASE_URL}{ASSET_PREFIX}` = `http://localhost:8080/asset/api/v1/sclera-cloud-device-asset-service/...`. Gateway routes `/asset/**` → `sclera-cloud-device-asset:8085` (StripPrefix=1) and CORS allows :3000 for GET/POST/PUT/DELETE.
- Wiring: `config.js` NAV_ITEMS +`{key:'qrcodes',label:'QR Codes',icon:'qrcode'}`; `Sidebar.jsx` `go()` +`if(key==='qrcodes') setView('qrcodes')`; `App.jsx` import + `{view==='qrcodes' && <QrCodesPage/>}`. `npm run build` passes.
- Run: `cd sclera-ui && npm run dev` → http://localhost:3000 (must be 3000 — the only origin in the gateway CORS allow-list).

## Known follow-ups (see ledger "Minor findings")
- `QrCodeTemplateService` delete should key S3 by template id, not the stored URL.
- `ClientQrCodeService` Excel upsert ON CONFLICT should target `client_qr_code_id` (re-tag generates a new UUID).
- 3 client-QR repo methods stubbed/worked-around (`getUnTaggedClientQrCode`, by-vdms+device/location) — add native queries.
- `QrCodeService.getQrCodeDetailsByQrCodeId` native projection — verify column aliases match DTO getters or add `@SqlResultSetMapping`.
- Add duplicate-prevention UNIQUE constraints via entity `@Table` for reliability under ddl-auto (not just the post-startup script).
- PDF export logos (`jll_logo_latest.png`/`powered_by_update.png`) may be absent from resources → empty logo cells.
