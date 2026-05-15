package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.KNXGroupDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class KNXService {
    public String getDeviceIdByKNXGroupAddress(String a, String b) { return null; }
    public Integer getKNXGroupCountByDeviceId(String deviceId) { return 0; }
    public Boolean getKNXGroupAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<KNXGroupDTO> getDeviceKNXGroups(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<SensorDTO> getKNXGroupsByDeviceAddress(String addr) { return Collections.emptyList(); }
    public List<ConditionsDTO> listKNXDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }

    public void updateKnxGroupDeviceId(String oldId, String newId, Set<String> ids) {}
}
