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
 *
 * Paths and verbs are aligned with HistoryController (GET-only camelCase
 * skeleton endpoints). Params are passed as query-params via Dapr's HTTP
 * wrapper. NOTE: methods that originally took a DTO body (addHistory,
 * addHistoryWithTimestamp) lose the body under GET routing — this is a
 * known PoC limitation; real POST routing is a Wave-2 prerequisite.
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
     * Maps to GET sclera-audit/history/insertDeviceStatusHistory.
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
            dapr.invokeMethod(APP_ID, "history/insertDeviceStatusHistory", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("HistoryClient.insertDeviceStatusHistory failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryService#addHistory}.
     * Maps to GET sclera-audit/history/addHistory.
     * NOTE: DTO body is lost under GET-only skeleton routing (Wave-2: needs POST).
     */
    public void addHistory(HistoryDTO historyDTO) {
        try {
            dapr.invokeMethod(APP_ID, "history/addHistory", historyDTO, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("HistoryClient.addHistory failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryService#addHistoryWithTimestamp}.
     * Maps to GET sclera-audit/history/addHistoryWithTimestamp.
     * NOTE: DTO body is lost under GET-only skeleton routing (Wave-2: needs POST).
     */
    public void addHistoryWithTimestamp(HistoryDTO historyDTO) {
        try {
            dapr.invokeMethod(APP_ID, "history/addHistoryWithTimestamp", historyDTO, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("HistoryClient.addHistoryWithTimestamp failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryRepository#deleteByDeviceId}.
     * Maps to GET sclera-audit/history/deleteByDeviceId.
     */
    public void deleteByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "history/deleteByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("HistoryClient.deleteByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code HistoryService#updateHistoryDeviceId}.
     * Maps to GET sclera-audit/history/updateHistoryDeviceId.
     */
    public void updateHistoryDeviceId(String oldId, String newId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "history/updateHistoryDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("HistoryClient.updateHistoryDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
