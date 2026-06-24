package io.sclera.repository;

import io.sclera.dto.DigitalTwinTemplateDTO;
import io.sclera.model.DigitalTwinTemplate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalTwinTemplateRepository extends JpaRepository<DigitalTwinTemplate, String> {

    @Query(value = "SELECT COUNT(*) FROM digital_twin_template WHERE name = ?1 AND vdms_id = ?2", nativeQuery = true)
    Integer checkDigitalTwinTemplateByNameAndVdmsId(String name, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO digital_twin_template(id, name, description, image_url, asset_sub_category_id, vdms_id) VALUES(?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addDigitalTwinTemplateByVdmsAndSubCategoryId(String digitalTwinTemplateId, String name, String description, String imageUrl, String subCategoryId, String vdmsId);

    @Query(nativeQuery = true)
    List<DigitalTwinTemplateDTO> getDigitalTwinTemplateListByVdmsId(String vdmsId, String key, String categoryId, String subCategoryId, int pageSize, int offset);

    @Query(nativeQuery = true)
    DigitalTwinTemplateDTO getDigitalTwinTemplateDetailsByVdmsId(String vdmsId, String digitalTwinTemplateId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE digital_twin_template SET name = ?1, description = ?2, asset_sub_category_id = ?3, image_url = ?4 WHERE vdms_id = ?5 AND id = ?6", nativeQuery = true)
    void updateDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(String name, String description, String subCategoryId, String imageUrl, String vdmsId, String digitalTwinTemplateId);

    @Query(value = "SELECT SUBSTRING_INDEX(image_url, '/', -1) FROM digital_twin_template WHERE id IN ?1 ", nativeQuery = true)
    List<String> getImageNameByDigitalTwinTemplateIds(List<String> digitalTwinTemplateIds);

    @Query(value = "SELECT SUBSTRING_INDEX(image_url, '/', -1) FROM digital_twin_template WHERE id = ?1 ", nativeQuery = true)
    String getImageNameByDigitalTwinTemplateId(String digitalTwinTemplateId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM digital_twin_template WHERE vdms_id = ?1 AND id IN ?2", nativeQuery = true)
    void deleteDigitalTwinTemplatesByVdmsAndDigitalTwinTemplateIds(String vdmsId, List<String> digitalTwinTemplateIds);

    @Query(value = "SELECT COUNT(*) FROM digital_twin_template WHERE asset_sub_category_id IN ?1", nativeQuery = true)
    Integer checkTaggedSubCategory(List<String> subCategoryIds);

    @Query(value = "SELECT id FROM digital_twin_template WHERE vdms_id = ?1", nativeQuery = true)
    List<String> getDigitalTwinTemplateIdsByVdmsId(String vdmsId);
}
