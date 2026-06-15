package io.sclera.it;

import io.sclera.Repository.DeviceOnboardStatusAssigneeRepository;
import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting IT for the JPA-converted DeviceOnboardStatusAssigneeRepository,
 * run against real PostgreSQL.
 *
 * Methods classified:
 *  - deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId: CONVERTED to JPQL bulk DELETE
 *    (navigates dosa.device_onboard_status.id — @ManyToOne relation).
 *  - addDeviceOnboardStatusAssignees: stays native (plain INSERT, already valid PG;
 *    save() would SELECT-before-insert due to assigned @Id + eager associations).
 *  - getDeviceOnboardStatusAssignees: CONVERTED to JPQL constructor expression.
 *  - getDeviceOnboardStatusAssigneesEmail: CONVERTED to JPQL DISTINCT select.
 */
@Sql(scripts = "/schema-pg.sql",                                      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-onboard-status-assignee-pilot.sql",       executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-onboard-status-assignee-pilot.sql",    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceOnboardStatusAssigneeRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceOnboardStatusAssigneeRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void getDeviceOnboardStatusAssignees_returnsProjectionForStatus() {
        Set<DeviceOnboardStatusAssigneeDTO> result = repo.getDeviceOnboardStatusAssignees("dos-a1");
        assertThat(result).hasSize(2);

        // Verify that the DTO is fully populated
        assertThat(result).extracting(DeviceOnboardStatusAssigneeDTO::getDevice_onboard_status_id)
                .containsOnly("dos-a1");
        assertThat(result).extracting(DeviceOnboardStatusAssigneeDTO::getEmail)
                .containsExactlyInAnyOrder("admin@x.com", "viewer@x.com");
        assertThat(result).extracting(DeviceOnboardStatusAssigneeDTO::getType)
                .containsExactlyInAnyOrder("ADMIN", "VIEWER");
        assertThat(result).extracting(DeviceOnboardStatusAssigneeDTO::getId)
                .containsExactlyInAnyOrder("asn1", "asn2");
    }

    @Test
    void getDeviceOnboardStatusAssignees_returnsEmptySetForUnknownStatus() {
        Set<DeviceOnboardStatusAssigneeDTO> result = repo.getDeviceOnboardStatusAssignees("ghost");
        assertThat(result).isEmpty();
    }

    @Test
    void getDeviceOnboardStatusAssigneesEmail_returnsDistinctEmails() {
        // admin@x.com appears in dos-a1 AND dos-a2 — distinct must collapse it to one entry
        Set<String> emails = repo.getDeviceOnboardStatusAssigneesEmail();
        assertThat(emails).containsExactlyInAnyOrder("admin@x.com", "viewer@x.com");
    }

    @Test
    void deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId_deletesMatchingRows() {
        // Verify two rows exist for dos-a1 before deletion
        Long countBefore = (Long) em.createQuery(
                "SELECT COUNT(dosa) FROM DeviceOnboardStatusAssignee dosa " +
                "WHERE dosa.device_onboard_status.id = 'dos-a1'")
                .getSingleResult();
        assertThat(countBefore).isEqualTo(2L);

        repo.deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId("dos-a1");

        // Only dos-a2's assignee should remain
        Long countAfter = (Long) em.createQuery(
                "SELECT COUNT(dosa) FROM DeviceOnboardStatusAssignee dosa " +
                "WHERE dosa.device_onboard_status.id = 'dos-a1'")
                .getSingleResult();
        assertThat(countAfter).isEqualTo(0L);

        Long remainingTotal = (Long) em.createQuery(
                "SELECT COUNT(dosa) FROM DeviceOnboardStatusAssignee dosa")
                .getSingleResult();
        assertThat(remainingTotal).isEqualTo(1L); // dos-a2's assignee survives
    }

    @Test
    void addDeviceOnboardStatusAssignees_insertsNewRow() {
        // Uses the native INSERT (stays native)
        repo.addDeviceOnboardStatusAssignees("asn-new", "newuser@x.com", "VIEWER", "dos-a2");

        Long count = (Long) em.createQuery(
                "SELECT COUNT(dosa) FROM DeviceOnboardStatusAssignee dosa " +
                "WHERE dosa.id = 'asn-new' AND dosa.device_onboard_status.id = 'dos-a2'")
                .getSingleResult();
        assertThat(count).isEqualTo(1L);
    }
}
