package io.sclera.it;

import io.sclera.Repository.ManagedSoftwareRepository;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ManagedSoftwareRepository} after conversion from native SQL to JPQL.
 *
 * Three @Sql phases:
 *  - BEFORE_TEST_CLASS:  create schema (idempotent via IF NOT EXISTS)
 *  - BEFORE_TEST_METHOD: seed minimal FK-respecting rows
 *  - AFTER_TEST_METHOD:  clean up seed rows
 *
 * Bulk-write assertions use scalar JPQL reads via EntityManager to avoid loading
 * full entity association graphs.
 */
@Sql(scripts = "/schema-pg.sql",                         executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/managed-software-pilot.sql",       executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-managed-software-pilot.sql",    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class ManagedSoftwareRepositoryIT extends PostgresJpaIT {

    @Autowired
    ManagedSoftwareRepository repo;

    @PersistenceContext
    EntityManager em;

    // ── getAllManagedSoftwares (Pageable) ────────────────────────────────────────

    @Test
    void getAllManagedSoftwares_conditionAll_returnsAll() {
        List<ManagedSoftwareDTO> result = repo.getAllManagedSoftwares("all", "null", PageRequest.of(0, 10));
        assertThat(result).hasSize(3);
    }

    @Test
    void getAllManagedSoftwares_conditionActive_returnsOnlyActive() {
        List<ManagedSoftwareDTO> result = repo.getAllManagedSoftwares("active", "null", PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ms-a1");
        assertThat(result.get(0).getStatus()).isEqualTo("active");
    }

    @Test
    void getAllManagedSoftwares_conditionExpired_returnsOnlyExpired() {
        List<ManagedSoftwareDTO> result = repo.getAllManagedSoftwares("expired", "null", PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ms-b2");
    }

    @Test
    void getAllManagedSoftwares_conditionOthers_returnsNeitherActiveNorExpired() {
        List<ManagedSoftwareDTO> result = repo.getAllManagedSoftwares("others", "null", PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ms-c3");
    }

    @Test
    void getAllManagedSoftwares_searchKey_filtersOnName() {
        List<ManagedSoftwareDTO> result = repo.getAllManagedSoftwares("all", "Antivirus", PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ms-a1");
    }

    @Test
    void getAllManagedSoftwares_paginationFirstPage_returnsPageSize() {
        List<ManagedSoftwareDTO> page = repo.getAllManagedSoftwares("all", "null", PageRequest.of(0, 2));
        assertThat(page).hasSize(2);
    }

    @Test
    void getAllManagedSoftwares_paginationSecondPage_returnsRemainder() {
        List<ManagedSoftwareDTO> page = repo.getAllManagedSoftwares("all", "null", PageRequest.of(1, 2));
        assertThat(page).hasSize(1);
    }

    // ── getManagedSoftwareById ──────────────────────────────────────────────────

    @Test
    void getManagedSoftwareById_returnsCorrectDto() {
        ManagedSoftwareDTO dto = repo.getManagedSoftwareById("ms-a1");
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Antivirus Pro");
        assertThat(dto.getVendor()).isEqualTo("VendorA");
        assertThat(dto.getSubscriptionType()).isEqualTo("monthly_fees");
        assertThat(dto.getUnitPrice()).isEqualTo(9.99);
        assertThat(dto.getApplicationId()).isEqualTo("app-001");
    }

    @Test
    void getManagedSoftwareById_unknownId_returnsNull() {
        ManagedSoftwareDTO dto = repo.getManagedSoftwareById("no-such-id");
        assertThat(dto).isNull();
    }

    // ── findExistingNames ────────────────────────────────────────────────────────

    @Test
    void findExistingNames_returnsOnlyPresentNames() {
        List<String> found = repo.findExistingNames(List.of("Antivirus Pro", "Office Suite", "Nonexistent"));
        assertThat(found).containsExactlyInAnyOrder("Antivirus Pro", "Office Suite");
    }

    // ── getManagedSoftwareIdByName ────────────────────────────────────────────────

    @Test
    void getManagedSoftwareIdByName_found_returnsId() {
        Optional<String> id = repo.getManagedSoftwareIdByName("Dev Tools");
        assertThat(id).isPresent().contains("ms-c3");
    }

    @Test
    void getManagedSoftwareIdByName_notFound_returnsEmpty() {
        Optional<String> id = repo.getManagedSoftwareIdByName("NoSuchSoftware");
        assertThat(id).isEmpty();
    }

    // ── getManagedSoftwareUsers ───────────────────────────────────────────────────

    @Test
    void getManagedSoftwareUsers_returnsAssociatedUserRows() {
        List<ManagedSoftwareUsersDTO> users = repo.getManagedSoftwareUsers("ms-a1");
        assertThat(users).hasSize(1);
        ManagedSoftwareUsersDTO u = users.get(0);
        assertThat(u.getUsername()).isEqualTo("alice");
        assertThat(u.getEmail()).isEqualTo("alice@example.com");
        assertThat(u.getRiskStatus()).isEqualTo(2);
        assertThat(u.getDeviceName()).isEqualTo("AliceLaptop");
        assertThat(u.getOsType()).isEqualTo("Linux");
    }

    @Test
    void getManagedSoftwareUsers_noUsers_returnsEmpty() {
        List<ManagedSoftwareUsersDTO> users = repo.getManagedSoftwareUsers("ms-b2");
        assertThat(users).isEmpty();
    }

    // ── COUNT methods ─────────────────────────────────────────────────────────────

    @Test
    void getAllStatusCounts_returnsTotal() {
        assertThat(repo.getAllStatusCounts()).isEqualTo(3);
    }

    @Test
    void getActiveStatusCounts_returnsActiveCount() {
        assertThat(repo.getActiveStatusCounts()).isEqualTo(1);
    }

    @Test
    void getExpiredStatusCounts_returnsExpiredCount() {
        assertThat(repo.getExpiredStatusCounts()).isEqualTo(1);
    }

    @Test
    void getOthersStatusCounts_returnsOthersCount() {
        assertThat(repo.getOthersStatusCounts()).isEqualTo(1);
    }

    @Test
    void getMonthlySubscribedCount_returnsMonthlyFees() {
        assertThat(repo.getMonthlySubscribedCount()).isEqualTo(1);
    }

    @Test
    void getYearlySubscribedCount_returnsAnnuallyCount() {
        assertThat(repo.getYearlySubscribedCount()).isEqualTo(1);
    }

    // ── findDistinctApplicationIds ────────────────────────────────────────────────

    @Test
    void findDistinctApplicationIds_excludesNulls() {
        Set<String> appIds = repo.findDistinctApplicationIds();
        // ms-c3 has null application_id, so only app-001 and app-002 appear
        assertThat(appIds).containsExactlyInAnyOrder("app-001", "app-002");
    }

    // ── getApplicationIdByManagedSoftwareId ───────────────────────────────────────

    @Test
    void getApplicationIdByManagedSoftwareId_returnsCorrectAppId() {
        String appId = repo.getApplicationIdByManagedSoftwareId("ms-a1");
        assertThat(appId).isEqualTo("app-001");
    }

    // ── findIdByApplicationId ────────────────────────────────────────────────────

    @Test
    void findIdByApplicationId_returnsCorrectId() {
        String id = repo.findIdByApplicationId("app-002");
        assertThat(id).isEqualTo("ms-b2");
    }

    // ── updateStatusById ─────────────────────────────────────────────────────────

    @Test
    void updateStatusById_updatesStatus() {
        repo.updateStatusById("ms-c3", "active");
        // Read back via scalar JPQL to avoid loading full entity associations
        String status = (String) em.createQuery(
                "SELECT ms.status FROM ManagedSoftware ms WHERE ms.id = 'ms-c3'")
                .getSingleResult();
        assertThat(status).isEqualTo("active");
    }
}
