package io.sclera.repository;

import io.sclera.dto.VdmsIntegrationDTO;
import io.sclera.model.VdmsIntegration;
import io.sclera.model.compositeclass.VdmsIntegrationIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;


@Repository
public interface VdmsIntegrationRepository extends JpaRepository<VdmsIntegration, VdmsIntegrationIds> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_integration(id,vdms_id,helper_id) VALUE(?1,?2,?3)", nativeQuery = true)
    void addVdmsIntegrationByAndVdmsIdAndId(String id,String vdms, String helperId);


    @Query(value = "SELECT helper_id FROM vdms_integration WHERE vdms_id = ?1",nativeQuery = true)
    List<String> getAllVdmsIntegrationsByVdmsId(String vdms);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms_integration SET active = ?1 WHERE vdms_id = ?2 AND id = ?3", nativeQuery = true)
    void updateVdmsIntegrationActivityByVdmsIdAndId(Integer activity, String vdms, String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms_integration WHERE vdms_id = ?1 AND id = ?2", nativeQuery = true)
    void deleteVdmsIntegrationByVdmsIdAndId(String vdms, String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms_integration WHERE vdms_id = ?1" ,nativeQuery = true)
    void deleteVdmsIntegrationByVdmsId(String vdms_id);

    @Query(nativeQuery = true)
    List<VdmsIntegrationDTO> getAllVdmsIntegrationsByVdmsIdAndHelperId(List<String> helperIds,String vdms);
}
