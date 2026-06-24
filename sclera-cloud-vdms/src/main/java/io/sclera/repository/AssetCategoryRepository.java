package io.sclera.repository;

import io.sclera.dto.CategoryDTO;
import io.sclera.model.AssetCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, String> {

    @Query(nativeQuery = true)
    List<CategoryDTO> getAllCategory(String key, String sort, int pageSize, int offset);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asset_category (id, name, icon_url, display_name, creation_timestamp) VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    void addCategory(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp);

    @Query(value = "SELECT icon_url FROM asset_category WHERE id = ?1", nativeQuery = true)
    String getImageUrlById(String categoryId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset_category WHERE id IN ?1", nativeQuery = true)
    void deleteCategoryByIds(List<String> categoryIds);


    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_category SET name = ?1, icon_url = ?2, display_name = ?3 WHERE id = ?4", nativeQuery = true)
    void updateCategoryById(String name, String iconUrl, String displayName, String id);

    @Query(value = "SELECT name FROM asset_category WHERE id = ?1", nativeQuery = true)
    String getNameById(String id);

    @Query(value = "SELECT COUNT(*) FROM asset_category WHERE name = ?1", nativeQuery = true)
    Integer checkCategoryByName(String name);

    @Query(value = "SELECT COUNT(*) FROM asset_category WHERE id != ?1 AND name = ?2", nativeQuery = true)
    Integer checkCategoryByIdAndName(String categoryId, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_category SET icon_url = ?1 WHERE id = ?2", nativeQuery = true)
    void updateCategoryIconById(String iconUrl, String categoryId);

    @Query(value = "SELECT icon_url FROM asset_category WHERE icon_url != NULL AND id IN ?1", nativeQuery = true)
    List<String> getImageUrlsByIds(List<String> categoryIds);
}
