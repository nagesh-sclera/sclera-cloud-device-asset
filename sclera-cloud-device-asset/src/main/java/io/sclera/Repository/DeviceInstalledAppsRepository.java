package io.sclera.Repository;

import io.sclera.models.DeviceInstalledApps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface DeviceInstalledAppsRepository extends JpaRepository<DeviceInstalledApps,String> {

    void deleteByDeviceId(String deviceId);

    List<DeviceInstalledApps> findByDeviceId(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_installed_apps SET device_id = ?2 WHERE device_specification_id = ?1", nativeQuery = true)
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);

    boolean existsByDeviceIdAndName(String deviceId, String name);

    @Query(value = "SELECT device_specification_id FROM device_installed_apps WHERE managed_software_id = ?1", nativeQuery = true)
    Set<String> getDeviceSpecIdsByManagedSoftwareId(String managedSoftwareId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_installed_apps " +
            "SET risk_status = ?3 " +
            "WHERE device_specification_id IN ?1 AND managed_software_id = ?2", nativeQuery = true)
    Integer updateRiskStatusForDevices(Set<String> deviceSpecId, String managedSoftwareId, Integer status);

    @Query(value = "SELECT COUNT(*) FROM device_installed_apps WHERE risk_status IN (0, 2) AND managed_software_id = ?1", nativeQuery = true)
    Integer getCompliantRiskStatusCount(String managedsoftwareid);

    @Query(value = "SELECT device_specification_id FROM device_installed_apps WHERE managed_software_id = ?1 AND risk_status = 1", nativeQuery = true)
    Set<String> getRiskyDeviceSpecIdsByManagedSoftwareId(String managedSoftwareId);

//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM device_installed_apps WHERE managed_software_id = ?1", nativeQuery = true)
//    Integer deleteByManagedSoftwareId(String managedSoftwareId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_installed_apps " +
            "SET managed_software_id = null, risk_status = null " +
            "WHERE managed_software_id = ?1", nativeQuery = true)
    Integer clearManagedSoftwareIdAndRiskStatus(String managedSoftwareId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_installed_apps " +
            "SET risk_status = null " +
            "WHERE managed_software_id = ?1", nativeQuery = true)
    Integer clearRiskStatusByManagedSoftwareId(String managedSoftwareId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_installed_apps " +
            "SET risk_status = ?3 " +
            "WHERE device_specification_id = ?1 AND managed_software_id = ?2", nativeQuery = true)
    Integer updateRiskStatusByDeviceSpecId(String deviceSpecId, String managedSoftwareId, Integer status);

}