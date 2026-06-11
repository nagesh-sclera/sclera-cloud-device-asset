package io.sclera.it;

import io.sclera.Repository.AiCallLogHistoryRepository;
import io.sclera.dto.AiCallLogHistoryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                           executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/ai-call-log-history-pilot.sql",      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-ai-call-log-history-pilot.sql",   executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AiCallLogHistoryRepositoryIT extends PostgresJpaIT {

    @Autowired
    AiCallLogHistoryRepository repo;

    @PersistenceContext
    EntityManager em;

    // -----------------------------------------------------------------------
    // getAiCallLogHistoryByAiCallLogId — JPQL constructor-expression projection
    // -----------------------------------------------------------------------

    @Test
    void getAiCallLogHistoryByAiCallLogId_returnsAllEntriesForLog_orderedAsc() {
        List<AiCallLogHistoryDTO> list = repo.getAiCallLogHistoryByAiCallLogId("acl1");

        assertThat(list).hasSize(2);
        // ordered by createdAt ASC: h1 (100) before h2 (200)
        assertThat(list.get(0).getId()).isEqualTo("h1");
        assertThat(list.get(0).getState()).isEqualTo("created");
        assertThat(list.get(0).getAiCallLogId()).isEqualTo("acl1");
        assertThat(list.get(0).getTechnicianId()).isEqualTo("t1");

        assertThat(list.get(1).getId()).isEqualTo("h2");
        assertThat(list.get(1).getState()).isEqualTo("in-progress");
    }

    @Test
    void getAiCallLogHistoryByAiCallLogId_returnsEmptyListWhenNoneExist() {
        assertThat(repo.getAiCallLogHistoryByAiCallLogId("nonexistent")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getAiCallLogHistoryById — JPQL constructor-expression projection
    // -----------------------------------------------------------------------

    @Test
    void getAiCallLogHistoryById_returnsCorrectDto() {
        AiCallLogHistoryDTO dto = repo.getAiCallLogHistoryById("h3");

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("h3");
        assertThat(dto.getState()).isEqualTo("resolved");
        assertThat(dto.getDescription()).isEqualTo("Network checked");
        assertThat(dto.getTechnicianId()).isEqualTo("t2");
        assertThat(dto.getAiCallLogId()).isEqualTo("acl2");
    }

    // -----------------------------------------------------------------------
    // findStatusesByAiCallLogId — JPQL scalar SELECT (note: filters by h.id, not aiCallLog.id)
    // -----------------------------------------------------------------------

    @Test
    void findStatusesByAiCallLogId_returnsStateForHistoryRecord() {
        // The JPQL query is: WHERE h.id = ?1  (history record id, not aiCallLog id)
        Set<String> states = repo.findStatusesByAiCallLogId("h1");
        assertThat(states).containsExactly("created");
    }

    // -----------------------------------------------------------------------
    // countByAiCallLogId — JPQL COUNT
    // -----------------------------------------------------------------------

    @Test
    void countByAiCallLogId_returnsCorrectCount() {
        assertThat(repo.countByAiCallLogId("acl1")).isEqualTo(2);
        assertThat(repo.countByAiCallLogId("acl2")).isEqualTo(1);
        assertThat(repo.countByAiCallLogId("nonexistent")).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // getLatestCallStatus — default wrapper over JPQL LIMIT-1 query
    // -----------------------------------------------------------------------

    @Test
    void getLatestCallStatus_returnsLatestStateForTechnicianAndLog() {
        // h1 (created_at=100), h2 (created_at=200) — latest is h2 → "in-progress"
        String latest = repo.getLatestCallStatus("acl1", "t1");
        assertThat(latest).isEqualTo("in-progress");
    }

    @Test
    void getLatestCallStatus_returnsNullWhenNoMatchingHistory() {
        assertThat(repo.getLatestCallStatus("acl1", "t2")).isNull();
    }

    // -----------------------------------------------------------------------
    // deleteAICallLogsHistoryByTechnicianIds — JPQL bulk DELETE
    // Verify via scalar JPQL read (not findById — entity has eager relations)
    // -----------------------------------------------------------------------

    @Test
    void deleteAICallLogsHistoryByTechnicianIds_deletesRowsForTechnician() {
        int deleted = repo.deleteAICallLogsHistoryByTechnicianIds(Set.of("t1"));
        assertThat(deleted).isEqualTo(2);

        // Verify via scalar JPQL — avoid loading full entity graph
        Long remaining = em.createQuery(
                "SELECT COUNT(h) FROM AiCallLogHistory h WHERE h.technician.id = 't1'", Long.class)
                .getSingleResult();
        assertThat(remaining).isEqualTo(0L);

        // t2 row should be untouched
        Long t2Remaining = em.createQuery(
                "SELECT COUNT(h) FROM AiCallLogHistory h WHERE h.technician.id = 't2'", Long.class)
                .getSingleResult();
        assertThat(t2Remaining).isEqualTo(1L);
    }
}
