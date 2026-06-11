package io.sclera.it;

import io.sclera.Repository.DeviceInstalledAppsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DeviceInstalledAppsRepository} after conversion from native SQL to JPQL.
 *
 * Three @Sql phases:
 *  - BEFORE_TEST_CLASS:  create schema (idempotent via IF NOT EXISTS)
 *  - BEFORE_TEST_METHOD: seed minimal FK-respecting rows
 *  - AFTER_TEST_METHOD:  clean up seed rows
 *
 * Bulk-write assertions use scalar JPQL reads via EntityManager or scalar repository
 * projections rather than findById to avoid loading eager association graphs.
 */
@Sql(scripts = "/schema-pg.sql",                            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-installed-apps-pilot.sql",     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-installed-apps-pilot.sql",  executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceInstalledAppsRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceInstalledAppsRepository repo;

    @PersistenceContext
    EntityManager em;

    // ── getDeviceSpecIdsByManagedSoftwareId ────────────────────────────────────

    @Test
    void getDeviceSpecIdsByManagedSoftwareId_returnsAllMatchingSpecIds() {
        Set<String> specIds = repo.getDeviceSpecIdsByManagedSoftwareId("ms-1");
        // app-1 (spec-1), app-2 (spec-2), app-3 (spec-1) => distinct would give 2, but Set deduplicates naturally
        assertThat(specIds).contains("spec-1", "spec-2");
    }

    @Test
    void getDeviceSpecIdsByManagedSoftwareId_returnsEmptyForUnknown() {
        Set<String> specIds = repo.getDeviceSpecIdsByManagedSoftwareId("no-such-ms");
        assertThat(specIds).isEmpty();
    }

    // ── getRiskyDeviceSpecIdsByManagedSoftwareId ────────────────────────────────

    @Test
    void getRiskyDeviceSpecIdsByManagedSoftwareId_returnsOnlyRiskStatus1() {
        Set<String> riskyIds = repo.getRiskyDeviceSpecIdsByManagedSoftwareId("ms-1");
        // Only app-2 has risk_status=1 (spec-2)
        assertThat(riskyIds).containsExactlyInAnyOrder("spec-2");
    }

    // ── getCompliantRiskStatusCount ────────────────────────────────────────────

    @Test
    void getCompliantRiskStatusCount_countsRiskStatus0And2() {
        // app-1 (status=0) + app-3 (status=2) = 2 compliant
        assertThat(repo.getCompliantRiskStatusCount("ms-1")).isEqualTo(2);
    }

    @Test
    void getCompliantRiskStatusCount_returnsZeroForUnknownSoftware() {
        assertThat(repo.getCompliantRiskStatusCount("no-such-ms")).isEqualTo(0);
    }

    // ── updateDeviceIdBySerialNumber ───────────────────────────────────────────

    @Test
    void updateDeviceIdBySerialNumber_updatesDeviceIdForMatchingSpecId() {
        repo.updateDeviceIdBySerialNumber("spec-1", "dev-other");

        Long count = em.createQuery(
                "SELECT COUNT(d) FROM DeviceInstalledApps d WHERE d.deviceSpecificationId = 'spec-1' AND d.deviceId = 'dev-other'",
                Long.class)
                .getSingleResult();
        // app-1, app-3, app-4 all have spec-1
        assertThat(count).isEqualTo(3);
    }

    // ── updateRiskStatusForDevices ─────────────────────────────────────────────

    @Test
    void updateRiskStatusForDevices_updatesMultipleSpecIds() {
        Set<String> specIds = Set.of("spec-1", "spec-2");
        Integer updated = repo.updateRiskStatusForDevices(specIds, "ms-1", 3);
        // app-1, app-2, app-3 all match (spec-1 or spec-2, managed by ms-1)
        assertThat(updated).isEqualTo(3);

        Long count = em.createQuery(
                "SELECT COUNT(d) FROM DeviceInstalledApps d WHERE d.managedSoftwareId = 'ms-1' AND d.riskStatus = 3",
                Long.class)
                .getSingleResult();
        assertThat(count).isEqualTo(3);
    }

    // ── updateRiskStatusByDeviceSpecId ────────────────────────────────────────

    @Test
    void updateRiskStatusByDeviceSpecId_updatesExactRow() {
        Integer updated = repo.updateRiskStatusByDeviceSpecId("spec-2", "ms-1", 5);
        assertThat(updated).isEqualTo(1);

        Integer riskStatus = (Integer) em.createQuery(
                "SELECT d.riskStatus FROM DeviceInstalledApps d WHERE d.id = 'app-2'")
                .getSingleResult();
        assertThat(riskStatus).isEqualTo(5);
    }

    // ── clearRiskStatusByManagedSoftwareId ────────────────────────────────────

    @Test
    void clearRiskStatusByManagedSoftwareId_setsRiskStatusNullForAllLinked() {
        Integer updated = repo.clearRiskStatusByManagedSoftwareId("ms-1");
        // app-1, app-2, app-3 have ms-1; app-4 has no managed_software_id
        assertThat(updated).isEqualTo(3);

        Long stillSet = em.createQuery(
                "SELECT COUNT(d) FROM DeviceInstalledApps d WHERE d.managedSoftwareId = 'ms-1' AND d.riskStatus IS NOT NULL",
                Long.class)
                .getSingleResult();
        assertThat(stillSet).isZero();
    }

    // ── clearManagedSoftwareIdAndRiskStatus ───────────────────────────────────

    @Test
    void clearManagedSoftwareIdAndRiskStatus_clearsBothColumns() {
        Integer updated = repo.clearManagedSoftwareIdAndRiskStatus("ms-1");
        assertThat(updated).isEqualTo(3);

        Long stillLinked = em.createQuery(
                "SELECT COUNT(d) FROM DeviceInstalledApps d WHERE d.managedSoftwareId = 'ms-1'",
                Long.class)
                .getSingleResult();
        assertThat(stillLinked).isZero();

        Long stillHasRisk = em.createQuery(
                "SELECT COUNT(d) FROM DeviceInstalledApps d WHERE d.id IN ('app-1','app-2','app-3') AND d.riskStatus IS NOT NULL",
                Long.class)
                .getSingleResult();
        assertThat(stillHasRisk).isZero();
    }
}
