package io.sclera.Repository;

import io.sclera.dto.AiCallLogDTO;

import io.sclera.dto.CallStatusDTO;
import io.sclera.models.AiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
public interface AiCallLogRepository extends JpaRepository<AiCallLog, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ai_call_log (id,created_at,assigned_at,issue_type,description,priority,status,is_completed,device_id,technician_id) VALUE (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10) ", nativeQuery = true)
    void insertAiCallLog(String id, BigInteger createdAt,BigInteger assignedAt, String issueType, String description, String priority, String status, Boolean isCompleted, String deviceId, String technicianId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE ai_call_log SET is_completed = ?2, status = ?3 WHERE id = ?1", nativeQuery = true)
    void updateAiCallLogIsCompletedAndStatus(String aiCallLogId, Boolean isCompleted, String status);

    @Query(nativeQuery = true)
    CallStatusDTO getStatusInformation(String deviceId);

    @Query(nativeQuery = true)
    List<AiCallLogDTO> getAllAiCallLog(Integer pageSize, Integer offset, String searchKey, String isCompleted);

    @Query(value = "SELECT COUNT(*) FROM ai_call_log WHERE is_completed = ?1 ", nativeQuery = true)
    Integer getCallStatusCount(Boolean isCompleted);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET assigned_at=?2,is_completed=?3,technician_id=?4,status=?5 WHERE id=?1", nativeQuery = true)
    void upsertStatus(String id, BigInteger assignedAt, Boolean isCompleted,String technicianId,String status) ;


    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.device_id = ?1 AND a.status != 'accepted' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByDeviceId(String deviceId);

    @Query(value = "SELECT issue_type FROM ai_call_log a WHERE a.device_id = ?1 AND a.status != 'accepted' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIssueTypeByDeviceId(String deviceId);

    @Query(value = "SELECT issue_type FROM ai_call_log a WHERE a.id = ?1", nativeQuery = true)
    String getAiCallLogIssueTypeByAiCallLogId(String aiCallLogId);

    @Query(value = "SELECT device_id FROM ai_call_log a WHERE a.id = ?1", nativeQuery = true)
    String getDeviceIdByAiCallLogId(String aiCallLogId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET status=?2 WHERE id=?1", nativeQuery = true)
    void updateStatus(String aiCallLogId,String statusForUpsert);

    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.device_id = ?1 AND a.status != 'accepted' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByDeviceIdForOnline(String deviceId);

    @Query(value = "SELECT a.status FROM ai_call_log a WHERE a.id = ?1", nativeQuery = true)
    String getStatusRemovedByAiCallLogId(String aiCallLogId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ai_call_log SET is_completed=?2 WHERE id=?1", nativeQuery = true)
    void updateIsCompleted(String aiCallLogId, Boolean isCompleted);

    @Query(value = "SELECT a.id FROM ai_call_log a WHERE a.id = ?1 AND a.is_completed=?2 AND a.status='ongoing' ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getAiCallLogIdByIsCompleted(String aiCallLogId,Boolean isCompleted);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ai_call_log WHERE technician_id IN ?1", nativeQuery = true)
    int deleteAICallLogsByTechnicianIds(Set<String> technicianIds);
}


