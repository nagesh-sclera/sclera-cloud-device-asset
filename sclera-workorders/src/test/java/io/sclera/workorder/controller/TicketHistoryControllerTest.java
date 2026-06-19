package io.sclera.workorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.workorder.dto.TicketHistoryDTO;
import io.sclera.workorder.exception.MaximoExceptionHandler;
import io.sclera.workorder.service.TicketHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketHistoryController.class)
@Import(MaximoExceptionHandler.class)
class TicketHistoryControllerTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private TicketHistoryService ticketHistoryService;

    // ── GET /user/{u}/vdms/{v}/ticket/{tid}/gettickethistory ──────────────────

    @Test
    void getTicketHistoryByTicketId_returns200WithResults() throws Exception {
        TicketHistoryDTO dto = new TicketHistoryDTO(
                "h-1", "Fan broken", BigInteger.valueOf(1000L),
                "open", "Ticket created", "000001", "alice", "t-1", "Test Ticket");
        when(ticketHistoryService.getTicketHistoryByTicketId("t-1", 1, 10))
                .thenReturn(Set.of(dto));

        mvc.perform(get("/ticket/t-1/gettickethistory").param("loggedInUser", "u1").param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("h-1"))
                .andExpect(jsonPath("$[0].status").value("open"))
                .andExpect(jsonPath("$[0].ticket_id").value("t-1"));

        verify(ticketHistoryService).getTicketHistoryByTicketId(eq("t-1"), eq(1), eq(10));
    }

    @Test
    void getTicketHistoryByTicketId_respectsCustomPagination() throws Exception {
        when(ticketHistoryService.getTicketHistoryByTicketId("t-1", 3, 5))
                .thenReturn(Collections.emptySet());

        mvc.perform(get("/ticket/t-1/gettickethistory")
                        .param("loggedInUser", "u1")
                        .param("vdms_id", "v1")
                        .param("pageno", "3")
                        .param("pagesize", "5"))
                .andExpect(status().isOk());

        verify(ticketHistoryService).getTicketHistoryByTicketId(eq("t-1"), eq(3), eq(5));
    }

    // ── GET /user/{u}/vdms/{v}/device/{did}/gettickethistory ─────────────────

    @Test
    void getAllTicketHistoryByDeviceId_returns200WithResults() throws Exception {
        TicketHistoryDTO dto = new TicketHistoryDTO(
                "h-2", "Sensor alert", BigInteger.valueOf(2000L),
                "closed", "Ticket closed", "000002", "bob", "t-2", "Ticket 2");
        when(ticketHistoryService.getAllTicketHistoryByDeviceId("dev-1", 1, 10))
                .thenReturn(Set.of(dto));

        mvc.perform(get("/ticket/device/dev-1/gettickethistory").param("loggedInUser", "u1").param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("h-2"))
                .andExpect(jsonPath("$[0].status").value("closed"));

        verify(ticketHistoryService).getAllTicketHistoryByDeviceId(eq("dev-1"), eq(1), eq(10));
    }

    @Test
    void getAllTicketHistoryByDeviceId_returnsEmptyArrayWhenNoHistory() throws Exception {
        when(ticketHistoryService.getAllTicketHistoryByDeviceId("dev-99", 1, 10))
                .thenReturn(Collections.emptySet());

        mvc.perform(get("/ticket/device/dev-99/gettickethistory").param("loggedInUser", "u1").param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
