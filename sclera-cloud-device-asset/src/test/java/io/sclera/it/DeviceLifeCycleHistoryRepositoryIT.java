package io.sclera.it;

import io.sclera.Repository.DeviceLifeCycleHistoryRepository;
import io.sclera.dto.DeviceLifecycleHistoryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                              executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-lifecycle-history-pilot.sql",    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-lifecycle-history-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceLifeCycleHistoryRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceLifeCycleHistoryRepository repo;

    /**
     * getDeviceLifeCycleHistory: returns DTOs ordered by created_timestamp DESC.
     * With page 0 / size 2 both rows should be returned, most recent (dlc-h2) first.
     */
    @Test
    void getDeviceLifeCycleHistory_returnsPagedDTOsInDescOrder() {
        List<DeviceLifecycleHistoryDTO> result =
                repo.getDeviceLifeCycleHistory("dlc-dev1", PageRequest.of(0, 2));

        assertThat(result).hasSize(2);
        // Most recent first (created_timestamp 2000)
        assertThat(result.get(0).getId()).isEqualTo("dlc-h2");
        assertThat(result.get(0).getOperational_status()).isEqualTo("inactive");
        assertThat(result.get(0).getAssignment_count()).isEqualTo(2);
        assertThat(result.get(0).getDevice_id()).isEqualTo("dlc-dev1");

        // Older row second (created_timestamp 1000)
        assertThat(result.get(1).getId()).isEqualTo("dlc-h1");
        assertThat(result.get(1).getOperational_status()).isEqualTo("active");
    }

    /**
     * getDeviceLifeCycleHistory: page 0 / size 1 should return only the most recent row.
     */
    @Test
    void getDeviceLifeCycleHistory_pageOneLimitsResults() {
        List<DeviceLifecycleHistoryDTO> result =
                repo.getDeviceLifeCycleHistory("dlc-dev1", PageRequest.of(0, 1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("dlc-h2");
    }

    /**
     * getDeviceLifeCycleHistory: unknown device returns empty list.
     */
    @Test
    void getDeviceLifeCycleHistory_unknownDevice_returnsEmpty() {
        List<DeviceLifecycleHistoryDTO> result =
                repo.getDeviceLifeCycleHistory("no-such-device", PageRequest.of(0, 10));
        assertThat(result).isEmpty();
    }

    /**
     * getLatestAssignedCount: returns the assignment_count from the most recent row (dlc-h2 has count=2).
     */
    @Test
    void getLatestAssignedCount_returnsMostRecentCount() {
        Integer count = repo.getLatestAssignedCount("dlc-dev1");
        assertThat(count).isEqualTo(2);
    }

    /**
     * getLatestAssignedCount: returns null when no history exists for the device.
     */
    @Test
    void getLatestAssignedCount_unknownDevice_returnsNull() {
        Integer count = repo.getLatestAssignedCount("no-such-device");
        assertThat(count).isNull();
    }

    /**
     * getLatestOperationalStatusFromHistory: returns status from most recent row (dlc-h2 = "inactive").
     */
    @Test
    void getLatestOperationalStatus_returnsMostRecentStatus() {
        String status = repo.getLatestOperationalStatusFromHistory("dlc-dev1");
        assertThat(status).isEqualTo("inactive");
    }

    /**
     * getLatestOperationalStatusFromHistory: returns null when no history exists.
     */
    @Test
    void getLatestOperationalStatus_unknownDevice_returnsNull() {
        String status = repo.getLatestOperationalStatusFromHistory("no-such-device");
        assertThat(status).isNull();
    }

}
