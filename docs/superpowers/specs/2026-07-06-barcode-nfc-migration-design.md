# Barcode & NFC Migration into `sclera-cloud-device-asset` — Design

Date: 2026-07-06
Branch: `sclera2.0/demo`

## Goal
Replicate, for **Barcode** and **NFC**, exactly what was done for **QR / Client-QR**:
port the feature from `sclera-cloud-vdms` into the self-contained `sclera-cloud-device-asset`
microservice (Postgres), and expose it in the `sclera-ui` React frontend on the asset
detail panel — matching the existing "QR Code" tab pattern.

## Guiding principle: "exactly like the QR merge"
The QR merge kept **only the DB paths** and dropped the cloud machinery. Confirmed by
audit: `QrCodeService` / `ClientQrCodeService` in device-asset contain **no**:
- WebSocket / STOMP push (`socketUtils.invokeWebSocketEndpoint`)
- multi-tenant / cross-region HTTP sync (`webClientService.multiTenantSyncApiCall`)
- proxy-profile / geofence auth (`getClientBarCodeProxyProfileByVdmsId`)
- `scleraRoleCheckUtils` role checks
- Excel import / preview

Barcode & NFC get the same treatment: **omit** all of the above (not stub — omit, as QR did).
Persistence is Postgres with `INSERT ... ON CONFLICT` upserts and `@SqlResultSetMapping`
projection into DTOs (no MapStruct for these reads — matches QR).

## Shape mapping
- **Barcode** = `client_bar_code` table only (legacy has no generated `bar_code` table) →
  mirrors the **Client-QR** half (`ClientQrCode*`).
- **NFC** = `nfc` (generated) + `client_nfc` (client) → mirrors the **QR + Client-QR** pair.
  Read endpoints merge `nfc` + `client_nfc` into one `NfcDTO` list, as legacy does.

## Backend files (package `io.sclera`)
Barcode (clone of `ClientQrCode*`):
- `models/ClientBarCode.java` — `@Entity` table `client_bar_code`; `@SqlResultSetMapping` +
  `@NamedNativeQuery` for the reads used.
- `dto/ClientBarCodeDTO.java`
- `Repository/ClientBarCodeRepository.java` — REPLACES the existing stub.
- `service/ClientBarCodeService.java` — trimmed: `tagClientBarCode` (insert-if-new / update /
  null-out=untag), `getClientBarCodeDetailsByVdmsIdAndDeviceId`, `getUnTaggedClientBarCode`,
  `getAdcCheckByClientBarCodeId` (exists), `getClientBarCodeDetailsByClientBarCodeId`.
- `controller/admin/ClientBarCodeController.java` — endpoints mirroring `ClientQrCodeController`.

NFC (clone of `QrCode*` + `ClientQrCode*`):
- `models/Nfc.java` (`@Table("nfc")`), `models/ClientNfc.java` (`@Table("client_nfc")`)
- `dto/NfcDTO.java`, `dto/ClientNfcDTO.java`
- `Repository/NfcRepository.java`, `Repository/ClientNfcRepository.java`
- `service/NfcService.java`, `service/ClientNfcService.java` (trimmed)
- `controller/admin/NfcController.java`, `controller/admin/ClientNfcController.java`

Legacy bugs fixed on the way in (not ported broken):
- `getUnTaggedClientNfc` malformed SQL (`WHERE  AND ...`).
- `NFC.getNfcDetailsById` mapping selecting a column the DTO mapping lacks.

## Endpoint set (per feature, under base `/api/v1/sclera-cloud-device-asset-service`)
Mirrors QR:
- `GET  /vdms/{vdmsId}/deviceId/{deviceId}/get<Feature>DetailsByVdmsIdAndDeviceId` — list tagged to device
- `POST /<feature>` (or `/updateNfc`) — tag; `deviceId:null` untags
- `GET  /vdms/{vdmsId}/getUnTagged<Feature>` — untagged pool for the dropdown
- `GET  /<feature>/{id}/get<Feature>CheckById` — exists/adc check

## Frontend files (`sclera-ui/src`)
- `services/api.js` — barcode + NFC function sets mirroring the QR functions
  (`barcodesForDevice`, `untaggedBarcodes`, `tagBarcodeToDevice`, `untagBarcode`,
  `barcodeExistsInDb`; same for NFC).
- `components/Icon.jsx` — add an `nfc` glyph (none exists).
- `components/DeviceDetailPanel.jsx` — add `'Barcode'` + `'NFC'` to `SUBTABS`; parallel state
  (`barcodeTags/barcodeUntagged/barcodeBusy`, `nfcTags/...`); `load*/tag*/untag*` handlers;
  lazy-load `useEffect`s; render `BarcodeTagView` / `NfcTagView`; wire the inert
  "Tag Bar Code" header button to `setSub('Barcode')`.
- `components/BarcodeScanModal.jsx` — clone of `QrScanModal` (html5-qrcode decodes 1D barcodes;
  set `formatsToSupport`). Reuses the double-camera-safe cleanup fix.
- NFC scan — Web NFC (`NDEFReader`) where the browser supports it (Chrome/Android over HTTPS),
  otherwise manual/paste entry. Same `{onResult, onClose, title}` contract.

## Non-goals (parity dropped, matching QR merge)
Cloud sync, WebSocket broadcast, multi-tenant, proxy/geofence auth, role checks, Excel
import/preview, touchscreen/BFF/mobile ADC controllers. These can be a later phase if needed.

## Verification
- Backend: `mvnw compile` (JBR 21) in `sclera-cloud-device-asset` after each slice.
- Frontend: `vite build` after wiring; live test on the running dev server (:3000).
- End-to-end tag/untag/list against Postgres `sclera_assets`.
