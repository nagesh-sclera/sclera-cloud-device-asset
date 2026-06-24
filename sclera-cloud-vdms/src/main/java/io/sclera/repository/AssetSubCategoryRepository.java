package io.sclera.repository;

import io.sclera.dto.SubCategoryDTO;
import io.sclera.model.AssetSubCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface AssetSubCategoryRepository extends JpaRepository<AssetSubCategory, String> {

    @Query(nativeQuery = true)
    List<SubCategoryDTO> getSubCategoryByCategoryId(String categoryId, String key, String sort);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asset_sub_category (id, name, icon_url, display_name, creation_timestamp, asset_category_id) VALUES (?1, ?2, ?3, ?4, ?5 , ?6)", nativeQuery = true)
    void addSubCategoryByCategoryId(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp, String categoryId);

    @Query(value = "SELECT SUBSTRING_INDEX(icon_url, '/', -1) FROM asset_sub_category WHERE asset_category_id = ?1", nativeQuery = true)
    List<String> getImageUrlByCategoryId(String categoryId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset_sub_category WHERE asset_category_id = ?1", nativeQuery = true)
    void deleteSubcategoryByCategoryId(String categoryId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset_sub_category WHERE asset_category_id = ?1 AND id IN ?2", nativeQuery = true)
    void deleteSubcategoryByCategoryAndSubcategoryIds(String categoryId, List<String> subCategoryIds);

    @Query(value = "SELECT icon_url FROM asset_sub_category WHERE id = ?1", nativeQuery = true)
    String getImageUrlById(String id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_sub_category SET name = ?1, icon_url = ?2, display_name = ?3 WHERE id = ?4 AND asset_category_id = ?5", nativeQuery = true)
    void updateSubCategoryByCategoryAndSubcategoryId(String name, String iconUrl, String displayName, String subCategoryId, String categoryId);

    @Query(nativeQuery = true)
    List<SubCategoryDTO> getAllSubCategories();


    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_sub_category SET icon_url = ?1 WHERE id = ?2", nativeQuery = true)
    void updateSubCategoryIconUrl(String iconURL, String id);

    @Query(value = "SELECT name FROM asset_sub_category WHERE id = ?1", nativeQuery = true)
    String getNameById(String id);

    @Query(value = "SELECT COUNT(*) FROM asset_sub_category WHERE ((id != ?1 AND name = ?2) AND asset_category_id = ?3)", nativeQuery = true)
    Integer checkSubCategoryByIdAndName(String subCategoryId, String name, String categoryId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_sub_category SET icon_url = ?1 WHERE id = ?2 AND asset_category_id = ?3", nativeQuery = true)
    void updateSubCategoryIconById(String iconUrl, String subCategoryId, String categoryId);

    @Query(value = "SELECT COUNT(*) FROM asset_sub_category WHERE name = ?1 AND asset_category_id = ?2", nativeQuery = true)
    Integer checkSubCategoryByNameAndCategoryId(String name, String categoryId);

    @Query(value = "SELECT icon_url FROM asset_sub_category WHERE icon_url != NULL AND id IN ?1", nativeQuery = true)
    List<String> getImageUrlsByIds(List<String> subCategoryIds);

    @Query(value = "SELECT id FROM asset_sub_category WHERE asset_category_id IN ?1", nativeQuery = true)
    List<String> getSubCategoryIdsByCategoryIds(List<String> categoryIds);

    @Query(value = "SELECT asset_category_id FROM asset_sub_category WHERE id = ?1", nativeQuery = true)
    String getAssetCategoryIdById(String id);
}
