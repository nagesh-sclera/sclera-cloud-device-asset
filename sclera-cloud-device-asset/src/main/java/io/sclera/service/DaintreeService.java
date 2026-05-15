package io.sclera.service;

import io.sclera.dto.DaintreeConfigurationDTO;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.DaintreeDeviceDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class DaintreeService {
    public String getDeviceIdByDaintreeDeviceId(String id) { return null; }
    public Integer getDeviceDaintreeDevicesCountByDeviceId(String deviceId) { return 0; }
    public Boolean getDaintreeAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<DaintreeDeviceDTO> getDaintreeDevicesByDeviceId(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<ConditionsDTO> listDaintreeDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }
    public void updateDaintreeDeviceByDeviceId(String oldId, String newId, Set<String> ids) {}

    public List<DaintreeConfigurationDTO> getDaintreeConfigurations(String vdmsId) {
        StubLog.warn("DaintreeService", "getDaintreeConfigurations", "AP-C2");
        return Collections.emptyList();
    }
}
