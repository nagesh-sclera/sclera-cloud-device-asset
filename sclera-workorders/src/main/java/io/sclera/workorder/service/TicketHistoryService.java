package io.sclera.workorder.service;

import io.sclera.workorder.dto.TicketHistoryDTO;

import java.math.BigInteger;
import java.util.Set;

/**
 * Records and queries the audit history of native tickets.
 *
 * <p>Each ticket state change appends a {@link io.sclera.workorder.entity.TicketHistory}
 * row; reads are paginated (page number is 1-based).
 */
public interface TicketHistoryService {

    /**
     * Appends a history entry for a ticket.
     *
     * @param message        human-readable change message
     * @param timestamp      epoch-millis of the change
     * @param status         ticket status at the time of the change
     * @param action_message action/audit message
     * @param ticket_number  business ticket number
     * @param username       user who performed the change
     * @param ticket_id      id of the ticket the history belongs to
     */
    void addNativeTicketHistory(String message, BigInteger timestamp, String status, String action_message,
                                String ticket_number, String username, String ticket_id);

    /**
     * Returns a page of history entries for a single ticket.
     *
     * @param ticket_id the ticket id
     * @param pageno    1-based page number
     * @param pagesize  page size
     * @return the requested page of history entries
     */
    Set<TicketHistoryDTO> getTicketHistoryByTicketId(String ticket_id, Integer pageno, Integer pagesize);

    /**
     * Returns a page of history entries across all tickets of a device.
     *
     * @param device_id the device id
     * @param pageno    1-based page number
     * @param pagesize  page size
     * @return the requested page of history entries
     */
    Set<TicketHistoryDTO> getAllTicketHistoryByDeviceId(String device_id, Integer pageno, Integer pagesize);
}
