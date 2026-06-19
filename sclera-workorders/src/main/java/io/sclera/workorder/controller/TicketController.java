package io.sclera.workorder.controller;

import io.sclera.workorder.dto.TicketDTO;
import io.sclera.workorder.dto.TicketFilterDTO;
import io.sclera.workorder.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Native ticket API — create/update, list, delete, count, and detail lookups.
 *
 * <p>Served under the service context-path {@code /api/v1/workorder-service/ticket}. The former
 * {@code /user/{user}/vdms/{vdmsId}} path prefix is now the <b>mandatory</b> {@code loggedInUser} /
 * {@code vdms_id} query parameters (a missing one yields HTTP 400). List endpoints are paginated
 * with 1-based page numbers.
 */
@RestController
@RequestMapping("/ticket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Creates or updates a ticket (upsert — new when the body has no id).
     *
     * @param loggedInUser acting user
     * @param vdms_id      VDMS scope
     * @param ticketdto    the ticket to persist (validated)
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertticket")
    public void upsertTicket(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @Valid @RequestBody TicketDTO ticketdto) {
        ticketService.upsertTicket(loggedInUser, vdms_id, ticketdto);
    }

    /**
     * Returns a filtered, paginated page of tickets.
     *
     * <p>Paging metadata is in the {@code X-Total-Count} / {@code X-Page} / {@code X-Page-Size}
     * response headers; the body keeps the plain set shape. <b>Note:</b> {@code X-Total-Count}
     * reflects the {@code ticketFilterDTO} filter only — it does not account for the free-text
     * {@code searchkey} (no count query mirrors the search term yet).
     *
     * @param ticketFilterDTO filter criteria (validated)
     * @param searchkey       free-text search ({@code null}/empty = none)
     * @param pageno          1-based page number
     * @param pagesize        page size
     */
    @RequestMapping(method = RequestMethod.POST, value = "/getalltickets")
    public ResponseEntity<Set<TicketDTO>> getAllTickets(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @Valid @RequestBody TicketFilterDTO ticketFilterDTO, @RequestParam(defaultValue = "null") String searchkey, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        Set<TicketDTO> page = ticketService.getAllTickets(loggedInUser, searchkey, ticketFilterDTO, pagesize, pageno);
        // The count map carries an "all" aggregate plus per-status keys — use the aggregate as the total.
        int total = ticketService.getTicketStatusCountByAssignee(loggedInUser, ticketFilterDTO)
                .getOrDefault("all", 0);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .header("X-Page", String.valueOf(pageno))
                .header("X-Page-Size", String.valueOf(pagesize))
                .body(page);
    }

    /**
     * Returns a filtered, paginated page of tickets for a single device.
     *
     * @param device_id the device id
     * @param pageno    1-based page number
     * @param pagesize  page size
     */
    @RequestMapping(method = RequestMethod.POST, value = "/device/{device_id}/getallticketsbydeviceid")
    public Set<TicketDTO> getAllTicketsByDeviceId(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @PathVariable String device_id, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestBody TicketFilterDTO ticketFilterDTO) {
        return ticketService.getAllTicketsByDeviceId(loggedInUser, device_id, pagesize, pageno, ticketFilterDTO);
    }

    /**
     * Soft-deletes a ticket.
     *
     * @param ticket_id the ticket id
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{ticket_id}/deleteticket")
    public void deleteTicketById(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @PathVariable String ticket_id) {
        ticketService.deleteTicketById(loggedInUser, vdms_id, ticket_id);
    }

    /**
     * @return ticket counts grouped by status for the assignee/filter
     */
    @RequestMapping(method = RequestMethod.POST, value = "/getticketcount")
    public Map<String, Integer> getTicketCountByUser(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @RequestBody TicketFilterDTO ticketFilterDTO) {
        return ticketService.getTicketStatusCountByAssignee(loggedInUser, ticketFilterDTO);
    }

    /**
     * @param ticket_id the ticket id
     * @return the full ticket detail
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{ticket_id}/getticketdetailsbyid")
    public TicketDTO getTicketDetailsById(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @PathVariable String ticket_id) {
        return ticketService.getNativeTicketDetailsById(ticket_id);
    }
}
