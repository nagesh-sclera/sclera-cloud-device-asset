package io.sclera.Repository;

import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import io.sclera.models.DeviceOnboardStatusAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link DeviceOnboardStatusAssignee} records.
 */
@Repository
public interface DeviceOnboardStatusAssigneeRepository extends JpaRepository<DeviceOnboardStatusAssignee, String> {
    /**
     * Deletes all assignee records belonging to the given onboard status.
     *
     * @param device_onboard_status_id the onboard status identifier
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_onboard_status_assignee  WHERE device_onboard_status_id =?1", nativeQuery = true)
    void deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(String device_onboard_status_id);

    /**
     * Inserts a new onboard status assignee record.
     *
     * @param id the assignee record identifier
     * @param email the assignee email
     * @param type the assignee type
     * @param device_onboard_status_id the onboard status identifier the assignee belongs to
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_onboard_status_assignee(id, email, type, device_onboard_status_id) VALUES(?1, ?2, ?3, ?4)", nativeQuery = true)
    void addDeviceOnboardStatusAssignees(String id, String email, String type, String device_onboard_status_id);

    /**
     * Returns the assignees for the given onboard status.
     *
     * @param deviceOnboardStatusId the onboard status identifier
     * @return the set of matching assignee records
     */
    @Query(nativeQuery = true)
    Set<DeviceOnboardStatusAssigneeDTO> getDeviceOnboardStatusAssignees(String deviceOnboardStatusId);

    /**
     * Returns the distinct emails across all onboard status assignees.
     *
     * @return the set of distinct assignee emails
     */
    @Query(value = "SELECT DISTINCT dosa.email FROM device_onboard_status_assignee dosa", nativeQuery = true)
    Set<String> getDeviceOnboardStatusAssigneesEmail();

}
