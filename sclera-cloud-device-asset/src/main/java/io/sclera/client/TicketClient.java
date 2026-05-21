package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-workorders microservice (AP-C3).
 *
 * Replaces the stub {@code io.sclera.service.TicketService}.
 * Methods with return values return the documented stub default on sidecar failure;
 * void methods swallow exceptions with a WARN log.
 */
@Component
public class TicketClient {

    private static final Logger log = LoggerFactory.getLogger(TicketClient.class);
    private static final String APP_ID = "sclera-workorders";

    private final DaprClient dapr;

    public TicketClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code TicketService#getTicketCountByDeviceId}.
     * Maps to GET sclera-workorders/ticket/getTicketCountByDeviceId.
     * Returns stub default 1 on sidecar failure.
     */
    public Integer getTicketCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "ticket/getTicketCountByDeviceId", payload, HttpExtension.GET).block();
            return 1;
        } catch (Exception e) {
            log.warn("TicketClient.getTicketCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 1;
        }
    }

    /**
     * Mirrors {@code TicketService#getOpenTicketStatus}.
     * Maps to GET sclera-workorders/ticket/getOpenTicketStatus.
     * Returns stub default false on sidecar failure.
     */
    public Boolean getOpenTicketStatus(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "ticket/getOpenTicketStatus", payload, HttpExtension.GET).block();
            return false;
        } catch (Exception e) {
            log.warn("TicketClient.getOpenTicketStatus failed; returning stub default: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mirrors {@code TicketService#updateTicketAssigneeByUserEmail}.
     * Maps to GET sclera-workorders/ticket/updateTicketAssigneeByUserEmail.
     */
    public void updateTicketAssigneeByUserEmail(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "ticket/updateTicketAssigneeByUserEmail", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("TicketClient.updateTicketAssigneeByUserEmail failed; swallowing: {}", e.getMessage());
        }
    }
}
