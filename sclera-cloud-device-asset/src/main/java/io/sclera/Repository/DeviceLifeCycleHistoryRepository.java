package io.sclera.Repository;

import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.models.DeviceLifecycleHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
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
    // NOT CONVERTED — stays native: plain INSERT already valid PostgreSQL; assigned-@Id entity +
    // save() = merge() = eager SELECT-before-insert which would explode against the minimal test schema.
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_lifecycle_history(" +
            "id, operational_status, usage_status, assigned_user_id, assignment_count, " +
            "created_timestamp, assigned_timestamp, device_id, description, assigned_by_user_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
    void addDeviceLifeCycleHistory(String id, String operational_status, String usage_status,
                                   String assigned_user_id, Integer assignment_count,
                                   BigInteger created_timestamp, BigInteger assigned_timestamp, String device_id,
                                   String description, String assigned_by_user_id);

    /**
     * Returns a page of lifecycle history records for the given device, ordered by most recent first.
     *
     * <p>Converted from {@code @NamedNativeQuery DeviceLifeCycleHistory.getDeviceLifeCycleHistory}
     * (LIMIT/OFFSET) to JPQL constructor expression + Pageable. Callers must pass
     * {@code PageRequest.of(pageno - 1, pagesize)} instead of raw offset/size integers.
     *
     * @param device_id the device identifier
     * @param pageable page and size constraints
     * @return the matching lifecycle history DTOs
     */
    @Query("SELECT new io.sclera.dto.DeviceLifecycleHistoryDTO(" +
            "h.id, h.operational_status, h.usage_status, h.assigned_user_id, " +
            "h.assignment_count, h.created_timestamp, h.assigned_timestamp, h.device.id, " +
            "h.description, h.assigned_by_user_id) " +
            "FROM DeviceLifecycleHistory h " +
            "WHERE h.device.id = ?1 " +
            "ORDER BY h.created_timestamp DESC, h.assignment_count DESC")
    List<DeviceLifecycleHistoryDTO> getDeviceLifeCycleHistory(String device_id, Pageable pageable);

    /**
     * Returns the assignment count from the most recent lifecycle history record for the device.
     *
     * @param device_id the device identifier
     * @return the latest assignment count, or {@code null} if no history exists
     */
    default Integer getLatestAssignedCount(String device_id) {
        List<Integer> results = getLatestAssignedCountList(device_id, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    @Query("SELECT h.assignment_count FROM DeviceLifecycleHistory h " +
            "WHERE h.device.id = ?1 " +
            "ORDER BY h.created_timestamp DESC")
    List<Integer> getLatestAssignedCountList(String device_id, Pageable pageable);

    /**
     * Returns the operational status from the most recent lifecycle history record for the device.
     *
     * @param deviceId the device identifier
     * @return the latest operational status, or {@code null} if no history exists
     */
    default String getLatestOperationalStatusFromHistory(String deviceId) {
        List<String> results = getLatestOperationalStatusList(deviceId, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    @Query("SELECT h.operational_status FROM DeviceLifecycleHistory h " +
            "WHERE h.device.id = ?1 " +
            "ORDER BY h.created_timestamp DESC")
    List<String> getLatestOperationalStatusList(String deviceId, Pageable pageable);

    /**
     * Returns the assigned user email from the most recent lifecycle history record for the device.
     *
     * @param deviceId the device identifier
     * @return the latest assigned user email, or {@code null} if no history exists
     */
    // NOT CONVERTED — stays native: references column `assigned_user_email` which does not
    // exist on the DeviceLifecycleHistory entity/table (no @Column mapping); JPQL has no path
    // for a non-mapped column. This method is also commented-out at every call site.
    @Query(value = "SELECT assigned_user_email FROM device_lifecycle_history WHERE device_id = :deviceId ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    String getLatestAssignedUserEmailFromHistory(String deviceId);

}
