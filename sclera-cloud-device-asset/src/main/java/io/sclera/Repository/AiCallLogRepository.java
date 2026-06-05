package io.sclera.Repository;

import io.sclera.dto.AiCallLogDTO;

import io.sclera.dto.CallStatusDTO;
import io.sclera.models.AiCallLog;
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
    @Query(nativeQuery = true)
    List<AiCallLogDTO> getAllAiCallLog(Integer pageSize, Integer offset, String searchKey, String isCompleted);

    /**
     * Counts the AI call logs matching the given completion flag.
     *
     * @param isCompleted the completion flag to match
     * @return the number of matching AI call logs
     */
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
    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET assigned_at=?2,is_completed=?3,technician_id=?4,status=?5 WHERE id=?1", nativeQuery = true)
    void upsertStatus(String id, BigInteger assignedAt, Boolean isCompleted,String technicianId,String status) ;


    /**
     * Returns the most recent non-accepted AI call log identifier for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching AI call log identifier
     */
    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.device_id = ?1 AND a.status != 'accepted' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByDeviceId(String deviceId);

    /**
     * Returns the issue type of the most recent non-accepted AI call log for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching issue type
     */
    @Query(value = "SELECT issue_type FROM ai_call_log a WHERE a.device_id = ?1 AND a.status != 'accepted' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIssueTypeByDeviceId(String deviceId);

    /**
     * Returns the issue type of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the issue type
     */
    @Query(value = "SELECT issue_type FROM ai_call_log a WHERE a.id = ?1", nativeQuery = true)
    String getAiCallLogIssueTypeByAiCallLogId(String aiCallLogId);

    /**
     * Returns the device identifier associated with the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the associated device identifier
     */
    @Query(value = "SELECT device_id FROM ai_call_log a WHERE a.id = ?1", nativeQuery = true)
    String getDeviceIdByAiCallLogId(String aiCallLogId);

    /**
     * Updates the status of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @param statusForUpsert the new status
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET status=?2 WHERE id=?1", nativeQuery = true)
    void updateStatus(String aiCallLogId,String statusForUpsert);

    /**
     * Returns the most recent non-accepted AI call log identifier for the given online device.
     *
     * @param deviceId the device identifier
     * @return the matching AI call log identifier
     */
    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.device_id = ?1 AND a.status != 'accepted' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByDeviceIdForOnline(String deviceId);

    /**
     * Returns the status of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the status
     */
    @Query(value = "SELECT a.status FROM ai_call_log a WHERE a.id = ?1", nativeQuery = true)
    String getStatusRemovedByAiCallLogId(String aiCallLogId);

    /**
     * Updates the completion flag of the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @param isCompleted the new completion flag
     */
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
    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.id = ?1 AND a.is_completed=?2 AND a.status='ongoing' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByIsCompleted(String aiCallLogId,Boolean isCompleted);

    /**
     * Deletes all AI call logs belonging to the given technicians.
     *
     * @param technicianIds the technician identifiers
     * @return the number of deleted records
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ai_call_log WHERE technician_id IN ?1", nativeQuery = true)
    int deleteAICallLogsByTechnicianIds(Set<String> technicianIds);
}


