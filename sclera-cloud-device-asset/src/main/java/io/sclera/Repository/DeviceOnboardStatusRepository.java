package io.sclera.Repository;

import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.models.DeviceOnboardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link DeviceOnboardStatus} records.
 */
@Repository
public interface DeviceOnboardStatusRepository extends JpaRepository<DeviceOnboardStatus, String> {

    /**
     * Returns the onboard status record identifier for the given device.
     *
     * @param device_id the device identifier
     * @return the onboard status identifier, or {@code null} if none exists
     */
    @Query("SELECT dos.id FROM DeviceOnboardStatus dos WHERE dos.device.id = ?1")
    String getOnboardAssetIdByDeviceId(String device_id);

    /**
     * Updates the onboard status record with the given id, retaining existing status values where the supplied ones are null.
     *
     * @param id the onboard status record identifier
     * @param device_id the device identifier to assign
     * @param assignee_email the assignee email to set
     * @param image_status the image onboarding status, or {@code null} to keep the current value
     * @param geolocation_status the geolocation onboarding status, or {@code null} to keep the current value
     * @param tag_status the tag onboarding status, or {@code null} to keep the current value
     * @param field_status the field onboarding status, or {@code null} to keep the current value
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: already valid PostgreSQL (IFNULL already ported to COALESCE). Its SET assigns the
    // device_id FK column (?2) directly; DeviceOnboardStatus maps `device` as a @OneToOne relation, and a JPQL bulk
    // UPDATE cannot set a relation from a bare scalar id.
    @Query(value = "UPDATE device_onboard_status SET device_id = ?2 ,assignee_email = ?3 , image_status = COALESCE(?4, image_status), " +
            " geolocation_status = COALESCE(?5, geolocation_status), tag_status = COALESCE(?6, tag_status), field_status = COALESCE(?7, field_status) WHERE id = ?1", nativeQuery = true)
    void updateOnboardAsset(String id, String device_id, String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status);

    /**
     * Inserts a new onboard status record.
     *
     * @param id the onboard status record identifier
     * @param device_id the device identifier
     * @param assignee_email the assignee email
     * @param image_status the image onboarding status
     * @param geolocation_status the geolocation onboarding status
     * @param tag_status the tag onboarding status
     * @param field_status the field onboarding status
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT, already valid PostgreSQL. Entity save() is worse here —
    // DeviceOnboardStatus has an assigned @Id, so Spring Data routes save() to merge(), whose SELECT-before-insert
    // eagerly materialises the @OneToOne device and the device's large eager association graph.
    @Query(value = "INSERT INTO device_onboard_status(id, device_id, assignee_email, image_status, geolocation_status, tag_status, field_status) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
    void addOnboardAsset(String id, String device_id, String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status);

    /**
     * Updates the onboarding status fields for the given device, retaining existing values where the supplied ones are null.
     *
     * @param device_id the device identifier
     * @param image_status the image onboarding status, or {@code null} to keep the current value
     * @param geolocation_status the geolocation onboarding status, or {@code null} to keep the current value
     * @param tag_status the tag onboarding status, or {@code null} to keep the current value
     * @param field_status the field onboarding status, or {@code null} to keep the current value
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceOnboardStatus dos SET dos.image_status = COALESCE(?2, dos.image_status),"
            + " dos.geolocation_status = COALESCE(?3, dos.geolocation_status), dos.tag_status = COALESCE(?4, dos.tag_status),"
            + " dos.field_status = COALESCE(?5, dos.field_status) WHERE dos.device.id = ?1")
    void updateAssetOnboardData(String device_id, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status);

    /**
     * Returns the onboard status data for the given device.
     *
     * @param device_id the device identifier
     * @return the matching onboard status data, or {@code null} if none exists
     */
    @Query("SELECT new io.sclera.dto.DeviceOnboardStatusDTO(dos.assignee_email, dos.image_status, dos.geolocation_status,"
            + " dos.tag_status, dos.field_status, d.onboard_status)"
            + " FROM DeviceOnboardStatus dos LEFT JOIN dos.device d WHERE dos.device.id = ?1")
    DeviceOnboardStatusDTO getOnboardDataByDeviceId(String device_id);

    /**
     * Returns the distinct assignee emails for devices currently in an in-progress onboard status.
     *
     * @return the set of distinct assignee emails
     */
    @Query("SELECT DISTINCT dos.assignee_email FROM DeviceOnboardStatus dos LEFT JOIN dos.device d"
            + " WHERE dos.assignee_email IS NOT NULL AND (d.onboard_status = 1 OR d.onboard_status = 2)")
    Set<String> getAssetOnboardAssignees();

    /**
     * Deletes the onboard status record for the given device.
     *
     * @param deviceId the device identifier
     */
    void deleteByDeviceId(String deviceId);
}
