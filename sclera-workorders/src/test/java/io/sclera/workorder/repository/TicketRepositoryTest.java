package io.sclera.workorder.repository;

import io.sclera.workorder.dto.TicketDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice test for the ORM ticket upsert
 * ({@link TicketRepositoryImpl#upsertTicketNative}) against H2 in PostgreSQL mode.
 *
 * Proves the load-then-apply upsert reproduces the former native
 * {@code INSERT ... ON CONFLICT (id) DO UPDATE} exactly:
 *   - insert sets every column (and {@code is_deleted = false});
 *   - update overwrites only the eight ON-CONFLICT columns, preserving the rest;
 *   - a null assignee / null close fields are WRITTEN (cleared), not preserved.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository repository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void upsert_insertsNewTicket_withAllColumns_andIsDeletedFalse() {
        repository.upsertTicketNative("t-1", "NUM-1", "new", "user msg", "1", "the description",
                "incident_request", "req-type", "creator", BigInteger.valueOf(100), null, null,
                null, null, "dev-1", "assignee@x.com", "Ticket Name");
        em.flush();
        em.clear();

        TicketDTO dto = repository.getNativeTicketById("t-1");
        assertThat(dto).isNotNull();
        assertThat(dto.getNumber()).isEqualTo("NUM-1");
        assertThat(dto.getStatus()).isEqualTo("new");
        assertThat(dto.getUser_message()).isEqualTo("user msg");
        assertThat(dto.getType()).isEqualTo("1");
        assertThat(dto.getDescription()).isEqualTo("the description");
        assertThat(dto.getCategory()).isEqualTo("incident_request");
        assertThat(dto.getRequest_type()).isEqualTo("req-type");
        assertThat(dto.getCreated_by()).isEqualTo("creator");
        assertThat(dto.getCreated_at()).isEqualTo(BigInteger.valueOf(100));
        assertThat(dto.getDevice_id()).isEqualTo("dev-1");
        assertThat(dto.getAssignee_user_email()).isEqualTo("assignee@x.com");
        assertThat(dto.getName()).isEqualTo("Ticket Name");

        Object isDeleted = em.createNativeQuery("SELECT is_deleted FROM ticket WHERE id = 't-1'")
                .getSingleResult();
        assertThat(isDeleted).isEqualTo(false);
    }

    @Test
    void upsert_updatesOnlyOnConflictColumns_preservingTheRest() {
        repository.upsertTicketNative("t-2", "NUM-2", "new", "orig msg", "1", "orig desc",
                "incident_request", "orig-rt", "creator", BigInteger.valueOf(100), null, null,
                null, null, "dev-1", "old@x.com", "Orig Name");
        em.flush();
        em.clear();

        // Pass DIFFERENT values for the non-ON-CONFLICT columns to prove they are ignored on update.
        repository.upsertTicketNative("t-2", "DIFFERENT-NUM", "closed", "new msg", "9", "DIFFERENT desc",
                "DIFFERENT-cat", "DIFFERENT-rt", "DIFFERENT-creator", BigInteger.valueOf(999), "updater",
                BigInteger.valueOf(200), "closer", BigInteger.valueOf(300), "DIFFERENT-dev", "new@x.com",
                "DIFFERENT Name");
        em.flush();
        em.clear();

        TicketDTO dto = repository.getNativeTicketById("t-2");
        // The eight ON CONFLICT columns are updated:
        assertThat(dto.getStatus()).isEqualTo("closed");
        assertThat(dto.getUser_message()).isEqualTo("new msg");
        assertThat(dto.getType()).isEqualTo("9");
        assertThat(dto.getUpdated_by()).isEqualTo("updater");
        assertThat(dto.getUpdated_at()).isEqualTo(BigInteger.valueOf(200));
        assertThat(dto.getClosed_by()).isEqualTo("closer");
        assertThat(dto.getClosed_at()).isEqualTo(BigInteger.valueOf(300));
        assertThat(dto.getAssignee_user_email()).isEqualTo("new@x.com");
        // Everything else keeps its stored value:
        assertThat(dto.getNumber()).isEqualTo("NUM-2");
        assertThat(dto.getDescription()).isEqualTo("orig desc");
        assertThat(dto.getCategory()).isEqualTo("incident_request");
        assertThat(dto.getRequest_type()).isEqualTo("orig-rt");
        assertThat(dto.getCreated_by()).isEqualTo("creator");
        assertThat(dto.getCreated_at()).isEqualTo(BigInteger.valueOf(100));
        assertThat(dto.getDevice_id()).isEqualTo("dev-1");
        assertThat(dto.getName()).isEqualTo("Orig Name");
    }

    @Test
    void upsert_updateWithNulls_clearsAssigneeAndCloseFields_notPreserved() {
        repository.upsertTicketNative("t-3", "NUM-3", "closed", "msg", "1", "desc", "cat", "rt",
                "creator", BigInteger.valueOf(100), "u1", BigInteger.valueOf(1), "closer",
                BigInteger.valueOf(2), "dev", "assignee@x.com", "Name");
        em.flush();
        em.clear();

        // Reopen + unassign: null assignee and null close fields must be WRITTEN (cleared).
        repository.upsertTicketNative("t-3", "NUM-3", "open", "msg", "1", "desc", "cat", "rt",
                "creator", BigInteger.valueOf(100), "u2", BigInteger.valueOf(3), null, null, "dev",
                null, "Name");
        em.flush();
        em.clear();

        TicketDTO dto = repository.getNativeTicketById("t-3");
        assertThat(dto.getStatus()).isEqualTo("open");
        assertThat(dto.getAssignee_user_email()).isNull();
        assertThat(dto.getClosed_by()).isNull();
        assertThat(dto.getClosed_at()).isNull();
    }

    // ── findMaxTicketNumberByPrefix ───────────────────────────────────────────
    // Semantics preserved from the former native query
    //   SELECT MAX(CASE WHEN SUBSTRING(number FROM LENGTH(?1)+1) ~ '^[0-9]+$'
    //               THEN CAST(SUBSTRING(number FROM LENGTH(?1)+1) AS INTEGER) ELSE 0 END)
    //   FROM ticket WHERE number LIKE CONCAT(?1,'%') AND is_deleted = false
    //   - matching rows whose suffix is all-digits contribute their integer value
    //   - matching rows whose suffix is non-numeric contribute 0
    //   - NO matching rows  → MAX over the empty set → null (caller maps null→0)

    /** Inserts a non-deleted ticket carrying the given number (other columns are filler). */
    private void seedTicket(String id, String number) {
        repository.upsertTicketNative(id, number, "new", "msg", "1", "desc",
                "incident_request", "rt", "creator", BigInteger.valueOf(1), null, null,
                null, null, "dev", "a@x.com", "Name");
    }

    @Test
    void findMax_emptyPrefix_returnsHighestNumericNumber() {
        seedTicket("m-1", "000001");
        seedTicket("m-2", "000005");
        seedTicket("m-3", "000003");
        em.flush();
        em.clear();

        assertThat(repository.findMaxTicketNumberByPrefix("")).isEqualTo(5);
    }

    @Test
    void findMax_nonNumericNumbers_countAsZero() {
        seedTicket("m-4", "ABC");
        em.flush();
        em.clear();

        // A matching row exists, so the result is 0 (the CASE ELSE 0 branch), not null.
        assertThat(repository.findMaxTicketNumberByPrefix("")).isEqualTo(0);
    }

    @Test
    void findMax_noMatchingRows_returnsNull() {
        seedTicket("m-5", "000007");
        em.flush();
        em.clear();

        // No row's number starts with "FOO" → MAX over empty set → null.
        assertThat(repository.findMaxTicketNumberByPrefix("FOO")).isNull();
    }

    @Test
    void findMax_excludesSoftDeletedTickets() {
        seedTicket("m-6", "000010");
        seedTicket("m-7", "000002");
        em.flush();
        em.clear();

        repository.deleteTicketById("m-6"); // soft-delete the higher number
        em.flush();
        em.clear();

        assertThat(repository.findMaxTicketNumberByPrefix("")).isEqualTo(2);
    }

    @Test
    void findMax_respectsPrefix_andStripsItBeforeParsing() {
        seedTicket("m-8", "INC-000007");
        seedTicket("m-9", "SRN-000009");
        em.flush();
        em.clear();

        // Only the INC- row matches; its numeric suffix after the prefix is 7.
        assertThat(repository.findMaxTicketNumberByPrefix("INC-")).isEqualTo(7);
    }
}
