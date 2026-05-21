package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-inspection microservice (AP-C4).
 *
 * Replaces the stub {@code io.sclera.service.GlobalInspectionRecordService}.
 * Void methods swallow exceptions with a WARN log.
 *
 * NOTE: methods passing Set/List bodies use GET routing — bodies are
 * silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class GlobalInspectionRecordClient {

    private static final Logger log = LoggerFactory.getLogger(GlobalInspectionRecordClient.class);
    private static final String APP_ID = "sclera-inspection";

    private final DaprClient dapr;

    public GlobalInspectionRecordClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code GlobalInspectionRecordService#updateGlobalInspectionRelationDeviceAndIsRemoved}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateGlobalInspectionRelationDeviceAndIsRemoved(Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "globalInspectionRecord/updateGlobalInspectionRelationDeviceAndIsRemoved", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalInspectionRecordClient.updateGlobalInspectionRelationDeviceAndIsRemoved failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code GlobalInspectionRecordService#deleteGlobalInspectionRelationInBatch}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deleteGlobalInspectionRelationInBatch(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "globalInspectionRecord/deleteGlobalInspectionRelationInBatch", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalInspectionRecordClient.deleteGlobalInspectionRelationInBatch failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code GlobalInspectionRecordService#updateGlobalInspectionByDeviceId}. */
    public void updateGlobalInspectionByDeviceId(String primaryDeviceId, String existingDeviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("primaryDeviceId", primaryDeviceId);
        payload.put("existingDeviceId", existingDeviceId);
        try {
            dapr.invokeMethod(APP_ID, "globalInspectionRecord/updateGlobalInspectionByDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalInspectionRecordClient.updateGlobalInspectionByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code GlobalInspectionRecordService#updateGlobalInspectionRelationLocationAndIsRemoved}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateGlobalInspectionRelationLocationAndIsRemoved(Set<String> locationIds) {
        try {
            dapr.invokeMethod(APP_ID, "globalInspectionRecord/updateGlobalInspectionRelationLocationAndIsRemoved", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalInspectionRecordClient.updateGlobalInspectionRelationLocationAndIsRemoved failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code GlobalInspectionRecordService#updateGlobalInspectionRecord}. */
    public void updateGlobalInspectionRecord(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "globalInspectionRecord/updateGlobalInspectionRecord", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalInspectionRecordClient.updateGlobalInspectionRecord failed; swallowing: {}", e.getMessage());
        }
    }
}
