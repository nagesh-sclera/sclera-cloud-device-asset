package io.sclera.Repository;

import io.sclera.models.DeviceSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link DeviceSpecification} entities.
 */
@Repository
public interface DeviceSpecificationRepository extends JpaRepository<DeviceSpecification,String> {

    /**
     * Deletes the specification associated with the given device.
     *
     * @param deviceId the device identifier whose specification is removed
     */
    void deleteByDeviceId(String deviceId);

    /**
     * Returns the distinct non-null email values across all device specifications.
     *
     * @return the list of distinct emails
     */
    @Query(value = "SELECT DISTINCT email FROM device_specification WHERE email IS NOT NULL", nativeQuery = true)
    List<String> findDistinctEmail();

    /**
     * Returns the distinct non-null OS types across all device specifications.
     *
     * @return the list of distinct OS types
     */
    @Query(value = "SELECT DISTINCT os_type FROM device_specification WHERE os_type IS NOT NULL", nativeQuery = true)
    List<String> findDistinctOsType();

    /**
     * Finds the specification for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching specification, or {@code null} if none exists
     */
    DeviceSpecification findByDeviceId(String deviceId);

    /**
     * Sets the device id on the specification identified by the given serial number.
     *
     * @param serialNumber the specification id (serial number) to update
     * @param deviceId the new device id to assign
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE device_specification SET device_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);

    /**
     * Returns the child devices associated with the given device.
     *
     * @param deviceId the device identifier
     * @return the child devices value
     */
    @Query(value = "SELECT child_devices FROM device_specification WHERE device_id = ?1", nativeQuery = true)
    String getChildDeviceByDeviceId(String deviceId);

    /**
     * Finds all specifications whose ids are contained in the given set.
     *
     * @param riskyDeviceSpecIds the set of specification ids to match
     * @return the matching specifications
     */
    List<DeviceSpecification> findByIdIn(Set<String> riskyDeviceSpecIds);
}