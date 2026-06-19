package io.sclera.workorder.controller;


import io.sclera.workorder.dto.TicketHistoryDTO;
import io.sclera.workorder.service.TicketHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/** Read API for ticket history (by ticket id or device id), paginated. */
@RestController
@RequestMapping("/ticket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }

    //get all ticket history by ticket id
    @GetMapping("/{ticket_id}/gettickethistory")
    public Set<TicketHistoryDTO> getTicketHistoryByTicketId(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @PathVariable String ticket_id, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return ticketHistoryService.getTicketHistoryByTicketId(ticket_id, pageno, pagesize);
    }

    //get all ticket history by device id
    @GetMapping("/device/{device_id}/gettickethistory")
    public Set<TicketHistoryDTO> getAllTicketHistoryByDeviceId(@RequestParam(value = "loggedInUser") String loggedInUser, @RequestParam(value = "vdms_id") String vdms_id, @PathVariable String device_id, @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return ticketHistoryService.getAllTicketHistoryByDeviceId(device_id, pageno, pagesize);
    }


}
