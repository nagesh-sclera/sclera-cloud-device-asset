package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.LorawanSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class LorawanService {
    public String getDeviceIdByLorawanSensorId(String id) { return null; }
    public Integer getLorawanSensorCountByDeviceId(String deviceId) { return 0; }
    public Boolean getLorawanSensorAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<LorawanSensorDTO> getDeviceLorawanSensors(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<SensorDTO> getLorawanSensorsByDeviceId(String deviceId) { return Collections.emptyList(); }
    public List<ConditionsDTO> listLorawanDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }

    public void updateLorawanSensorDeviceId(String oldId, String newId, Set<String> ids) {}
}
