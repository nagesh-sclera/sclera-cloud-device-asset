package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MonnitSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class MonnitService {
    public String getDeviceIdByMonnitSensorId(String id) { return null; }
    public Integer getMonnitCountByDeviceId(String deviceId) { return 0; }
    public Boolean getMonnitAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<MonnitSensorDTO> getDeviceMonnitSensors(String a, String b, String c) { return Collections.emptySet(); }
    public List<SensorDTO> getMonnitSensorsByDeviceId(String deviceId) { return Collections.emptyList(); }
    public List<ConditionsDTO> listmonnitDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }

    public void updateMonnitSensorDeviceId(String oldId, String newId, Set<String> ids) {}
}
