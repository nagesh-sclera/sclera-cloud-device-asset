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
    @Query("SELECT DISTINCT ds.email FROM DeviceSpecification ds WHERE ds.email IS NOT NULL")
    List<String> findDistinctEmail();

    /**
     * Returns the distinct non-null OS types across all device specifications.
     *
     * @return the list of distinct OS types
     */
    @Query("SELECT DISTINCT ds.osType FROM DeviceSpecification ds WHERE ds.osType IS NOT NULL")
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
    // NOT CONVERTED — stays native: sets the device_id FK column from a scalar String id;
    // JPQL UPDATE cannot assign a @OneToOne relation column via a plain scalar value.
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
    // NOT CONVERTED — stays native: childDevices is @Lob String (ClobJdbcType in Hibernate 7/PG);
    // a JPQL scalar projection of an @Lob column produces a type-mapping error on PostgreSQL.
    // The native query reads the raw TEXT column directly without Lob conversion.
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
