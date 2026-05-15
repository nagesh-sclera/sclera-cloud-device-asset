package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.DisruptiveSensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class DisruptiveService {
    public String getDeviceIdByDisruptiveSensorId(String id) { return null; }
    public Integer getDisruptiveSensorCountByDeviceId(String deviceId) { return 0; }
    public Boolean getDisruptiveSensorAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<DisruptiveSensorDTO> getDeviceDisruptiveSensors(String a, String b, String c) { return Collections.emptySet(); }
    public List<ConditionsDTO> listDisruptiveDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }

    public void updateDisruptiveSensorDeviceId(String oldId, String newId, Set<String> ids) {}
}
