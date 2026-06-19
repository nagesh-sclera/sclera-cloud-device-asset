package io.sclera.workorder.service.impl;

import io.sclera.workorder.service.TicketHistoryService;

import com.fasterxml.uuid.Generators;
import io.sclera.workorder.dto.TicketHistoryDTO;
import io.sclera.workorder.entity.TicketHistory;
import io.sclera.workorder.repository.TicketHistoryRepository;
import io.sclera.workorder.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

/** Default {@link TicketHistoryService} implementation backed by repository queries. */
@Slf4j
@Service
@Transactional
public class TicketHistoryServiceImpl implements TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketRepository ticketRepository;

    public TicketHistoryServiceImpl(TicketHistoryRepository ticketHistoryRepository,
                                    TicketRepository ticketRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * {@inheritDoc} Generates a time-based (v1) UUID for the entry id, then persists the
     * entry via {@code save()} (replaces the former native {@code INSERT … VALUES}). The
     * parent ticket is attached as a lazy reference ({@link TicketRepository#getReferenceById})
     * so only the {@code ticket_id} foreign key is written — no extra SELECT.
     */
    @Override
    public void addNativeTicketHistory(String message, BigInteger timestamp, String status, String action_message, String ticket_number, String username, String ticket_id) {
        String id = Generators.timeBasedGenerator().generate().toString();
        TicketHistory history = new TicketHistory(id, message, timestamp, status, action_message,
                ticket_number, username, ticketRepository.getReferenceById(ticket_id));
        ticketHistoryRepository.save(history);
        log.info("Added ticket history id={} for ticket_id={} ticket_number={} status={} by user={}",
                id, ticket_id, ticket_number, status, username);
    }

    /** {@inheritDoc} Converts the 1-based page number to a Pageable (LIMIT/OFFSET) and runs the paged query. */
    @Override
    @Transactional(readOnly = true)
    public Set<TicketHistoryDTO> getTicketHistoryByTicketId(String ticket_id, Integer pageno, Integer pagesize) {
        log.debug("Fetching ticket history for ticket_id={} (pageno={}, pagesize={})", ticket_id, pageno, pagesize);
        return new LinkedHashSet<>(ticketHistoryRepository.getTicketHistoryByTicketId(ticket_id, PageRequest.of(pageno - 1, pagesize)));
    }

    /** {@inheritDoc} Converts the 1-based page number to a Pageable (LIMIT/OFFSET) and runs the paged query. */
    @Override
    @Transactional(readOnly = true)
    public Set<TicketHistoryDTO> getAllTicketHistoryByDeviceId(String device_id, Integer pageno, Integer pagesize) {
        log.debug("Fetching all ticket history for device_id={} (pageno={}, pagesize={})", device_id, pageno, pagesize);
        return new LinkedHashSet<>(ticketHistoryRepository.getAllTicketHistoryByDeviceId(device_id, PageRequest.of(pageno - 1, pagesize)));
    }

}
