package io.sclera.it;

import io.sclera.Repository.ApplicationUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ApplicationUserRepository} after conversion from native SQL to JPQL.
 *
 * Three @Sql phases:
 *  - BEFORE_TEST_CLASS:  create schema (idempotent via IF NOT EXISTS)
 *  - BEFORE_TEST_METHOD: seed minimal FK-respecting rows
 *  - AFTER_TEST_METHOD:  clean up seed rows
 *
 * Bulk-write assertions use scalar JPQL reads via EntityManager to avoid
 * loading eager association graphs (ApplicationUser.managedSoftware is lazy,
 * but its FK target ManagedSoftware carries @Lob-free fields so it is safe here).
 *
 * Seed summary (from application-user-pilot.sql):
 *   au-1 email=user1@example.com  managed_software=ms-au1
 *   au-2 email=user2@example.com  managed_software=ms-au1
 *   au-3 email=user3@example.com  managed_software=NULL
 */
@Sql(scripts = "/schema-pg.sql",                           executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/application-user-pilot.sql",         executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-application-user-pilot.sql",      executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class ApplicationUserRepositoryIT extends PostgresJpaIT {

    @Autowired
    ApplicationUserRepository repo;

    @PersistenceContext
    EntityManager em;

    // ── findEmailsByUserIds ─────────────────────────────────────────────────────

    @Test
    void findEmailsByUserIds_returnsMatchingEmails() {
        Set<String> emails = repo.findEmailsByUserIds(List.of("au-1", "au-3", "no-such"));
        assertThat(emails).containsExactlyInAnyOrder("user1@example.com", "user3@example.com");
    }

    @Test
    void findEmailsByUserIds_emptyInput_returnsEmpty() {
        Set<String> emails = repo.findEmailsByUserIds(List.of());
        assertThat(emails).isEmpty();
    }

    // ── findExistingUserIds ─────────────────────────────────────────────────────

    @Test
    void findExistingUserIds_returnsOnlyPresentIds() {
        Set<String> found = repo.findExistingUserIds(Set.of("au-1", "au-2", "no-such-id"));
        assertThat(found).containsExactlyInAnyOrder("au-1", "au-2");
    }

    // ── findUserIdsByIds ────────────────────────────────────────────────────────

    @Test
    void findUserIdsByIds_returnsExistingIds() {
        Set<String> found = repo.findUserIdsByIds(Set.of("au-1", "au-3", "ghost"));
        assertThat(found).containsExactlyInAnyOrder("au-1", "au-3");
    }

    // ── clearManagedSoftwareId ──────────────────────────────────────────────────

    @Test
    void clearManagedSoftwareId_clearsColumnForGivenIds() {
        int rows = repo.clearManagedSoftwareId(Set.of("au-1", "au-2"));
        assertThat(rows).isEqualTo(2);

        String ms1 = (String) em.createQuery(
                "SELECT u.managedSoftwareId FROM ApplicationUser u WHERE u.id = 'au-1'")
                .getSingleResult();
        assertThat(ms1).isNull();

        String ms2 = (String) em.createQuery(
                "SELECT u.managedSoftwareId FROM ApplicationUser u WHERE u.id = 'au-2'")
                .getSingleResult();
        assertThat(ms2).isNull();
    }

    // ── updateManagedSoftwareIdByUserIds ────────────────────────────────────────

    @Test
    void updateManagedSoftwareIdByUserIds_assignsManagedSoftware() {
        int rows = repo.updateManagedSoftwareIdByUserIds(Set.of("au-3"), "ms-au2");
        assertThat(rows).isEqualTo(1);

        String ms = (String) em.createQuery(
                "SELECT u.managedSoftwareId FROM ApplicationUser u WHERE u.id = 'au-3'")
                .getSingleResult();
        assertThat(ms).isEqualTo("ms-au2");
    }

    // ── updateManagedSoftwareIdByUserId (single) ────────────────────────────────

    @Test
    void updateManagedSoftwareIdByUserId_assignsManagedSoftware() {
        int rows = repo.updateManagedSoftwareIdByUserId("au-3", "ms-au1");
        assertThat(rows).isEqualTo(1);

        String ms = (String) em.createQuery(
                "SELECT u.managedSoftwareId FROM ApplicationUser u WHERE u.id = 'au-3'")
                .getSingleResult();
        assertThat(ms).isEqualTo("ms-au1");
    }

    // ── clearManagedSoftwareByManagedSoftwareIds ────────────────────────────────

    @Test
    void clearManagedSoftwareByManagedSoftwareIds_clearsAllUsersOfGivenSoftware() {
        int rows = repo.clearManagedSoftwareByManagedSoftwareIds(List.of("ms-au1"));
        assertThat(rows).isEqualTo(2); // au-1 and au-2 both reference ms-au1

        List<String> msIds = em.createQuery(
                "SELECT u.managedSoftwareId FROM ApplicationUser u WHERE u.id IN ('au-1','au-2')",
                String.class)
                .getResultList();
        // Both should now be NULL — the list will have 2 null elements
        assertThat(msIds).hasSize(2);
        assertThat(msIds).allSatisfy(v -> assertThat(v).isNull());
    }

    // ── deleteApplicationUsersByIds ─────────────────────────────────────────────

    @Test
    void deleteApplicationUsersByIds_deletesSpecifiedRows() {
        int rows = repo.deleteApplicationUsersByIds(Set.of("au-1", "au-2"));
        assertThat(rows).isEqualTo(2);

        long count = (long) em.createQuery(
                "SELECT COUNT(u) FROM ApplicationUser u WHERE u.id IN ('au-1','au-2')")
                .getSingleResult();
        assertThat(count).isZero();
    }

    // ── deleteApplicationUsersById (single) ────────────────────────────────────

    @Test
    void deleteApplicationUsersById_deletesSingleRow() {
        int rows = repo.deleteApplicationUsersById("au-3");
        assertThat(rows).isEqualTo(1);

        long count = (long) em.createQuery(
                "SELECT COUNT(u) FROM ApplicationUser u WHERE u.id = 'au-3'")
                .getSingleResult();
        assertThat(count).isZero();
    }

    // ── deleteByManagedSoftwareId ────────────────────────────────────────────────

    @Test
    void deleteByManagedSoftwareId_deletesAllUsersReferencingSoftware() {
        int rows = repo.deleteByManagedSoftwareId("ms-au1");
        assertThat(rows).isEqualTo(2); // au-1 and au-2

        long count = (long) em.createQuery(
                "SELECT COUNT(u) FROM ApplicationUser u WHERE u.managedSoftwareId = 'ms-au1'")
                .getSingleResult();
        assertThat(count).isZero();
    }
}
