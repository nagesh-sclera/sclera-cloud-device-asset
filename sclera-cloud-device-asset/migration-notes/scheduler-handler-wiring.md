# Scheduler Handler Wiring — Follow-ups

Date: 2026-06-08  
Task: Wire 10 `DeviceAssetJobHandlers` stubs to real service logic.

---

## Summary

| Job | Status | Service.method (if wired) |
|-----|--------|--------------------------|
| `historyRecord` | STUBBED | see below |
| `unlinkVendorOrganisation` | STUBBED | see below |
| `internetBandwidthCheck` | STUBBED | see below |
| `vdmsSystemHealth` | STUBBED | see below |
| `connectedStatusForIOC` | STUBBED | see below |
| `qrcodeNfcBarcodeSync` | STUBBED | see below |
| `syncAssetCountToCloud` | STUBBED | see below |
| `userActionLog` | STUBBED | see below |
| `deviceDndEnable` | **WIRED** | `DeviceService.dndCheckAndUpdate()` |
| `offlineDeviceCheck` | **WIRED** | `DeviceSpecificationService.updateDeviceStatusToOffline()` |

---

## WIRED handlers

### `deviceDndEnable`
- Monolith ref: `DeviceService` lines 2811-2835 (inline logic calling `UpdateDeviceDndEnabledAndTimestamp`, no named scheduler method)
- Wired to: `DeviceService.dndCheckAndUpdate()`
- What it does: iterates DND-enabled devices; if `dnd_timestamp` is more than 24 hours old, calls `updateDeviceDndAndSystemDndStatus(deviceId, false)` to disable DND.

### `offlineDeviceCheck`
- Monolith ref: stale-device sweep (spec-timestamp based)
- Wired to: `DeviceSpecificationService.updateDeviceStatusToOffline()`
- What it does: fetches all online devices (status == 1), compares each device's `DeviceSpecification.updatedAt` against now; if inactive for more than 90 seconds, sets status to 0 (offline) and records `last_seen_on`.

---

## STUBBED handlers — follow-up required

### `historyRecord`
- Monolith ref: `SchedularService.scheduleHistoryRecord()` (lines 135-153)
- Operations needed:
  1. `histroyService.deleteHistoryRecords()` — HistoryService (sclera-audit) is not in this service; `HistoryClient` does not expose a `deleteHistoryRecords()` call.
  2. `userActionLogService.deleteUserActionLogRecords()` — this service's `UserActionLogService` only publishes events; it has no delete-records operation.
  3. `inspectionRecordHistoryService.deleteInspectionRecordHistoryRecords()` — InspectionRecordHistoryService is absent from this service.
- Follow-up: Add a `deleteHistoryRecords` method to `HistoryClient` pointing at sclera-audit, and add `deleteUserActionLogRecords` to this service's `UserActionLogService`.

### `unlinkVendorOrganisation`
- Monolith ref: `SchedularService.scheduleUnlinkVendorOrganisation()` → `VendorOrganisationService.deleteUnlinkedVendorOrganisations()`
- Missing: `VendorOrganisationService` was not extracted into this service. No equivalent exists here.
- Follow-up: Determine ownership — if vendor-org data lives in sclera-cloud-device-asset's DB, extract `VendorOrganisationService` + repository; otherwise route to a remote service.

### `internetBandwidthCheck`
- Monolith ref: `SchedularService.scheduleInternetBandwidthCheck()` → `dockerService.getDockerInternetRequired()` + `dockerService.getNetworkSpeed(docker)`
- Missing: This service's `DockerService` is itself a stub (`STUB: replace with remote call to edge-D`). Neither `getDockerInternetRequired()` nor `getNetworkSpeed()` are present.
- Follow-up: Wire once DockerService is backed by the real edge-D integration or a Dapr remote call; add both methods to the service/client.

### `vdmsSystemHealth`
- Monolith ref: `Schedular.scheduleVdmsSystemHealth()` which calls:
  1. `schedularService.scheduleLorawanDownlink()` — iterates LorawanService sensors and pushes chirpstack downlinks
  2. `schedularService.scheduleVdmsSystemHealth()` → `vdmsService.AddSystemHealthAsResponse()`
  3. `schedularService.scheduleVDMSDataForIOCSync()` → `vdmsService.sendVDMSData()`
- Missing: LorawanService, and `VdmsService.AddSystemHealthAsResponse()` / `VdmsService.sendVDMSData()` — none of these are in this service.
- Follow-up: If VdmsService is partially present, add the missing methods; LorawanService likely belongs to a separate integration service.

### `connectedStatusForIOC`
- Monolith ref: `Schedular.scheduleConnectedStatusForIOC()` which calls:
  1. `scheduleConnectedStatusForIOC()` → `scheduleConnectedStatusAllIntegrationsForIOC()` → DaintreeService, SiemensService, PortUtilityService, IOCService.sendAllOnlineConnectionData()
  2. `scheduleUserActivityData(BigInteger)` → `utils.generateTimestamps()` + `rabbitmqService.rabbitMqUserActivityData()`
- Missing: IOCService, DaintreeService, SiemensService, PortUtilityService, RabbitmqService, Utils.generateTimestamps — none present in this service.
- Follow-up: These belong to integration-specific services. Wire the IOC sync portion once an `IOCClient` is available; the user-activity-data push is a RabbitMQ concern and needs its own Dapr binding or topic.

### `qrcodeNfcBarcodeSync`
- Monolith ref: `SchedularService.scheduleQrcodeNFCBarcodeSync()` → `essentialService.syncQrCodeNfcBarCode(vdmsId, new VdmsSyncDTO(2, 2, 2), "on scheduler")`
- Missing: `EssentialService` is entirely commented out in this service. `QrCodeClient` and `NfcClient` do exist (Dapr clients) but there is no `syncQrCodeNfcBarCode` orchestration wrapper.
- Follow-up: Either uncomment/restore `EssentialService.syncQrCodeNfcBarCode()` (with references to `QrCodeClient`, `NfcClient`, `ClientBarCodeService`) or create a thin `QrcodeNfcBarcodeSyncService` that orchestrates the three sync calls directly.

### `syncAssetCountToCloud`
- Monolith ref: `SchedularService.syncAssetCountToCloud()` → `deviceService.getAssetCount()` + `apiCallService.updateVdmsAssetCountCloud(vdmsId, assetCountJSONObject)`
- Partial: `DeviceService.getAssetCount()` **exists** in this service. However, `APICallClient` does not have `updateVdmsAssetCountCloud()`.
- Follow-up: Add `updateVdmsAssetCountCloud(String vdmsId, JSONObject body)` to `APICallClient` (Dapr call to `sclera-edge`), then wire: `deviceService.getAssetCount()` → build JSON → `apiCallClient.updateVdmsAssetCountCloud(vdmsService.getVDMSId(), json)`.

### `userActionLog`
- Monolith ref: The `scheduleHistoryRecord` method calls `userActionLogService.deleteUserActionLogRecords()`, and `scheduleConnectedStatusForIOC` calls `scheduleUserActivityData` which uses `utils.generateTimestamps()` + `rabbitmqService.rabbitMqUserActivityData()` to fetch user activity records and push them.
- Missing:
  - `UserActionLogService.deleteUserActionLogRecords()` — this service's UserActionLogService only publishes events, no repository-backed delete.
  - `UserActionLogService.getUserActivityRecords(BigInteger, BigInteger)` — absent.
  - `RabbitmqService` / `Utils.generateTimestamps()` — absent.
- Follow-up: If user-action-log records are owned by this service's DB, add a repository-backed delete + query; otherwise route to sclera-audit. The activity-push to RabbitMQ needs a Dapr output binding or topic.
