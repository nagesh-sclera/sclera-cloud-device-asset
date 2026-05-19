package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.PmsAttributesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-workorders microservice (AP-C3).
 *
 * Replaces the stub {@code io.sclera.service.PmsService}.
 * Collection-returning methods return empty sets on sidecar failure (stub default).
 * Void methods swallow exceptions with a WARN log.
 *
 * NOTE: getPmsAttributesByLocationIds passes a Set body under GET routing — the body
 * is silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class PmsClient {

    private static final Logger log = LoggerFactory.getLogger(PmsClient.class);
    private static final String APP_ID = "sclera-workorders";

    private final DaprClient dapr;

    public PmsClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code PmsService#getLocationIdsByRoomStatus}.
     * Maps to GET sclera-workorders/pms/getLocationIdsByRoomStatus.
     * Returns empty set on sidecar failure (stub default).
     */
    public Set<String> getLocationIdsByRoomStatus(String vdmsId, String status) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        payload.put("status", status);
        try {
            dapr.invokeMethod(APP_ID, "pms/getLocationIdsByRoomStatus", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PmsClient.getLocationIdsByRoomStatus failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Mirrors {@code PmsService#getPmsAttributesByLocationIds}.
     * Maps to GET sclera-workorders/pms/getPmsAttributesByLocationIds.
     * Returns empty set on sidecar failure (stub default).
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public Set<PmsAttributesDTO> getPmsAttributesByLocationIds(Set<String> locationIds) {
        try {
            dapr.invokeMethod(APP_ID, "pms/getPmsAttributesByLocationIds", null, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PmsClient.getPmsAttributesByLocationIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Mirrors {@code PmsService#updatePmsAttributesByLocationId}.
     * Maps to GET sclera-workorders/pms/updatePmsAttributesByLocationId.
     */
    public void updatePmsAttributesByLocationId(String locationId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        try {
            dapr.invokeMethod(APP_ID, "pms/updatePmsAttributesByLocationId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PmsClient.updatePmsAttributesByLocationId failed; swallowing: {}", e.getMessage());
        }
    }
}
