#!/usr/bin/env python3
"""Fix all remaining compile errors by updating stub files."""
import os

BASE = r"C:\Users\KNageshNayak\Desktop\Work\Sclara-2.0\sclera-cloud-device-asset\src\main\java\io\sclera"
SVC = os.path.join(BASE, "service")

def write(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print(f"WROTE: {os.path.relpath(path)}")

# 1. AlertService - add sendDownloadEmail
write(os.path.join(SVC, "AlertService.java"), """\
package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigInteger;

/** STUB: replace with remote call to AP-C5 */
@Service
public class AlertService {
    public void sendDeviceConditionsAlertInfo(DeviceAlertDTO deviceAlert, AlertProfileDTO alertProfile, BigInteger timestamp) {}
    public void sendSensorAlertInfo(Object sensorAlert, AlertProfileDTO alertProfile, BigInteger timestamp) {}
    public void sendDownloadEmail(JSONObject body, MultipartFile file, String type, String vdmsId) {}
}
""")

# 2. AlertProfileService - add getAlertProfileById + getAlertProfileDetailsById
write(os.path.join(SVC, "AlertProfileService.java"), """\
package io.sclera.service;

import io.sclera.dto.AlertProfileDTO;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C5 */
@Service
public class AlertProfileService {
    public AlertProfileDTO getAlertProfileById(String alertProfileId) { return null; }
    public AlertProfileDTO getAlertProfileDetailsById(String a, String b, String deviceId) { return null; }
}
""")

# 3. ModbusService - add missing methods
write(os.path.join(SVC, "ModbusService.java"), """\
package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.ModbusRegisterDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class ModbusService {
    public Set<ModbusRegisterDTO> getDeviceModbusRegisters(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<SensorDTO> getModbusRegistersByDeviceId(String deviceId) { return Collections.emptyList(); }
    public List<ConditionsDTO> listModbusDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }
    public String getDeviceIdByModbusRegisterId(String modbusRegisterId) { return null; }
    public Boolean getModbusRegisterAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Integer getModbusRegistersCountByDeviceId(String deviceId) { return 0; }
    public void updateModbusRegisterDeviceId(String oldId, String newId, Set<String> ids) {}
}
""")

# 4. ConditionsAdvanceExportExcelDto - add schedule fields
dto_path = os.path.join(BASE, "dto", "ConditionsAdvanceExportExcelDto.java")
write(dto_path, """\
package io.sclera.dto;

/** STUB: belongs to a future microservice */
public class ConditionsAdvanceExportExcelDto {
    private String conditionId;
    private String conditionName;
    private String alertCondition;
    private String valueName;
    private String alertMessage;
    private String priority;
    private String alertProfileId;
    private String alertProfileName;
    private Integer ioc;
    private Boolean showAlert;
    private Boolean showAlertMessageAsValue;
    private Integer enableThresholdLineOnChart;
    private String colorOfThresholdLineOnChart;
    private Integer alertAfterTime;
    private String measuringInstrumentId;
    private Integer scheduleAlert;
    private String scheduleStartTime;
    private String scheduleEndTime;
    private String scheduleConditions;
    private Integer alertCountEnable;

    public String getConditionId() { return conditionId; }
    public void setConditionId(String v) { this.conditionId = v; }
    public String getConditionName() { return conditionName; }
    public void setConditionName(String v) { this.conditionName = v; }
    public String getAlertCondition() { return alertCondition; }
    public void setAlertCondition(String v) { this.alertCondition = v; }
    public String getValueName() { return valueName; }
    public void setValueName(String v) { this.valueName = v; }
    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String v) { this.alertMessage = v; }
    public String getPriority() { return priority; }
    public void setPriority(String v) { this.priority = v; }
    public String getAlertProfileId() { return alertProfileId; }
    public void setAlertProfileId(String v) { this.alertProfileId = v; }
    public String getAlertProfileName() { return alertProfileName; }
    public void setAlertProfileName(String v) { this.alertProfileName = v; }
    public Integer getIoc() { return ioc; }
    public void setIoc(Integer v) { this.ioc = v; }
    public Boolean getShowAlert() { return showAlert; }
    public void setShowAlert(Boolean v) { this.showAlert = v; }
    public Boolean getShowAlertMessageAsValue() { return showAlertMessageAsValue; }
    public void setShowAlertMessageAsValue(Boolean v) { this.showAlertMessageAsValue = v; }
    public Integer getEnableThresholdLineOnChart() { return enableThresholdLineOnChart; }
    public void setEnableThresholdLineOnChart(Integer v) { this.enableThresholdLineOnChart = v; }
    public String getColorOfThresholdLineOnChart() { return colorOfThresholdLineOnChart; }
    public void setColorOfThresholdLineOnChart(String v) { this.colorOfThresholdLineOnChart = v; }
    public Integer getAlertAfterTime() { return alertAfterTime; }
    public void setAlertAfterTime(Integer v) { this.alertAfterTime = v; }
    public String getMeasuringInstrumentId() { return measuringInstrumentId; }
    public void setMeasuringInstrumentId(String v) { this.measuringInstrumentId = v; }
    public Integer getScheduleAlert() { return scheduleAlert; }
    public void setScheduleAlert(Integer v) { this.scheduleAlert = v; }
    public String getScheduleStartTime() { return scheduleStartTime; }
    public void setScheduleStartTime(String v) { this.scheduleStartTime = v; }
    public String getScheduleEndTime() { return scheduleEndTime; }
    public void setScheduleEndTime(String v) { this.scheduleEndTime = v; }
    public String getScheduleConditions() { return scheduleConditions; }
    public void setScheduleConditions(String v) { this.scheduleConditions = v; }
    public Integer getAlertCountEnable() { return alertCountEnable; }
    public void setAlertCountEnable(Integer v) { this.alertCountEnable = v; }
}
""")

# 5. SiemensBmsExportDTO - add alertProfileName
write(os.path.join(BASE, "dto", "SiemensBmsExportDTO.java"), """\
package io.sclera.dto;

/** STUB: belongs to a future microservice */
public class SiemensBmsExportDTO {
    private String assetId;
    private String assetName;
    private String assetConditionId;
    private String alertCondition;
    private String priority;
    private String alertProfileName;

    public String getAssetId() { return assetId; }
    public void setAssetId(String v) { this.assetId = v; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String v) { this.assetName = v; }
    public String getAssetConditionId() { return assetConditionId; }
    public void setAssetConditionId(String v) { this.assetConditionId = v; }
    public String getAlertCondition() { return alertCondition; }
    public void setAlertCondition(String v) { this.alertCondition = v; }
    public String getPriority() { return priority; }
    public void setPriority(String v) { this.priority = v; }
    public String getAlertProfileName() { return alertProfileName; }
    public void setAlertProfileName(String v) { this.alertProfileName = v; }
}
""")

# 6. SiemensService - add updateSiemensDeviceId
siemens_path = os.path.join(SVC, "SiemensService.java")
with open(siemens_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateSiemensDeviceId' not in content:
    content = content.rstrip().rstrip('}') + "\n    public void updateSiemensDeviceId(String oldId, String newId) {}\n}\n"
    write(siemens_path, content)

# 7. GlobalChecklistService
write(os.path.join(SVC, "GlobalChecklistService.java"), """\
package io.sclera.service;

import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C4 */
@Service
public class GlobalChecklistService {
    public void updateDeviceGlobalChecklistDeviceId(String oldId, String newId) {}
    public void deleteGlobalChecklistByDeviceId(String deviceId) {}
}
""")

# 8. RecordChecklistService - add updateRecordChecklistByDeviceId + getAllRecordChecklistByBuildings
write(os.path.join(SVC, "RecordChecklistService.java"), """\
package io.sclera.service;

import io.sclera.dto.RecordChecklistDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C4 */
@Service
public class RecordChecklistService {

    private static final Logger log = LoggerFactory.getLogger(RecordChecklistService.class);

    public void updateRecordChecklistDeviceAndIsRemoved(Set<String> ids) { log.warn("STUB: updateRecordChecklistDeviceAndIsRemoved called"); }
    public void deleteRecordChecklistInBatch(List<String> ids) { log.warn("STUB: deleteRecordChecklistInBatch called"); }
    public List<String> deleteAllRecordChecklistByDeviceId(String deviceId) { log.warn("STUB: deleteAllRecordChecklistByDeviceId called"); return Collections.emptyList(); }
    public void deleteAllRecordChecklistImagesByUrls(List<String> urls) { log.warn("STUB: deleteAllRecordChecklistImagesByUrls called"); }
    public String getRecordChecklistStatusByDeviceId(String deviceId, String x) { log.warn("STUB: getRecordChecklistStatusByDeviceId called"); return null; }
    public Integer getChecklistStatusCountDeviceId(String a, String b, String c) { log.warn("STUB: getChecklistStatusCountDeviceId called"); return 0; }
    public void updateRecordChecklistByDeviceId(String oldId, String newId, Set<String> ids) {}
    public List<RecordChecklistDTO> getAllRecordChecklistByBuildings(List<String> buildingIds, List<String> floorIds, List<String> locationIds) { return Collections.emptyList(); }
}
""")

# 9. BacnetService - add updateBacnetObjectDeviceId
bacnet_path = os.path.join(SVC, "BacnetService.java")
with open(bacnet_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateBacnetObjectDeviceId' not in content:
    content = content.rstrip().rstrip('}') + "\n    public void updateBacnetObjectDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    # Make sure Set is imported
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    write(bacnet_path, content)

# 10. DaintreeService
daintree_path = os.path.join(SVC, "DaintreeService.java")
with open(daintree_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateDaintreeDeviceByDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updateDaintreeDeviceByDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(daintree_path, content)

# 11. DisruptiveService
disruptive_path = os.path.join(SVC, "DisruptiveService.java")
with open(disruptive_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateDisruptiveSensorDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updateDisruptiveSensorDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(disruptive_path, content)

# 12. EcobeeService
ecobee_path = os.path.join(SVC, "EcobeeService.java")
with open(ecobee_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateEcobeeSensorDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updateEcobeeSensorDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(ecobee_path, content)

# 13. KNXService
knx_path = os.path.join(SVC, "KNXService.java")
with open(knx_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateKnxGroupDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updateKnxGroupDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(knx_path, content)

# 14. LorawanService
lorawan_path = os.path.join(SVC, "LorawanService.java")
with open(lorawan_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateLorawanSensorDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updateLorawanSensorDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(lorawan_path, content)

# 15. MonnitService
monnit_path = os.path.join(SVC, "MonnitService.java")
with open(monnit_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updateMonnitSensorDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updateMonnitSensorDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(monnit_path, content)

# 16. PelicanService
pelican_path = os.path.join(SVC, "PelicanService.java")
with open(pelican_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'updatePelicanSensorDeviceId' not in content:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n    public void updatePelicanSensorDeviceId(String oldId, String newId, Set<String> ids) {}\n}\n"
    write(pelican_path, content)

# 17. SnmpService
snmp_path = os.path.join(SVC, "SnmpService.java")
with open(snmp_path, 'r', encoding='utf-8') as f:
    content = f.read()
additions = []
if 'updateSnmpObjectDeviceId' not in content:
    additions.append("    public void updateSnmpObjectDeviceId(String oldId, String newId, Set<String> ids) {}")
if 'getAllNetworkSnmpDeviceData' not in content:
    additions.append("    public Object getAllNetworkSnmpDeviceData(String deviceId) { return null; }")
if additions:
    if 'import java.util.Set;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Set;')
    content = content.rstrip().rstrip('}') + "\n" + "\n".join(additions) + "\n}\n"
    write(snmp_path, content)

# 18. PolyLensService
write(os.path.join(SVC, "PolyLensService.java"), """\
package io.sclera.service;

import io.sclera.dto.PolyLensDeviceDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class PolyLensService {
    public Set<PolyLensDeviceDTO> getAllPolyLensDevices(String a, String b, String c, String d) { return Collections.emptySet(); }
    public void updatePolyLensDeviceId(String oldId, String newId, Set<String> ids) {}
    public Integer getPolyLensDeviceCountByDeviceId(String deviceId) { return 0; }
}
""")

# 19. MqttService
write(os.path.join(SVC, "MqttService.java"), """\
package io.sclera.service;

import io.sclera.dto.MqttDeviceDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class MqttService {
    public Set<MqttDeviceDTO> getAllMqttDevices(String a, String b, String c, String d) { return Collections.emptySet(); }
    public Integer getMqttDeviceCountByDeviceId(String deviceId) { return 0; }
}
""")

# 20. HistoryService
write(os.path.join(SVC, "HistoryService.java"), """\
package io.sclera.service;

import io.sclera.dto.HistoryDTO;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C6 */
@Service
public class HistoryService {
    public void insertDeviceStatusHistory(Integer alarm, String ipAddress, Object o, Object o1, String finalDeviceId) {}
    public void addHistory(HistoryDTO historyDTO) {}
    public void addHistoryWithTimestamp(HistoryDTO historyDTO) {}
    public void updateHistoryDeviceId(String oldId, String newId) {}
}
""")

# 21. UserActionLogDTO - add action field
write(os.path.join(SVC, "UserActionLogDTO.java"), """\
package io.sclera.service;

import java.math.BigInteger;

/** STUB: UserActionLogDTO for compile compatibility */
public class UserActionLogDTO {

    private String type;
    private String sub_type;
    private String status;
    private String primary_id;
    private String email;
    private String secondary_id;
    private String table_name;
    private BigInteger created_timestamp;
    private String message;
    private String action;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSub_type() { return sub_type; }
    public void setSub_type(String sub_type) { this.sub_type = sub_type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrimary_id() { return primary_id; }
    public void setPrimary_id(String primary_id) { this.primary_id = primary_id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSecondary_id() { return secondary_id; }
    public void setSecondary_id(String secondary_id) { this.secondary_id = secondary_id; }
    public String getTable_name() { return table_name; }
    public void setTable_name(String table_name) { this.table_name = table_name; }
    public BigInteger getCreated_timestamp() { return created_timestamp; }
    public void setCreated_timestamp(BigInteger created_timestamp) { this.created_timestamp = created_timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
""")

# 22. IOCService - add sendDigitalTwinData
write(os.path.join(SVC, "IOCService.java"), """\
package io.sclera.service;

import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import io.sclera.dto.DeviceConditionsDTO;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class IOCService {
    public void sendDeviceAlertDataIOC(DeviceConditionsDTO deviceConditionsDTO, DeviceAlertDTO deviceAlert, Integer status, AlertProfileDTO alertProfile, BigInteger timestamp) {}
    public void sendDigitalTwinData(Set<String> deviceIds) {}
}
""")

# 23. UserActionLogService
write(os.path.join(SVC, "UserActionLogService.java"), """\
package io.sclera.service;

import org.springframework.stereotype.Service;
import java.util.List;

/** STUB: replace with remote call to AP-C6 */
@Service
public class UserActionLogService {
    public void addUserAction(String username, String asset, String update, String s, String success, String assetInfo, String id) {}
    public void batchUpdateUserActionLogs(List<UserActionLogDTO> logs) {}
}
""")

# 24. WebClientService
write(os.path.join(SVC, "WebClientService.java"), """\
package io.sclera.service;

import io.sclera.auth.dto.TenantDTO;
import io.sclera.dto.FloorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class WebClientService {

    private static final Logger log = LoggerFactory.getLogger(WebClientService.class);

    public byte[] getImageBytesByUrl(String link) { return new byte[1024]; }
    public TenantDTO getAllTenants(String issuer) { log.warn("STUB: getAllTenants called with issuer={}", issuer); return null; }
    public void multiEditDigitalTwin(String vdmsId, MultipartFile file, String username, Set<String> deviceIds) {}
    public String deleteFloorMapsByImageUrl(String vdmsId, List<FloorDTO> floors) { return null; }
}
""")

# 25. AssetMapperService
write(os.path.join(BASE, "service", "touchscreen", "assetmapper", "AssetMapperService.java"), """\
package io.sclera.service.touchscreen.assetmapper;

import io.sclera.dto.DeviceTypesDTO;
import org.springframework.stereotype.Service;
import java.util.List;

/** STUB: replace with remote call to AP-C2 sensor mapper */
@Service
public class AssetMapperService {
    public void updateDeviceTypeForAllAsset(List<DeviceTypesDTO> deviceTypes) {}
}
""")

# 26. SyslogService
write(os.path.join(SVC, "SyslogService.java"), """\
package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to future microservice */
@Service
public class SyslogService {
    public JSONObject getSyslogExcludeDeviceIds(String a, String b, String c, String d) { return new JSONObject(); }
}
""")

# 27. SocketService - add socketDeviceUpdate + updateDeviceInterfaceStatus
write(os.path.join(BASE, "sockets", "SocketService.java"), """\
package io.sclera.sockets;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InterfaceDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Set;

/** STUB: edge-only websocket broadcast */
@Service
public class SocketService {

    private static final Logger log = LoggerFactory.getLogger(SocketService.class);

    public void socketDeviceCount() {}
    public void sockerDeviceCountByDocker(String dockername, String assignee) {}
    public void socketAiCallLogHistoryUpdate(String id) { log.warn("STUB: socketAiCallLogHistoryUpdate called with id={}", id); }
    public void socketAiCallLogOngoingHistoryUpdate(String id) { log.warn("STUB: socketAiCallLogOngoingHistoryUpdate called with id={}", id); }
    public void socketDeviceStatus(DeviceMonitorDTO dto) { log.warn("STUB: socketDeviceStatus"); }
    public void socketOnlineDevice(String deviceId) { log.warn("STUB: socketOnlineDevice"); }
    public void socketOfflineDevice(String deviceId) { log.warn("STUB: socketOfflineDevice"); }
    public void socketDeviceUpdate(Set<DeviceDTO> devices) {}
    public void updateDeviceInterfaceStatus(InterfaceDTO dto, String a, String b) {}
}
""")

# 28. QrCodeRepository
write(os.path.join(BASE, "Repository", "QrCodeRepository.java"), """\
package io.sclera.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
public interface QrCodeRepository {
    Integer countByDeviceId(String deviceId);
}
""")

# 29. ClientQrCodeRepository
write(os.path.join(BASE, "Repository", "ClientQrCodeRepository.java"), """\
package io.sclera.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
public interface ClientQrCodeRepository {
    Integer countByDeviceId(String deviceId);
}
""")

# 30. VdmsRepository - add getSyncDetailsForADC
write(os.path.join(BASE, "Repository", "VdmsRepository.java"), """\
package io.sclera.Repository;

import io.sclera.dto.VdmsDTO;
import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface VdmsRepository {
    String getCustomerOrgIdByVdmsId(String vdms_id);
    VdmsDTO getSyncDetailsForADC();
}
""")

# 31. APICallService - fix return types
write(os.path.join(SVC, "APICallService.java"), """\
package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.BuildingDTO;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.dto.DeviceTypesDTO;
import io.sclera.dto.ProductDTO;
import io.sclera.dto.touchscreen.SnmpValuesDTO;
import io.sclera.dto.touchscreen.settings.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class APICallService {

    private static final Logger log = LoggerFactory.getLogger(APICallService.class);

    public List<UserDTO> getUsersByOrgId(String organisation_id, String vdms_id) { return Collections.emptyList(); }
    public List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id) { return Collections.emptyList(); }

    public Flux<org.json.JSONObject> sendDescription(org.json.JSONObject requestBody, String vdmsId, String technicianId,
                                                     String technicianName, String contactNumber,
                                                     String formattedDateTime, String aiCallLogId) {
        return Flux.empty();
    }

    public ResponseEntity<String> sendCallFlowMail(JSONObject payload) { return ResponseEntity.ok(""); }
    public ResponseEntity<String> sendCallFlowMessage(JSONObject payload) { return ResponseEntity.ok(""); }

    public String getDeviceHostNameByIP(String a, String b) { return null; }
    public ProductDTO getProductDetailsByModelAndMBV(String model, String mbv) { return null; }
    public ProductDTO getProductDetailsByProductId(String productId) { return null; }

    public JSONArray getNfcIdsByVdmsAndType(String vdmsId, String type) { return new JSONArray(); }
    public JSONArray getQrCodeIdsByVdmsIdAndType(String vdmsId, String type) { return new JSONArray(); }
    public void deleteDigitalTwinImageUrl(Set<String> imageUrls, String username, String vdmsId) {}

    public JSONArray getTemporaryProductByIds(Set<String> ids) { return new JSONArray(); }
    public void deleteTemporaryProductByIds(Set<String> ids) {}

    public Boolean syncBuildingToADC(Object dto, String orgId, String configId) { return Boolean.FALSE; }
    public BuildingDTO addSingleBuildingObject(String locationId, String vdmsId) { return null; }
    public Boolean deleteBuildingFromADC(String orgId, String configId, List<String> propertyIds) { return Boolean.FALSE; }
    public List<BuildingDTO> getAllLocations(String vdmsId) { return Collections.emptyList(); }
    public String getFloorPathByFloorId(Object a, String vdmsId, String buildingId, String floorId) { return null; }

    public void generateChatbotMessage(String query, Object emitter) {}
    public void updateChatbotDeviceData(JSONArray bodyArray) {}

    public Set<ClientBarCodeDTO> getAllClientBarCodeByVdmsId(String vdmsId, int page, int size) { return Collections.emptySet(); }
    public Set<ClientBarCodeDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int page, int size) { return Collections.emptySet(); }

    public void sendAgentDataToInventory(Object obj) {}
    public Set<DeviceTypesDTO> getUpdatedAssetTypes(Object ts, int page, int size, String search, String vdmsId) { return Collections.emptySet(); }

    public Object updatePropertyDetails(String vdmsId) { return null; }
    public void updateVdmsDetailCloud(String vdmsId, Object dto) {}
    public Object getVendorByMacAddress(String mac) { return null; }
    public void syncAllAttribute(String ip) {}
    public void syncBacnet(String ip) {}
    public void syncSnmpWalk(String ip) {}
    public void snmpInterface(String ip) {}
    public void snmpTopology(String ip) {}
    public void internetConnectivity(String ip) {}
    public List<Object> getAllVendorsByOrganisationId(String orgId, String vdmsId, String dockerName) { return Collections.emptyList(); }
    public Object getTransferVendor(String vdmsId, String dockerName) { return null; }
    public String getCustomerOrgIdByVdmsId(String vdmsId) { return null; }
    public void updateVdmsTranfer(String vdmsId) {}
    public Object updateVdmsStatus(String vdmsId) { return null; }
    public void updateQrCodeSyncByVdmsId(String vdmsId) {}
    public void updateNfcSyncByVdmsId(String vdmsId) {}
    public JSONArray getJSONArrayFromJSONString(String json, Class<String> clazz) { return new JSONArray(); }
    public void syncSnmpInterfacebyDeviceId(String deviceId, SnmpValuesDTO dto) {}
}
""")

# 32. MyDevicesCompanyDTO
write(os.path.join(BASE, "dto", "MyDevicesCompanyDTO.java"), """\
package io.sclera.dto;

/** STUB: AP-C2 sensor / non-AP-C1 DTO */
public class MyDevicesCompanyDTO {
    private String id;
    private String company_name;
    private String location_name;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompany_name() { return company_name; }
    public void setCompany_name(String v) { this.company_name = v; }
    public String getLocation_name() { return location_name; }
    public void setLocation_name(String v) { this.location_name = v; }
}
""")

# 33. ShareConditionsDTO
write(os.path.join(BASE, "dto", "ShareConditionsDTO.java"), """\
package io.sclera.dto;

import java.util.Collections;
import java.util.List;

public class ShareConditionsDTO {
    private List<String> devices;
    private String condition_method;
    private List<DeviceConditionsDTO> deviceConditions;

    public List<String> getDevices() { return devices != null ? devices : Collections.emptyList(); }
    public void setDevices(List<String> v) { this.devices = v; }
    public String getCondition_method() { return condition_method; }
    public void setCondition_method(String v) { this.condition_method = v; }
    public List<DeviceConditionsDTO> getDeviceConditions() { return deviceConditions != null ? deviceConditions : Collections.emptyList(); }
    public void setDeviceConditions(List<DeviceConditionsDTO> v) { this.deviceConditions = v; }
}
""")

# 34. RecordChecklistDTO
write(os.path.join(BASE, "dto", "RecordChecklistDTO.java"), """\
package io.sclera.dto;

public class RecordChecklistDTO {
    private String building_id;
    private String record_type;
    private String inspection_record_id;

    public String getBuilding_id() { return building_id; }
    public void setBuilding_id(String v) { this.building_id = v; }
    public String getRecord_type() { return record_type; }
    public void setRecord_type(String v) { this.record_type = v; }
    public String getInspection_record_id() { return inspection_record_id; }
    public void setInspection_record_id(String v) { this.inspection_record_id = v; }
}
""")

# 35. InterfaceService - full stub replacement
write(os.path.join(SVC, "InterfaceService.java"), """\
package io.sclera.service;

import io.sclera.dto.InterfaceDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/** STUB: replace with remote call to AP-C3 */
@Service
public class InterfaceService {
    public Integer getInterfaceCountByDevice(String deviceId) { return 0; }
    public List<InterfaceDTO> listDeviceSnmpInterfaceByDeviceId(String a, String b, String c, String d) { return Collections.emptyList(); }
    public void deleteInterfaceByDeviceId(String deviceId) {}
    public void updateInterfaceDeviceId(String oldId, String newId) {}
}
""")

# 36. MyDevicesService - remove RabbitmqService references
myd_path = os.path.join(SVC, "MyDevicesService.java")
with open(myd_path, 'r', encoding='utf-8') as f:
    content = f.read()
# Remove rabbitmq import
content = content.replace("import io.sclera.rabbitmq.RabbitmqService;\n", "")
# Remove @Autowired RabbitmqService block
import re
content = re.sub(r'\s*@Autowired\s*\n\s*RabbitmqService rabbitmqService;\s*\n', '\n', content)
write(myd_path, content)

# 37. Product_DetailsService - add autowired field
prod_path = os.path.join(SVC, "Product_DetailsService.java")
with open(prod_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'product_detailsRepository' not in content or '@Autowired' not in content or 'Product_DetailsRepository' not in [line.strip() for line in content.split('\n') if 'product_detailsRepository' in line and '@Autowired' in content[:content.find(line)] if '@Autowired' in content]:
    # Add @Autowired field after class declaration if not present
    if '@Autowired\n    Product_DetailsRepository product_detailsRepository' not in content:
        content = content.replace(
            '@Autowired\n    DeviceService deviceService;',
            '@Autowired\n    Product_DetailsRepository product_detailsRepository;\n\n    @Autowired\n    DeviceService deviceService;'
        )
        write(prod_path, content)

# 38. Product_DetailsRepository - add getProductsImageUrlById
write(os.path.join(BASE, "Repository", "Product_DetailsRepository.java"), """\
package io.sclera.Repository;

import io.sclera.dto.ProductDTO;
import io.sclera.models.Product_Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_DetailsRepository extends JpaRepository<Product_Details, String> {
    ProductDTO getProductsImageUrlById(String productId);
}
""")

print("\\nAll stub fixes applied successfully.")
