package io.sclera.service;

import io.sclera.dto.BacnetAdvanceExportExcelDTO;
import io.sclera.dto.SiemensAdvanceExportExcelDTO;
import io.sclera.dto.SiemensBmsExportDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/** STUB: replace with remote call to AP-C2 */
@Service
public class SiemensService {
    public List<BacnetAdvanceExportExcelDTO> getBacnetDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId) { return Collections.emptyList(); }
    public List<SiemensAdvanceExportExcelDTO> getSiemensDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId) { return Collections.emptyList(); }
    public List<SiemensBmsExportDTO> getSiemensBmsData(String username, String vdmsId, String deviceId) { return Collections.emptyList(); }

    public void updateSiemensDeviceId(String oldId, String newId) {}
}
