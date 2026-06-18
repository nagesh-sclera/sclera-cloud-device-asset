package io.sclera.it;

import io.sclera.Repository.DeviceOnboardStatusRepository;
import io.sclera.dto.DeviceOnboardStatusDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting IT for the JPA-converted DeviceOnboardStatusRepository, run against real PostgreSQL.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-onboard-status-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-onboard-status-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceOnboardStatusRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceOnboardStatusRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void getOnboardAssetIdByDeviceId_returnsStatusRecordId() {
        assertThat(repo.getOnboardAssetIdByDeviceId("d1")).isEqualTo("dos1");
        assertThat(repo.getOnboardAssetIdByDeviceId("ghost")).isNull();
    }

    @Test
    void getAssetOnboardAssignees_returnsDistinctAssigneesForInProgressDevices() {
        // d1 (onboard_status 1) and d2 (2) qualify; d3 (0) is excluded
        Set<String> assignees = repo.getAssetOnboardAssignees();
        assertThat(assignees).containsExactlyInAnyOrder("a@x.com", "b@x.com");
        assertThat(assignees).doesNotContain("c@x.com");
    }

    @Test
    void getOnboardDataByDeviceId_returnsProjectionWithDeviceOnboardStatus() {
        DeviceOnboardStatusDTO dto = repo.getOnboardDataByDeviceId("d1");
        assertThat(dto.getAssignee_email()).isEqualTo("a@x.com");
        assertThat(dto.getImage_status()).isEqualTo(1);
        assertThat(dto.getGeolocation_status()).isEqualTo(2);
        assertThat(dto.getField_status()).isEqualTo(4);
        assertThat(dto.getOnboard_status()).isEqualTo(1); // joined from device.onboard_status
    }

    @Test
    void updateAssetOnboardData_coalescesNullsKeepingExistingValues() {
        // update only image_status; the rest are null -> retained
        repo.updateAssetOnboardData("d1", 9, null, null, null);
        assertThat((Integer) em.createQuery(
                "SELECT dos.image_status FROM DeviceOnboardStatus dos WHERE dos.device.id = 'd1'").getSingleResult())
                .isEqualTo(9);
        assertThat((Integer) em.createQuery(
                "SELECT dos.geolocation_status FROM DeviceOnboardStatus dos WHERE dos.device.id = 'd1'").getSingleResult())
                .isEqualTo(2); // unchanged (COALESCE kept existing)
        assertThat((Integer) em.createQuery(
                "SELECT dos.field_status FROM DeviceOnboardStatus dos WHERE dos.device.id = 'd1'").getSingleResult())
                .isEqualTo(4); // unchanged
    }
}
