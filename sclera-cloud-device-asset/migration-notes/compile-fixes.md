# Compile-pass log

Running log of compile errors and resolutions during Phase 2.

| Date | Missing symbol | Classification (B/C/D) | Action | Notes |
|---|---|---|---|---|
## Pass 2 - 2026-05-14 (this session)

All 30+ "cannot find symbol" errors resolved. `mvn compile` exits cleanly.

| File | Error | Fix |
|---|---|---|
| FloorService.java:331 | `JsonProcessingException` caught but never thrown | Changed to `catch (Exception e)` |
| PropertyQrcodeService | 10 missing stub methods | Added stubs with StubLog.warn |
| DockerService | `getDockerInterfaceList`, `getVdmsConfigInterfaceList` missing | Added stubs |
| SocketService | `socketDockerInterfaceStatus` missing | Added stub |
| VdmsRepository | `getVDMSId()` missing from interface | Added to interface |
| SettingsService | `getSystemInterfaces()` missing; wrong InterfaceDTO type | Added stub with correct `touchscreen.settings.InterfaceDTO` |
| QrCodeService | 5 missing stub methods | Added stubs |
| ClientQrCodeService | 3 missing stub methods | Added stubs |
| NfcService | `syncAllNfc`, `syncNfc` missing | Added stubs |
| ClientNfcService | `syncAllClientNfc`, `syncClientNfc` missing | Added stubs |
| APICallService | 17 missing stubs; `getJSONArrayFromJSONString` wrong generic `Class<String>` | Added stubs; changed to `<T> List<T>` |
| MonitorService | `deviceUpsertbyId`, `insertDevicesHistory` missing | Added stubs |
| InventoryDeviceSyncDTO | `getEmail()` missing | Added field + getter/setter |
| InventoryDeviceService | `upsertInventoryDevices` missing | Added stub |
| VdmsSyncDTO | `inventory_device_sync` was `Integer`, callers pass `InventoryDeviceSyncDTO` | Changed field type to `InventoryDeviceSyncDTO` |

## Pass 1 - initial compile

Unique missing-symbol error lines: 100

Note: also stripped a UTF-8 BOM from src/main/java/io/sclera/ScleraCloudDeviceAssetApplication.java (artifact of T7 PowerShell Set-Content default encoding) so javac could proceed past the entry-point file and surface the real error landscape.

### Sample (first 20)

```
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/AssetDeviceMapping.java:[3,45] package io.sclera.dto.touchscreen.assetmapper does not exist
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2733,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2788,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2811,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2841,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2845,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2849,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2852,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2856,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2860,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2864,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2868,13] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2871,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2877,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2881,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2884,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2887,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2890,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2896,17] cannot find symbol
[ERROR] /C:/Users/DhanushVasanth/Desktop/AssetManagement POD/Microservice/sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java:[2899,17] cannot find symbol
```

