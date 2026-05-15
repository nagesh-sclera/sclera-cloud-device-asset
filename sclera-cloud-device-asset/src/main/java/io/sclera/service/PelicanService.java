package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.PelicanSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class PelicanService {
    public String getDeviceIdByPelicanSensorId(String id) { return null; }
    public Integer getPelicanSensorCountByDeviceId(String deviceId) { return 0; }
    public Boolean getPelicanSensorAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<PelicanSensorDTO> getDevicePelicanSensors(String a, String b, String c) { return Collections.emptySet(); }
    public List<SensorDTO> getPelicanSensorsByDeviceId(String deviceId) { return Collections.emptyList(); }
    public List<ConditionsDTO> listpelicanDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }

    public void updatePelicanSensorDeviceId(String oldId, String newId, Set<String> ids) {}
}
