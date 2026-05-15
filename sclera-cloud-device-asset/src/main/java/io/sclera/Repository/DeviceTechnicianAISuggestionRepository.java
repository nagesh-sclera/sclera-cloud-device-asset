package io.sclera.Repository;

import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import io.sclera.models.DeviceTechnicianAISuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
@Repository
public interface DeviceTechnicianAISuggestionRepository extends JpaRepository<DeviceTechnicianAISuggestion,String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_technician_ai_suggestion (id, device_type, technicians, vdms_id) " +
            "VALUES (?1, ?2, CAST(?3 AS JSON), ?4)", nativeQuery = true)
    Integer createTechnicianSuggestion(String id, String deviceType, String technicians, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_technician_ai_suggestion " +
            "SET device_type = ?2, technicians = CAST(?3 AS JSON), vdms_id = ?4 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianSuggestion(String id, String deviceType, String technicians, String vdmsId);



    @Query(nativeQuery = true)
    DeviceTechnicianAISuggestionDTO getdevicetechnicianbyid(String id);




    @Query(nativeQuery = true)
    List<DeviceTechnicianAISuggestionDTO> getAlldevicetechnician();


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_technician_ai_suggestion WHERE id = ?1", nativeQuery = true)
    void deletedevicetechnicianById(String id);


    @Query(value = "SELECT technicians FROM device_technician_ai_suggestion WHERE device_type = ?1 AND vdms_id = ?2", nativeQuery = true)
    String getDeviceTechnicianAISuggestionByDeviceType(String deviceType, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_technician_ai_suggestion (id, device_type, technicians, vdms_id) " +
            "VALUES (?1, ?2, CAST(?3 AS JSON), ?4) " +
            "ON DUPLICATE KEY UPDATE " +
            "device_type = ?2, technicians = CAST(?3 AS JSON)",
            nativeQuery = true)
    Integer upsertTechnicianSuggestion(String id, String deviceType, String technicians, String vdmsId);
}