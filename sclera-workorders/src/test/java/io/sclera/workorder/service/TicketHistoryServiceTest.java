package io.sclera.workorder.service;

import io.sclera.workorder.entity.Ticket;
import io.sclera.workorder.entity.TicketHistory;
import io.sclera.workorder.repository.TicketRepository;
import io.sclera.workorder.service.impl.TicketHistoryServiceImpl;
import io.sclera.workorder.dto.TicketHistoryDTO;
import io.sclera.workorder.repository.TicketHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketHistoryServiceTest {

    @Mock private TicketHistoryRepository ticketHistoryRepository;
    @Mock private TicketRepository ticketRepository;
    @InjectMocks private TicketHistoryServiceImpl ticketHistoryService;

    // ── addNativeTicketHistory ────────────────────────────────────────────────

    @Test
    void addNativeTicketHistory_savesEntity_withGeneratedIdAndTicketReference() {
        Ticket ticketRef = mock(Ticket.class);    // stand-in for the lazy proxy
        when(ticketRepository.getReferenceById("t-1")).thenReturn(ticketRef);

        ticketHistoryService.addNativeTicketHistory(
                "msg", BigInteger.valueOf(1000L), "open", "action", "TKT-1", "alice", "t-1");

        ArgumentCaptor<TicketHistory> captor = ArgumentCaptor.forClass(TicketHistory.class);
        verify(ticketHistoryRepository).save(captor.capture());

        TicketHistory saved = captor.getValue();
        assertThat(saved).extracting("id").asString().isNotBlank();
        assertThat(saved).extracting("message", "timestamp", "status", "action_message",
                        "ticket_number", "created_by", "ticket")
                .containsExactly("msg", BigInteger.valueOf(1000L), "open", "action",
                        "TKT-1", "alice", ticketRef);
    }

    // ── getTicketHistoryByTicketId ────────────────────────────────────────────

    @Test
    void getTicketHistoryByTicketId_computesOffsetAndDelegatesToRepository() {
        TicketHistoryDTO dto = new TicketHistoryDTO(
                "h-1", "msg", BigInteger.ONE, "open", "act", "T-1", "alice", "t-1", "title");
        when(ticketHistoryRepository.getTicketHistoryByTicketId("t-1", PageRequest.of(0, 10)))
                .thenReturn(List.of(dto));

        Set<TicketHistoryDTO> result = ticketHistoryService.getTicketHistoryByTicketId("t-1", 1, 10);

        assertThat(result).containsExactly(dto);
        verify(ticketHistoryRepository).getTicketHistoryByTicketId("t-1", PageRequest.of(0, 10));
    }

    @Test
    void getTicketHistoryByTicketId_page2_computesCorrectOffset() {
        when(ticketHistoryRepository.getTicketHistoryByTicketId("t-1", PageRequest.of(1, 10)))
                .thenReturn(List.of());

        ticketHistoryService.getTicketHistoryByTicketId("t-1", 2, 10);

        verify(ticketHistoryRepository).getTicketHistoryByTicketId("t-1", PageRequest.of(1, 10));
    }

    // ── getAllTicketHistoryByDeviceId ─────────────────────────────────────────

    @Test
    void getAllTicketHistoryByDeviceId_computesOffsetAndDelegatesToRepository() {
        TicketHistoryDTO dto = new TicketHistoryDTO(
                "h-2", "msg", BigInteger.TWO, "closed", "act", "T-2", "bob", "t-2", "title2");
        when(ticketHistoryRepository.getAllTicketHistoryByDeviceId("dev-1", PageRequest.of(0, 5)))
                .thenReturn(List.of(dto));

        Set<TicketHistoryDTO> result = ticketHistoryService.getAllTicketHistoryByDeviceId("dev-1", 1, 5);

        assertThat(result).containsExactly(dto);
        verify(ticketHistoryRepository).getAllTicketHistoryByDeviceId("dev-1", PageRequest.of(0, 5));
    }
}
