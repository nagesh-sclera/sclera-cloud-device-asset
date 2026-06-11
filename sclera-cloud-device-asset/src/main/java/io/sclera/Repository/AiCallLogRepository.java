package io.sclera.Repository;

import io.sclera.dto.AiCallLogDTO;

import io.sclera.dto.CallStatusDTO;
import io.sclera.models.AiCallLog;
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
 * Manages persistence and querying of {@link AiCallLog} entities.
 */
@Repository
public interface AiCallLogRepository extends JpaRepository<AiCallLog, String> {

    /**
     * Inserts a new AI call log record.
     *
     * @param id the AI call log identifier
     * @param createdAt the creation timestamp
     * @param assignedAt the assignment timestamp
     * @param issueType the reported issue type
     * @param description the issue description
     * @param priority the call priority
     * @param status the call status
     * @param isCompleted whether the call is completed
     * @param deviceId the associated device identifier
     * @param technicianId the assigned technician identifier
     */
    // NOT CONVERTED — stays native: plain INSERT, already PG-valid; no ON CONFLICT / MySQL-specific syntax
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ai_call_log (id,created_at,assigned_at,issue_type,description,priority,status,is_completed,device_id,technician_id) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10) ", nativeQuery = true)
    void insertAiCallLog(String id, BigInteger createdAt,BigInteger assignedAt, String issueType, String description, String priority, String status, Boolean isCompleted, String deviceId, String technicianId);

    /**
     * Updates the completion flag and status of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @param isCompleted the new completion flag
     * @param status the new status
     */
    // NOT CONVERTED — stays native: entity field isCompleted is String but parameter is Boolean;
    // JPQL UPDATE a.isCompleted = ?2 would cause a type mismatch at Hibernate query parsing time
    @Modifying
    @Transactional
    @Query(value = "UPDATE ai_call_log SET is_completed = ?2, status = ?3 WHERE id = ?1", nativeQuery = true)
    void updateAiCallLogIsCompletedAndStatus(String aiCallLogId, Boolean isCompleted, String status);

    /**
     * Returns call status information for the given device.
     *
     * @param deviceId the device identifier
     * @return the call status information as a DTO
     */
    // NOT CONVERTED — stays native: CallStatusDTO is a stub with only deviceName; the 7-column
    // @SqlResultSetMapping constructor required (propertyId, propertyName, buildingId, etc.)
    // does not exist; multi-table JOIN (device→location→floor→building→vdms) has no portable JPQL form
    @Query(nativeQuery = true)
    CallStatusDTO getStatusInformation(String deviceId);

    /**
     * Returns a paginated, filtered list of AI call logs.
     *
     * @param pageSize the maximum number of records to return
     * @param offset the number of records to skip
     * @param searchKey the search filter term
     * @param isCompleted the completion filter
     * @return the matching AI call logs as DTOs
     */
    // NOT CONVERTED — stays native: entity field isCompleted is String but AiCallLogDTO constructor
    // requires Boolean isCompleted; JPQL constructor expression would fail type resolution;
    // arbitrary LIMIT/OFFSET (not page-aligned) cannot be expressed as PageRequest
    @Query(nativeQuery = true)
    List<AiCallLogDTO> getAllAiCallLog(Integer pageSize, Integer offset, String searchKey, String isCompleted);

    /**
     * Counts the AI call logs matching the given completion flag.
     *
     * @param isCompleted the completion flag to match
     * @return the number of matching AI call logs
     */
    // NOT CONVERTED — stays native: entity field isCompleted is String but parameter is Boolean;
    // JPQL WHERE a.isCompleted = ?1 would produce a String/Boolean type mismatch
    @Query(value = "SELECT COUNT(*) FROM ai_call_log WHERE is_completed = ?1 ", nativeQuery = true)
    Integer getCallStatusCount(Boolean isCompleted);

    /**
     * Updates the assignment, completion, technician, and status of the given AI call log.
     *
     * @param id the AI call log identifier
     * @param assignedAt the assignment timestamp
     * @param isCompleted the new completion flag
     * @param technicianId the assigned technician identifier
     * @param status the new status
     */
    // NOT CONVERTED — stays native: sets technician_id (a @ManyToOne relation column) from a scalar id;
    // JPQL bulk UPDATE cannot navigate a.technician.id on the SET side
    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET assigned_at=?2,is_completed=?3,technician_id=?4,status=?5 WHERE id=?1", nativeQuery = true)
    void upsertStatus(String id, BigInteger assignedAt, Boolean isCompleted,String technicianId,String status);

    /**
     * Backing query for {@link #getAiCallLogIdByDeviceId}: returns up to one id ordered by
     * creation time descending.  Callers use the {@code default} wrapper below.
     */
    @Query("SELECT a.id FROM AiCallLog a WHERE a.device.id = ?1 AND a.status <> 'accepted' ORDER BY a.createdAt DESC")
    List<String> findAiCallLogIdByDeviceIdList(String deviceId, Pageable pageable);

    /**
     * Returns the most recent non-accepted AI call log identifier for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching AI call log identifier, or {@code null} if none
     */
    default String getAiCallLogIdByDeviceId(String deviceId) {
        List<String> results = findAiCallLogIdByDeviceIdList(
                deviceId, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Backing query for {@link #getAiCallLogIssueTypeByDeviceId}: returns up to one issue type.
     * Callers use the {@code default} wrapper below.
     */
    @Query("SELECT a.issueType FROM AiCallLog a WHERE a.device.id = ?1 AND a.status <> 'accepted' ORDER BY a.createdAt DESC")
    List<String> findAiCallLogIssueTypeByDeviceIdList(String deviceId, Pageable pageable);

    /**
     * Returns the issue type of the most recent non-accepted AI call log for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching issue type, or {@code null} if none
     */
    default String getAiCallLogIssueTypeByDeviceId(String deviceId) {
        List<String> results = findAiCallLogIssueTypeByDeviceIdList(
                deviceId, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Returns the issue type of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the issue type
     */
    @Query("SELECT a.issueType FROM AiCallLog a WHERE a.id = ?1")
    String getAiCallLogIssueTypeByAiCallLogId(String aiCallLogId);

    /**
     * Returns the device identifier associated with the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the associated device identifier
     */
    @Query("SELECT a.device.id FROM AiCallLog a WHERE a.id = ?1")
    String getDeviceIdByAiCallLogId(String aiCallLogId);

    /**
     * Updates the status of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @param statusForUpsert the new status
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE AiCallLog a SET a.status = ?2 WHERE a.id = ?1")
    void updateStatus(String aiCallLogId, String statusForUpsert);

    /**
     * Backing query for {@link #getAiCallLogIdByDeviceIdForOnline}: returns up to one id.
     * Callers use the {@code default} wrapper below.
     */
    @Query("SELECT a.id FROM AiCallLog a WHERE a.device.id = ?1 AND a.status <> 'accepted' ORDER BY a.createdAt DESC")
    List<String> findAiCallLogIdByDeviceIdForOnlineList(String deviceId, Pageable pageable);

    /**
     * Returns the most recent non-accepted AI call log identifier for the given online device.
     *
     * @param deviceId the device identifier
     * @return the matching AI call log identifier, or {@code null} if none
     */
    default String getAiCallLogIdByDeviceIdForOnline(String deviceId) {
        List<String> results = findAiCallLogIdByDeviceIdForOnlineList(
                deviceId, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Returns the status of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the status
     */
    @Query("SELECT a.status FROM AiCallLog a WHERE a.id = ?1")
    String getStatusRemovedByAiCallLogId(String aiCallLogId);

    /**
     * Updates the completion flag of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @param isCompleted the new completion flag
     */
    // NOT CONVERTED — stays native: entity field isCompleted is String but parameter is Boolean;
    // JPQL UPDATE a.isCompleted = ?2 would cause a type mismatch at Hibernate query parsing time
    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET is_completed=?2 WHERE id=?1", nativeQuery = true)
    void updateIsCompleted(String aiCallLogId, Boolean isCompleted);

    /**
     * Returns the identifier of the given AI call log when it is ongoing and matches the completion flag.
     *
     * @param aiCallLogId the AI call log identifier
     * @param isCompleted the completion flag to match
     * @return the matching AI call log identifier
     */
    // NOT CONVERTED — stays native: entity field isCompleted is String but parameter is Boolean;
    // JPQL WHERE a.isCompleted = ?2 would produce a String/Boolean type mismatch
    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.id = ?1 AND a.is_completed=?2 AND a.status='ongoing' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByIsCompleted(String aiCallLogId, Boolean isCompleted);

    /**
     * Deletes all AI call logs belonging to the given technicians.
     *
     * @param technicianIds the technician identifiers
     * @return the number of deleted records
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM AiCallLog a WHERE a.technician.id IN ?1")
    int deleteAICallLogsByTechnicianIds(Set<String> technicianIds);
}


