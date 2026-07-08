# NFC self-contained in device-asset — design

**Date:** 2026-07-08
**Status:** approved (design), pre-implementation
**Precedent:** `docs/superpowers/specs/2026-06-22-qr-self-contained-device-asset-design.md`, `migration-notes/qr-self-contained.md`

## Goal

Make NFC fully self-contained inside the `sclera-cloud-device-asset` microservice — exactly like QR and barcode — so it has **no runtime dependency on `sclera-integrations`** (removed from the stack), and so per-device NFC counts and NFC filtering reflect real data. The React UI (`sclera-ui`) already consumes NFC; no UI changes.

## Current state (findings)

- **Local NFC stack already exists** in device-asset and mirrors QR/barcode: `NfcController`/`ClientNfcController` (serving the exact endpoints the UI calls), `NfcService`/`ClientNfcService`, `NfcRepository`/`ClientNfcRepository`/`ClientNfcQueryRepository`, entities `Nfc`/`ClientNfc` (Postgres-shaped: `uuid` + `is_deleted`), DTOs, and tables `nfc`/`client_nfc` (Hibernate DDL). Tag/untag/list already work locally (verified 200 via gateway).
- **Only remaining coupling:** `DeviceService` (fields `nfcService`/`clientNfcService` @ :341/:344; calls @ :5440, :5862, :5954, :6001, :6364, :6452, :6485, :9209) and `LocationService` (fields @ :88/:91; ~11 call sites) still inject the **Dapr stubs** `client/NfcClient` / `client/ClientNfcClient` (`APP_ID="sclera-integrations"`) for: per-device counts (`setNfc_count`), tagged-device-id lookups (NFC search filter), NFC-by-device-ids, and location lookups.
- Those Dapr calls **always returned 0/empty** — `sclera-integrations` only ever had a no-op stub for NFC (real logic lived in an external edge-server never in this repo). Removing the service turned "fast empty" into "slow empty" (the hang fixed earlier via fail-fast resiliency).
- **`nfc`/`client_nfc` tables are empty (0 rows)** — nothing to display until NFC tags exist, and there is no NFC generation endpoint.

## Scope

**In:**
1. Author the local NFC query methods that never existed (per-device counts + tagged/lookup queries).
2. Rewire `DeviceService`/`LocationService` off the Dapr stubs onto the local `NfcService`/`ClientNfcService`.
3. Delete the Dapr stubs (`NfcClient`, `ClientNfcClient`) and their tests.
4. Add an Excel import path for client NFC (bulk-load tags), mirroring the client-QR/barcode import.

**Out:** generated-NFC generation/export/templates; multi-tenant HTTP sync + WebSocket push (dropped, matching the QR migration); UI changes; seed SQL (data comes via import).

## Design

### 1. Local NFC query methods

Add to `NfcService`/`NfcRepository` (generated `nfc`) and `ClientNfcService`/`ClientNfcRepository` (`client_nfc`). All Postgres, `device_id`/`location_id`/`vdms_id`/`is_deleted` columns per the device-asset entities. Return the **same types the Dapr stubs did** so call sites barely change (`Integer`, `Set<NfcDTO>`, `com.alibaba.fastjson.JSONArray`).

| Method (keep stub signature) | Query |
|---|---|
| `getQrNfcCountByDeviceId(deviceId)` → Integer | `SELECT COUNT(*) FROM nfc WHERE device_id=? AND is_deleted=false` |
| `getClientNfcCountByDeviceId(deviceId)` → Integer | `SELECT COUNT(*) FROM client_nfc WHERE device_id=? AND is_deleted=false` |
| `getDeviceIdsTaggedToNfc(vdmsId)` → JSONArray | `SELECT DISTINCT device_id FROM nfc WHERE vdms_id=? AND device_id IS NOT NULL AND is_deleted=false` |
| `getDeviceIdsTaggedToClientNfc(vdmsId)` → JSONArray | same against `client_nfc` |
| `getLocationIdsTaggedToNfc(vdmsId)` → JSONArray | `SELECT DISTINCT location_id FROM nfc WHERE vdms_id=? AND location_id IS NOT NULL AND is_deleted=false` |
| `getLocationIdsTaggedToClientNfc(vdmsId)` → JSONArray | same against `client_nfc` |
| `getNfcsByDeviceIds(Set<String>)` → Set<NfcDTO> | wire existing named query `Nfc.getNfcsByDeviceIds` (UNION ALL over `nfc`+`client_nfc`) |
| `getNfcsByLocationIds(Set<String>)` → Set<NfcDTO> | wire existing named query `Nfc.getNfcsByLocationIds` |

These follow the QR precedent (`GlobalQrcodeService.getDeviceQrcodeCountByDeviceId`) and reuse the named queries already present on the `Nfc` entity.

### 2. Rewire consumers

- In `DeviceService`, change fields `nfcService`/`clientNfcService` from `io.sclera.client.NfcClient`/`ClientNfcClient` to the local `io.sclera.service.NfcService`/`ClientNfcService`. Call sites keep the same method names/return types. `setNfc_count` now sums the two local counts and reflects real tags.
- In `LocationService`, same field-type swap; the `getNfcsByLocationIds` / `getLocationIdsTaggedTo*` call sites are unchanged.
- Guard for null/empty inputs to avoid `IN ()` SQL errors (return empty when the id set is empty), matching the stubs' safe-default behavior.

### 3. Delete the Dapr coupling

Remove `client/NfcClient.java`, `client/ClientNfcClient.java`, and tests `NfcClientTest`, `ClientNfcClientTest`. NFC no longer references `sclera-integrations`. The fail-fast resiliency file stays (other clients — alerts/inspection/edge/etc. — still use `sclera-integrations`); just drop nothing NFC-specific since it targets whole app-ids.

### 4. Excel import (client NFC)

Add to `ClientNfcController`/`ClientNfcService`, mirroring `importClientQrCode`/`importClientBarCode`:
- `POST /importClientNfc` (params `orgId`,`email`,`loggedInUser`; multipart `file`) — Apache POI; columns `nfc_id, device_id, location_id, vdms_id`; per row insert-or-tag into `client_nfc` via the existing `ClientNfcQueryRepository` upsert (`ON CONFLICT (id) DO UPDATE`) / `addClientNFC`/`tagClientNfc`.
- `POST /vdms/{vdmsId}/clientNfc/preview` (multipart `file`) — validate content-type xls/xlsx, header order = `{nfc_id, device_id, location_id, vdms_id}`, forbid mixing device_id+location_id, single consistent `vdms_id` matching the path; return validation messages on `ClientNfcDTO`.
- **Postgres/sync conversions** per `migration-notes/qr-self-contained.md`: MySQL `INSERT … VALUE` → `VALUES`; `ON DUPLICATE KEY` → `ON CONFLICT … EXCLUDED`; `IFNULL`→`COALESCE`; `LIMIT a,b`→`LIMIT b OFFSET a`; drop `Vdms`/`Customer_Organisation` FK objects for plain String columns; **drop the multi-tenant HTTP + WebSocket sync fan-out entirely**. Fix the known-broken vdms `getUnTaggedClientNfc` SQL (`WHERE  AND …`) when porting.
- Ship a small sample spreadsheet (e.g. `sample-nfc-import.xlsx`) with a few rows tagging NFC ids to `right_wing`/`VDMS760` demo devices.

## Data & verification

No seed SQL. To demonstrate end-to-end: import the sample spreadsheet via `POST /importClientNfc` (Swagger or curl through the gateway), then confirm:
1. `client_nfc` rows exist for the demo devices.
2. Device-list `nfc_count` > 0 for those devices.
3. UI NFC sub-tab lists the tags; the NFC filter facet includes those assets.

## Testing

Docker-free unit tests (per the build/test env: JBR 21 `JAVA_HOME`, module `mvnw`):
- New count/lookup query methods (against an in-memory/Testcontainer-free slice or mocked repos).
- Excel import parsing + preview validation (valid file, bad header order, mixed device/location, wrong vdms).
- Adjust/remove `NfcClientTest`/`ClientNfcClientTest`.

## Risks / notes

- **Schema drift:** vdms used `uid` + `nfc_sync_status`/`adc_*`; device-asset uses `uuid` + `is_deleted`. The import must write only device-asset columns. Do not introduce sync columns.
- **`IN (?)` with empty sets** must short-circuit to avoid SQL errors.
- Behavior change: NFC counts/filter go from always-0 to real values — intended, but any consumer that assumed 0 will now see real data.
