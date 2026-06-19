package io.sclera.workorder.repository;

import io.sclera.workorder.dto.TicketHistoryDTO;
import io.sclera.workorder.entity.TicketHistory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice test for the ORM ticket-history insert against H2 in PostgreSQL mode.
 *
 * Proves the {@code save()}-based insert (which replaced the former native
 * {@code INSERT INTO ticket_history(...) VALUES(...)}) persists every column AND the
 * {@code ticket_id} foreign key, attaching the parent ticket as a lazy reference
 * exactly as {@code TicketHistoryServiceImpl.addNativeTicketHistory} does.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketHistoryRepositoryTest {

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void save_insertsRow_withColumnsAndTicketForeignKey() {
        // Parent ticket — the FK target.
        ticketRepository.upsertTicketNative("tk-1", "NUM-1", "open", "msg", "1", "desc",
                "incident_request", "rt", "creator", BigInteger.valueOf(1), null, null,
                null, null, "dev", "a@x.com", "Name");
        em.flush();

        // Mirror the service insert: lazy ticket reference (no extra SELECT) + save().
        TicketHistory history = new TicketHistory("h-1", "hist msg", BigInteger.valueOf(2000L),
                "open", "created ticket", "NUM-1", "alice", ticketRepository.getReferenceById("tk-1"));
        ticketHistoryRepository.save(history);
        em.flush();
        em.clear();

        // Read back through the JPQL projection — proves the columns + FK persisted.
        List<TicketHistoryDTO> rows =
                ticketHistoryRepository.getTicketHistoryByTicketId("tk-1", PageRequest.of(0, 10));

        assertThat(rows).hasSize(1);
        TicketHistoryDTO dto = rows.get(0);
        assertThat(dto.getId()).isEqualTo("h-1");
        assertThat(dto.getMessage()).isEqualTo("hist msg");
        assertThat(dto.getTimestamp()).isEqualTo(BigInteger.valueOf(2000L));
        assertThat(dto.getStatus()).isEqualTo("open");
        assertThat(dto.getAction_message()).isEqualTo("created ticket");
        assertThat(dto.getTicket_number()).isEqualTo("NUM-1");
        assertThat(dto.getCreated_by()).isEqualTo("alice");
        assertThat(dto.getTicket_id()).isEqualTo("tk-1");   // foreign key written correctly
    }
}
