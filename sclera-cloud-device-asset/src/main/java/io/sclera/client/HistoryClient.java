package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.HistoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-audit microservice (AP-C6).
 *
 * Replaces the no-op {@code io.sclera.service.HistoryService} stub.
 * All methods are void: they invoke the remote endpoint and swallow any
 * exception (sidecar-down, network error) with a WARN log so that AP-C1
 * call sites are never interrupted by audit failures.
 */
@Component
public class HistoryClient {

    private static final Logger log = LoggerFactory.getLogger(HistoryClient.class);
    private static final String APP_ID = "sclera-audit";

    private final DaprClient dapr;

    public HistoryClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code HistoryService#insertDeviceStatusHistory}.
     * Maps to POST sclera-audit/history/device-status.
     */
    public void insertDeviceStatusHistory(Integer alarm, String ipAddress,
                                          Object extra1, Object extra2,
                                          String finalDeviceId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("alarm", alarm);
        payload.put("ipAddress", ipAddress);
        payload.put("extra1", extra1);
        payload.put("extra2", extra2);
        payload.put("deviceId", finalDeviceId);
        try {
            dapr.invokeMethod(APP_ID, "history/device-status", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("HistoryClient.insertDeviceStatusHistory failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryService#addHistory}.
     * Maps to POST sclera-audit/history/add.
     */
    public void addHistory(HistoryDTO historyDTO) {
        try {
            dapr.invokeMethod(APP_ID, "history/add", historyDTO, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("HistoryClient.addHistory failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryService#addHistoryWithTimestamp}.
     * Maps to POST sclera-audit/history/add-with-timestamp.
     */
    public void addHistoryWithTimestamp(HistoryDTO historyDTO) {
        try {
            dapr.invokeMethod(APP_ID, "history/add-with-timestamp", historyDTO, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("HistoryClient.addHistoryWithTimestamp failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryService#updateHistoryDeviceId}.
     * Maps to POST sclera-audit/history/update-device-id.
     */
    public void updateHistoryDeviceId(String oldId, String newId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "history/update-device-id", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("HistoryClient.updateHistoryDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
