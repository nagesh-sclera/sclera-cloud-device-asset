# Stub inventory — sclera-cloud-device-asset

Generated 2026-05-19. Total stubs: **70**
(45 service-level stubs + 21 repository stubs at original FQN backed by 16 dedicated `io.sclera.stubs.*Stub` components and 8 `*Impl` siblings + 4 placeholder DTO/client stubs).

## Summary

| Target microservice | Service stubs | Repository stubs (interfaces) | Empty stubs | Total |
|---|---|---|---|---|
| AP-C2 sensor-integrations | 16 | 0 | 0 | 16 |
| AP-C3 workorders/tickets  |  5 | 0 | 0 |  5 |
| AP-C4 inspection          |  6 | 0 | 0 |  6 |
| AP-C5 alerts              |  3 | 2 | 0 |  5 |
| AP-C6 history/audit       |  3 | 1 | 0 |  4 |
| AP-C8 inventory           |  1 | 1 | 0 |  2 |
| AP-C9 ADC                 |  0 | 0 | 0 |  0 (no file in repo) |
| CP-2 identity             |  3 | 2 | 0 |  5 |
| AP-C1edge (legacy edge)   |  5 | 2 | 0 |  7 |
| Edge-only (Bucket-D)      | 18 |14 | 0 | 32 |
| Placeholder DTOs/clients  |  – | – | – |  3 |
| Unclassified              |  0 | 0 | 0 |  0 |

> "Repository stub" counts the public `@Repository` *interface*; each has either a `*Impl` (under `io.sclera.Repository`) or a `*Stub` (under `io.sclera.stubs`) — sometimes both. They are treated as one logical stub each.

## Methodology notes
- A "stub method" is identified by either (a) a body that calls `io.sclera.utils.StubLog.warn(...)`, (b) a body that logs `STUB:` / `[STUB]` via a private `Logger`, or (c) a body that is empty / returns a safe default in a class explicitly annotated `STUB` in its Javadoc.
- "Empty stub" means the class exists at its target FQN with `@Service`/`@Repository` but defines no methods. **None remain** in this codebase — every stub from the original `status-2026-05-13.md` list has had at least one method demanded by a call site.
- For repository stubs, the interface lives at `io.sclera.Repository.<Name>` (mirrors the original FQN); the body implementation lives either at `io.sclera.Repository.<Name>Impl` (annotated `@Primary`) or at `io.sclera.stubs.<Name>Stub` (annotated `@Component`). Both are listed as discriminators.
- Method signatures captured verbatim from source.
- If a class contains BOTH real methods and stub methods, only the stub methods are listed.

---

## AP-C2 sensor-integrations

### `io.sclera.service.BacnetService`
File: `src/main/java/io/sclera/service/BacnetService.java`
- `String getDeviceIdByBacnetObjectId(String bacnetDeviceId, String bacnetObjectId)` — returns null
- `Integer getBacnetObjectCountByDeviceId(String deviceId)` — returns 0
- `Boolean getBacnetObjectAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<BacnetObjectDTO> getDeviceBacnetObjects(String a, String b, String c, String d)` — returns empty set
- `List<SensorDTO> getBacnetObjectsByDeviceId(String deviceId)` — returns empty list
- `List<ConditionsDTO> listBacnetDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `List<BacnetAdvanceExportExcelDTO> getBacnetDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId)` — returns empty list
- `void updateBacnetObjectDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.DaintreeService`
File: `src/main/java/io/sclera/service/DaintreeService.java`
- `String getDeviceIdByDaintreeDeviceId(String id)` — returns null
- `Integer getDeviceDaintreeDevicesCountByDeviceId(String deviceId)` — returns 0
- `Boolean getDaintreeAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<DaintreeDeviceDTO> getDaintreeDevicesByDeviceId(String a, String b, String c, String d)` — returns empty set
- `List<ConditionsDTO> listDaintreeDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `void updateDaintreeDeviceByDeviceId(String oldId, String newId, Set<String> ids)` — no-op
- `List<DaintreeConfigurationDTO> getDaintreeConfigurations(String vdmsId)` — StubLog.warn + empty list

### `io.sclera.service.DisruptiveService`
File: `src/main/java/io/sclera/service/DisruptiveService.java`
- `String getDeviceIdByDisruptiveSensorId(String id)` — returns null
- `Integer getDisruptiveSensorCountByDeviceId(String deviceId)` — returns 0
- `Boolean getDisruptiveSensorAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<DisruptiveSensorDTO> getDeviceDisruptiveSensors(String a, String b, String c)` — returns empty set
- `List<ConditionsDTO> listDisruptiveDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `void updateDisruptiveSensorDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.EcobeeService`
File: `src/main/java/io/sclera/service/EcobeeService.java`
- `Set<EcobeeSensorDTO> getEcobeeDevicesByDeviceId(String a, String b, String c, String d)` — returns empty set
- `Integer getEcobeeSensorCountByDeviceId(String deviceId)` — returns 0
- `Boolean getEcobeeSensorAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `String getDeviceIdByEcobeeSensorId(String id)` — returns null
- `void updateEcobeeSensorDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.IntegrationService`
File: `src/main/java/io/sclera/service/IntegrationService.java`
- `void updateCustomerOrgByIntegrationId(String customerOrgId)` — log.warn

### `io.sclera.service.KNXService`
File: `src/main/java/io/sclera/service/KNXService.java`
- `String getDeviceIdByKNXGroupAddress(String a, String b)` — returns null
- `Integer getKNXGroupCountByDeviceId(String deviceId)` — returns 0
- `Boolean getKNXGroupAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<KNXGroupDTO> getDeviceKNXGroups(String a, String b, String c, String d)` — returns empty set
- `List<SensorDTO> getKNXGroupsByDeviceAddress(String addr)` — returns empty list
- `List<ConditionsDTO> listKNXDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `void updateKnxGroupDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.LorawanService`
File: `src/main/java/io/sclera/service/LorawanService.java`
- `String getDeviceIdByLorawanSensorId(String id)` — returns null
- `Integer getLorawanSensorCountByDeviceId(String deviceId)` — returns 0
- `Boolean getLorawanSensorAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<LorawanSensorDTO> getDeviceLorawanSensors(String a, String b, String c, String d)` — returns empty set
- `List<SensorDTO> getLorawanSensorsByDeviceId(String deviceId)` — returns empty list
- `List<ConditionsDTO> listLorawanDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `void updateLorawanSensorDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.ModbusService`
File: `src/main/java/io/sclera/service/ModbusService.java`
- `Set<ModbusRegisterDTO> getDeviceModbusRegisters(String a, String b, String c, String d)` — returns empty set
- `List<SensorDTO> getModbusRegistersByDeviceId(String deviceId)` — returns empty list
- `List<ConditionsDTO> listModbusDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `String getDeviceIdByModbusRegisterId(String modbusRegisterId)` — returns null
- `Boolean getModbusRegisterAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Integer getModbusRegistersCountByDeviceId(String deviceId)` — returns 0
- `void updateModbusRegisterDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.MonnitService`
File: `src/main/java/io/sclera/service/MonnitService.java`
- `String getDeviceIdByMonnitSensorId(String id)` — returns null
- `Integer getMonnitCountByDeviceId(String deviceId)` — returns 0
- `Boolean getMonnitAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<MonnitSensorDTO> getDeviceMonnitSensors(String a, String b, String c)` — returns empty set
- `List<SensorDTO> getMonnitSensorsByDeviceId(String deviceId)` — returns empty list
- `List<ConditionsDTO> listmonnitDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `void updateMonnitSensorDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.MqttService`
File: `src/main/java/io/sclera/service/MqttService.java`
- `Set<MqttDeviceDTO> getAllMqttDevices(String a, String b, String c, String d)` — returns empty set
- `Integer getMqttDeviceCountByDeviceId(String deviceId)` — returns 0

### `io.sclera.service.PelicanService`
File: `src/main/java/io/sclera/service/PelicanService.java`
- `String getDeviceIdByPelicanSensorId(String id)` — returns null
- `Integer getPelicanSensorCountByDeviceId(String deviceId)` — returns 0
- `Boolean getPelicanSensorAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<PelicanSensorDTO> getDevicePelicanSensors(String a, String b, String c)` — returns empty set
- `List<SensorDTO> getPelicanSensorsByDeviceId(String deviceId)` — returns empty list
- `List<ConditionsDTO> listpelicanDevicesAlertMessagesByDeviceIds(List<String> ids)` — returns empty list
- `void updatePelicanSensorDeviceId(String oldId, String newId, Set<String> ids)` — no-op

### `io.sclera.service.PolyLensService`
File: `src/main/java/io/sclera/service/PolyLensService.java`
- `Set<PolyLensDeviceDTO> getAllPolyLensDevices(String a, String b, String c, String d)` — returns empty set
- `void updatePolyLensDeviceId(String oldId, String newId, Set<String> ids)` — no-op
- `Integer getPolyLensDeviceCountByDeviceId(String deviceId)` — returns 0

### `io.sclera.service.SiemensService`
File: `src/main/java/io/sclera/service/SiemensService.java`
- `List<BacnetAdvanceExportExcelDTO> getBacnetDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId)` — returns empty list
- `List<SiemensAdvanceExportExcelDTO> getSiemensDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId)` — returns empty list
- `List<SiemensBmsExportDTO> getSiemensBmsData(String username, String vdmsId, String deviceId)` — returns empty list
- `void updateSiemensDeviceId(String oldId, String newId)` — no-op

### `io.sclera.service.SnmpService`
File: `src/main/java/io/sclera/service/SnmpService.java`
- `Boolean getSnmpDeviceAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `String getDeviceIdBySnmpDeviceId(String snmpDeviceId)` — returns null
- `Integer getSnmpDeviceCountByDeviceAndSnmpConfiguration(String deviceId)` — returns 0
- `Integer getSnmpObjectCountByDeviceId(String deviceId)` — returns 0
- `String getDeviceIdBySnmpObjectId(String a, String b)` — returns null
- `Boolean getSnmpObjectAlertStatusByDeviceId(String deviceId)` — returns Boolean.FALSE
- `Set<SnmpObjectDTO> getDeviceSnmpObjects(String a, String b, String c, String d)` — returns empty set
- `List<SensorDTO> getSnmpDevicesByDeviceId(String deviceId)` — returns empty list
- `void deleteGlobalSnmpByDeviceId(String deviceId)` — no-op
- `void upsertGlobalSnmpByDeviceId(Set<Product_SnmpDTO> snmpSet, String deviceId)` — no-op
- `void updateSnmpObjectDeviceId(String oldId, String newId, Set<String> ids)` — no-op
- `Object getAllNetworkSnmpDeviceData(String deviceId)` — returns null

### `io.sclera.service.touchscreen.assetmapper.AssetMapperService`
File: `src/main/java/io/sclera/service/touchscreen/assetmapper/AssetMapperService.java`
- `void updateDeviceTypeForAllAsset(List<DeviceTypesDTO> deviceTypes)` — no-op

### `io.sclera.service.PropertyQrcodeService` (AP-C2 adjacent — sensor-mapper property service)
File: `src/main/java/io/sclera/service/PropertyQrcodeService.java`
- `void updatePropertyServiceLocations(String locationId)` — StubLog.warn
- `PropertyServiceDTO upsertPropertyServiceDetails(String username, String vdmsId, PropertyServiceDTO dto)` — StubLog.warn + echo
- `void addPropertyServiceLocations(String username, String vdmsId, String serviceId, Set<LocationDTO> locations)` — StubLog.warn
- `void multiUpdatePropertyServiceResponse(String username, String vdmsId, Set<PropertyServiceResponseDTO> responses)` — StubLog.warn
- `Set<PropertyServiceDTO> getPropertyServices(String username, String vdmsId)` — StubLog.warn + empty set
- `Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(String username, String vdmsId, String serviceId)` — StubLog.warn + empty set
- `void deletePropertyServiceRequests(String username, String vdmsId, Set<PropertyServiceRequestDTO> requests)` — StubLog.warn
- `void deletePropertyServiceLocations(String username, String vdmsId, String serviceId, Set<String> locations)` — StubLog.warn
- `void deletePropertyService(String username, String vdmsId, String serviceId)` — StubLog.warn
- `Set<PropertyQrcodeDTO> getZoneMap(String username, String vdmsId, String buildingId, String floorId, String locationId, String serviceId)` — StubLog.warn + empty set
- `void syncServiceValue(String vdmsId)` — StubLog.warn

---

## AP-C3 workorders/tickets

### `io.sclera.service.CorrigoService`
File: `src/main/java/io/sclera/service/CorrigoService.java`
- `CorrigoConfigurationDTO getCorrigoConfigurationDetails()` — log.warn + null
- `void updateCorrigoAssets(String username, String vdmsid, Integer pageNo, Integer pageSize, String searchKey, CorrigoConfigurationDTO config)` — log.warn
- `JSONArray getWorkordersByAssetIdForBot(DeviceDTO device)` — log.warn + null
- `void corrigoUrlSync(Object url, String vdmsId)` — log.warn
- `void updateCorrigoCredentialsFromCloud(String vdmsId)` — log.warn
- `void updateCorrigoCredentialsMigration(String vdmsId)` — log.warn

### `io.sclera.service.MyDevicesService`
File: `src/main/java/io/sclera/service/MyDevicesService.java`
- `void startMyDevicesService()` — no-op
- `void upsertMyDevicesCompany(String username, String vdmsid, MyDevicesCompanyDTO myDevicesCompany)` — no-op
- `void updateMyDevicesEventData(JSONObject myDevicesEventData)` — no-op
- `List<MyDevicesCompanyDTO> getMyDevicesCompanies(String vdmsId, Integer page, Integer size)` — returns empty list
- `List<MyDevicesSensorDTO> getMyDevicesSensors(String vdmsId, String companyId, Integer page, Integer size)` — returns empty list
- `void deleteMyDevicesCompany(String username, String vdmsid, String id)` — StubLog.warn
- `void deleteMyDevicesSensor(String id)` — no-op
- `String getDeviceIdByMyDevicesSensorId(String sensorId)` — StubLog.warn + null
- `Integer getMyDevicesSensorCountByDeviceId(String deviceId)` — StubLog.warn + 0
- `Boolean getMyDevicesSensorAlertStatusByDeviceId(String deviceId)` — StubLog.warn + null
- `Set<MyDevicesSensorDTO> getDeviceMyDevicesSensors(String vdmsId, String companyId, String deviceId)` — StubLog.warn + empty set
- `List<SensorDTO> getMydevicesSensorsByDeviceId(String deviceId)` — StubLog.warn + empty list
- `Collection<? extends ConditionsDTO> listmydevicesDeviceAlertMessagesByDeviceIds(List<String> deviceIds)` — StubLog.warn + empty list
- `void updateMyDevicesSensorDeviceId(String oldDeviceId, String newDeviceId, Set<String> sensorIds)` — StubLog.warn
- `List<MyDevicesCompanyDTO> getAllMyDevicesCompanies(String username, String vdmsId)` — StubLog.warn + empty list
- `Set<MyDevicesCompanyDTO> getMyDevicesCompaniesPagination(String username, String vdmsId, String searchkey, Integer pageno, Integer pagesize)` — StubLog.warn + empty set
- `MyDevicesSensorDTO getMyDevicesSensor(String username, String vdmsId, String sensorId)` — StubLog.warn + null
- `void updateMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors)` — StubLog.warn
- `void deleteMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors)` — StubLog.warn
- `void updateDeviceMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors)` — StubLog.warn
- `void deleteDeviceMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors)` — StubLog.warn
- `List<MyDevicesSensorDTO> getAllMyDevicesSensors(String username, String vdmsId)` — StubLog.warn + empty list
- `List<MyDevicesSensorDTO> getAllMyDevicesSensorsByPagination(String username, String vdmsId, String searchkey, Integer pageno, Integer pagesize)` — StubLog.warn + empty list
- `Set<MyDevicesSensorDTO> getMyDevicesSensorsByPagination(String username, String vdmsId, String companyId, String searchkey, Integer pageno, Integer pagesize)` — StubLog.warn + empty set
- `void updateMyDevicesSensorAttributes(String username, String vdmsId, List<MyDevicesSensorAttributesDTO> attrs)` — StubLog.warn

### `io.sclera.service.PmsService`
File: `src/main/java/io/sclera/service/PmsService.java`
- `Set<String> getLocationIdsByRoomStatus(String vdmsId, String status)` — StubLog.warn + empty set
- `Set<PmsAttributesDTO> getPmsAttributesByLocationIds(Set<String> locationIds)` — StubLog.warn + empty set
- `void updatePmsAttributesByLocationId(String locationId)` — StubLog.warn

### `io.sclera.service.TicketService`
File: `src/main/java/io/sclera/service/TicketService.java`
- `Integer getTicketCountByDeviceId(String deviceId)` — returns 1
- `Boolean getOpenTicketStatus(String deviceId)` — returns false
- `void updateTicketAssigneeByUserEmail(String email)` — StubLog.warn

### `io.sclera.service.WorkorderTemplateService`
File: `src/main/java/io/sclera/service/WorkorderTemplateService.java`
- `String getWorkOrderTemplateComment(String templateId)` — log.warn + null

---

## AP-C4 inspection

### `io.sclera.service.CheckListTemplateService`
File: `src/main/java/io/sclera/service/CheckListTemplateService.java`
- `Integer getCheckListTemplatesCountByDeviceId(String deviceId)` — returns 0

### `io.sclera.service.GlobalChecklistConditionsService`
File: `src/main/java/io/sclera/service/GlobalChecklistConditionsService.java`
- `void updateGlobalChecklistConditionsDeviceAndIsRemoved(Set<String> ids)` — StubLog.warn
- `void updateGlobalChecklistConditionsLocationAndIsRemoved(Set<String> locationIds)` — StubLog.warn

### `io.sclera.service.GlobalChecklistService`
File: `src/main/java/io/sclera/service/GlobalChecklistService.java`
- `void updateDeviceGlobalChecklistDeviceId(String oldId, String newId)` — no-op
- `void deleteGlobalChecklistByDeviceId(String deviceId)` — no-op

### `io.sclera.service.GlobalInspectionRecordService`
File: `src/main/java/io/sclera/service/GlobalInspectionRecordService.java`
- `void updateGlobalInspectionRelationDeviceAndIsRemoved(Set<String> ids)` — StubLog.warn
- `void deleteGlobalInspectionRelationInBatch(List<String> ids)` — StubLog.warn
- `void updateGlobalInspectionByDeviceId(String primaryDeviceId, String existingDeviceId)` — StubLog.warn
- `void updateGlobalInspectionRelationLocationAndIsRemoved(Set<String> locationIds)` — StubLog.warn
- `void updateGlobalInspectionRecord(String email)` — StubLog.warn

### `io.sclera.service.InspectionRecordService`
File: `src/main/java/io/sclera/service/InspectionRecordService.java`
- `void updateInspectionRecordStatus(String a, String b, String id, boolean status)` — log.warn
- `void updateInspectionStatusOnDeviceArchive(Set<String> ids)` — log.warn
- `void updateInspectionRecord(String email)` — log.warn

### `io.sclera.service.RecordChecklistService`
File: `src/main/java/io/sclera/service/RecordChecklistService.java`
- `void updateRecordChecklistDeviceAndIsRemoved(Set<String> ids)` — StubLog.warn
- `void deleteRecordChecklistInBatch(List<String> ids)` — StubLog.warn
- `List<String> deleteAllRecordChecklistByDeviceId(String deviceId)` — StubLog.warn + empty list
- `void deleteAllRecordChecklistImagesByUrls(List<String> urls)` — StubLog.warn
- `String getRecordChecklistStatusByDeviceId(String deviceId, String x)` — StubLog.warn + null
- `Integer getChecklistStatusCountDeviceId(String a, String b, String c)` — StubLog.warn + 0
- `void updateRecordChecklistByDeviceId(String oldId, String newId, Set<String> ids)` — StubLog.warn
- `Set<RecordChecklistDTO> getAllRecordChecklistByBuildings(List<String> buildingIds, List<String> floorIds, List<String> locationIds)` — StubLog.warn + new HashSet
- `String getRecordChecklistStatusByLocationId(String locationId, String status)` — StubLog.warn + null
- `Integer getChecklistStatusCountLocationId(String locationId, String a, String b)` — StubLog.warn + 0
- `void updateRecordChecklistLocationAndIsRemoved(Set<String> locationIds)` — StubLog.warn
- `void deleteRecordChecklistByLocationId(String locationId)` — StubLog.warn
- `List<String> deleteAllRecordChecklistByLocationId(String locationId)` — StubLog.warn + null
- `void updateRecordChecklist(String email)` — StubLog.warn

---

## AP-C5 alerts

### `io.sclera.service.AlertDowntimeScheduleService`
File: `src/main/java/io/sclera/service/AlertDowntimeScheduleService.java`
- `Boolean checkAlertDowntime(String deviceId, String alertProfileId)` — returns Boolean.FALSE

### `io.sclera.service.AlertProfileService`
File: `src/main/java/io/sclera/service/AlertProfileService.java`
- `AlertProfileDTO getAlertProfileById(String alertProfileId)` — returns null
- `AlertProfileDTO getAlertProfileDetailsById(String a, String b, String deviceId)` — returns null

### `io.sclera.service.AlertService`
File: `src/main/java/io/sclera/service/AlertService.java`
- `void sendDeviceConditionsAlertInfo(DeviceAlertDTO deviceAlert, AlertProfileDTO alertProfile, BigInteger timestamp)` — no-op
- `void sendSensorAlertInfo(Object sensorAlert, AlertProfileDTO alertProfile, BigInteger timestamp)` — no-op
- `void sendDownloadEmail(JSONObject body, MultipartFile file, String type, String vdmsId)` — no-op

### Repository stubs (AP-C5)

#### `io.sclera.Repository.CallFlowRuleRepository`
Interface: `src/main/java/io/sclera/Repository/CallFlowRuleRepository.java`
Bodies: `Repository/CallFlowRuleRepositoryImpl.java` (`@Primary`) + `stubs/CallFlowRuleRepositoryStub.java`
- `List<CallFlowRuleDTO> getAllCallFlowRules(Integer offset, Integer pagesize, String searchkey)` — log.warn + empty list
- `void deleteById(String id)` — log.warn
- `String checkCallFlowByDeviceid(String deviceId)` — log.warn + null
- `void upsertAiCallFlow(String id, String name, String createdBy, BigInteger createdAt, String updatedBy, BigInteger updatedAt, String deviceId)` — log.warn
- `List<CallFlowRuleDTO> getCallFlowByDeviceId(String deviceId)` — log.warn + empty list

#### `io.sclera.Repository.CallFlowRuleConditionRepository`
Interface: `src/main/java/io/sclera/Repository/CallFlowRuleConditionRepository.java`
Bodies: `Repository/CallFlowRuleConditionRepositoryImpl.java` (`@Primary`) + `stubs/CallFlowRuleConditionRepositoryStub.java`
- `void upsertCallFlowRuleCondition(String id, String criteria, String actionType, String actionValue, String actionMessage, String callFlowRuleId)` — log.warn
- `List<CallFlowRuleConditionDTO> getCallFlowRuleConditionsByCallFlowRuleId(String callFlowRuleId)` — log.warn + empty list
- `List<CallFlowRuleConditionDTO> getCallFlowRuleConditionByRuleIdAndCriteria(String ruleId, String criteria)` — log.warn + empty list
- `void deleteCallFlowRuleConditionById(List<String> ids)` — log.warn

---

## AP-C6 history/audit

### `io.sclera.service.ArchivedRecordService`
File: `src/main/java/io/sclera/service/ArchivedRecordService.java`
- `void batchUpdateArchivedRecords(List<UserActionLogDTO> logs)` — log.warn

### `io.sclera.service.HistoryService`
File: `src/main/java/io/sclera/service/HistoryService.java`
- `void insertDeviceStatusHistory(Integer alarm, String ipAddress, Object o, Object o1, String finalDeviceId)` — no-op
- `void addHistory(HistoryDTO historyDTO)` — no-op
- `void addHistoryWithTimestamp(HistoryDTO historyDTO)` — no-op
- `void updateHistoryDeviceId(String oldId, String newId)` — no-op

### `io.sclera.service.SyslogService`
File: `src/main/java/io/sclera/service/SyslogService.java`
- `JSONObject getSyslogExcludeDeviceIds(String a, String b, String c, String d)` — returns empty JSONObject

### Repository stubs (AP-C6)

#### `io.sclera.Repository.HistoryRepository`
Interface: `src/main/java/io/sclera/Repository/HistoryRepository.java`
Body: `Repository/HistoryRepositoryImpl.java` (`@Primary`)
- `void deleteByDeviceId(String deviceId)` — log.warn

---

## AP-C8 inventory

### `io.sclera.service.InventoryDeviceService`
File: `src/main/java/io/sclera/service/InventoryDeviceService.java`
- `void retireInventoryDevice(String vdmsId, String deviceId, String username, String description, String inventoryTrackingId)` — log.warn
- `Set<DeviceDTO> upsertInventoryDevices(JSONObject stockedOutItems, String vdmsId, String email, InventoryDeviceSyncDTO dto)` — StubLog.warn + empty set

### Repository stubs (AP-C8)

#### `io.sclera.Repository.InventoryDeviceRepository`
Interface: `src/main/java/io/sclera/Repository/InventoryDeviceRepository.java`
Body: `Repository/InventoryDeviceRepositoryImpl.java` (`@Primary`)
- `void deleteByDeviceId(String deviceId)` — log.warn

---

## AP-C9 ADC

No `ADCService` file exists in the repo. Status notes list it as a planned stub but the file has not been created — call sites have not yet demanded it.

---

## CP-2 identity (user/org/phonebook)

### `io.sclera.service.PhonebookService`
File: `src/main/java/io/sclera/service/PhonebookService.java`
- `void addPhoneBookByDeviceId(String a, String b, String c, Set<PhonebookAddressDto> d, String e)` — no-op
- `PhonebookAddressDto getPhoneAddressById(String id)` — returns null

### `io.sclera.service.VendorAdminService`
File: `src/main/java/io/sclera/service/VendorAdminService.java`
- `void deleteVendorsByOrganisationId(String vendorOrgId)` — log.warn
- `void insertVendors(VendorDTO vendor)` — log.warn

### `io.sclera.service.touchscreen.CustomerOrganisationService`
File: `src/main/java/io/sclera/service/touchscreen/CustomerOrganisationService.java`
- `void upsertCustomerByOrganisationIdSync(String orgId)` — log.warn
- `void deleteCustomerOrgById(String orgId)` — log.warn

### Repository stubs (CP-2)

#### `io.sclera.Repository.UserRepository`
Interface: `src/main/java/io/sclera/Repository/UserRepository.java`
Bodies: `Repository/UserRepositoryImpl.java` (`@Primary`) + `stubs/UserRepositoryStub.java`
- `String getOrganisationIdByUserEmail(String email)` — log.warn + null
- `int checkUser(String email, String organisation_id)` — log.warn + 0
- `void insertUser(String email, String company_name, String created_by, BigInteger creation_timestamp, String name, String phone, String phone_type, String value, String website, String organisation_id, String image_url, String language, String role)` — log.warn
- `void updateUser(String company_name, String created_by, String name, String phone, String phone_type, String value, String website, String organisation_id, String email)` — log.warn
- `void deleteUsersByOrganisationId(String customer_org_id)` — log.warn
- `Set<UserDTO> getAllUsers(Integer pagesize, Integer offset, String searchkey)` — log.warn + empty set
- `void deleteById(String email)` — log.warn
- `void editUsers(String company_name, String name, String phone, String phone_type, String value, String website, String email, String language)` — log.warn
- `Set<UserDTO> getAllOrganisationUsersByPagination(Integer pagesize, Integer offset, String searchkey, String customer_org_id)` — log.warn + empty set
- `Set<UserDTO> getAllOtherUsersByPagination(Integer pagesize, Integer offset, String searchkey)` — log.warn + empty set
- `UserDTO getUserByEmail(String email)` — log.warn + null
- `Set<String> getAllUsersEmail()` — log.warn + empty set
- `void updateAllUser(String company_name, String created_by, String name, String phone, String phone_type, String value, String website, String organisation_id, String email, String image_url, String language, String role, BigInteger creation_timestamp)` — log.warn
- `List<UserDTO> getAllUsersByOrganisationId(String customer_org_id)` — log.warn + empty list
- `void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id)` — log.warn
- `Set<UserDTO> getUsers()` — log.warn + empty set
- `String getUserNameByEmail(String email)` — log.warn + null
- `String getAllUserRoles(String email)` — log.warn + null
- `int checkUserByEmail(String email)` — log.warn + 0
- `String getMasterUserEmail()` — log.warn + null

#### `io.sclera.Repository.VendorOrganisationRepository`
Interface: `src/main/java/io/sclera/Repository/VendorOrganisationRepository.java`
Body: `Repository/VendorOrganisationRepositoryImpl.java` (`@Primary`)
- `void addVendor(String vendorOrgId, String vdmsId)` — log.warn

---

## AP-C1edge (legacy edge — slated for absorption into AP-C1 or its own service)

### `io.sclera.service.ClientNfcService`
File: `src/main/java/io/sclera/service/ClientNfcService.java`
- `JSONArray getDeviceIdsTaggedToClientNfc(String id)` — empty JSONArray
- `Integer getClientNfcCountByDeviceId(String deviceId)` — returns 0
- `JSONArray getLocationIdsTaggedToClientNfc(String id)` — StubLog.warn + empty JSONArray
- `void syncAllClientNfc(String vdmsId)` — StubLog.warn
- `void syncClientNfc(String vdmsId)` — StubLog.warn

### `io.sclera.service.ClientQrCodeService`
File: `src/main/java/io/sclera/service/ClientQrCodeService.java`
- `JSONArray getDeviceIdsTaggedToClientQrCode(String id)` — empty JSONArray
- `Integer getClientQrCodeCountByDeviceId(String deviceId)` — returns 0
- `BigInteger maxUpdatedClientQrCodeTimeStamp(String deviceId)` — returns null
- `JSONArray getLocationIdsTaggedToClientQrCode(String id)` — StubLog.warn + empty JSONArray
- `Set<ClientQrCodeDTO> syncClientQrCodes(String vdmsId)` — StubLog.warn + empty set
- `void upsertClientQrCodesInBatch(Set<ClientQrCodeDTO> dtos)` — StubLog.warn
- `void syncAllClientQrCodes(String vdmsId)` — StubLog.warn

### `io.sclera.service.GlobalQrcodeService`
File: `src/main/java/io/sclera/service/GlobalQrcodeService.java`
- `Integer getDeviceQrcodeCountByDeviceId(String deviceId)` — returns 0
- `void deleteGlobalQRCodeByLocationId(String locationId)` — StubLog.warn

### `io.sclera.service.NfcService`
File: `src/main/java/io/sclera/service/NfcService.java`
- `JSONArray getDeviceIdsTaggedToNfc(String nfcId)` — empty JSONArray
- `Set<NfcDTO> getNfcsByDeviceIds(Set<String> deviceIds)` — empty set
- `Integer getQrNfcCountByDeviceId(String deviceId)` — returns 0
- `JSONArray getLocationIdsTaggedToNfc(String nfcId)` — StubLog.warn + empty JSONArray
- `Set<NfcDTO> getNfcsByLocationIds(Set<String> locationIds)` — StubLog.warn + empty set
- `void syncAllNfc(String vdmsId)` — StubLog.warn
- `void syncNfc(String vdmsId)` — StubLog.warn

### `io.sclera.service.QrCodeService`
File: `src/main/java/io/sclera/service/QrCodeService.java`
- `JSONArray getDeviceIdsTaggedToQrCode(String qrCodeId)` — empty JSONArray
- `Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceIds)` — empty set
- `Integer getQrCodeCountByDeviceId(String deviceId)` — returns 0
- `BigInteger getMaxUpdatedQrCodeTimeStamp(String deviceId)` — returns null
- `JSONArray getLocationIdsTaggedToQrCode(String qrCodeId)` — StubLog.warn + empty JSONArray
- `Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds)` — StubLog.warn + empty set
- `Set<QrCodeDTO> syncQrCodes(String vdmsId)` — StubLog.warn + empty set
- `Set<QrCodeDTO> getQrCodeDetailsByIds(Set<String> ids)` — StubLog.warn + empty set
- `void upsertQrCodesInBatch(Set<QrCodeDTO> dtos)` — StubLog.warn
- `Set<QrCodeDTO> getClientQrCodeDetailsByIds(Set<String> ids)` — StubLog.warn + empty set
- `void syncAlQrCodes(String vdmsId)` — StubLog.warn

### Repository stubs (AP-C1edge)

#### `io.sclera.Repository.ClientQrCodeRepository`
Interface: `src/main/java/io/sclera/Repository/ClientQrCodeRepository.java`
Body: `stubs/ClientQrCodeRepositoryStub.java`
- `Integer countByDeviceId(String deviceId)` — returns 0

#### `io.sclera.Repository.QrCodeRepository`
Interface: `src/main/java/io/sclera/Repository/QrCodeRepository.java`
Body: `stubs/QrCodeRepositoryStub.java`
- `Integer countByDeviceId(String deviceId)` — returns 0

---

## Edge-only (Bucket-D, not for extraction)

These stubs likely stay stubs or are deleted at Phase 2 — they represent capabilities owned by the edge agent, not by any future cloud microservice.

### `io.sclera.proxy.ProxyService`
File: `src/main/java/io/sclera/proxy/ProxyService.java`
- `void verifyAndRestartProxyClient(String vdmsId)` — log.warn
- `void syncProxyServer(String vdmsId)` — log.warn
- `void syncProxyClient(String vdmsId)` — log.warn

### `io.sclera.rabbitmq.RabbitmqService`
File: `src/main/java/io/sclera/rabbitmq/RabbitmqService.java`
- `void rabbitmqDeviceEvent(String eventType, DeviceHistoryDTO dto)` — StubLog.warn
- `void rabbitmqMeasuringInstrumentData(String deviceId, String sensorType, BigInteger sensorValue, String unit)` — StubLog.warn

### `io.sclera.service.APICallService`
File: `src/main/java/io/sclera/service/APICallService.java`
- `List<UserDTO> getUsersByOrgId(String organisation_id, String vdms_id)` — empty list
- `List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id)` — empty list
- `Flux<JSONObject> sendDescription(JSONObject requestBody, String vdmsId, String technicianId, String technicianName, String contactNumber, String formattedDateTime, String aiCallLogId)` — Flux.empty
- `ResponseEntity<String> sendCallFlowMail(JSONObject payload)` — ok("")
- `ResponseEntity<String> sendCallFlowMessage(JSONObject payload)` — ok("")
- `String getDeviceHostNameByIP(String a, String b)` — null
- `ProductDTO getProductDetailsByModelAndMBV(String model, String mbv)` — null
- `ProductDTO getProductDetailsByProductId(String productId)` — null
- `JSONArray getNfcIdsByVdmsAndType(String vdmsId, String type)` — empty JSONArray
- `JSONArray getQrCodeIdsByVdmsIdAndType(String vdmsId, String type)` — empty JSONArray
- `void deleteDigitalTwinImageUrl(Set<String> imageUrls, String username, String vdmsId)` — no-op
- `JSONArray getTemporaryProductByIds(Object ids)` — empty JSONArray
- `void deleteTemporaryProductByIds(Object ids)` — no-op
- `Boolean syncBuildingToADC(Object dto, String orgId, String configId)` — Boolean.FALSE
- `BuildingDTO addSingleBuildingObject(String locationId, String vdmsId)` — null
- `Boolean deleteBuildingFromADC(String orgId, String configId, List<String> propertyIds)` — Boolean.FALSE
- `List<BuildingDTO> getAllLocations(String vdmsId)` — empty list
- `String getFloorPathByFloorId(Object a, String vdmsId, String buildingId, String floorId)` — null
- `void generateChatbotMessage(Object query, Object emitter)` — no-op
- `void updateChatbotDeviceData(JSONArray bodyArray)` — no-op
- `Set<ClientBarCodeDTO> getAllClientBarCodeByVdmsId(String vdmsId, int page, int size)` — empty set
- `Set<ClientBarCodeDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int page, int size)` — empty set
- `void sendAgentDataToInventory(Object obj)` — no-op
- `Set<DeviceTypesDTO> getUpdatedAssetTypes(Object ts, int page, int size, String search, String vdmsId)` — empty set
- `PropertyAddressDTO updatePropertyDetails(String vdmsId)` — null
- `void updateVdmsDetailCloud(String vdmsId, VdmsSyncDTO dto)` — no-op
- `String getVendorByMacAddress(String mac)` — null
- `void syncAllAttribute(String ip)` — no-op
- `void syncBacnet(String ip)` — no-op
- `void syncSnmpWalk(String ip)` — no-op
- `void snmpInterface(String ip)` — no-op
- `void snmpTopology(String ip)` — no-op
- `void internetConnectivity(String ip)` — no-op
- `List<VendorDTO> getAllVendorsByOrganisationId(String orgId, String vdmsId, String dockerName)` — empty list
- `VendorTransferDTO getTransferVendor(String vdmsId, String dockerName)` — new instance
- `String getCustomerOrgIdByVdmsId(String vdmsId)` — null
- `void updateVdmsTranfer(String vdmsId)` — no-op
- `VdmsSyncDTO updateVdmsStatus(String vdmsId)` — null
- `void updateQrCodeSyncByVdmsId(String vdmsId)` — no-op
- `void updateNfcSyncByVdmsId(String vdmsId)` — no-op
- `<T> List<T> getJSONArrayFromJSONString(String json, Class<T> clazz)` — empty list
- `void syncSnmpInterfacebyDeviceId(String deviceId, SnmpValuesDTO dto)` — no-op
- `Object fetchMeasuringInstruments()` — StubLog.warn + null
- `Boolean syncLocationToADC(List<LocationDTO> locations, String orgId, String configId, String vdmsId)` — StubLog.warn + true
- `Boolean deleteLocationFromADC(String orgId, String configId, String vdmsId, String buildingId, List<String> locationIds)` — StubLog.warn + true
- `Boolean syncFloorToADC(String orgId, List<FloorDTO> floors, String configId, String vdmsId)` — StubLog.warn + false
- `Boolean deleteFloorFromADC(String orgId, String configId, String buildingId, List<String> floorIds)` — StubLog.warn + false
- `JSONArray getApplicationUsersFromInventory(String vdmsId, String applicationId)` — StubLog.warn + empty JSONArray
- `JSONObject getLicenseDetailsFromInventory(String vdmsId, String applicationId)` — StubLog.warn + null
- `JSONArray getAllInventoryApplications(String vdmsId)` — StubLog.warn + empty JSONArray
- `void updateBarCodeSyncByVdmsId(String vdmsId)` — StubLog.warn
- `List<TechnicianDTO> getAllTechnicians(String vdmsId)` — StubLog.warn + empty list
- `void resetSyncStatusByTechnicianIds(Set<String> ids)` — StubLog.warn
- `List<TechnicianSkillDTO> getAllTechnicianSkills(String vdmsId)` — StubLog.warn + empty list
- `void resetSyncByTechnicianSkillIds(Set<String> ids)` — StubLog.warn
- `List<TechnicianAvailabilityDTO> getAllTechniciansAvailability(String vdmsId)` — StubLog.warn + empty list
- `void resetSyncByTechnicianAvailabilityIds(Set<String> ids)` — StubLog.warn
- `List<TechnicianCertificateDTO> getAllTechniciansCertificates(String vdmsId)` — StubLog.warn + empty list
- `void resetSyncByTechnicianCertificateIds(Set<String> ids)` — StubLog.warn
- `List<DeviceTechnicianAISuggestionDTO> getAllDeviceTechnicianAISuggestions(String vdmsId)` — StubLog.warn + empty list
- `void resetSyncByDeviceTechnicianAiSuggestionIds(Set<String> ids)` — StubLog.warn
- `JSONObject getInventoryItemsByStockOutId(InventoryDeviceSyncDTO dto)` — StubLog.warn + null
- `void updateTaggedInventoryItems(Set<DeviceDTO> devices)` — StubLog.warn
- `String getAgentPermissionsByVdmsId(String vdmsId)` — StubLog.warn + null
- `JSONArray getAllApplicationUsersFromInventory(String vdmsId)` — StubLog.warn + empty JSONArray
- `void syncApplicationUsers(Set<String> ids, String status)` — StubLog.warn
- `void syncApplication(Set<String> ids, String status)` — StubLog.warn
- `void getVdmsAccessToken(String vdmsId, String password)` — no-op

### `io.sclera.service.AsyncService`
File: `src/main/java/io/sclera/service/AsyncService.java`
- `void updateVendorByMacAddress(String mac, String vendor)` — no-op

### `io.sclera.service.DataHoistService`
File: `src/main/java/io/sclera/service/DataHoistService.java`
- `Set<DataHoistDTO> getDataHoistDeviceById(String a, String b, String c)` — returns empty set

### `io.sclera.service.DockerService`
File: `src/main/java/io/sclera/service/DockerService.java`
- `DockerDTO checkIfHostNetworkPresentByNetworkOrigin(Integer networkOrigin)` — log.warn + null
- `String getDockerInternalIp(String dockerName)` — log.warn + null
- `String getGatewayIp(String dockerName)` — log.warn + null
- `void updateDockerNetworkOrigin(String vdmsId)` — log.warn
- `String getInternalIPbyDockername(String vdmsId, String dockerName)` — log.warn + null
- `String getVendorOrgIdByNetworkName(String networkName)` — log.warn + null
- `void updateVendorOrgIdbydocker(String vendorOrgId, String vdmsId, String dockerName)` — log.warn
- `String getInternalInterfaceByDockerName(String dockerName)` — log.warn + null
- `List<DockerInfoDto> getDockerInterfaceList(Integer networkOrigin)` — log.warn + empty list
- `List<DockerInfoDto> getVdmsConfigInterfaceList()` — log.warn + empty list

### `io.sclera.service.IOCService`
File: `src/main/java/io/sclera/service/IOCService.java`
- `void sendDeviceAlertDataIOC(DeviceConditionsDTO deviceConditionsDTO, DeviceAlertDTO deviceAlert, Integer status, AlertProfileDTO alertProfile, BigInteger timestamp)` — no-op
- `void sendDigitalTwinData(Set<String> deviceIds)` — no-op
- `void sendSensorValueDataToIOC(String deviceId, BigInteger sensorValue)` — StubLog.warn

### `io.sclera.service.JobSchedulerService`
File: `src/main/java/io/sclera/service/JobSchedulerService.java`
- `String createScheduledJob(ScheduledJobDTO dto)` — returns null
- `void addScheduledJob(Set<ScheduledJobDTO> dtos)` — no-op
- `void deleteScheduledJob(Set<String> jobIds)` — no-op
- `ScheduledJobDTO getScheduledJobByConditionId(String conditionId)` — returns null

### `io.sclera.service.MasterSlaveAPICallService`
File: `src/main/java/io/sclera/service/MasterSlaveAPICallService.java`
- `String accessMasterFromSlave(String url, String method, Object body, Object headers)` — log.warn + null
- `String accessSlaveFromMaster(String url, String method, Object body, Object headers)` — log.warn + null

### `io.sclera.service.RemoteDesktopSessionService`
File: `src/main/java/io/sclera/service/RemoteDesktopSessionService.java`
- `ResponseEntity<?> updateRemoteConnectFlag(JSONObject json)` — StubLog.warn + ok(null)
- `ResponseEntity<?> getRemoteConnectInfo(String deviceId, String username)` — StubLog.warn + ok(null)
- `RemoteAgentServerDetailsDTO getRemoteSessionDetails(String id)` — StubLog.warn + null
- `void updateAcknowledge(JSONObject json)` — StubLog.warn

### `io.sclera.service.UtilsService`
File: `src/main/java/io/sclera/service/UtilsService.java`
- `String upsertPhoneAddressById(String username, String vdmsid, String dockername, PhonebookAddressDto phonebookaddressdto)` — returns "TEST"

### `io.sclera.service.WebClientService`
File: `src/main/java/io/sclera/service/WebClientService.java`
- `byte[] getImageBytesByUrl(String link)` — returns new byte[1024]
- `TenantDTO getAllTenants(String issuer)` — log.warn + null
- `JSONArray multiEditDigitalTwin(String vdmsId, MultipartFile file, String username, Set<String> deviceIds)` — empty JSONArray
- `String deleteFloorMapsByImageUrl(String vdmsId, List<FloorDTO> floors)` — returns null
- `List<FloorDTO> uploadFloorImages(String vdmsId, List<FloorDTO> floors)` — log.warn + empty list
- `List<FloorDTO> addFloorImages(String vdmsId, MultipartFile file, List<FloorDTO> floors)` — log.warn + empty list
- `List<FloorDTO> syncFloorMapImageByFloorId(String vdmsId, List<FloorDTO> floors)` — log.warn + empty list
- `List<FloorDTO> syncFloorMapTilesFolder(String vdmsId, List<FloorDTO> floors)` — log.warn + empty list

### `io.sclera.service.touchscreen.DeviceMonitorService`
File: `src/main/java/io/sclera/service/touchscreen/DeviceMonitorService.java`
- `List<String> getUniqueAssignedUserEmail(String vdmsId, String networkName)` — log.warn + empty list

### `io.sclera.service.touchscreen.MonitorService`
File: `src/main/java/io/sclera/service/touchscreen/MonitorService.java`
- `void deviceUpsertbyId(String dockerName, List<DeviceMonitorDTO> deviceMonitors, String type)` — StubLog.warn
- `void insertDevicesHistory(String dockerName, List<DeviceHistoryDTO> devicesHistory)` — StubLog.warn

### `io.sclera.service.touchscreen.RemoteAccessSessionService`
File: `src/main/java/io/sclera/service/touchscreen/RemoteAccessSessionService.java`
- `List<RemoteAccessSessionDTO> getAllRemoteAccessSessions()` — StubLog.warn + empty list
- `void stopRemoteAccess(String email, String vdmsId, String networkName, RemoteAccessSessionDTO dto, String ipAddress)` — StubLog.warn

### `io.sclera.service.touchscreen.SettingsService`
File: `src/main/java/io/sclera/service/touchscreen/SettingsService.java`
- `List<InterfaceDTO> getSystemInterfaces()` — StubLog.warn + empty list

### `io.sclera.sockets.SocketService`
File: `src/main/java/io/sclera/sockets/SocketService.java`
- `void socketDeviceCount()` — no-op
- `void sockerDeviceCountByDocker(String dockername, String assignee)` — no-op
- `void socketAiCallLogHistoryUpdate(String id)` — StubLog.warn
- `void socketAiCallLogOngoingHistoryUpdate(String id)` — StubLog.warn
- `void socketDeviceStatus(DeviceMonitorDTO dto)` — StubLog.warn
- `void socketOnlineDevice(String deviceId)` — StubLog.warn
- `void socketOfflineDevice(String deviceId)` — StubLog.warn
- `void socketDeviceUpdate(Set<DeviceDTO> devices)` — no-op
- `void updateDeviceInterfaceStatus(InterfaceDTO dto, String a, String b)` — no-op
- `void socketMeasuringInstrumentSensorValueUpdate(String deviceId)` — StubLog.warn
- `void socketDockerInterfaceStatus(String interfaceName, String interfaceStatus, Integer networkOrigin)` — StubLog.warn

### `io.sclera.websocket.client.WebSocketClient_Application`
File: `src/main/java/io/sclera/websocket/client/WebSocketClient_Application.java`
- `void connectP2PSocket()` — log.warn
- `void connectIntegrationSocket()` — log.warn

### Repository stubs (Edge-only / local-db)

#### `io.sclera.Repository.ConnectedDevicesRepository`
File: `src/main/java/io/sclera/Repository/ConnectedDevicesRepository.java` (concrete `@Component @Repository` class — no separate interface)
- `void addConnectedDevices(String deviceId, String connectedDeviceId, String type)` — StubLog.warn
- `List<ConnectedDevicesDTO> getConnectedDevicesSpecifications(String deviceId, Integer page, Integer size)` — StubLog.warn + empty list
- `List<ConnectedDevicesDTO> getConnectedSpecificationsByDeviceId(String deviceId)` — StubLog.warn + empty list
- `List<ConnectedDevicesDTO> getAllInputConnectedSpecifications(String deviceId)` — StubLog.warn + empty list
- `List<ConnectedDevicesDTO> getAllOutputConnectedSpecifications(String deviceId)` — StubLog.warn + empty list
- `void untagPowerSource(String specificationsId, String connectedSpecificationsId)` — StubLog.warn
- `void untagDevice(String specificationsId, String connectedSpecificationsId)` — StubLog.warn
- `void untagPowerSourceByDeviceId(String deviceId)` — StubLog.warn
- `List<PowerSourceConnectionsDTO> getPowerSourceTopologyForDevice(Set<String> deviceIds)` — StubLog.warn + empty list
- `List<ConnectedDevicesDTO> getAllConnectedDevicesForLoadCalculation(String specificationId)` — StubLog.warn + empty list
- `Integer getPowerSourceTopologyConnectionsCount()` — StubLog.warn + 0
- `List<PowerSourceConnectionsDTO> getPowerSourceTopologyByPagination(Integer pageSize, Integer offset)` — StubLog.warn + empty list
- `void deleteConnectedDevicesBySpecificationId(String specificationsId)` — StubLog.warn

#### `io.sclera.Repository.DockerRepository`
Interface: `src/main/java/io/sclera/Repository/DockerRepository.java`
Bodies: `Repository/DockerRepositoryImpl.java` (`@Primary`) + `stubs/DockerRepositoryStub.java`
- `List<DockerDTO> getAllNetworksByNetworkOrigin(Integer networkOrigin)` — log.warn + empty list

#### `io.sclera.Repository.MeasuringInstrumentAttributesRepository`
Interface: `src/main/java/io/sclera/Repository/MeasuringInstrumentAttributesRepository.java`
Body: `stubs/MeasuringInstrumentAttributesRepositoryStub.java`
- `void upsertMeasuringInstrumentAttribute(String id, String name, String type, String unit, String value, String protocol, String category, String primaryId, String secondaryId, String tertiaryId, String measuringInstrumentId, Integer attributeIndex)` — no-op
- `MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id)` — null
- `List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes()` — empty list
- `List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId)` — empty list

#### `io.sclera.Repository.MyDevicesCompanyRepository`
Interface: `src/main/java/io/sclera/Repository/MyDevicesCompanyRepository.java`
Body: `stubs/MyDevicesCompanyRepositoryStub.java`
- _(no methods — interface body is empty; stub class implements interface with nothing to override)_

#### `io.sclera.Repository.MyDevicesSensorAttributesRepository`
Interface: `src/main/java/io/sclera/Repository/MyDevicesSensorAttributesRepository.java`
Body: `stubs/MyDevicesSensorAttributesRepositoryStub.java`
- `void upsertMeasuringInstrumentAttribute(String id, String name, String type, String unit, String value, String protocol, String category, String primaryId, String secondaryId, String tertiaryId, String measuringInstrumentId, Integer attributeIndex)` — no-op
- `MeasuringInstrumentAttributesDTO getMeasuringInstrumentAttributeById(String id)` — null
- `List<MeasuringInstrumentAttributesDTO> getAllMeasuringInstrumentAttributes()` — empty list
- `List<MeasuringInstrumentAttributesDTO> getMeasuringInstrumentAttributesByMeasuringInstrumentId(String measuringInstrumentId)` — empty list

#### `io.sclera.Repository.MyDevicesSensorRepository`
Interface: `src/main/java/io/sclera/Repository/MyDevicesSensorRepository.java`
Body: `stubs/MyDevicesSensorRepositoryStub.java`
- _(no methods)_

#### `io.sclera.Repository.RemoteAgentServerDetailsRepository`
Interface: `src/main/java/io/sclera/Repository/RemoteAgentServerDetailsRepository.java`
Bodies: `Repository/RemoteAgentServerDetailsRepositoryImpl.java` (`@Primary`) + `stubs/RemoteAgentServerDetailsRepositoryStub.java`
- _(no methods)_

#### `io.sclera.Repository.RemoteDesktopSessionRepository`
Interface: `src/main/java/io/sclera/Repository/RemoteDesktopSessionRepository.java`
Bodies: `Repository/RemoteDesktopSessionRepositoryImpl.java` (`@Primary`) + `stubs/RemoteDesktopSessionRepositoryStub.java`
- `void deleteByDeviceId(String deviceId)` — log.warn

#### `io.sclera.Repository.ScheduledJobRepository`
Interface: `src/main/java/io/sclera/Repository/ScheduledJobRepository.java`
Bodies: `Repository/ScheduledJobRepositoryImpl.java` (`@Primary`) + `stubs/ScheduledJobRepositoryStub.java`
- `void deleteByConditionId(String conditionId)` — log.warn

#### `io.sclera.Repository.SpecificationsRepository`
Interface: `src/main/java/io/sclera/Repository/SpecificationsRepository.java`
Bodies: `Repository/SpecificationsRepositoryImpl.java` (`@Primary`) + `stubs/SpecificationsRepositoryStub.java`
- `void editDeviceSpecifications(String id, String keyValue, String keyUnit, String keyName)` — log.warn
- `Integer checkSpecificationByDeviceId(String deviceId, String keyName)` — log.warn + 0
- `List<SpecificationsDTO> getDeviceSpecificationsBasedOnDeviceId(String deviceId)` — log.warn + empty list
- `void upsertDeviceSpecification(String id, String keyName, String keyValue, String keyUnit, String deviceId)` — log.warn
- `void deleteById(String id)` — log.warn
- `SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String deviceId, String keyName)` — log.warn + null
- `SpecificationsDTO getPower(String deviceId, String keyName)` — log.warn + null

#### `io.sclera.Repository.VdmsRepository`
Interface: `src/main/java/io/sclera/Repository/VdmsRepository.java`
Body: `stubs/VdmsRepositoryStub.java`
- `String getCustomerOrgIdByVdmsId(String vdms_id)` — log.warn + null
- `VdmsDTO getSyncDetailsForADC()` — log.warn + null
- `String getVDMSId()` — log.warn + null
- `VdmsDTO getVdmsDetails()` — returns null
- `void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId)` — no-op
- `String getVDMSPassword()` — returns ""
- `Integer getIsMaster()` — returns 0

#### `io.sclera.Repository.VdmsconfigurationRepository`
Interface: `src/main/java/io/sclera/Repository/VdmsconfigurationRepository.java`
Bodies: `Repository/VdmsconfigurationRepositoryImpl.java` (`@Primary`) + `stubs/VdmsconfigurationRepositoryStub.java`
- `VdmsConfigurationDTO getConfiguration()` — log.warn + null

---

## Placeholder DTOs / clients

These are placeholder data containers tagged STUB but they are pure value objects / clients — included for completeness, no behaviour to extract.

### `io.sclera.integration.dto.LocationIntegrationDTO`
File: `src/main/java/io/sclera/integration/dto/LocationIntegrationDTO.java`
- Empty class (no fields, no methods) — replace when AP-C2 integration DTOs land.

### `io.sclera.integration.dto.ResponseDTO`
File: `src/main/java/io/sclera/integration/dto/ResponseDTO.java`
- Plain DTO with fields `data`, `success`, `message`, `status`, `timestamp` plus two constructors and getters/setters. STUB Javadoc marker only.

### `io.sclera.utils.StubLog`
File: `src/main/java/io/sclera/utils/StubLog.java`
- Helper used by every stub. Not itself a stub call site.

---

## Unclassified

None. Every stub maps to one of the buckets in `status-2026-05-13.md`.

---

## Surprising findings (vs. `status-2026-05-13.md`)

1. **`io.sclera.service.touchscreen.VdmsService`** — listed in status notes as a Bucket-D stub, but the file in the working tree is a **real, fully-implemented service** (~360 LOC, delegates to `VdmsRepository`, `VdmsDetailsRepository`, `APICallService`, etc.). Several methods are commented out (`getCurrentTimeZone`, `updateTimeZone`, `activateAgent`, `updatePropertyDetails`) but the bulk is live code. Excluded from this inventory.
2. **`io.sclera.service.UserService`** — listed under "CP-2 identity" stubs, but the file is a **real service** delegating to `UserRepository`/`APICallService`/`VdmsRepository`. The *repository* it depends on is stubbed (`UserRepositoryImpl` / `UserRepositoryStub` both return safe defaults), so `UserService` calls effectively short-circuit, but the service code itself is not a stub.
3. **`io.sclera.service.Product_DetailsService`** — listed under "AP-C8 inventory" stubs, but contains real logic (`deleteProductDetailsById` does file cleanup via `Utils`). Two methods are degenerate (`checkProductId` → 0, `upsertProductDetail` no-op, `addProductImages` no-op). Mixed — not a pure stub. Excluded from this inventory.
4. **`io.sclera.service.UserActionLogService`** — listed in status as audit stub, but the file in the working tree publishes real events via `VdmsClient.publishEvent` — it is a **real service**, not a stub. Excluded from this inventory.
5. **`io.sclera.service.ADCService`** (AP-C9) — listed in status notes, but **no file exists in the repo**. No call site has demanded it yet.
6. **`io.sclera.service.CustomerOrganisationService`** (top-level CP-2) — status notes mention it, but only `io.sclera.service.touchscreen.CustomerOrganisationService` exists. A separate top-level cloud-side service has not been created.
7. **`io.sclera.Repository.ConnectedDevicesRepository`** — created as a **concrete `@Component`+`@Repository` class** (not an interface+Impl pair). Listed in status as an empty stub repo but is now populated with 13 StubLog-bodied methods.
8. **No "empty" stubs remain** — every stub the original notes listed as empty has at least one method. The "empty" category in the summary table is zero across the board.
9. **`InterfaceRepository`** was listed in status as an empty stub repo but is now a **real `JpaRepository<Interface, String>`** — promoted out of stub status by a real entity binding.
10. **`Product_DetailsRepository`, `NotesRepository`, `MeasuringInstrumentRepository`, `ConditionsRepository`, `ClientBarCodeRepository`, `SystemInterfaceRepository`** appeared in status but in the working tree are real `JpaRepository` interfaces — also promoted out of stub status.
