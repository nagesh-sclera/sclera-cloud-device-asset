package io.sclera.Repository;

import io.sclera.dto.AiCallLogHistoryDTO;
import io.sclera.models.AiCallLogHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
public interface AiCallLogHistoryRepository extends JpaRepository<AiCallLogHistory, String> {

    @Query(nativeQuery = true)
    List<AiCallLogHistoryDTO> getAiCallLogHistoryByAiCallLogId(String id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ai_call_log_history (id, created_at, description, technician_id, ai_call_log_id, state) VALUES (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
    void insertAiCallLogHistoryState(String id, BigInteger timestamp, String description, String technicianId, String aiCallLogId, String state);

    @Query(nativeQuery = true)
    AiCallLogHistoryDTO getAiCallLogHistoryById(String aiCallLogHistoryId);


    @Query(value = "SELECT state FROM ai_call_log_history a WHERE a.id = ?1", nativeQuery = true)
    Set<String> findStatusesByAiCallLogId(String aiCallLogId);

    @Query(value = "SELECT COUNT(*) FROM ai_call_log_history a WHERE a.ai_call_log_id = ?1", nativeQuery = true)
    int countByAiCallLogId(String aiCallLogId);

    @Query(value = "SELECT state FROM ai_call_log_history a WHERE a.ai_call_log_id = ?1 AND a.technician_id=?2 ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getLatestCallStatus(String aiCallLogId,String technicianId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ai_call_log_history WHERE technician_id IN ?1", nativeQuery = true)
    int deleteAICallLogsHistoryByTechnicianIds(Set<String> technicianIds);
}
