package io.sclera.it;

import io.sclera.Repository.TechnicianRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                   executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/technician-pilot.sql",       executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-technician-pilot.sql",    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class TechnicianRepositoryIT extends PostgresJpaIT {

    @Autowired
    TechnicianRepository repo;

    @PersistenceContext
    EntityManager em;

    // -----------------------------------------------------------------------
    // updateTechnician — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void updateTechnician_updatesAllFields() {
        int rows = repo.updateTechnician(
                "tech-r-1",
                "alice-new@example.com", "9999999999", "+44",
                "Alice Updated", "ops", "senior",
                "America/New_York", "superadmin", 1800000000000L, null);
        assertThat(rows).isEqualTo(1);
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT t.email, t.name, t.department, t.timeZone " +
                "FROM Technician t WHERE t.id = 'tech-r-1'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("alice-new@example.com");
        assertThat(row[1]).isEqualTo("Alice Updated");
        assertThat(row[2]).isEqualTo("ops");
        assertThat(row[3]).isEqualTo("America/New_York");
    }

    // -----------------------------------------------------------------------
    // updateTechnicianByEmailAndPhone — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void updateTechnicianByEmailAndPhone_updatesMatchingRow() {
        int rows = repo.updateTechnicianByEmailAndPhone(
                "+33", "Bob Renamed", "field", "technician",
                "Europe/Paris", "admin", 1800000000001L, null,
                "bob@example.com", "2222222222");
        assertThat(rows).isEqualTo(1);
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT t.name, t.countryCode, t.department " +
                "FROM Technician t WHERE t.email = 'bob@example.com'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("Bob Renamed");
        assertThat(row[1]).isEqualTo("+33");
        assertThat(row[2]).isEqualTo("field");
    }

    @Test
    void updateTechnicianByEmailAndPhone_noMatchReturnsZero() {
        int rows = repo.updateTechnicianByEmailAndPhone(
                "+1", "Ghost", "none", "none",
                "UTC", "admin", 1L, null,
                "ghost@example.com", "0000000000");
        assertThat(rows).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // deleteTechnicianById — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void deleteTechnicianById_removesRow() {
        repo.deleteTechnicianById("tech-r-1");
        em.flush();
        assertThat(repo.count()).isEqualTo(2);
        List<String> ids = em.createQuery(
                "SELECT t.id FROM Technician t", String.class).getResultList();
        assertThat(ids).containsExactlyInAnyOrder("tech-r-2", "tech-r-3");
    }

    // -----------------------------------------------------------------------
    // getTechnicianNameById — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void getTechnicianNameById_returnsCorrectName() {
        String name = repo.getTechnicianNameById("tech-r-2");
        assertThat(name).isEqualTo("Bob");
    }

    @Test
    void getTechnicianNameById_returnsNullForMissing() {
        String name = repo.getTechnicianNameById("no-such-id");
        assertThat(name).isNull();
    }

    // -----------------------------------------------------------------------
    // getUniqueTechnicianDepartments — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void getUniqueTechnicianDepartments_returnsDistinctValues() {
        List<String> depts = repo.getUniqueTechnicianDepartments();
        assertThat(depts).containsExactlyInAnyOrder("engineering", "operations");
    }

    // -----------------------------------------------------------------------
    // findExistingTechniciansByIds — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void findExistingTechniciansByIds_returnsOnlyExisting() {
        Set<String> found = repo.findExistingTechniciansByIds(
                List.of("tech-r-1", "tech-r-2", "tech-r-nonexistent"));
        assertThat(found).containsExactlyInAnyOrder("tech-r-1", "tech-r-2");
    }

    // -----------------------------------------------------------------------
    // deleteTechniciansByIds — CONVERTED
    // -----------------------------------------------------------------------

    @Test
    void deleteTechniciansByIds_removesMatchingRows() {
        int deleted = repo.deleteTechniciansByIds(Set.of("tech-r-1", "tech-r-2"));
        assertThat(deleted).isEqualTo(2);
        em.flush();
        assertThat(repo.count()).isEqualTo(1);
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
