package io.sclera.repository;

import io.sclera.dto.FeatureDTO;
import io.sclera.model.Feature;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO feature(id, name, image_url, deep_link_url) VALUES(?1,?2,?3,?4)", nativeQuery = true)
    void addFeature(String id, String name, String imageUrl, String deepLinkUrl);

    @Query(nativeQuery = true)
    List<FeatureDTO> getFeatureList();

    @Query(nativeQuery = true)
    FeatureDTO getFeatureDetailsById(String featureId);

    @Query(nativeQuery = true)
    List<FeatureDTO> getVdmsFeatureByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE feature SET name = ?1, image_url = ?2, deep_link_url = ?3 WHERE id = ?4", nativeQuery = true)
    void updateFeatureDetailsById(String name, String imageUrl, String deepLinkUrl, String featureId);

    @Query(value = "SELECT image_url FROM feature WHERE id = ?1", nativeQuery = true)
    String getImageUrlById(String featureId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM feature WHERE id = ?1", nativeQuery = true)
    void deleteFeatureById(String featureId);

    @Query(nativeQuery = true)
    List<FeatureDTO> getVdmsFeatureByOrgId(String orgId);
}
