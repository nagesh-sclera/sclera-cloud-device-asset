package io.sclera.service;

import io.sclera.dto.SnmpObjectDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class SnmpService {
    public Boolean getSnmpDeviceAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public String getDeviceIdBySnmpDeviceId(String snmpDeviceId) { return null; }
    public Integer getSnmpDeviceCountByDeviceAndSnmpConfiguration(String deviceId) { return 0; }
    public Integer getSnmpObjectCountByDeviceId(String deviceId) { return 0; }
    public String getDeviceIdBySnmpObjectId(String a, String b) { return null; }
    public Boolean getSnmpObjectAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<SnmpObjectDTO> getDeviceSnmpObjects(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<SensorDTO> getSnmpDevicesByDeviceId(String deviceId) { return Collections.emptyList(); }
    public void deleteGlobalSnmpByDeviceId(String deviceId) {}
    public void upsertGlobalSnmpByDeviceId(Set<io.sclera.dto.Product_SnmpDTO> snmpSet, String deviceId) {}

    public void updateSnmpObjectDeviceId(String oldId, String newId, Set<String> ids) {}
    public Object getAllNetworkSnmpDeviceData(String deviceId) { return null; }
}
