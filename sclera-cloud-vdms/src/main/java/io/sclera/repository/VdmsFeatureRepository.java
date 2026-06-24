package io.sclera.repository;

import io.sclera.dto.VdmsFeatureDTO;
import io.sclera.model.VdmsFeature;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VdmsFeatureRepository extends JpaRepository<VdmsFeature, String> {


    @Query(nativeQuery = true)
    List<VdmsFeatureDTO> getVdmsFeatureDetailsByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_feature(id, feature_id, vdms_id) VALUES (?1,?2,?3)", nativeQuery = true)
    void addVdmsFeatureByVdmsAndFeatureId(String id, String featureId, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms_feature WHERE vdms_id = ?1", nativeQuery = true)
    void deleteVdmsFeatureByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms_feature WHERE feature_id = ?1", nativeQuery = true)
    void deleteVdmsFeatureByFeatureId(String featureId);
}
