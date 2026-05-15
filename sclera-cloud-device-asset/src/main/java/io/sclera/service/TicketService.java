package io.sclera.service;

import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C3 */
@Service
public class TicketService {
    public Integer getTicketCountByDeviceId(String deviceId) {
        return 1;
    }

    public Boolean getOpenTicketStatus(String deviceId) {
        return false;
    }

    public void updateTicketAssigneeByUserEmail(String email) {
        StubLog.warn("TicketService", "updateTicketAssigneeByUserEmail", "AP-C3");
    }
}
