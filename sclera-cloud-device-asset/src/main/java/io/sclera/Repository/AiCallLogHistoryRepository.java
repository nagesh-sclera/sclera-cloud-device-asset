package io.sclera.Repository;

import io.sclera.dto.AiCallLogHistoryDTO;
import io.sclera.models.AiCallLogHistory;
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
 * Manages persistence and querying of {@link AiCallLogHistory} entities.
 */
@Repository
public interface AiCallLogHistoryRepository extends JpaRepository<AiCallLogHistory, String> {

    /**
     * Returns the history records associated with the given AI call log, ordered by creation time.
     *
     * @param id the AI call log identifier
     * @return the matching history records as DTOs
     */
    @Query("SELECT new io.sclera.dto.AiCallLogHistoryDTO(" +
           "h.id, h.createdAt, h.description, h.technician.id, h.state, h.aiCallLog.id) " +
           "FROM AiCallLogHistory h WHERE h.aiCallLog.id = ?1 ORDER BY h.createdAt ASC")
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
    // NOT CONVERTED — stays native: plain INSERT, already PG-valid; no ON CONFLICT / MySQL-specific syntax
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
    @Query("SELECT new io.sclera.dto.AiCallLogHistoryDTO(" +
           "h.id, h.createdAt, h.description, h.technician.id, h.state, h.aiCallLog.id) " +
           "FROM AiCallLogHistory h WHERE h.id = ?1")
    AiCallLogHistoryDTO getAiCallLogHistoryById(String aiCallLogHistoryId);

    /**
     * Returns the distinct states recorded for the given history record identifier.
     *
     * @param aiCallLogId the history record identifier
     * @return the set of recorded states
     */
    @Query("SELECT h.state FROM AiCallLogHistory h WHERE h.id = ?1")
    Set<String> findStatusesByAiCallLogId(String aiCallLogId);

    /**
     * Counts the history records belonging to the given AI call log.
     *
     * @param aiCallLogId the AI call log identifier
     * @return the number of matching history records
     */
    @Query("SELECT COUNT(h) FROM AiCallLogHistory h WHERE h.aiCallLog.id = ?1")
    int countByAiCallLogId(String aiCallLogId);

    /**
     * Backing query for {@link #getLatestCallStatus}: returns up to one state ordered by creation
     * time descending.  Callers use the {@code default} wrapper below.
     */
    @Query("SELECT h.state FROM AiCallLogHistory h " +
           "WHERE h.aiCallLog.id = ?1 AND h.technician.id = ?2 " +
           "ORDER BY h.createdAt DESC")
    List<String> findLatestCallStatusList(String aiCallLogId, String technicianId, Pageable pageable);

    /**
     * Returns the most recent state recorded for the given AI call log and technician.
     *
     * @param aiCallLogId the AI call log identifier
     * @param technicianId the technician identifier
     * @return the latest recorded state, or {@code null} if none
     */
    default String getLatestCallStatus(String aiCallLogId, String technicianId) {
        List<String> results = findLatestCallStatusList(
                aiCallLogId, technicianId,
                org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Deletes all history records belonging to the given technicians.
     *
     * @param technicianIds the technician identifiers
     * @return the number of deleted records
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM AiCallLogHistory h WHERE h.technician.id IN ?1")
    int deleteAICallLogsHistoryByTechnicianIds(Set<String> technicianIds);
}
