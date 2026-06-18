package io.sclera.it;

import io.sclera.Repository.AiCallLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                 executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/ai-call-log-pilot.sql",    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-ai-call-log-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AiCallLogRepositoryIT extends PostgresJpaIT {

    @Autowired
    AiCallLogRepository repo;

    @PersistenceContext
    EntityManager em;

    // -----------------------------------------------------------------------
    // getAiCallLogIdByDeviceId — default wrapper (LIMIT 1, ORDER BY createdAt DESC, status != 'accepted')
    // -----------------------------------------------------------------------

    @Test
    void getAiCallLogIdByDeviceId_returnsLatestNonAccepted() {
        // dev1: acl1 (accepted, older) and acl2 (ongoing, newer) → should return acl2
        assertThat(repo.getAiCallLogIdByDeviceId("dev1")).isEqualTo("acl2");
    }

    @Test
    void getAiCallLogIdByDeviceId_returnsNullWhenNoneMatch() {
        // dev1 only has acl2 (ongoing); querying for nonexistent device returns null
        assertThat(repo.getAiCallLogIdByDeviceId("nonexistent")).isNull();
    }

    // -----------------------------------------------------------------------
    // getAiCallLogIdByDeviceIdForOnline — same logic as getAiCallLogIdByDeviceId
    // -----------------------------------------------------------------------

    @Test
    void getAiCallLogIdByDeviceIdForOnline_returnsLatestNonAccepted() {
        assertThat(repo.getAiCallLogIdByDeviceIdForOnline("dev1")).isEqualTo("acl2");
    }

    // -----------------------------------------------------------------------
    // getAiCallLogIssueTypeByDeviceId — default wrapper (LIMIT 1, ORDER BY createdAt DESC)
    // -----------------------------------------------------------------------

    @Test
    void getAiCallLogIssueTypeByDeviceId_returnsIssueTypeOfLatestNonAccepted() {
        // dev1 → latest non-accepted is acl2 → issueType "network"
        assertThat(repo.getAiCallLogIssueTypeByDeviceId("dev1")).isEqualTo("network");
    }

    @Test
    void getAiCallLogIssueTypeByDeviceId_returnsNullWhenNoMatch() {
        assertThat(repo.getAiCallLogIssueTypeByDeviceId("nonexistent")).isNull();
    }

    // -----------------------------------------------------------------------
    // getAiCallLogIssueTypeByAiCallLogId — JPQL scalar SELECT
    // -----------------------------------------------------------------------

    @Test
    void getAiCallLogIssueTypeByAiCallLogId_returnsCorrectIssueType() {
        assertThat(repo.getAiCallLogIssueTypeByAiCallLogId("acl3")).isEqualTo("software");
    }

    // -----------------------------------------------------------------------
    // getDeviceIdByAiCallLogId — JPQL relation navigation (a.device.id)
    // -----------------------------------------------------------------------

    @Test
    void getDeviceIdByAiCallLogId_returnsCorrectDeviceId() {
        assertThat(repo.getDeviceIdByAiCallLogId("acl2")).isEqualTo("dev1");
        assertThat(repo.getDeviceIdByAiCallLogId("acl3")).isEqualTo("dev2");
    }

    // -----------------------------------------------------------------------
    // getStatusRemovedByAiCallLogId — JPQL scalar SELECT
    // -----------------------------------------------------------------------

    @Test
    void getStatusRemovedByAiCallLogId_returnsStatus() {
        assertThat(repo.getStatusRemovedByAiCallLogId("acl1")).isEqualTo("accepted");
        assertThat(repo.getStatusRemovedByAiCallLogId("acl2")).isEqualTo("ongoing");
    }

    // -----------------------------------------------------------------------
    // updateStatus — JPQL bulk UPDATE
    // Verify via scalar JPQL read (avoid loading full entity with eager relations)
    // -----------------------------------------------------------------------

    @Test
    void updateStatus_updatesStatusForMatchingId() {
        repo.updateStatus("acl2", "resolved");

        // Read back via scalar JPQL to avoid eager-load issues
        String newStatus = em.createQuery(
                "SELECT a.status FROM AiCallLog a WHERE a.id = 'acl2'", String.class)
                .getSingleResult();
        assertThat(newStatus).isEqualTo("resolved");
    }

    @Test
    void updateStatus_doesNotAffectOtherRows() {
        repo.updateStatus("acl2", "resolved");

        String acl1Status = em.createQuery(
                "SELECT a.status FROM AiCallLog a WHERE a.id = 'acl1'", String.class)
                .getSingleResult();
        assertThat(acl1Status).isEqualTo("accepted");
    }

    // -----------------------------------------------------------------------
    // deleteAICallLogsByTechnicianIds — JPQL bulk DELETE
    // -----------------------------------------------------------------------

    @Test
    void deleteAICallLogsByTechnicianIds_deletesRowsForTechnicians() {
        int deleted = repo.deleteAICallLogsByTechnicianIds(Set.of("t1"));
        // t1 owns acl1 and acl2
        assertThat(deleted).isEqualTo(2);

        Long remaining = em.createQuery(
                "SELECT COUNT(a) FROM AiCallLog a WHERE a.technician.id = 't1'", Long.class)
                .getSingleResult();
        assertThat(remaining).isEqualTo(0L);

        // t2 row (acl3) should be untouched
        Long t2Remaining = em.createQuery(
                "SELECT COUNT(a) FROM AiCallLog a WHERE a.technician.id = 't2'", Long.class)
                .getSingleResult();
        assertThat(t2Remaining).isEqualTo(1L);
    }
}
