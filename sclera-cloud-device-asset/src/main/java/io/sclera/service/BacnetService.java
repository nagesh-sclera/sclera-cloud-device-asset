package io.sclera.service;

import io.sclera.dto.BacnetAdvanceExportExcelDTO;
import io.sclera.dto.BacnetObjectDTO;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class BacnetService {
    public String getDeviceIdByBacnetObjectId(String bacnetDeviceId, String bacnetObjectId) { return null; }
    public Integer getBacnetObjectCountByDeviceId(String deviceId) { return 0; }
    public Boolean getBacnetObjectAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Set<BacnetObjectDTO> getDeviceBacnetObjects(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<SensorDTO> getBacnetObjectsByDeviceId(String deviceId) { return Collections.emptyList(); }
    public List<ConditionsDTO> listBacnetDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }
    public List<BacnetAdvanceExportExcelDTO> getBacnetDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId) { return Collections.emptyList(); }

    public void updateBacnetObjectDeviceId(String oldId, String newId, Set<String> ids) {}
}
