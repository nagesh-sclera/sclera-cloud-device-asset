package io.sclera.workorder.service;

import io.sclera.workorder.service.impl.TicketServiceImpl;
import io.sclera.workorder.client.DeviceAssetClient;
import io.sclera.workorder.dto.TicketDTO;
import io.sclera.workorder.dto.TicketFilterDTO;
import io.sclera.workorder.enums.TicketType;
import io.sclera.workorder.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private DeviceAssetClient deviceAssetClient;
    @Mock private TicketHistoryService ticketHistoryService;
    @Mock private UserActionLogService userActionLogService;

    @InjectMocks private TicketServiceImpl ticketService;

    @Captor private ArgumentCaptor<String> actionMessageCaptor;

    // ── upsertTicket — new ticket ─────────────────────────────────────────

    @Test
    void upsertTicket_newTicket_assignsIdAndTimestamps() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getId()).isNotNull().isNotBlank();
        assertThat(dto.getCreated_at()).isNotNull();
        assertThat(dto.getCreated_by()).isEqualTo("alice");
    }

    @Test
    void upsertTicket_newTicket_defaultsStatusToNewWhenNull() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        dto.setStatus(null);
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getStatus()).isEqualTo("new");
    }

    @Test
    void upsertTicket_newTicket_preservesExplicitStatus() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        dto.setStatus("open");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getStatus()).isEqualTo("open");
    }

    @Test
    void upsertTicket_newTicket_setsTypeToGeneratedByCustomer() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.GENERATED_BY_CUSTOMER.getValue()));
    }

    @Test
    void upsertTicket_newTicket_setsDescriptionFromUserMessage() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        dto.setUser_message("Fan is overheating");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getDescription()).isEqualTo("Fan is overheating");
    }

    @Test
    void upsertTicket_newTicket_persistsViaRepository() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(3);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        verify(ticketRepository).upsertTicketNative(
                anyString(), anyString(), anyString(), any(), any(), any(), any(), any(),
                eq("alice"), any(), any(), any(), any(), any(),
                eq("device-1"), any(), any()
        );
    }

    @Test
    void upsertTicket_newTicket_syncsDeviceTicketStats() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        verify(deviceAssetClient).syncTicketStats("device-1");
    }

    @Test
    void upsertTicket_newTicket_logsAddAudit() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        verify(userActionLogService).addUserAction(
                eq("alice"), eq("v1"), eq("tickets"), eq("ADD"),
                anyString(), eq("success"), eq("native_tickets"), anyString()
        );
    }

    @Test
    void upsertTicket_newTicket_generatesNumberFromMaxPlusOne() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(9);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getNumber()).isEqualTo("INC-v1000010");
    }

    @Test
    void upsertTicket_newTicket_numberStartsAtOneWhenNoExisting() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "incident_request");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getNumber()).isEqualTo("INC-v1000001");
    }

    // ── getNextTicketNumberByCategory — number format ─────────────────────────

    @Test
    void nextNumber_incident_buildsIncPrefixWithVdmsNumericIdAndSequence() {
        // VDMS533 → numeric id 533 → INC-533 + 6-digit sequence (no existing rows → 1)
        assertThat(ticketService.getNextTicketNumberByCategory("incident_request", "VDMS533"))
                .isEqualTo("INC-533000001");
    }

    @Test
    void nextNumber_incident_vdms000_matchesExampleFormat() {
        // VDMS000 → numeric id 000 → exactly INC-000000001 (the requested example)
        assertThat(ticketService.getNextTicketNumberByCategory("incident_request", "VDMS000"))
                .isEqualTo("INC-000000001");
    }

    @Test
    void nextNumber_service_buildsSrnPrefixWithVdmsNumericIdAndSequence() {
        assertThat(ticketService.getNextTicketNumberByCategory("service_request", "VDMS533"))
                .isEqualTo("SRN-533000001");
    }

    @Test
    void nextNumber_stripsLeadingVdmsPrefixCaseInsensitively() {
        // lowercase "vdms000" is stripped the same as "VDMS000" → INC-000000001
        assertThat(ticketService.getNextTicketNumberByCategory("incident_request", "vdms000"))
                .isEqualTo("INC-000000001");
    }

    @Test
    void nextNumber_unknownCategory_hasNoPrefix() {
        assertThat(ticketService.getNextTicketNumberByCategory("other", "VDMS533"))
                .isEqualTo("000001");
    }

    @Test
    void nextNumber_incrementsFromExistingMax() {
        when(ticketRepository.findMaxTicketNumberByPrefix("INC-533")).thenReturn(7);

        assertThat(ticketService.getNextTicketNumberByCategory("incident_request", "VDMS533"))
                .isEqualTo("INC-533000008");
    }

    // ── upsertTicket — update existing ticket ────────────────────────────────

    @Test
    void upsertTicket_updateExisting_setsUpdatedAtAndBy() {
        when(ticketRepository.getNativeTicketById("t-1")).thenReturn(existingTicket("t-1", "new", "msg"));

        TicketDTO dto = updateTicketDto("t-1", "open", "msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getUpdated_at()).isNotNull();
        assertThat(dto.getUpdated_by()).isEqualTo("bob");
    }

    @Test
    void upsertTicket_updateExisting_preservesCreatedBy() {
        when(ticketRepository.getNativeTicketById("t-1")).thenReturn(existingTicket("t-1", "new", "msg"));

        TicketDTO dto = updateTicketDto("t-1", "open", "msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getCreated_by()).isEqualTo("original");
    }

    @Test
    void upsertTicket_closingTicket_setsClosedAtAndClosedBy() {
        when(ticketRepository.getNativeTicketById("t-2")).thenReturn(existingTicket("t-2", "open", "msg"));

        TicketDTO dto = updateTicketDto("t-2", "closed", "msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getClosed_at()).isNotNull();
        assertThat(dto.getClosed_by()).isEqualTo("bob");
        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.CLOSED.getValue()));
    }

    @Test
    void upsertTicket_statusChanged_setsStatusUpdatedType() {
        when(ticketRepository.getNativeTicketById("t-3")).thenReturn(existingTicket("t-3", "new", "same msg"));

        TicketDTO dto = updateTicketDto("t-3", "open", "same msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.STATUS_UPDATED_BY_CUSTOMER.getValue()));
    }

    @Test
    void upsertTicket_messageChanged_setsMessageUpdatedType() {
        when(ticketRepository.getNativeTicketById("t-4")).thenReturn(existingTicket("t-4", "open", "old msg"));

        TicketDTO dto = updateTicketDto("t-4", "open", "new msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.MESSAGE_UPDATED_BY_CUSTOMER.getValue()));
    }

    @Test
    void upsertTicket_statusAndMessageChanged_setsCombinedType() {
        when(ticketRepository.getNativeTicketById("t-5")).thenReturn(existingTicket("t-5", "new", "old msg"));

        TicketDTO dto = updateTicketDto("t-5", "open", "new msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.STATUS_AND_MESSAGE_UPDATED_BY_CUSTOMER.getValue()));
    }

    @Test
    void upsertTicket_reopened_setsReopenedType() {
        when(ticketRepository.getNativeTicketById("t-6")).thenReturn(existingTicket("t-6", "closed", "msg"));

        TicketDTO dto = updateTicketDto("t-6", "open", "msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.REOPENED_BY_CUSTOMER.getValue()));
    }

    @Test
    void upsertTicket_assigneeAdded_setsAssignedType() {
        TicketDTO old = existingTicket("t-7", "open", "msg");
        old.setAssignee_user_email(null);
        when(ticketRepository.getNativeTicketById("t-7")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-7", "open", "msg");
        dto.setAssignee_user_email("tech@sclera.io");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.ASSIGNED.getValue()));
    }

    @Test
    void upsertTicket_assigneeRemoved_setsUnassignedType() {
        TicketDTO old = existingTicket("t-8", "open", "msg");
        old.setAssignee_user_email("tech@sclera.io");
        when(ticketRepository.getNativeTicketById("t-8")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-8", "open", "msg");
        dto.setAssignee_user_email(null);
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.UNASSIGNED.getValue()));
    }

    @Test
    void upsertTicket_updateExisting_logsUpdateAudit() {
        when(ticketRepository.getNativeTicketById("t-9")).thenReturn(existingTicket("t-9", "new", "msg"));

        TicketDTO dto = updateTicketDto("t-9", "open", "msg");
        ticketService.upsertTicket("bob", "v1", dto);

        verify(userActionLogService).addUserAction(
                eq("bob"), eq("v1"), eq("tickets"), eq("UPDATE"),
                anyString(), eq("success"), eq("native_tickets"), eq("t-9")
        );
    }

    // ── getAllTickets ─────────────────────────────────────────────────────────

    @Test
    void getAllTickets_computesOffsetAsPageSizeTimesPagenoMinusOne() {
        TicketFilterDTO filter = filterWithStatus(null);
        when(ticketRepository.getAllNativeTickets(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ticketService.getAllTickets("alice", "key", filter, 10, 3);

        verify(ticketRepository).getAllNativeTickets(
                eq("key"), isNull(), eq("all"), isNull(), isNull(), eq("alice"), eq(PageRequest.of(2, 10))
        );
    }

    @Test
    void getAllTickets_withNullStatus_passesAllToRepository() {
        TicketFilterDTO filter = filterWithStatus(null);
        when(ticketRepository.getAllNativeTickets(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ticketService.getAllTickets("alice", null, filter, 10, 1);

        verify(ticketRepository).getAllNativeTickets(
                isNull(), isNull(), eq("all"), isNull(), isNull(), eq("alice"), eq(PageRequest.of(0, 10))
        );
    }

    @Test
    void getAllTickets_withAllStatus_passesAllToRepository() {
        TicketFilterDTO filter = filterWithStatus(Set.of("all"));
        when(ticketRepository.getAllNativeTickets(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ticketService.getAllTickets("alice", null, filter, 10, 1);

        verify(ticketRepository).getAllNativeTickets(
                isNull(), isNull(), eq("all"), isNull(), isNull(), eq("alice"), eq(PageRequest.of(0, 10))
        );
    }

    @Test
    void getAllTickets_withSpecificStatus_passesItToRepository() {
        TicketFilterDTO filter = filterWithStatus(Set.of("open"));
        when(ticketRepository.getAllNativeTickets(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ticketService.getAllTickets("alice", null, filter, 10, 1);

        verify(ticketRepository).getAllNativeTickets(
                isNull(), isNull(), eq("open"), isNull(), isNull(), eq("alice"), eq(PageRequest.of(0, 10))
        );
    }

    // ── deleteTicketById ──────────────────────────────────────────────────────

    @Test
    void deleteTicketById_success_callsRepoAndSyncsStats() {
        TicketDTO existing = existingTicket("t-del", "open", "msg");
        when(ticketRepository.getNativeTicketById("t-del")).thenReturn(existing);

        ticketService.deleteTicketById("alice", "v1", "t-del");

        verify(ticketRepository).deleteTicketById("t-del");
        verify(deviceAssetClient).syncTicketStats("device-1");
    }

    @Test
    void deleteTicketById_success_logsSuccessAudit() {
        when(ticketRepository.getNativeTicketById("t-del")).thenReturn(existingTicket("t-del", "open", "msg"));

        ticketService.deleteTicketById("alice", "v1", "t-del");

        verify(userActionLogService).addUserAction(
                eq("alice"), eq("v1"), eq("tickets"), eq("DELETE"),
                anyString(), eq("success"), eq("native_tickets"), eq("t-del")
        );
    }

    @Test
    void deleteTicketById_whenRepositoryThrows_logsFailedAuditAndRethrows() {
        when(ticketRepository.getNativeTicketById("t-bad"))
                .thenThrow(new RuntimeException("db error"));

        assertThatThrownBy(() -> ticketService.deleteTicketById("alice", "v1", "t-bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db error");

        verify(userActionLogService).addUserAction(
                eq("alice"), eq("v1"), eq("tickets"), eq("DELETE"),
                anyString(), eq("failed"), eq("native_tickets"), eq("t-bad")
        );
        verify(ticketRepository, never()).deleteTicketById(any());
    }

    // ── getTicketStatusCountByAssignee ────────────────────────────────────────

    @Test
    void getTicketStatusCountByAssignee_queriesAllFiveStatuses() {
        TicketFilterDTO filter = new TicketFilterDTO();
        filter.setAssignee_user_email("tech@sclera.io");
        when(ticketRepository.getTicketCountByStatusAndUser(anyString(), anyString(), anyString()))
                .thenReturn(5);

        Map<String, Integer> counts = ticketService.getTicketStatusCountByAssignee("alice", filter);

        assertThat(counts).containsKeys("all", "new", "open", "on_hold", "closed");
        verify(ticketRepository).getTicketCountByStatusAndUser("all", "tech@sclera.io", "alice");
        verify(ticketRepository).getTicketCountByStatusAndUser("new", "tech@sclera.io", "alice");
        verify(ticketRepository).getTicketCountByStatusAndUser("open", "tech@sclera.io", "alice");
        verify(ticketRepository).getTicketCountByStatusAndUser("on_hold", "tech@sclera.io", "alice");
        verify(ticketRepository).getTicketCountByStatusAndUser("closed", "tech@sclera.io", "alice");
    }

    @Test
    void getTicketStatusCountByAssignee_returnsRepoValues() {
        TicketFilterDTO filter = new TicketFilterDTO();
        filter.setAssignee_user_email(null);
        when(ticketRepository.getTicketCountByStatusAndUser("all", null, "alice")).thenReturn(10);
        when(ticketRepository.getTicketCountByStatusAndUser("new", null, "alice")).thenReturn(3);
        when(ticketRepository.getTicketCountByStatusAndUser("open", null, "alice")).thenReturn(4);
        when(ticketRepository.getTicketCountByStatusAndUser("on_hold", null, "alice")).thenReturn(1);
        when(ticketRepository.getTicketCountByStatusAndUser("closed", null, "alice")).thenReturn(2);

        Map<String, Integer> counts = ticketService.getTicketStatusCountByAssignee("alice", filter);

        assertThat(counts.get("all")).isEqualTo(10);
        assertThat(counts.get("new")).isEqualTo(3);
        assertThat(counts.get("open")).isEqualTo(4);
        assertThat(counts.get("on_hold")).isEqualTo(1);
        assertThat(counts.get("closed")).isEqualTo(2);
    }

    // ── determineTicketType — combined branches ───────────────────────────────

    @Test
    void upsertTicket_noChanges_skipsHistoryLog() {
        TicketDTO old = existingTicket("t-nc", "open", "same msg");
        when(ticketRepository.getNativeTicketById("t-nc")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-nc", "open", "same msg");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.NO_CHANGES.getValue()));
        verify(ticketHistoryService, never()).addNativeTicketHistory(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void upsertTicket_statusAndMessageChangedAndAssigneeAdded_setsCorrectType() {
        TicketDTO old = existingTicket("t-sma", "new", "old msg");
        old.setAssignee_user_email(null);
        when(ticketRepository.getNativeTicketById("t-sma")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-sma", "open", "new msg");
        dto.setAssignee_user_email("tech@sclera.io");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.STATUS_AND_MESSAGE_UPDATED_AND_ASSIGNED.getValue()));
        verify(ticketHistoryService).addNativeTicketHistory(
                any(), any(), any(), actionMessageCaptor.capture(), any(), any(), any());
        assertThat(actionMessageCaptor.getValue())
                .contains("status changed to").contains("message updated").contains("assigned to");
    }

    @Test
    void upsertTicket_statusAndMessageChangedAndAssigneeRemoved_setsCorrectType() {
        TicketDTO old = existingTicket("t-smu", "new", "old msg");
        old.setAssignee_user_email("tech@sclera.io");
        when(ticketRepository.getNativeTicketById("t-smu")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-smu", "open", "new msg");
        dto.setAssignee_user_email(null);
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.STATUS_AND_MESSAGE_UPDATED_AND_UNASSIGNED.getValue()));
        verify(ticketHistoryService).addNativeTicketHistory(
                any(), any(), any(), actionMessageCaptor.capture(), any(), any(), any());
        assertThat(actionMessageCaptor.getValue())
                .contains("status changed to").contains("message updated").contains("unassigned");
    }

    @Test
    void upsertTicket_statusChangedAndAssigneeAdded_setsCorrectType() {
        TicketDTO old = existingTicket("t-sa", "new", "same msg");
        old.setAssignee_user_email(null);
        when(ticketRepository.getNativeTicketById("t-sa")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-sa", "open", "same msg");
        dto.setAssignee_user_email("tech@sclera.io");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.STATUS_UPDATED_AND_ASSIGNED.getValue()));
        verify(ticketHistoryService).addNativeTicketHistory(
                any(), any(), any(), actionMessageCaptor.capture(), any(), any(), any());
        assertThat(actionMessageCaptor.getValue()).contains("status updated to").contains("assigned to");
    }

    @Test
    void upsertTicket_statusChangedAndAssigneeRemoved_setsCorrectType() {
        TicketDTO old = existingTicket("t-su", "new", "same msg");
        old.setAssignee_user_email("tech@sclera.io");
        when(ticketRepository.getNativeTicketById("t-su")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-su", "open", "same msg");
        dto.setAssignee_user_email(null);
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.STATUS_UPDATED_AND_UNASSIGNED.getValue()));
        verify(ticketHistoryService).addNativeTicketHistory(
                any(), any(), any(), actionMessageCaptor.capture(), any(), any(), any());
        assertThat(actionMessageCaptor.getValue()).contains("status changed to").contains("unassigned");
    }

    @Test
    void upsertTicket_messageChangedAndAssigneeAdded_setsCorrectType() {
        TicketDTO old = existingTicket("t-ma", "open", "old msg");
        old.setAssignee_user_email(null);
        when(ticketRepository.getNativeTicketById("t-ma")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-ma", "open", "new msg");
        dto.setAssignee_user_email("tech@sclera.io");
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.MESSAGE_UPDATED_AND_ASSIGNED.getValue()));
        verify(ticketHistoryService).addNativeTicketHistory(
                any(), any(), any(), actionMessageCaptor.capture(), any(), any(), any());
        assertThat(actionMessageCaptor.getValue()).contains("message updated").contains("assigned to");
    }

    @Test
    void upsertTicket_messageChangedAndAssigneeRemoved_setsCorrectType() {
        TicketDTO old = existingTicket("t-mu", "open", "old msg");
        old.setAssignee_user_email("tech@sclera.io");
        when(ticketRepository.getNativeTicketById("t-mu")).thenReturn(old);

        TicketDTO dto = updateTicketDto("t-mu", "open", "new msg");
        dto.setAssignee_user_email(null);
        ticketService.upsertTicket("bob", "v1", dto);

        assertThat(dto.getType()).isEqualTo(String.valueOf(TicketType.MESSAGE_UPDATED_AND_UNASSIGNED.getValue()));
        verify(ticketHistoryService).addNativeTicketHistory(
                any(), any(), any(), actionMessageCaptor.capture(), any(), any(), any());
        assertThat(actionMessageCaptor.getValue()).contains("message updated").contains("unassigned");
    }

    // ── getTicketNameByCategory — service_request branch ─────────────────────

    @Test
    void upsertTicket_newServiceRequestTicket_setsNameFromRequestType() {
        when(ticketRepository.findMaxTicketNumberByPrefix("SRN-v1")).thenReturn(null);

        TicketDTO dto = newTicketDto(null, "service_request");
        dto.setRequest_type("Hardware Replacement");
        ticketService.upsertTicket("alice", "v1", dto);

        assertThat(dto.getName()).isEqualTo("Hardware Replacement");
        assertThat(dto.getNumber()).isEqualTo("SRN-v1000001");
    }

    // ── getAllTicketsByDeviceId ────────────────────────────────────────────────

    @Test
    void getAllTicketsByDeviceId_computesOffsetAndDelegatesToRepository() {
        when(ticketRepository.getAllNativeTicketsByDeviceId(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        TicketFilterDTO filter = new TicketFilterDTO();
        ticketService.getAllTicketsByDeviceId("alice", "dev-1", 10, 3, filter);

        verify(ticketRepository).getAllNativeTicketsByDeviceId(eq("dev-1"), isNull(), eq("alice"), eq(PageRequest.of(2, 10)));
    }

    // ── getNativeTicketDetailsById ────────────────────────────────────────────

    @Test
    void getNativeTicketDetailsById_returnsEnrichedTicket() {
        TicketDTO ticket = existingTicket("t-1", "open", "msg");
        ticket.setAssignee_user_email("tech@sclera.io");
        when(ticketRepository.getNativeTicketById("t-1")).thenReturn(ticket);

        TicketDTO result = ticketService.getNativeTicketDetailsById("t-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("t-1");
        assertThat(result.getAssignee_user_email()).isEqualTo("tech@sclera.io");
    }

    @Test
    void getNativeTicketDetailsById_returnsNullWhenNotFound() {
        when(ticketRepository.getNativeTicketById("missing")).thenReturn(null);

        assertThat(ticketService.getNativeTicketDetailsById("missing")).isNull();
    }

    // ── updateTicketAssigneeByUserEmail ───────────────────────────────────────

    @Test
    void updateTicketAssigneeByUserEmail_delegatesToRepository() {
        ticketService.updateTicketAssigneeByUserEmail("user@test.com");

        verify(ticketRepository).updateTicketAssigneeByUserEmail("user@test.com");
    }

    // ── getTicketCountByDeviceId / getOpenTicketStatus (device-asset read-back) ─

    @Test
    void getTicketCountByDeviceId_delegatesToRepository() {
        when(ticketRepository.getTicketCountByDeviceId("dev-1")).thenReturn(5);

        assertThat(ticketService.getTicketCountByDeviceId("dev-1")).isEqualTo(5);
    }

    @Test
    void getOpenTicketStatus_trueWhenOpenTicketsExist() {
        when(ticketRepository.getOpenTicketStatus("dev-1")).thenReturn(2);

        assertThat(ticketService.getOpenTicketStatus("dev-1")).isTrue();
    }

    @Test
    void getOpenTicketStatus_falseWhenNoOpenTickets() {
        when(ticketRepository.getOpenTicketStatus("dev-1")).thenReturn(0);

        assertThat(ticketService.getOpenTicketStatus("dev-1")).isFalse();
    }

    @Test
    void getOpenTicketStatus_falseWhenRepositoryReturnsNull() {
        when(ticketRepository.getOpenTicketStatus("dev-1")).thenReturn(null);

        assertThat(ticketService.getOpenTicketStatus("dev-1")).isFalse();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private TicketDTO newTicketDto(String id, String category) {
        TicketDTO dto = new TicketDTO();
        dto.setId(id);
        dto.setCategory(category);
        dto.setDevice_id("device-1");
        dto.setUser_message("Some message");
        return dto;
    }

    private TicketDTO updateTicketDto(String id, String newStatus, String newMessage) {
        TicketDTO dto = new TicketDTO();
        dto.setId(id);
        dto.setStatus(newStatus);
        dto.setUser_message(newMessage);
        dto.setDevice_id("device-1");
        return dto;
    }

    private TicketDTO existingTicket(String id, String status, String message) {
        TicketDTO old = new TicketDTO();
        old.setId(id);
        old.setStatus(status);
        old.setUser_message(message);
        old.setNumber("000001");
        old.setName("Test Ticket");
        old.setDescription("desc");
        old.setCreated_by("original");
        old.setCreated_at(BigInteger.valueOf(1000000L));
        old.setDevice_id("device-1");
        return old;
    }

    private TicketFilterDTO filterWithStatus(Set<String> statusSet) {
        TicketFilterDTO f = new TicketFilterDTO();
        f.setStatus(statusSet);
        return f;
    }
}
