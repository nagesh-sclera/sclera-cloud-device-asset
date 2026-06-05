package io.sclera.Repository;

import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.models.DeviceLifecycleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

/**
 * Manages persistence and querying of {@link DeviceLifecycleHistory} records.
 */
@Repository
public interface DeviceLifeCycleHistoryRepository extends JpaRepository<DeviceLifecycleHistory, String> {

    /**
     * Deletes all lifecycle history records for the given device.
     *
     * @param deviceId the device identifier
     */
    void deleteByDeviceId(String deviceId);

    /**
     * Inserts a new lifecycle history record.
     *
     * @param id the history record identifier
     * @param operational_status the operational status at the time of the event
     * @param usage_status the usage status at the time of the event
     * @param assigned_user_id the identifier of the user the device is assigned to
     * @param assignment_count the running count of assignments for the device
     * @param created_timestamp the timestamp the record was created
     * @param assigned_timestamp the timestamp the assignment took effect
     * @param device_id the device identifier
     * @param description a description of the lifecycle event
     * @param assigned_by_user_id the identifier of the user who performed the assignment
     */
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

    /**
     * Returns a page of lifecycle history records for the given device.
     *
     * @param device_id the device identifier
     * @param pagesize the maximum number of records to return
     * @param offset the number of records to skip
     * @return the set of matching lifecycle history records
     */
    @Query(name = "DeviceLifeCycleHistory.getDeviceLifeCycleHistory", nativeQuery = true)
    Set<DeviceLifecycleHistoryDTO> getDeviceLifeCycleHistory(String device_id, Integer pagesize, Integer offset);

    /**
     * Returns the assignment count from the most recent lifecycle history record for the device.
     *
     * @param device_id the device identifier
     * @return the latest assignment count, or {@code null} if no history exists
     */
    @Query(value = "SELECT assignment_count FROM device_lifecycle_history " +
            "WHERE device_id = ?1 ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    Integer getLatestAssignedCount(String device_id);

    /**
     * Returns the operational status from the most recent lifecycle history record for the device.
     *
     * @param deviceId the device identifier
     * @return the latest operational status, or {@code null} if no history exists
     */
    @Query(value = "SELECT operational_status FROM device_lifecycle_history WHERE device_id = ?1 ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    String getLatestOperationalStatusFromHistory(String deviceId);

    /**
     * Returns the assigned user email from the most recent lifecycle history record for the device.
     *
     * @param deviceId the device identifier
     * @return the latest assigned user email, or {@code null} if no history exists
     */
    @Query(value = "SELECT assigned_user_email FROM device_lifecycle_history WHERE device_id = :deviceId ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    String getLatestAssignedUserEmailFromHistory(String deviceId);

}