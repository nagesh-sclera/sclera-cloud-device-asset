package io.sclera.Repository;

import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import io.sclera.models.DeviceTechnicianAISuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
/**
 * Manages persistence and querying of {@link DeviceTechnicianAISuggestion} entities.
 */
@Repository
public interface DeviceTechnicianAISuggestionRepository extends JpaRepository<DeviceTechnicianAISuggestion,String> {

    /**
     * Inserts a new technician suggestion record.
     *
     * @param id the suggestion identifier
     * @param deviceType the device type the suggestion applies to
     * @param technicians the technicians payload stored as JSONB
     * @param vdmsId the owning VDMS identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_technician_ai_suggestion (id, device_type, technicians, vdms_id) " +
            "VALUES (?1, ?2, CAST(?3 AS jsonb), ?4)", nativeQuery = true)
    Integer createTechnicianSuggestion(String id, String deviceType, String technicians, String vdmsId);

    /**
     * Updates an existing technician suggestion record.
     *
     * @param id the suggestion identifier to update
     * @param deviceType the new device type
     * @param technicians the new technicians payload stored as JSONB
     * @param vdmsId the new owning VDMS identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE device_technician_ai_suggestion " +
            "SET device_type = ?2, technicians = CAST(?3 AS jsonb), vdms_id = ?4 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianSuggestion(String id, String deviceType, String technicians, String vdmsId);



    /**
     * Retrieves a single technician suggestion projection by its identifier.
     *
     * @param id the suggestion identifier
     * @return the matching suggestion projection
     */
    @Query(nativeQuery = true)
    DeviceTechnicianAISuggestionDTO getdevicetechnicianbyid(String id);




    /**
     * Retrieves all technician suggestion projections.
     *
     * @return the list of suggestion projections
     */
    @Query(nativeQuery = true)
    List<DeviceTechnicianAISuggestionDTO> getAlldevicetechnician();


    /**
     * Deletes the technician suggestion identified by the given id.
     *
     * @param id the suggestion identifier to delete
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_technician_ai_suggestion WHERE id = ?1", nativeQuery = true)
    void deletedevicetechnicianById(String id);


    /**
     * Returns the technicians payload for the given device type within a VDMS.
     *
     * @param deviceType the device type to match
     * @param vdmsId the owning VDMS identifier
     * @return the technicians JSONB value
     */
    @Query(value = "SELECT technicians FROM device_technician_ai_suggestion WHERE device_type = ?1 AND vdms_id = ?2", nativeQuery = true)
    String getDeviceTechnicianAISuggestionByDeviceType(String deviceType, String vdmsId);

    /**
     * Inserts a technician suggestion, updating the device type and technicians on id conflict.
     *
     * @param id the suggestion identifier
     * @param deviceType the device type the suggestion applies to
     * @param technicians the technicians payload stored as JSONB
     * @param vdmsId the owning VDMS identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO device_technician_ai_suggestion (id, device_type, technicians, vdms_id) " +
            "VALUES (?1, ?2, CAST(?3 AS jsonb), ?4) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "device_type = EXCLUDED.device_type, technicians = CAST(?3 AS jsonb)",
            nativeQuery = true)
    Integer upsertTechnicianSuggestion(String id, String deviceType, String technicians, String vdmsId);
}