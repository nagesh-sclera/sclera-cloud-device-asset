package io.sclera.Repository;

import io.sclera.models.DeviceInstalledApps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link DeviceInstalledApps} entities.
 */
@Repository
public interface DeviceInstalledAppsRepository extends JpaRepository<DeviceInstalledApps, String> {

    /**
     * Deletes all installed-app records for the given device.
     *
     * @param deviceId the device identifier
     */
    void deleteByDeviceId(String deviceId);

    /**
     * Returns the installed-app records for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching installed-app entities
     */
    List<DeviceInstalledApps> findByDeviceId(String deviceId);

    /**
     * Reassigns installed-app records to a device by their device-specification identifier.
     *
     * @param serialNumber the device-specification identifier
     * @param deviceId     the device identifier to assign
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceInstalledApps d SET d.deviceId = ?2 WHERE d.deviceSpecificationId = ?1")
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);

    /**
     * Returns whether an installed app with the given name exists for the device.
     *
     * @param deviceId the device identifier
     * @param name     the installed-app name
     * @return {@code true} if a matching record exists
     */
    boolean existsByDeviceIdAndName(String deviceId, String name);

    /**
     * Returns the device-specification identifiers associated with a managed software entry.
     *
     * @param managedSoftwareId the managed software identifier
     * @return the matching device-specification identifiers
     */
    @Query("SELECT d.deviceSpecificationId FROM DeviceInstalledApps d WHERE d.managedSoftwareId = ?1")
    Set<String> getDeviceSpecIdsByManagedSoftwareId(String managedSoftwareId);

    /**
     * Updates the risk status of installed apps for the given devices and managed software.
     *
     * @param deviceSpecId      the device-specification identifiers
     * @param managedSoftwareId the managed software identifier
     * @param status            the risk status to set
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceInstalledApps d SET d.riskStatus = ?3 WHERE d.deviceSpecificationId IN ?1 AND d.managedSoftwareId = ?2")
    Integer updateRiskStatusForDevices(Set<String> deviceSpecId, String managedSoftwareId, Integer status);

    /**
     * Returns the count of compliant installed apps (risk status 0 or 2) for a managed software entry.
     *
     * @param managedsoftwareid the managed software identifier
     * @return the count of compliant records
     */
    @Query("SELECT COUNT(d) FROM DeviceInstalledApps d WHERE d.riskStatus IN (0, 2) AND d.managedSoftwareId = ?1")
    Integer getCompliantRiskStatusCount(String managedsoftwareid);

    /**
     * Returns the device-specification identifiers flagged as risky (risk status 1) for a managed software entry.
     *
     * @param managedSoftwareId the managed software identifier
     * @return the matching device-specification identifiers
     */
    @Query("SELECT d.deviceSpecificationId FROM DeviceInstalledApps d WHERE d.managedSoftwareId = ?1 AND d.riskStatus = 1")
    Set<String> getRiskyDeviceSpecIdsByManagedSoftwareId(String managedSoftwareId);

//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM device_installed_apps WHERE managed_software_id = ?1", nativeQuery = true)
//    Integer deleteByManagedSoftwareId(String managedSoftwareId);

    /**
     * Clears the managed software association and risk status for all matching installed apps.
     *
     * @param managedSoftwareId the managed software identifier
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceInstalledApps d SET d.managedSoftwareId = null, d.riskStatus = null WHERE d.managedSoftwareId = ?1")
    Integer clearManagedSoftwareIdAndRiskStatus(String managedSoftwareId);

    /**
     * Clears the risk status for all installed apps linked to the given managed software entry.
     *
     * @param managedSoftwareId the managed software identifier
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceInstalledApps d SET d.riskStatus = null WHERE d.managedSoftwareId = ?1")
    Integer clearRiskStatusByManagedSoftwareId(String managedSoftwareId);

    /**
     * Updates the risk status of a single installed app identified by device-specification and managed software.
     *
     * @param deviceSpecId      the device-specification identifier
     * @param managedSoftwareId the managed software identifier
     * @param status            the risk status to set
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceInstalledApps d SET d.riskStatus = ?3 WHERE d.deviceSpecificationId = ?1 AND d.managedSoftwareId = ?2")
    Integer updateRiskStatusByDeviceSpecId(String deviceSpecId, String managedSoftwareId, Integer status);

}
