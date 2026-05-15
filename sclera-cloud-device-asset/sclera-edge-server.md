# Sclera VDMS Edge Server — Full Project Reference

**Version:** v6.25 | **Framework:** Spring Boot 2.6.5 | **Java:** 11 | **Build:** Maven

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Infrastructure & Config](#infrastructure--config)
3. [Domain Map](#domain-map)
4. [Controllers](#controllers)
5. [Services](#services)
6. [Repositories](#repositories)
7. [Models & Entities](#models--entities)
8. [DTOs](#dtos)
9. [Special Packages](#special-packages)

---

## Architecture Overview

```
HTTP/HTTPS Request
      │
      ▼
WebSecurityConfig (OAuth2 JWT, multi-tenant, IP-based bypass)
      │
      ▼
Controller  ──►  Service  ──►  Repository (Spring Data JPA)  ──►  MySQL
                   │
                   ├──► DeviceService.update*Count()  (sync integration counts back to device)
                   ├──► RabbitmqPublisher             (async events)
                   ├──► SocketService / WebSocketClient (real-time push)
                   └──► AsyncService                  (fire-and-forget background work)
```

**Package root:** `io.sclera`

| Package | Purpose | Count |
|---|---|---|
| `controller/` | REST controllers | ~104 |
| `service/` | Business logic | ~150 |
| `Repository/` | Spring Data JPA (capital R) | ~181 |
| `models/` | JPA entities | ~192 |
| `dto/` | Request/response objects | ~263 |
| `config/` | Spring configuration | 8 |
| `auth/` | Multi-tenant JWT | 4 |
| `mcp/` | AI tool endpoints | 1 controller, 1 service, 33 DTOs |
| `integration/` | Reactive/room status integrations | 18 |
| `rabbitmq/` | Message queue | 3 |
| `sockets/` | STOMP WebSocket | 3 |
| `websocket/` | WS client (ADC, P2P) | 11 |
| `startup/` | App init & schedulers | 4 |
| `offline/` | Offline sync & data export | 22 |
| `proxy/` | Reverse proxy | 2 |

---

## Infrastructure & Config

### Ports
| Port | Protocol | Purpose |
|---|---|---|
| 8887 | HTTPS | Primary API |
| 8888 | HTTP | Redirects to 8887; serves static images at `/images/**` → `/home/sclera/images/` |

### External Dependencies (Docker network)
| Service | Host | Port |
|---|---|---|
| MySQL | `scleravdmsdatabase` | 3306 (db: `vdms`) |
| RabbitMQ | `sclerarabbitmq` | 5672 |
| ChirpStack v3 | `localhost` | 8080 |
| ChirpStack v4 | `localhost` | 8090 |
| Analytics | `localhost` | 5123 |

### Spring Config Classes (`io.sclera.config`)

| Class | What it configures |
|---|---|
| `WebSecurityConfig` | OAuth2 Resource Server, multi-tenant JWT (`TenantJWSKeySelector`, `TenantJwtIssuerValidator`). Bypasses auth for `/ws/**`, Swagger, localhost, local IPs, subnets. Security headers: HSTS, CSP, Referrer-Policy |
| `CorsConfig` | Permissive CORS — all origins, methods, headers, credentials enabled |
| `ServerConfig` | Tomcat dual-port: 8887 HTTPS + 8888 HTTP redirect |
| `CacheConfig` | Caffeine cache, 60-min TTL, cache name: `inspection_activity_signout` |
| `SearchConfig` | `JdbcTemplate` bean with LEVENSHTEIN SQL function for fuzzy search |
| `OpenApiConfig` | `GroupedOpenApi` bean scoping Swagger to `/mcp/**` endpoints |
| `ResourceConfigs` | Static resource handler — `/images/**` → `/home/sclera/images/`, no-cache |

### Active Spring Profiles
`application-dev.yml`, `application-development.yml`, `application-qa.yml`, `application-uat.yml`, `application.yml` (default/prod)

### Custom `sclera.*` Properties
```
sclera.vdms-server-url, vendor-server-url, product-server-url
sclera.analytics-server-url
sclera.cloud-server-ip-address / services-cloud-server-url
sclera.p2p-web-socket-url, integration-web-socket-url
sclera.auth-server-app-url          → https://accounts.sclera.com/sclera
sclera.chirpstack-server-url / chirpstack-serverv4-url
sclera.server-*-images-url / absolute-path  (qrcodes, floors, checklist, assets, ocr)
sclera.backup-path
sclera.mri.instruction-query-filter-id
```

---

## Domain Map

| Domain | Controller(s) | Service(s) | Repository | Model |
|---|---|---|---|---|
| **Device** | `DeviceController` | `DeviceService` + 12 sub-services | `DeviceRepository` | `Device` |
| **Asset** | `AssetMapperController`, `AssetOnboardController`, `AssetFieldController` | `AssetMapperService`, `AssetOnboardService`, `AssetFieldService` | `AssetRepository`, `AssetDeviceMappingRepository` | `Asset`, `AssetDeviceMapping`, `AssetField` |
| **MCP (AI Tools)** | `DeviceMcpController` `/mcp/device/**` | `DeviceMcpService` | `DeviceRepository` (mcp* methods) | — |
| **Building / Floor / Location** | `BuildingController`, `FloorController`, `LocationController` | `BuildingService` | `BuildingRepository`, `FloorRepository`, `LocationRepository` | `Building`, `Floor`, `Location` |
| **Alert** | `AlertProfileController`, `AlertDowntimeScheduleController` | `AlertProfileService`, `AlertDowntimeScheduleService` | `AlertProfileRepository`, `AlertDowntimeScheduleRepository` | `AlertProfile`, `AlertDowntimeSchedule` |
| **Checklist** | `CheckListTemplateController`, `CheckListRecordController`, `GlobalChecklistController` | `CheckListTemplateService`, `CheckListRecordService`, `GlobalChecklistConditionsService` | `CheckListTemplateRepository`, `CheckListRecordRepository`, `GlobalChecklistRepository` | `CheckListTemplate`, `CheckListRecord`, `GlobalChecklist` |
| **Inspection** | `GlobalInspectionRecordController`, `InspectionRecordController`, `InspectionRecordHistoryController`, `InspectionReportController` | `GlobalChecklistConditionsService`, `InspectionRecordHistoryService`, `GlobalInspectionRelationConditionsService` | `GlobalInspectionRecordAssigneesRepository`, `InspectionRecordHistoryRepository` | `GlobalInspectionRelation`, `InspectionRecordAssignees` |
| **Ticket / Workorder** | `TicketController`, `TicketHistoryController`, `WorkorderController`, `WorkorderTemplateController`, `WorkorderActionHistoryController` | `TicketService`, `TicketHistoryService`, `WorkorderService` | `TicketRepository`, `WorkorderRepository`, `WorkorderTemplateRepository` | `Ticket`, `Workorder`, `WorkorderTemplate` |
| **SNMP** | `SnmpController`, `SnmpDeviceController`, `SnmpConfigurationController`, `SnmpValuesController` | `SnmpDeviceService`, `SnmpConfigurationService`, `SnmpValuesService` | `SnmpDeviceRepository`, `SnmpConfigurationRepository`, `SnmpObjectRepository` | `SnmpDevice`, `SnmpConfiguration`, `SnmpObject` |
| **BACnet** | `BacnetController` | `BacnetService` | `BacnetDeviceRepository`, `BacnetObjectRepository` | `Bacnet_Device`, `Bacnet_Object`, `Bacnet_Attributes` |
| **LoRaWAN** | `LorawanController`, `LorawanV4Controller` | `LorawanService`, `Lorawanv4Service` | `LorawanSensorRepository`, `LorawanConfigurationRepository` | `Lorawan_Sensor`, `LorawanConfiguration` |
| **Modbus** | `ModbusController` | `ModbusService` | `ModbusDeviceRepository`, `ModbusRegisterRepository` | `ModbusDevice`, `ModbusRegister` |
| **Airthings** | `AirthingController` | `AirthingService` | `AirthingConfigurationRepository`, `AirthingDeviceRepository` | `AirthingConfiguration`, `AirthingDevice`, `AirthingDeviceSensor` |
| **Awair** | `AwairController` | `AwairService` | `AwairConfigurationRepository`, `AwairDeviceRepository` | `AwairConfiguration`, `AwairDevice` |
| **Ecobee** | `EcobeeController` | `EcobeeService` | `EcobeeConfigurationRepository`, `EcobeeSensorRepository` | `EcobeeConfiguration`, `EcobeeSensor` |
| **Pelican** | `PelicanController` | `PelicanService` | `PelicanConfigurationRepository`, `PelicanSensorRepository` | `PelicanConfiguration`, `PelicanSensor` |
| **Monnit** | `MonnitController` | `MonnitService` | `MonnitConfigurationRepository`, `MonnitSensorRepository` | `MonnitConfiguration`, `Monnit_Sensor` |
| **Disruptive** | `DisruptiveController` | `DisruptiveService` | `DisruptiveConfigurationRepository`, `DisruptiveSensorRepository` | `DisruptiveConfiguration`, `DisruptiveSensor` |
| **KNX** | `KNXController` | `KNXService` | `KNXDeviceRepository`, `KNXGroupRepository` | `KNXDevice`, `KNXGroup`, `KNXInterface` |
| **Gaiamesh** | `GaiameshController` | `GaiameshService` | `GaiameshConfigurationRepository`, `GaiameshControllerRepository` | `GaiameshConfiguration`, `GaiameshController` |
| **Daintree** | `DaintreeController` | `DaintreeService` | `DaintreeConfigurationRepository`, `DaintreeDeviceRepository` | `DaintreeConfiguration`, `DaintreeDevice` |
| **MyDevices** | `MyDevicesController` | `MyDevicesService` | `MyDevicesCompanyRepository`, `MyDevicesSensorRepository` | `MyDevicesCompany`, `MyDevicesSensor` |
| **VergeSense** | `VergeSenseController` | `VergeSenseService` | `VergeSenseConfigurationRepository`, `VergeSenseSpaceRepository` | `VergeSenseConfiguration`, `VergeSenseSpace` |
| **PolyLens** | `PolyLensController` | `PolyLensService` | `PolyLensConfigurationRepository` | `PolyLensConfiguration` |
| **MQTT** | `MqttController` | `MqttService` | `MqttConfigurationRepository`, `MqttDeviceRepository` | `MqttConfiguration`, `MqttDevice` |
| **Siemens** | `SiemensController`, `SiemensConditionsController` | `SiemensService` | `SiemensConfigurationRepository`, `SiemensAssetRepository` | `SiemensConfiguration`, `SiemensAsset`, `SiemensAssetAttributes`, `SiemensAssetConditions` |
| **MRI** | `MriController`, `MriTaskController`, `MriTaskHistoryController` | `MriService` | `MriConfigurationRepository`, `MriTaskRepository` | `MriConfiguration`, `MriTask`, `MriTaskHistory` |
| **Corrigo** | (part of AssetOnboard / WorkorderController) | `CorrigoService` | `CorrigoWorkorderHistoryRepository` | `CorrigoWorkorderHistory` |
| **Maximo** | `MaximoController` | `MaximoService` | `MaximoConfigurationRepository` | `MaximoConfiguration` |
| **AI Call Flow** | `AiCallLogController` | `AiCallService` | `CallFlowRuleRepository`, `CallFlowRuleConditionRepository` | `AiCallLog`, `CallFlowRule`, `CallFlowRuleCondition` |
| **ChatGPT** | `ChatGPTController` | `ChatGPTService` | — | — |
| **Technician** | `TechnicianController` | `TechnicianService`, `TechnicianAvailabilityService` | `TechnicianRepository`, `TechnicianCertificateRepository` | `Technician`, `TechnicianCertificate`, `TechnicianSkill` |
| **Remote Access** | `RemoteAccessController` | `RemoteAccessService`, `RemoteDesktopSessionService` | `RemoteAccessRepository`, `RemoteAccessSessionRepository` | `RemoteAccess`, `RemoteAccessSession` |
| **QR / NFC / Barcode** | `GlobalQrcodeContoller` | `QrCodeService`, `ClientQrCodeService`, `ClientNfcService`, `ClientBarCodeService` | `ClientQrCodeRepository`, `ClientNfcRepository`, `GlobalQrcodeRepository` | `ClientQrCode`, `Nfc`, `GlobalQrcode` |
| **Document / Media / Notes** | `DocumentController`, `MediaController`, `NoteController` | `DocumentService`, `MediaService`, `NotesService` | `DocumentRepository`, `MediaRepository`, `NotesRepository` | `Document`, `Media`, `Notes` |
| **Inventory** | `InventoryController` | `InventoryService`, `InventoryDeviceService` | `InventoryRepository`, `InventoryDeviceRepository` | `Inventory`, `InventoryDevice` |
| **Measuring Instrument** | `MeasuringInstrumentController` | `MeasuringInstrumentService` | `MeasuringInstrumentRepository`, `MeasuringInstrumentAttributesRepository` | `MeasuringInstrument`, `MeasuringInstrument_Attributes` |
| **VDMS / System** | `VdmsController`, `SyncController`, `GenericController` | `VdmsService` | `VdmsRepository`, `VdmsDetailsRepository`, `VdmsconfigurationRepository` | `Vdms`, `Vdms_Status`, `Vdms_Configuration`, `Vdms_Details` |
| **User / Profile** | `UserController`, `ProfileController` | `ApplicationUserService` | `ApplicationUserRepository` | `ApplicationUser` |
| **History / Audit** | `HistoryController`, `UserActionLogController` | `HistoryService` | `HistoryRepository`, `UserActionLogRepository` | `History` |
| **Location History** | `LocationHistoryController` | `LocationHistoryService` | `LocationHistoryRepository` | `LocationHistory` |
| **Reports** | `ReportConditionsController` | `ReportConditionsService` | `ReportTemplateRepository`, `ReportAttributesRepository`, `ReportConditionsRepository` | `ReportTemplate`, `ReportAttributes`, `ReportConditions` |
| **Analytics** | `AnalyticsController` | `AnalyticsService` | — | — |
| **Offline Sync** | `OfflineSyncController`, `ConfigController`, `DataMigrationController`, `DataExportController` | `SyncService`, `ConfigService`, `DatabaseMigrationService`, `DataExportService` | `ConfigRepository`, `ImageRepository` | `Config`, `ImageConfig` |
| **Interface / Port / VLAN** | `InterfaceController`, `PortController`, `VlanController` | `InterfaceService`, `PortService`, `VlanService` | `InterfaceRepository`, `PortService`, `VlanRepository` | `Interface`, `Connection`, `Vlan` |
| **Syslog** | `SyslogController` | `SyslogService` | `SyslogRepository` | `Syslog` |
| **IPS** | `IPSController` | `IPSService` | `IPSDeviceRepository`, `IPSScanLevelRepository` | `IPSDevice`, `IPSScanLevel` |
| **PMS / IOC** | `PmsController` | `PmsService`, `IOCService` | `PmsRepository` | `Pms` |
| **Managed Software** | `ManagedSoftwareController` | `ManagedSoftwareService`, `ManagedSoftwareSearchService` | `ManagedSoftwareRepository` | `ManagedSoftware` |
| **Scheduler / Jobs** | `JobSchedulerController` | `SchedularService` | `ScheduledJobRepository` | — |
| **DataHoist** | `DataHoistController` | `DataHoistService` | `DataHoistRepository` | `DataHoist` |
| **Proxy** | `ProxyController` | `ProxyService` | — | — |
| **Touchscreen/Monitor** | `MonitorController`, `VdmsController`, `P2PController`, `NotificationController` | `MonitorService`, `DeviceMonitorService`, `P2PService`, `NotificationService` | `DeviceOnboardStatusRepository` | — |
| **Reactive/Integration** | `ReactiveServiceIntegrationController`, `LocationIntegrationController` | `ReactiveServiceIntegrationService`, `LocationIntegrationService`, `RoomStatusIntegrationService` | — | — |

---

## Controllers

### URL Pattern

Most admin controllers follow:
```
/user/{username}/vdms/{vdmsid}/...
/user/{username}/vdms/{vdms_id}/docker/{dockername}/...
```
No class-level `@RequestMapping` — all paths are on individual methods.

### DeviceController

```
GET    /user/{username}/vdms/{vdmsid}/docker/{dockername}/listdevices
GET    /user/{username}/vdms/{vdmsid}/docker/{dockername}/getfilterdevices
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/editdevice/{device_id}
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/multideviceupdate
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/quickupdate
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/addvirtualdevice
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/editvirtualdevice/{device_id}
DELETE /user/{username}/vdms/{vdmsid}/docker/{dockername}/deletevirtualdevice/{device_id}
DELETE /user/{username}/vdms/{vdmsid}/docker/{dockername}/softdeletedevices
DELETE /user/{username}/vdms/{vdmsid}/docker/{dockername}/deletedevices
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/linkvendor
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/unlinkvendor
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/updatetopology
GET    /user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getsensors
GET    /user/{username}/vdms/{vdmsid}/docker/{dockername}/getdevicenames
... (60+ total endpoints)
```

### AssetMapperController (no base path)

```
POST   /upload
GET    /getAssets
GET    /getSubSystemParentAssets
GET    /asset/{asset_id}/getSubSystemAssets
GET    /getAssetFields
GET    /getAssetColumns
POST   /getOrderedAssetsByColumn
POST   /getDistinctValuesByFieldName
POST   /getAssetsByValue
POST   /getOrderedUnmappedAssetsByColumn
POST   /getDistinctValuesByUnmappedAssetsFieldName
POST   /getUnmappedAssetsByValue
DELETE /deleteAssetsById
GET    /getSubAssetsByParentId/{asset_id}
GET    /startMatching
GET    /getMatchedDevices
POST   /mapAssetsToProducts
POST   /getUnmappedAssets
POST   /getUnmappedSubSystemParentAssets
PUT    /confirmProductMatchingByAssetId
PUT    /updateMapping
POST   /user/{username}/vdms/{vdms_id}/docker/{docker_name}/saveAssetMappings
POST   /user/{username}/vdms/{vdms_id}/docker/{docker_name}/saveAssets
POST   /vdms/{vdms_id}/deletesearchkey
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/asset/{asset_id}/matchassetproducts
GET    /user/{username}/vdms/{vdmsid}/getassetcount
```

### AssetOnboardController

```
POST   /user/{username}/vdms/{vdmsid}/addaionboardassets
POST   /user/{username}/vdms/{vdmsid}/updatecorrigoassets
POST   /user/{username}/vdms/{vdmsid}/upsertonboardassets
POST   /user/{username}/vdms/{vdmsid}/updateassetonboardstatus
POST   /user/{username}/vdms/{vdmsid}/device/{device_id}/updateassetonboarddata
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/getassetonboardcount
GET    /user/{username}/vdms/{vdms_id}/getassetonboardassignees
GET    /user/{username}/vdms/{vdms_id}/getpropertydetails
```

### AssetFieldController
```
GET    /user/{username}/vdms/{vdms_id}/asset-fields
```

### AlertProfileController
```
POST   /user/{username}/vdms/{vdmsid}/upsertalertprofiles
GET    /user/{username}/vdms/{vdmsid}/getalertprofiles
GET    /user/{username}/vdms/{vdmsid}/alert_profile/{alert_profile_id}/getalertprofilebyid
DELETE /user/{username}/vdms/{vdmsid}/deletealertprofiles
```

### AlertDowntimeScheduleController
```
POST   /user/{username}/vdms/{vdmsid}/upsertalertdowntimeschedule
GET    /user/{username}/vdms/{vdmsid}/getalertdowntimeschedule
GET    /user/{username}/vdms/{vdmsid}/getalertdowntimeschedulebyid
DELETE /user/{username}/vdms/{vdmsid}/deletealertdowntimeschedule
```

### BuildingController
```
POST   /user/{username}/vdms/{vdms_id}/upsertbuildings
GET    /user/{username}/vdms/{vdms_id}/building/floor/location/{location_id}/getbuildingbylocation
GET    /user/{username}/vdms/{vdms_id}/getbuildingsbyvdmsid
DELETE /user/{username}/vdms/{vdmsid}/deletebuildings
GET    /syncbuildings
GET    /vdms/{vdms_id}/syncfloormaps
POST   /vdms/{vdms_id}/updatefloormaps
GET    /syncfloormapstiles
```

### CheckListTemplateController
```
POST   /user/{username}/vdms/{vdmsid}/upsertchecklisttemplate
DELETE /user/{username}/vdms/{vdmsid}/checklisttemplate/{checklist_id}/deletechecklisttemplate
GET    /user/{username}/vdms/{vdmsid}/getchecklisttemplate
GET    /user/{username}/vdms/{vdmsid}/device/{device_id}/getchecklisttemplatebydeviceid
POST   /user/{username}/vdms/{vdmsid}/tagchecklisttemplatetodevice
POST   /user/{username}/vdms/{vdmsid}/untagchecklisttemplatetodevice
```

### CheckListRecordController
```
POST   /user/{username}/vdms/{vdmsid}/upsertchecklistrecord
DELETE /user/{username}/vdms/{vdmsid}/deletechecklistrecord
GET    /user/{username}/vdms/{vdmsid}/device/{device_id}/getchecklistrecordbyid
```

### AiCallLogController
```
POST   /user/{username}/vdms/{vdmsid}/deviceId/{deviceId}/createcalllog
GET    /user/{username}/vdms/{vdmsid}/getallcallstatus
GET    /user/{username}/vdms/{vdmsid}/getcallstatuscount
GET    /getdeviceinfo/{deviceId}
POST   /insertcallresponse
POST   /user/{username}/vdms/{vdmsid}/upsertcallflow
GET    /user/{username}/vdms/{vdmsid}/browsedockers
GET    /user/{username}/vdms/{vdmsid}/docker/{dockername}/browsedevices
GET    /user/{username}/vdms/{vdmsid}/getcallflow
DELETE /user/{username}/vdms/{vdmsid}/configuration/deletecallflowbyid
GET    /user/{username}/vdms/{vdmsid}/{deviceid}/{criteria}/{calllogid}/triggercallflow
```

### ChatGPTController
```
POST   /user/{username}/vdms/{vdmsid}/docker/{dockername}/troubleshoot-asset  → SSE stream (ResponseBodyEmitter)
```

### AirthingController
```
POST   /user/{username}/vdms/{vdmsid}/getairthinglocations
POST   /user/{username}/vdms/{vdmsid}/upsertairthingconfiguration
GET    /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/getairthingconfigurationbyid
GET    /user/{username}/vdms/{vdmsid}/getallairthingconfigurations
DELETE /user/{username}/vdms/{vdmsid}/configuration/deleteairthingconfiguration
GET    /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/getairthingdevices
POST   /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/upsertairthingdevice
GET    /user/{username}/vdms/{vdmsid}/device/{device_id}/getairthingdevicebyid
GET    /user/{username}/vdms/{vdmsid}/getallairthingdevicesbypagination
GET    /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/getairthingdevicesbypagination
GET    /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/devices/getairthingdevicebatchdata
GET    /user/{username}/vdms/{vdmsid}/syncairthingdevicesensor
DELETE /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/device/deleteairthingdevicebyid
GET    /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/status/details
GET    /user/{username}/vdms/{vdmsid}/configuration/{configuration_id}/exportairthingslogs
```

### VdmsController (Touchscreen / System)
```
GET    /getdevicefieldslist
POST   /vdms/{vdms_id}/updatevdmslayoutdata
GET    /getvdmslayoutdata
POST   /vdms/{vdms_id}/updatecorrigolayoutdata
POST   /user/{username}/vdms/{vdms_id}/upsertdevicecustomfields
GET    /user/{username}/vdms/{vdms_id}/getdevicecustomfields
GET    /user/{username}/vdms/{vdms_id}/updateconnectionstatus
GET    /vdms/{vdms_id}/getpresignedurl
POST   /vdms/{vdms_id}/cleanupcustomfields
GET    /user/{username}/vdms/{vdms_id}/getscleraagentpermission
POST   /upsertproxydetails
PUT    /vdms/{vdms_id}/updateinsepectionactivitytimeout
GET    /vdms/{vdms_id}/getinspectionactivitytimeout
GET    /getConfiguration
```

### MonitorController (Touchscreen)
```
POST   /docker/{dockername}/deviceupsert
GET    /getvdmsversion
POST   /vdmsversion/{version}/updatevdmsversion
GET    /docker/{dockername}/getdevicelist
GET    /docker/{dockername}/getdevicelistip
GET    /docker/{dockername}/getgatewayip
POST   /docker/{dockername}/insertdeviceshistory
```

### DeviceMcpController  (`/mcp/device/**` — all POST, require `?loggedInUser=`)
```
POST   /mcp/device/summary          → DeviceMcpSummaryResponse
POST   /mcp/device/list             → DeviceMcpListResponse
POST   /mcp/device/detail           → DeviceMcpDetailResponse
POST   /mcp/device/status           → DeviceMcpStatusResponse
POST   /mcp/device/assets           → DeviceMcpAssetsResponse
POST   /mcp/device/onboarding       → DeviceMcpOnboardingResponse
POST   /mcp/device/integrations     → DeviceMcpIntegrationsResponse
POST   /mcp/device/search           → DeviceMcpListResponse
POST   /mcp/device/offline-risk     → DeviceMcpOfflineRiskResponse
POST   /mcp/device/unassigned       → DeviceMcpListResponse
POST   /mcp/device/cost-summary     → DeviceMcpCostSummaryResponse
POST   /mcp/device/network-health   → DeviceMcpNetworkHealthResponse
POST   /mcp/device/alert-config     → DeviceMcpAlertConfigResponse
POST   /mcp/device/ticket-health    → DeviceMcpTicketHealthResponse
POST   /mcp/device/checklist-health → DeviceMcpChecklistHealthResponse
POST   /mcp/device/dnd              → DeviceMcpListResponse
POST   /mcp/device/vendor-breakdown → DeviceMcpVendorBreakdownResponse
POST   /mcp/device/warranty-status  → DeviceMcpWarrantyStatusResponse
POST   /mcp/device/location-gaps    → DeviceMcpLocationGapsResponse
POST   /mcp/device/by-location      → DeviceMcpByLocationResponse
POST   /mcp/device/subsystems       → DeviceMcpListResponse
POST   /mcp/device/recent-activity  → DeviceMcpListResponse
```

### Offline Controllers
```
POST   /offline/config/upsert              (ConfigController)
GET    /offline/config/get
POST   /offline/imageconfig/upsert         (ImageConfigController)
GET    /offline/imageconfig/get
POST   /offline/image/upload               (ImageUploadDownloadController)
GET    /offline/image/download/{id}
POST   /offline/sync                       (OfflineSyncController)
POST   /offline/datamigration/migrate      (DataMigrationController)
POST   /offline/export                     (DataExportController)
```

### ProxyController
```
ALL    /proxy/**   → forwards request to configured remote agent server
```

---

## Services

### DeviceService — Key Method Groups

**Listing**
```java
Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(username, vdmsid, dockername)
Set<DeviceDTO> getfilterdevices(username, vdmsid, dockername, condition, ...)
Set<DeviceDTO> getAllSubsystemDevices(username, vdmsid, dockername, device_id, condition)
Set<DeviceListDTO> listDevicesTs(networkname, buildingid, floorid, locationid, status)
DeviceDetailsDTO getDeviceInfoById(deviceid)
```

**CRUD**
```java
void upsertDeviceListByVdmsIdAndDockerName(List<DeviceDTO>, username, vdmsid, dockername)
DeviceDTO editDeviceByDeviceID(username, vdmsid, dockername, device_id, DeviceDTO, request, assignee)
void multiDeviceUpdate(username, vdmsid, dockername, ...)
Set<DeviceDTO> quickUpdate(username, vdmsid, dockername, TagDeviceOrLocationDTO, request, assignee)
void softDeleteDevicesById / deleteDevicesById(...)
```

**Virtual Devices**
```java
void addVirtualDeviceByVdmsIdAndDockerName(...)
void editVirtualDeviceByVirtualDeviceId(...)
void deleteVirtualDeviceByVirtualDeviceId(...)
DeviceDTO updateVirtualDeviceStatusByVirtualDeviceId(...)
```

**Integration Count Updaters** — called by each protocol's service when sensors are added/removed
```java
void updateDeviceSnmpCount(device_id)        void updateDeviceSnmpStatus(snmp_device_id)
void updateDeviceBacnetCount(device_id)      void updateDeviceBacnetStatus(...)
void updateDeviceLorawanCount(device_id)     void updateDeviceLorawanStatus(...)
void updateDeviceModbusCount(device_id)      void updateDeviceModbusStatus(...)
void updateDeviceDisruptiveCount(device_id)  void updateDeviceDisruptiveStatus(...)
void updateDeviceMonnitCount(device_id)      void updateDeviceMonnitStatus(...)
void updateDevicePelicanCount(device_id)     void updateDevicePelicanStatus(...)
void updateDeviceKNXCount(device_id)         void updateDeviceKNXStatus(...)
void updateDeviceEcobeeCount(device_id)      void updateDeviceEcobeeStatus(...)
void updateDeviceMyDevicesCount(device_id)   void updateDeviceMyDevicesStatus(...)
void updateDeviceDaintreeDevicesCount(...)
void updateDeviceMeasureCountByDeviceId(device_id)
void updateDeviceDocumentsCountByDeviceId(device_id)
void updateDeviceMediaCountByDeviceId(device_id)
void updateDeviceCheckListsCountByDeviceId(device_id)
void updateDeviceTicketCount(device_id)      void updateDeviceTicketStatus(device_id)
void updateDeviceSnmpObjectCountByDeviceId(device_id)
void updateDeviceInterfaceCount(device_id)
void updateDeviceNotesCount(device_id)
```

**Monitoring / SNMP / Topology**
```java
void deviceUpsertbyId(dockername, List<DeviceMonitorDTO>, assignee)
void updateOnlineStatus(DeviceMonitorDTO) / updateofflineStatus(DeviceMonitorDTO)
List<DeviceMonitorDTO> getDeviceListMonitor(dockername)
void updateSnmpParent(dockername, SnmpValuesDTO)
void updateTopology(username, vdmsid, dockername, device_id, snmp_parent, request, assignee)
String getGatewayId(dockername)
```

### Other Device Sub-Services

| Service | Purpose |
|---|---|
| `DeviceConditionsService` | Sensor threshold / condition alert evaluation |
| `DeviceLifecycleHistoryService` | Audit trail of device field changes |
| `DeviceInstalledAppsService` | Software installed on device (kiosk/touchscreen) |
| `DeviceOnboardStatusAssigneeService` | Assignee tracking per onboarding stage |
| `DeviceSpecificationService` | Device spec sheet CRUD |
| `DeviceTechnicianAISuggestionService` | AI-driven technician recommendation |
| `DeviceSearchService` | Cross-field device keyword search |
| `DeviceTypeService` | Device type rename / bulk update |
| `InventoryDeviceService` | Inventory tracking / serial number management |
| `ConnectedDevicesService` | Parent-child connectivity (SNMP topology) |
| `SnmpDeviceService` | SNMP object polling and status |
| `MyDevicesService` | MyDevices IoT platform integration |

### AssetMapperService — Key Methods
```java
ResponseEntity<Void> upload(MultipartFile, fieldMapping, vdms_id, request)
ResponseEntity<List<AssetDTO>> getContents(pageNo, pageSize, importType, searchKey)
ResponseEntity<?> matchAssetsWithDevicesInLocalDatabase()
ResponseEntity<?> saveAssetMappings(username, vdms_id, docker_name)
ResponseEntity<?> saveAssets(username, vdms_id, docker_name, List<String> asset_ids, importType, assignee)
ResponseEntity<?> updateAssetMapping(AssetMappingDTO)
DeviceDTO matchDeviceProducts(username, vdmsid, dockername, asset_id)
Integer getAssetCount(username, vdmsid, importType, searchKey)
```

### AssetOnboardService — Key Methods
```java
void addAssetOnboardedData(username, vdmsid, JSONObject)
void upsertOnboardAssets(username, vdmsid, JSONObject, request)
void updateAssetOnboardStatus(username, vdmsid, JSONObject, request)
void updateAssetOnboardData(username, vdmsid, device_id, DeviceOnboardStatusDTO, onboard_status, request)
Map<String,Integer> getAssetOnboardCount(username, vdmsid, dockername, JSONObject)
void assetUpsert(DeviceDTO, vdmsid, importType, username)
void updateCorrigoAssets(username, vdmsid, pageNo, pageSize, searchKey, CorrigoConfigurationDTO)
```

### Startup & Scheduler
```java
// Startup.java — runs on ApplicationReadyEvent
// Schedular.java — @Scheduled tasks:
//   - Device monitor polling
//   - Virtual device status sync
//   - Alert checks
//   - RabbitMQ reconnect
// TableModifier.java — runs DDL modifications not handled by Hibernate on startup
```

---

## Repositories

### DeviceRepository (key method categories)

**Reads:** `listAllDevicebyVdmsidAndDockerName`, `getfilterdevices`, `getDeviceByDeviceId`, `getDeviceById`, `getSubsystemParentDevicesByPagination`, `getSubsystemDevicesByPagination`, `getDevicesByIdList`, `getDeviceInfoById`, `getDeviceListMonitor`, `getAssetMapperDevicesByIdList`

**Counts:** `onlineOfflineCountByDocker`, `monitorCountByDocker`, `assignedCountByDocker`, `getMatchedUnmatchedDeviceCountByDocker`, `getOnboardedDeviceCountByDocker`, `getAllDeviceCountByDocker`, `getAssetOnboardCount`

**Write:** `insertDevice`, `addDevice`, `updateDevice`, `editDeviceByDeviceID` (34 params), `addVirtualDevice`, `editVirtualDeviceByVirtualDeviceId`, `deleteVirtualDeviceByeviceId`, `quickUpdate`, `multiDeviceUpdateByDeviceId`, `upsertVirtualDeviceByAssetMapper`, `addAssetOnboardedDevices`

**Field-specific updates (70+):** `updateDeviceSnmpCount`, `updateDeviceBacnetCount`, `updateDeviceLorawanCount`, `updateDeviceTicketCount`, `updateDeviceRecordChecklistCount`, `updateDeviceAssetStatus`, `updateDeviceMatchedProductIds`, `updateDevicePosition`, `updateOnboardAssetStatus`, `updateAssetImage`, `toggleDndStatus`, `updateAssignedUserEmail`, `tagInventoryDevices`, etc.

**MCP queries (native SQL):** `mcpGetKpiCounts`, `mcpGetKpiCountsFiltered`, `mcpCountByTypeFiltered`, `mcpCountByCategoryFiltered`, `mcpGetIntegrationCounts` (20-index), `mcpGetNetworkHealth`, `mcpGetAlertConfigCounts`, `mcpGetTicketHealthCounts`, `mcpGetOfflineRiskDevices`, `mcpGetCostTotals`, `mcpGetWarrantyCounts`, `mcpGetLocationGapCounts`, `mcpSearchDevices`, `mcpGetUnassignedDevices`, `mcpGetDndDevices`, `mcpGetSubsystemDevices`, `mcpGetRecentDevices` (17 total aggregate queries)

### AssetRepository
```java
// Read
List<AssetDTO> getPaginatedAssets(limit, offset, importType, searchKey)
List<AssetDTO> getLinkedAssets(device_id)
List<AssetDTO> getUnmappedAssets(pageSize, offset)
List<AssetDTO> getSubSystemParentAssets(pageSize, offset, importType)
List<AssetDTO> getSubSystemAssets(asset_id, pageSize, offset)
List<AssetDTO> getUnmappedMatchedAssets()
Integer getTotalAssetCount()

// Write
void setMatched(Boolean, asset_id)
void saveMatchedProductsById(asset_id, matchedProducts)
void updateSubsystemParentId(assetId, subsystemParentId)
void assetUpsert(id, display_name, description, type, mac_address, model, vendor, ip_address,
                 network_layer, serial_number, warranty, original_keys, custom_fields,
                 subsystem_parent_id, is_matched, matched_products, vdms_id, subsystem_count, import_type)
void deleteAllRecords() / deleteAllMatchedRecords() / deleteAllById(ArrayList<String>)
```

### AssetDeviceMappingRepository
```java
void saveNewAssetMapping(id, match_score, Asset, Device)
List<String> findByDeviceIds(List<String>)
String findByAssetId(asset_id)
List<AssetDeviceMappingDTO> findMappings()
void deleteAllRecords()
void deleteByDeviceId(device_id)
```

---

## Models & Entities

### Entity Relationships

```
Vdms
 └── Device (docker_vdms_id FK)
       ├── AssetDeviceMapping ──► Asset
       ├── DeviceSpecification
       ├── DeviceConditions
       ├── DeviceLifecycleHistory
       ├── DeviceOnboardStatus ──► DeviceOnboardStatusAssignee
       ├── SnmpDevice ──► SnmpObject ──► SnmpCondition
       ├── BacnetDevice ──► BacnetObject ──► BacnetAttributes
       ├── LorawanSensor ──► LorawanSensorAttributes
       ├── ModbusDevice ──► ModbusRegister
       ├── SiemensAsset ──► SiemensAssetAttributes
       │                └── SiemensAssetConditions ──► AlertProfile
       ├── AirthingDevice
       ├── AwairDevice
       ├── EcobeeSensor
       ├── PelicanSensor
       ├── MonnitSensor
       ├── DisruptiveSensor
       ├── KNXDevice ──► KNXGroup
       ├── MqttDevice ──► MqttDeviceAttributes
       ├── MyDevicesSensor
       ├── VergeSenseSpace
       ├── DaintreeDevice ──► DaintreePoint
       ├── GaiameshController ──► GaiameshControllerPoint
       ├── MeasuringInstrument ──► MeasuringInstrument_Attributes
       ├── Interface ──► Connection
       ├── Ticket ──► TicketHistory
       ├── CheckListTemplate ──► CheckListRecord
       ├── Document / Media / Notes
       ├── RemoteAccess ──► RemoteAccessSession
       └── InventoryDevice

Building ──► Floor ──► Location ──► Device (location_id FK)
AlertProfile ◄── DeviceConditions / SnmpCondition / SiemensAssetConditions
GlobalChecklist ──► GlobalChecklistConditions ──► Device (tagged)
GlobalInspectionRelation ──► GlobalInspectionRelationAssignees
MriConfiguration ──► MriTask ──► MriTaskHistory
```

### Device — Key Fields

| Group | Fields |
|---|---|
| Identity | `id`, `mac_address`, `ip_address`, `docker_vdms_id`, `docker_name` |
| Status | `status` (0=offline/1=online), `monitor` (0/1), `virtual_device_type`, `operational_status` |
| Metadata | `type`, `vendor`, `model`, `user_data_name/model/vendor`, `serial_number`, `warranty` |
| Location | `location_id` → Location, `building_id` → Building, `floor_id` → Floor, `latitude`, `longitude`, `position` |
| Hierarchy | `parent`, `snmp_parent`, `subsystem_parent_id` (self-ref), `subsystem_count` |
| Asset match | `asset_match_status` (0=unmatched/1=matched/2=verified/3=archived) |
| Onboarding | `onboard_status` (0–3), `assigned_user_email` |
| Alerts/DND | `email_alert`, `sms_alert`, `popup_notification`, `alarm`, `ai_call`, `is_dnd_enabled`, `system_dnd_enabled` |
| Costs | `cost_value` (BigDecimal), `cost_unit` |
| Integration counts | `snmp_count`, `bacnet_count`, `lorawan_count`, `modbus_count`, `monnit_count`, `disruptive_count`, `knx_count`, `pelican_count`, `ecobee_count`, `my_devices_count`, `daintree_count` |
| Integration statuses | `*_status` String for each protocol (OK/WARNING/CRITICAL) |
| Operational counts | `ticket_count`, `notes_count`, `document_count`, `media_count`, `checklist_template_count`, `record_checklist_count`, `snmp_object_count`, `measuring_instrument_count`, `interface_count`, `qrcode_count` |
| Images | `asset_image_url`, `asset_ocr_image_url`, `digital_twin_image_url` |
| Audit | `created_timestamp` (BigInteger), `created_email`, `updated_timestamp`, `updated_email` |

### Asset — Key Fields
`id`, `display_name`, `description`, `type`, `vendor`, `model`, `mac_address`, `ip_address`, `network_layer`, `serial_number`, `warranty`, `is_matched` (Boolean), `subsystem_parent_id`, `subsystem_count`, `import_type`, `originalKeys` (LONGTEXT JSON), `customFields` (LONGTEXT JSON), `matched_product_ids`

**Relationship:** `@OneToMany AssetDeviceMapping`, `@ManyToOne Vdms`

---

## DTOs

### DeviceDTO — 22 constructors, 100+ fields

| Group | Key Fields |
|---|---|
| Core | `id`, `name`, `display_name`, `mac_address`, `ip_address`, `vdms_id`, `docker_name`, `docker_vdms_id` |
| Status | `status`, `monitor`, `virtual_device_type`, `remote_access`, `operational_status` |
| Metadata | `type`, `vendor`, `model`, `user_data_name/model/vendor`, `serial_number`, `warranty` |
| Location | `location`, `location_id`, `building`, `building_id`, `floor`, `floor_id`, `latitude`, `longitude`, `position` |
| Hierarchy | `parent`, `snmp_parent`, `subsystem_parent_id`, `subsystem_count`, `subsystems` (Set) |
| Asset | `asset_match_status`, `matched_product_ids`, `asset_group`, `category`, `sub_category` |
| Onboard | `onboard_status`, `onboard_data`, `assignee_email`, `location_status` |
| Alerts | `email_alert`, `sms_alert`, `popup_notification`, `alarm`, `ai_call`, `is_dnd_enabled`, `system_dnd_enabled` |
| Vendors | `local_vendor_id`, `global_vendor_id`, `other_vendor_1/2/3_id` |
| Integration counts | `snmp_count`, `bacnet_count`, `lorawan_count`, `modbus_count`, `monnit_count`, `disruptive_count`, `knx_count`, `pelican_count`, `ecobee_count`, `my_devices_count`, `daintree_count` |
| Integration statuses | `*_status` String per protocol |
| Op counts | `ticket_count`, `notes_count`, `document_count`, `media_count`, `checklist_template_count`, `record_checklist_count`, `snmp_object_count`, `measuring_instrument_count`, `qrcode_count` |
| Cost | `cost_value` (BigDecimal), `cost_unit`, `assigned_user_email` |
| Images | `asset_image_url`, `asset_ocr_image_url`, `digital_twin_image_url`, `asset_tag_images_url` |
| Audit | `created_timestamp`, `created_email`, `updated_timestamp`, `updated_email` |
| Product | `product_id`, `system_type_name`, `asset_type_name`, `asset_sub_type_name`, `source_type` |
| Misc | `custom_fields`, `description`, `adc_json`, `inventory_tracking_id`, `user_connection_type` |

**DeviceDTO constructors** are named via `@ConstructorResult` aliases on `Device.java`. When writing a new `@NamedNativeQuery`, the column count in `SELECT` must exactly match the matching constructor — a mismatch causes a runtime error, not a compile error.

### AssetDTO
`id`, `display_name`, `description`, `mac_address`, `model`, `vendor`, `type`, `ip_address`, `network_layer`, `serial_number`, `warranty`, `match_score`, `matched_product_ids`, `originalKeys`, `customFields`, `exactMatch`, `subsystem_parent_id`, `import_type`, `subsystem_count`, `subsystems` (List)

### MCP Request DTOs (all in `io.sclera.mcp.dto`)

| DTO | `@NotNull` | Optional |
|---|---|---|
| `DeviceSummaryRequest` | `vdmsId` | dockerName, status, monitorStatus, assetMatchStatus, onboardStatus, assigneeEmail, category, virtualDeviceType |
| `DeviceListRequest` | `vdmsId` | dockerName, virtualDeviceType, status, monitorStatus, assetMatchStatus, onboardStatus, assigneeEmail, pageNo, pageSize |
| `DeviceDetailRequest` | `deviceId` | — |
| `DeviceVdmsRequest` | `vdmsId` | dockerName |
| `DeviceUnassignedRequest` | `vdmsId` | dockerName, pageNo, pageSize |
| `DeviceSearchRequest` | `vdmsId` | dockerName, keyword, pageNo, pageSize |
| `DeviceNetworkHealthRequest` | `vdmsId` | — |
| `DeviceSubsystemsRequest` | `deviceId` | pageNo, pageSize |
| `DeviceRecentActivityRequest` | `vdmsId` | dockerName, days, pageNo, pageSize |

---

## Special Packages

### `io.sclera.rabbitmq`
- `RabbitmqService` — connection factory and template setup
- `RabbitmqPublisher` — publishes events (device status changes, alerts, etc.)
- `RabbitmqSubscriber` — listens to queues and dispatches to services

### `io.sclera.sockets`
- `SocketService` — STOMP WebSocket push to connected clients
- `SocketTopics` — constants for STOMP destination topics
- `SocketUtils` — payload serialization helpers

### `io.sclera.websocket`
- `WebSocketConfig` — configures STOMP endpoint at `/ws`
- `WebSocketClient_Application` — outbound WS client connecting to `p2p-web-socket-url` and `integration-web-socket-url`
- `ADCStompSessionHandler` / `P2PSessionSocketHandler` / `IntegrationServerSocketHandler` — inbound message handlers
- `SocketReconnectListener` — auto-reconnect on disconnect

### `io.sclera.startup`
- `Startup` — fires on `ApplicationReadyEvent`: seeds default data, wires up initial state
- `Schedular` — `@Scheduled` recurring tasks (polling, sync, alert checks)
- `TableModifier` — executes one-time DDL migrations on startup

### `io.sclera.offline`
- Full offline-mode support: sync API (`/offline/sync`), data export (`/offline/export`), image upload/download, DB migration
- Uses its own `Config` and `ImageConfig` entities/repos separate from main domain

### `io.sclera.integration`
- `ReactiveServiceIntegrationController/Service` — bridges reactive service (third-party FM platform) events
- `LocationIntegrationController/Service` — syncs location/room data from integration platforms
- `RoomStatusIntegrationService` — maps room occupancy status from integrated sensors

### `io.sclera.proxy`
- `ProxyController` — intercepts `/proxy/**` and forwards to the configured remote agent server (stored in `RemoteAgentServerDetails`)

### `io.sclera.auth`
- `TenantJWSKeySelector` — picks the JWK Set URL per JWT issuer (multi-tenant)
- `TenantJwtIssuerValidator` — validates `iss` claim against known tenants

---

## Integration Count Update Pattern

Every IoT protocol follows this chain when a sensor is linked/unlinked to a device:

```
Protocol Service (e.g. BacnetService)
  └──► DeviceService.updateDeviceBacnetCount(device_id)
         └──► BacnetObjectRepository.countByDeviceId(device_id)
               └──► DeviceRepository.updateDeviceBacnetCount(device_id, count)
  └──► DeviceService.updateDeviceBacnetStatus(bacnet_device_id, bacnet_object_id)
         └──► [evaluates alert rules]
               └──► DeviceRepository.updateDeviceBacnetStatus(device_id, status)
```

If any protocol service stops calling the `DeviceService` updater (e.g. due to a refactor), the `*_count` and `*_status` fields on the device become stale — all MCP queries, dashboards, and filter counts will return wrong values.
