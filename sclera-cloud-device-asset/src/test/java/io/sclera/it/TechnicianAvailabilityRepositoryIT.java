package io.sclera.it;

import io.sclera.Repository.TechnicianAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                               executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/technician-availability-pilot.sql",      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-technician-availability-pilot.sql",   executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class TechnicianAvailabilityRepositoryIT extends PostgresJpaIT {

    @Autowired
    TechnicianAvailabilityRepository repo;

    @PersistenceContext
    EntityManager em;

    // -----------------------------------------------------------------------
    // findExistingTechnicianAvailabilityByIds — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void findExistingTechnicianAvailabilityByIds_returnsOnlyExisting() {
        Set<String> found = repo.findExistingTechnicianAvailabilityByIds(
                List.of("avail-1", "avail-2", "avail-nonexistent"));
        assertThat(found).containsExactlyInAnyOrder("avail-1", "avail-2");
    }

    @Test
    void findExistingTechnicianAvailabilityByIds_emptyWhenNoneMatch() {
        Set<String> found = repo.findExistingTechnicianAvailabilityByIds(
                List.of("no-match-1", "no-match-2"));
        assertThat(found).isEmpty();
    }

    // -----------------------------------------------------------------------
    // updateTechnicianAvailability — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void updateTechnicianAvailability_updatesFieldsCorrectly() {
        int rows = repo.updateTechnicianAvailability(
                "avail-1",
                1800000000000L, 1800086400000L,
                "10:00", "18:00",
                true, "monthly", "{\"days\":[\"FRI\"],\"exceptions\":[]}", "tech-avail-1");
        assertThat(rows).isEqualTo(1);
        em.flush();
        em.clear();

        // Read back via scalar JPQL to avoid loading the full entity graph
        Object[] row = (Object[]) em.createQuery(
                "SELECT ta.startDate, ta.endDate, ta.startTime, ta.endTime, ta.isAllDay, ta.frequency " +
                "FROM TechnicianAvailability ta WHERE ta.id = 'avail-1'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo(1800000000000L);
        assertThat(row[1]).isEqualTo(1800086400000L);
        assertThat(row[2]).isEqualTo("10:00");
        assertThat(row[3]).isEqualTo("18:00");
        assertThat(row[4]).isEqualTo(true);
        assertThat(row[5]).isEqualTo("monthly");
    }

    // -----------------------------------------------------------------------
    // deleteTechnicianAvailabilityById — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void deleteTechnicianAvailabilityById_removesRow() {
        repo.deleteTechnicianAvailabilityById("avail-1");
        em.flush();
        assertThat(repo.count()).isEqualTo(2);
        List<String> ids = em.createQuery(
                "SELECT ta.id FROM TechnicianAvailability ta", String.class)
                .getResultList();
        assertThat(ids).containsExactlyInAnyOrder("avail-2", "avail-3");
    }

    // -----------------------------------------------------------------------
    // deleteTechnicianAvailabilityByIds — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void deleteTechnicianAvailabilityByIds_removesMatchingRows() {
        int deleted = repo.deleteTechnicianAvailabilityByIds(Set.of("avail-1", "avail-2"));
        assertThat(deleted).isEqualTo(2);
        em.flush();
        assertThat(repo.count()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // deleteTechnicianAvailabilityByTechnicianIds — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void deleteTechnicianAvailabilityByTechnicianIds_removesAllForTechnicians() {
        int deleted = repo.deleteTechnicianAvailabilityByTechnicianIds(
                Set.of("tech-avail-1"));
        assertThat(deleted).isEqualTo(2);  // avail-1 and avail-2 belong to tech-avail-1
        em.flush();
        assertThat(repo.count()).isEqualTo(1);  // avail-3 belongs to tech-avail-2
    }

    // -----------------------------------------------------------------------
    // count — sanity
    // -----------------------------------------------------------------------

    @Test
    void contextLoads_andSeedRowsPresent() {
        assertThat(repo).isNotNull();
        assertThat(repo.count()).isEqualTo(3);
    }
}
