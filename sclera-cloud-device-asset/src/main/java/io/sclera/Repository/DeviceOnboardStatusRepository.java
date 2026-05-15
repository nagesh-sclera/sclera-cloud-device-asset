package io.sclera.Repository;

import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.models.DeviceOnboardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Set;

@Repository
public interface DeviceOnboardStatusRepository extends JpaRepository<DeviceOnboardStatus, String> {

    @Query(value = "SELECT id FROM device_onboard_status WHERE device_id = ?1", nativeQuery = true)
    String getOnboardAssetIdByDeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_onboard_status SET device_id = ?2 ,assignee_email = ?3 , image_status = IFNULL(?4, image_status), " +
            " geolocation_status = IFNULL(?5, geolocation_status), tag_status = IFNULL(?6, tag_status), field_status = IFNULL(?7, field_status) WHERE id = ?1", nativeQuery = true)
    void updateOnboardAsset(String id, String device_id, String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_onboard_status(id, device_id, assignee_email, image_status, geolocation_status, tag_status, field_status) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
    void addOnboardAsset(String id, String device_id, String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_onboard_status SET image_status = IFNULL(?2, image_status), geolocation_status = IFNULL(?3, geolocation_status),"
            + " tag_status = IFNULL(?4, tag_status), field_status = IFNULL(?5, field_status) WHERE device_id = ?1", nativeQuery = true)
    void updateAssetOnboardData(String device_id, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status);

    @Query(nativeQuery = true)
    DeviceOnboardStatusDTO getOnboardDataByDeviceId(String device_id);

    @Query(value = "SELECT DISTINCT dos.assignee_email FROM device_onboard_status dos LEFT JOIN device d ON d.id = dos.device_id WHERE dos.assignee_email IS NOT NULL AND (d.onboard_status = 1 OR d.onboard_status = 2)", nativeQuery = true)
    Set<String> getAssetOnboardAssignees();

    void deleteByDeviceId(String deviceId);
}
