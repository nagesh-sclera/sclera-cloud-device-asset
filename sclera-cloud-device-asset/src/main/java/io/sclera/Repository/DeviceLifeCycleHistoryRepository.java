package io.sclera.Repository;

import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.models.DeviceLifecycleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

@Repository
public interface DeviceLifeCycleHistoryRepository extends JpaRepository<DeviceLifecycleHistory, String> {

    void deleteByDeviceId(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_lifecycle_history(" +
            "id, operational_status, usage_status, assigned_user_id, assignment_count, " +
            "created_timestamp, assigned_timestamp, device_id, description, assigned_by_user_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
    void addDeviceLifeCycleHistory(String id, String operational_status, String usage_status,
                                   String assigned_user_id, Integer assignment_count,
                                   BigInteger created_timestamp,BigInteger assigned_timestamp, String device_id,
                                   String description, String assigned_by_user_id);

    @Query(name = "DeviceLifeCycleHistory.getDeviceLifeCycleHistory", nativeQuery = true)
    Set<DeviceLifecycleHistoryDTO> getDeviceLifeCycleHistory(String device_id, Integer pagesize, Integer offset);

    @Query(value = "SELECT assignment_count FROM device_lifecycle_history " +
            "WHERE device_id = ?1 ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    Integer getLatestAssignedCount(String device_id);

    @Query(value = "SELECT operational_status FROM device_lifecycle_history WHERE device_id = ?1 ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    String getLatestOperationalStatusFromHistory(String deviceId);

    @Query(value = "SELECT assigned_user_email FROM device_lifecycle_history WHERE device_id = :deviceId ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    String getLatestAssignedUserEmailFromHistory(String deviceId);

}