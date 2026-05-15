package io.sclera.service;

import io.sclera.dto.EcobeeSensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class EcobeeService {
    public Set<EcobeeSensorDTO> getEcobeeDevicesByDeviceId(String a, String b, String c, String d) { return Collections.emptySet(); }
    public Integer getEcobeeSensorCountByDeviceId(String deviceId) { return 0; }
    public Boolean getEcobeeSensorAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public String getDeviceIdByEcobeeSensorId(String id) { return null; }

    public void updateEcobeeSensorDeviceId(String oldId, String newId, Set<String> ids) {}
}
