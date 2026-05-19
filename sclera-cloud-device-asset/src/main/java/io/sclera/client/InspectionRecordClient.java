package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-inspection microservice (AP-C4).
 *
 * Replaces the stub {@code io.sclera.service.InspectionRecordService}.
 * Void methods swallow exceptions with a WARN log.
 *
 * NOTE: methods passing Set bodies use GET routing — bodies are
 * silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class InspectionRecordClient {

    private static final Logger log = LoggerFactory.getLogger(InspectionRecordClient.class);
    private static final String APP_ID = "sclera-inspection";

    private final DaprClient dapr;

    public InspectionRecordClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code InspectionRecordService#updateInspectionRecordStatus}. */
    public void updateInspectionRecordStatus(String a, String b, String id, boolean status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", a);
        payload.put("b", b);
        payload.put("id", id);
        payload.put("status", status);
        try {
            dapr.invokeMethod(APP_ID, "inspectionRecord/updateInspectionRecordStatus", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("InspectionRecordClient.updateInspectionRecordStatus failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code InspectionRecordService#updateInspectionStatusOnDeviceArchive}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateInspectionStatusOnDeviceArchive(Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "inspectionRecord/updateInspectionStatusOnDeviceArchive", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("InspectionRecordClient.updateInspectionStatusOnDeviceArchive failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code InspectionRecordService#updateInspectionRecord}. */
    public void updateInspectionRecord(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "inspectionRecord/updateInspectionRecord", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("InspectionRecordClient.updateInspectionRecord failed; swallowing: {}", e.getMessage());
        }
    }
}
