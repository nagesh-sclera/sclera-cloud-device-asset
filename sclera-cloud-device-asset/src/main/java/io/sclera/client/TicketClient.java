package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Thin Dapr client delegating to the sclera-workorders microservice.
 *
 * <p>Replaces the stub {@code io.sclera.service.TicketService}. Targets the workorder
 * service's REST endpoints under its servlet context-path {@code /api/v1/workorder-service}
 * — the context-path is part of the Dapr method string because Dapr forwards the invoke
 * to {@code http://<workorder-app>/<method>}.
 *
 * <p>Read methods deserialize the real response; on sidecar failure they fall back to a
 * safe default (count {@code 0}, status {@code false}) and the void method swallows the
 * exception with a WARN log.
 */
@Component
public class TicketClient {

    private static final Logger log = LoggerFactory.getLogger(TicketClient.class);
    private static final String APP_ID = "sclera-workorders";
    /** Workorder ticket API base, including the service servlet context-path. */
    private static final String BASE = "api/v1/workorder-service/ticket";

    private final DaprClient dapr;

    public TicketClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code TicketService#getTicketCountByDeviceId}.
     * GET {base}/device/{deviceId}/ticketcount → the device's ticket count.
     * Returns 0 on sidecar failure.
     */
    public Integer getTicketCountByDeviceId(String deviceId) {
        try {
            Integer count = dapr.invokeMethod(APP_ID,
                    BASE + "/device/" + deviceId + "/ticketcount",
                    null, HttpExtension.GET, Integer.class).block();
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("TicketClient.getTicketCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Mirrors {@code TicketService#getOpenTicketStatus}.
     * GET {base}/device/{deviceId}/openticketstatus → whether the device has an open ticket.
     * Returns false on sidecar failure.
     */
    public Boolean getOpenTicketStatus(String deviceId) {
        try {
            Boolean open = dapr.invokeMethod(APP_ID,
                    BASE + "/device/" + deviceId + "/openticketstatus",
                    null, HttpExtension.GET, Boolean.class).block();
            return open != null && open;
        } catch (Exception e) {
            log.warn("TicketClient.getOpenTicketStatus failed; returning stub default: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mirrors {@code TicketService#updateTicketAssigneeByUserEmail}.
     * POST {base}/assignee/{email}/synctickets. Fire-and-forget: logs a warning on failure.
     */
    public void updateTicketAssigneeByUserEmail(String email) {
        try {
            dapr.invokeMethod(APP_ID,
                    BASE + "/assignee/" + email + "/synctickets",
                    null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("TicketClient.updateTicketAssigneeByUserEmail failed; swallowing: {}", e.getMessage());
        }
    }
}
