package io.sclera.workorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.workorder.dto.TicketDTO;
import io.sclera.workorder.dto.TicketFilterDTO;
import io.sclera.workorder.exception.MaximoExceptionHandler;
import io.sclera.workorder.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketController.class)
@Import(MaximoExceptionHandler.class)
class TicketControllerTest {

    @Autowired private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private TicketService ticketService;

    // ── POST /user/{username}/vdms/{vdmsid}/upsertticket ─────────────────────

    @Test
    void upsertTicket_returns200() throws Exception {
        doNothing().when(ticketService).upsertTicket(anyString(), anyString(), any());

        mvc.perform(post("/ticket/upsertticket")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void upsertTicket_routesPathVarsToService() throws Exception {
        TicketDTO body = new TicketDTO();
        body.setUser_message("Fan broken");

        mvc.perform(post("/ticket/upsertticket")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(ticketService).upsertTicket(eq("alice"), eq("v1"), any(TicketDTO.class));
    }

    // ── POST /user/{username}/vdms/{vdmsid}/getalltickets ────────────────────

    @Test
    void getAllTickets_returns200WithTicketSet() throws Exception {
        TicketDTO ticket = sampleTicket("t-1");
        when(ticketService.getAllTickets(anyString(), anyString(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.singleton(ticket));
        when(ticketService.getTicketStatusCountByAssignee(anyString(), any()))
                .thenReturn(Map.of("all", 7));

        mvc.perform(post("/ticket/getalltickets")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "7"))
                .andExpect(header().string("X-Page", "1"))
                .andExpect(header().string("X-Page-Size", "10"));
    }

    @Test
    void getAllTickets_usesDefaultPaginationParams() throws Exception {
        when(ticketService.getAllTickets(anyString(), anyString(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptySet());
        when(ticketService.getTicketStatusCountByAssignee(anyString(), any()))
                .thenReturn(Collections.emptyMap());

        mvc.perform(post("/ticket/getalltickets")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(ticketService).getAllTickets(eq("alice"), eq("null"), any(TicketFilterDTO.class), eq(10), eq(1));
    }

    @Test
    void getAllTickets_respectsCustomPaginationParams() throws Exception {
        when(ticketService.getAllTickets(anyString(), anyString(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptySet());
        when(ticketService.getTicketStatusCountByAssignee(anyString(), any()))
                .thenReturn(Collections.emptyMap());

        mvc.perform(post("/ticket/getalltickets")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .param("pageno", "2")
                        .param("pagesize", "5")
                        .param("searchkey", "fan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(ticketService).getAllTickets(eq("alice"), eq("fan"), any(), eq(5), eq(2));
    }

    // ── POST /user/{username}/vdms/{vdmsid}/device/{device_id}/getallticketsbydeviceid ──

    @Test
    void getAllTicketsByDeviceId_returns200() throws Exception {
        when(ticketService.getAllTicketsByDeviceId(anyString(), anyString(), anyInt(), anyInt(), any()))
                .thenReturn(Collections.emptySet());

        mvc.perform(post("/ticket/device/dev-1/getallticketsbydeviceid")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(ticketService).getAllTicketsByDeviceId(eq("alice"), eq("dev-1"), anyInt(), anyInt(), any());
    }

    // ── DELETE /user/{username}/vdms/{vdmsid}/ticket/{ticket_id}/deleteticket ─

    @Test
    void deleteTicket_returns200() throws Exception {
        doNothing().when(ticketService).deleteTicketById(anyString(), anyString(), anyString());

        mvc.perform(delete("/ticket/t-1/deleteticket")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTicket_routesPathVarsToService() throws Exception {
        mvc.perform(delete("/ticket/t-99/deleteticket")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1"))
                .andExpect(status().isOk());

        verify(ticketService).deleteTicketById("alice", "v1", "t-99");
    }

    // ── POST /user/{username}/vdms/{vdmsid}/getticketcount ──────────────────

    @Test
    void getTicketCount_returns200WithCountMap() throws Exception {
        Map<String, Integer> counts = Map.of(
                "all", 10, "new", 3, "open", 4, "on_hold", 1, "closed", 2
        );
        when(ticketService.getTicketStatusCountByAssignee(anyString(), any())).thenReturn(counts);

        mvc.perform(post("/ticket/getticketcount")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.all").value(10))
                .andExpect(jsonPath("$.new").value(3))
                .andExpect(jsonPath("$.open").value(4));
    }

    @Test
    void getTicketCount_routesUsernameToService() throws Exception {
        when(ticketService.getTicketStatusCountByAssignee(anyString(), any()))
                .thenReturn(Collections.emptyMap());

        mvc.perform(post("/ticket/getticketcount")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(ticketService).getTicketStatusCountByAssignee(eq("alice"), any(TicketFilterDTO.class));
    }

    // ── GET /user/{username}/vdms/{vdmsid}/ticket/{ticket_id}/getticketdetailsbyid ──

    @Test
    void getTicketDetailsById_returns200WithTicketPayload() throws Exception {
        TicketDTO ticket = sampleTicket("t-1");
        when(ticketService.getNativeTicketDetailsById("t-1")).thenReturn(ticket);

        mvc.perform(get("/ticket/t-1/getticketdetailsbyid")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t-1"))
                .andExpect(jsonPath("$.status").value("open"));
    }

    @Test
    void getTicketDetailsById_returnsNullBodyWhenNotFound() throws Exception {
        when(ticketService.getNativeTicketDetailsById("missing")).thenReturn(null);

        mvc.perform(get("/ticket/missing/getticketdetailsbyid")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1"))
                .andExpect(status().isOk());
    }

    // ── GET /ticket/device/{device_id}/ticketcount (device-asset read-back) ──

    @Test
    void getTicketCountByDeviceId_returns200WithCount() throws Exception {
        when(ticketService.getTicketCountByDeviceId("dev-1")).thenReturn(7);

        mvc.perform(get("/ticket/device/dev-1/ticketcount"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));

        verify(ticketService).getTicketCountByDeviceId("dev-1");
    }

    // ── GET /ticket/device/{device_id}/openticketstatus ──────────────────────

    @Test
    void getOpenTicketStatus_returns200WithTrue() throws Exception {
        when(ticketService.getOpenTicketStatus("dev-1")).thenReturn(true);

        mvc.perform(get("/ticket/device/dev-1/openticketstatus"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(ticketService).getOpenTicketStatus("dev-1");
    }

    // ── POST /ticket/assignee/{email}/synctickets ────────────────────────────

    @Test
    void syncTicketAssignee_returns200AndDelegates() throws Exception {
        mvc.perform(post("/ticket/assignee/user@test.com/synctickets"))
                .andExpect(status().isOk());

        verify(ticketService).updateTicketAssigneeByUserEmail("user@test.com");
    }

    // ── Negative cases: mandatory params & body validation ────────────────────

    @Test
    void upsertTicket_invalidAssigneeEmail_returns400() throws Exception {
        TicketDTO bad = new TicketDTO();
        bad.setAssignee_user_email("not-an-email");

        mvc.perform(post("/ticket/upsertticket")
                        .param("loggedInUser", "alice")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("assignee_user_email")));
    }

    @Test
    void upsertTicket_missingVdmsId_returns400() throws Exception {
        mvc.perform(post("/ticket/upsertticket")
                        .param("loggedInUser", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.message", containsString("vdms_id")));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private TicketDTO sampleTicket(String id) {
        TicketDTO dto = new TicketDTO();
        dto.setId(id);
        dto.setStatus("open");
        dto.setUser_message("Some issue");
        dto.setTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
        return dto;
    }
}
