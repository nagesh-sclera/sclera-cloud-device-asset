package io.sclera.Repository;

import io.sclera.dto.AiCallLogHistoryDTO;
import io.sclera.models.AiCallLogHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link AiCallLogHistory} entities.
 */
@Repository
public interface AiCallLogHistoryRepository extends JpaRepository<AiCallLogHistory, String> {

    /**
     * Returns the history records associated with the given AI call log.
     *
     * @param id the AI call log identifier
     * @return the matching history records as DTOs
     */
    @Query(nativeQuery = true)
    List<AiCallLogHistoryDTO> getAiCallLogHistoryByAiCallLogId(String id);

    /**
     * Inserts a new AI call log history record capturing a state transition.
     *
     * @param id the history record identifier
     * @param timestamp the creation timestamp
     * @param description the description of the state change
     * @param technicianId the technician associated with the change
     * @param aiCallLogId the parent AI call log identifier
     * @param state the recorded state
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ai_call_log_history (id, created_at, description, technician_id, ai_call_log_id, state) VALUES (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
    void insertAiCallLogHistoryState(String id, BigInteger timestamp, String description, String technicianId, String aiCallLogId, String state);

    /**
     * Returns the history record with the given identifier.
     *
     * @param aiCallLogHistoryId the history record identifier
     * @return the matching history record as a DTO
     */
    @Query(nativeQuery = true)
    AiCallLogHistoryDTO getAiCallLogHistoryById(String aiCallLogHistoryId);


    /**
     * Returns the distinct states recorded for the given history record identifier.
     *
     * @param aiCallLogId the history record identifier
     * @return the set of recorded states
     */
    @Query(value = "SELECT state FROM ai_call_log_history a WHERE a.id = ?1", nativeQuery = true)
    Set<String> findStatusesByAiCallLogId(String aiCallLogId);

    /**
     * Counts the history records belonging to the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the number of matching history records
     */
    @Query(value = "SELECT COUNT(*) FROM ai_call_log_history a WHERE a.ai_call_log_id = ?1", nativeQuery = true)
    int countByAiCallLogId(String aiCallLogId);

    /**
     * Returns the most recent state recorded for the given AI call log and technician.
     *
     * @param aiCallLogId the AI call log identifier
     * @param technicianId the technician identifier
     * @return the latest recorded state
     */
    @Query(value = "SELECT state FROM ai_call_log_history a WHERE a.ai_call_log_id = ?1 AND a.technician_id=?2 ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    String getLatestCallStatus(String aiCallLogId,String technicianId);

    /**
     * Deletes all history records belonging to the given technicians.
     *
     * @param technicianIds the technician identifiers
     * @return the number of deleted records
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ai_call_log_history WHERE technician_id IN ?1", nativeQuery = true)
    int deleteAICallLogsHistoryByTechnicianIds(Set<String> technicianIds);
}
