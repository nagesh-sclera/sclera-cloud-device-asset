package io.sclera.Repository;

import io.sclera.models.DeviceNetworkSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

/**
 * Manages persistence and querying of {@link DeviceNetworkSpecification} records.
 */
@Repository
public interface DeviceNetworkSpecificationRepository extends JpaRepository<DeviceNetworkSpecification,String> {

    /**
     * Returns the network specification for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching network specification, or {@code null} if none exists
     */
    DeviceNetworkSpecification findByDeviceId(String deviceId);

    /**
     * Sets the device identifier on the network specification record with the given id.
     *
     * @param serialNumber the network specification record identifier
     * @param deviceId the device identifier to assign
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE device_network_specification SET device_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);

    /**
     * Deletes the network specification for the given device.
     *
     * @param deviceId the device identifier
     */
    void deleteByDeviceId(String deviceId);
}
