package io.sclera.Repository;

import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import io.sclera.models.DeviceOnboardStatusAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Set;

@Repository
public interface DeviceOnboardStatusAssigneeRepository extends JpaRepository<DeviceOnboardStatusAssignee, String> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_onboard_status_assignee  WHERE device_onboard_status_id =?1", nativeQuery = true)
    void deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(String device_onboard_status_id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_onboard_status_assignee(id, email, type, device_onboard_status_id) VALUES(?1, ?2, ?3, ?4)", nativeQuery = true)
    void addDeviceOnboardStatusAssignees(String id, String email, String type, String device_onboard_status_id);

    @Query(nativeQuery = true)
    Set<DeviceOnboardStatusAssigneeDTO> getDeviceOnboardStatusAssignees(String deviceOnboardStatusId);

    @Query(value = "SELECT DISTINCT dosa.email FROM device_onboard_status_assignee dosa", nativeQuery = true)
    Set<String> getDeviceOnboardStatusAssigneesEmail();

}
