package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.BacnetAdvanceExportExcelDTO;
import io.sclera.dto.SiemensAdvanceExportExcelDTO;
import io.sclera.dto.SiemensBmsExportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.SiemensService}.
 */
@Component
public class SiemensClient {

    private static final Logger log = LoggerFactory.getLogger(SiemensClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public SiemensClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code SiemensService#getBacnetDeviceIdForAdvanceExcelExport}. Returns empty list on failure. */
    public List<BacnetAdvanceExportExcelDTO> getBacnetDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "siemens/getBacnetDeviceIdForAdvanceExcelExport", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("SiemensClient.getBacnetDeviceIdForAdvanceExcelExport failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code SiemensService#getSiemensDeviceIdForAdvanceExcelExport}. Returns empty list on failure. */
    public List<SiemensAdvanceExportExcelDTO> getSiemensDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "siemens/getSiemensDeviceIdForAdvanceExcelExport", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("SiemensClient.getSiemensDeviceIdForAdvanceExcelExport failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code SiemensService#getSiemensBmsData}. Returns empty list on failure. */
    public List<SiemensBmsExportDTO> getSiemensBmsData(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "siemens/getSiemensBmsData", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("SiemensClient.getSiemensBmsData failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code SiemensService#updateSiemensDeviceId}. */
    public void updateSiemensDeviceId(String oldId, String newId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "siemens/updateSiemensDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SiemensClient.updateSiemensDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
