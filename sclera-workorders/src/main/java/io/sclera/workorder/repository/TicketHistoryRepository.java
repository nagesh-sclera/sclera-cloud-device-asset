package io.sclera.workorder.repository;


import io.sclera.workorder.dto.TicketHistoryDTO;
import io.sclera.workorder.entity.TicketHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, String> {


    /**
     * Ticket history for one ticket, newest first. Converted from the entity's
     * {@code TicketHistory.getTicketHistoryByTicketId} named native query to a JPQL
     * constructor projection over the {@code TicketHistory -> Ticket} association;
     * {@code LIMIT/OFFSET} is supplied by {@code Pageable}.
     */
    @Query("SELECT new io.sclera.workorder.dto.TicketHistoryDTO(" +
            "th.id, th.message, th.timestamp, th.status, th.action_message, th.ticket_number, th.created_by, t.id, t.name) " +
            "FROM TicketHistory th LEFT JOIN th.ticket t " +
            "WHERE th.ticket.id = ?1 " +
            "ORDER BY th.timestamp DESC")
    List<TicketHistoryDTO> getTicketHistoryByTicketId(String ticket_id, Pageable pageable);


    /**
     * Ticket history for every ticket on a device, newest first. Converted from the
     * entity's {@code TicketHistory.getAllTicketHistoryByDeviceId} named native query
     * to JPQL; {@code LIMIT/OFFSET} is supplied by {@code Pageable}.
     */
    @Query("SELECT new io.sclera.workorder.dto.TicketHistoryDTO(" +
            "th.id, th.message, th.timestamp, th.status, th.action_message, th.ticket_number, th.created_by, t.id, t.name) " +
            "FROM TicketHistory th LEFT JOIN th.ticket t " +
            "WHERE t.device = ?1 " +
            "ORDER BY th.timestamp DESC")
    List<TicketHistoryDTO> getAllTicketHistoryByDeviceId(String deviceId, Pageable pageable);


}
