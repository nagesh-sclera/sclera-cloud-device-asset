package io.sclera.workorder.service;

import io.sclera.workorder.dto.TicketDTO;
import io.sclera.workorder.dto.TicketFilterDTO;
import io.sclera.workorder.enums.TicketType;

import java.util.Map;
import java.util.Set;

/**
 * Native ticket management: create/update (upsert), query, delete, history, and alerts.
 *
 * <p>List queries are paginated (1-based page numbers). State transitions are derived
 * from the old/new ticket and recorded in the ticket history.
 */
public interface TicketService {

    /**
     * Creates a new ticket (when {@code id} is null) or updates an existing one, deriving the
     * transition type, writing history, and emitting an audit event.
     *
     * @param username  acting user
     * @param vdmsId    the VDMS scope
     * @param ticketDTO the ticket to create or update
     */
    void upsertTicket(String username, String vdmsId, TicketDTO ticketDTO);

    /**
     * @param category ticket category ({@code incident_request} → {@code INC-}, {@code service_request} → {@code SRN-})
     * @param vdmsId   the VDMS id; its numeric part (leading {@code VDMS} stripped) is embedded after the prefix
     * @return the next ticket number, e.g. {@code INC-533000001} (prefix + vdms numeric id + 6-digit sequence)
     */
    String getNextTicketNumberByCategory(String category, String vdmsId);

    /**
     * @param ticketDTO the ticket
     * @return the display name derived from the ticket's category
     */
    String getTicketNameByCategory(TicketDTO ticketDTO);

    /**
     * Returns a filtered, paginated set of tickets.
     *
     * @param username        acting user
     * @param searchkey       free-text search
     * @param ticketFilterDTO filter criteria
     * @param pagesize        page size
     * @param pageno          1-based page number
     * @return the matching tickets
     */
    Set<TicketDTO> getAllTickets(String username, String searchkey, TicketFilterDTO ticketFilterDTO, Integer pagesize, Integer pageno);

    /**
     * Returns a filtered, paginated set of tickets for a single device.
     *
     * @return the matching tickets for the device
     */
    Set<TicketDTO> getAllTicketsByDeviceId(String username, String device_id, Integer pagesize, Integer pageno, TicketFilterDTO ticketFilterDTO);

    /**
     * Soft-deletes a ticket and records the action.
     *
     * @param username  acting user
     * @param vdmsId    the VDMS scope
     * @param ticket_id id of the ticket to delete
     */
    void deleteTicketById(String username, String vdmsId, String ticket_id);

    /**
     * @return ticket counts grouped by status for the assignee's filter
     */
    Map<String, Integer> getTicketStatusCountByAssignee(String username, TicketFilterDTO ticketFilterDTO);

    /**
     * Sends a notification/alert for a ticket transition.
     *
     * @param ticket      the ticket
     * @param ticket_type the transition type that occurred
     */
    void sendNativeTicketAlertInfo(TicketDTO ticket, TicketType ticket_type);

    /**
     * @param ticket_id the ticket id
     * @return the full ticket detail projection
     */
    TicketDTO getNativeTicketDetailsById(String ticket_id);

    /**
     * Reassigns tickets to the given assignee (bulk update by user e-mail).
     *
     * @param assignee_user_email the new assignee's e-mail
     */
    void updateTicketAssigneeByUserEmail(String assignee_user_email);

    /**
     * @param device_id the device id
     * @return the number of non-deleted tickets logged against the device
     */
    Integer getTicketCountByDeviceId(String device_id);

    /**
     * @param device_id the device id
     * @return {@code true} when the device has at least one non-closed ticket
     */
    Boolean getOpenTicketStatus(String device_id);
}
