package io.sclera.repository;

import io.sclera.dto.CategoryDTO;
import io.sclera.model.AssetType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, String> {

    @Query(nativeQuery = true)
    List<CategoryDTO> getAssetType(String assetTypeGroupName, String key, String sort, int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asset_type (id, name, icon_url, display_name, creation_timestamp, asset_type_group_name) VALUES (?1, ?2, ?3, ?4, ?5 , ?6)", nativeQuery = true)
    void addAssetTypeByAssetTypeGroupName(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp, String assetTypeGroupName);

    @Query(value = "SELECT SUBSTRING_INDEX(icon_url, '/', -1) FROM asset_type WHERE asset_type_group_name = ?1", nativeQuery = true)
    List<String> getImageUrlByAssetTypeGroupName(String assetTypeGroupName);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset_type WHERE asset_type_group_name = ?1", nativeQuery = true)
    void deleteAssetTypeByAssetTypeGroupName(String assetTypeGroupName);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset_type WHERE id IN ?1", nativeQuery = true)
    void deleteAssetTypeByAssetTypeIds(List<String> assetTypeIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_type SET name = ?1, icon_url = ?2, display_name = ?3, asset_type_group_name = ?4,updated_timestamp = ?5 WHERE id = ?6", nativeQuery = true)
    void updateAssetTypeByAssetTypeAndAssetTypeGroupName(String name, String iconUrl, String displayName, String assetTypeGroupName,
                                                         BigInteger updatedTimestamp, String assetTypeId);

    @Query(value = "SELECT name FROM asset_type WHERE id = ?1", nativeQuery = true)
    String getNameById(String id);

    @Query(value = "SELECT COUNT(*) FROM asset_type WHERE ((id != ?1 AND name = ?2) AND asset_type_group_name = ?3)", nativeQuery = true)
    Integer checkAssetTypeByIdAndName(String assetTypeIds, String name, String assetTypeGroupName);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_type SET icon_url = ?1 WHERE id = ?2 AND asset_type_group_name = ?3", nativeQuery = true)
    void updateAssetTypeIconById(String iconUrl, String assetTypeId, String assetTypeGroupName);

    @Query(value = "SELECT COUNT(*) FROM asset_type WHERE name = ?1 AND asset_type_group_name = ?2", nativeQuery = true)
    Integer checkAssetTypeByNameAndAssetTypeGroupName(String name, String assetTypeGroupName);

    @Query(nativeQuery = true)
    List<CategoryDTO> getAllAssetTypes(String searchKey, String sort);

    @Query(nativeQuery = true)
    List<CategoryDTO> getAssetTypeByIds(List<String> assetTypeIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_type SET asset_type_group_name = ?1 WHERE asset_type_group_name IN ?2", nativeQuery = true)
    void updateAssetTypeGroupName(String generic,List<String> assetTypeGroupNames);

    @Query(value = "SELECT icon_url FROM asset_type WHERE name = ?1",nativeQuery = true)
    String getIconUrlByAssetTypeName(String type);

    @Query(value = "SELECT icon_url FROM asset_type WHERE display_name = ?1",nativeQuery = true)
    String getIconUrlByAssetTypeDisplayName(String type);

    @Query(nativeQuery = true)
    List<CategoryDTO> getUpdatedAssetType(String assetTypeGroupName, String key, String sort, int pageSize, int offset, String updatedTimestamp);

    @Query(value = "SELECT name FROM asset_type", nativeQuery = true)
    List<String> getAllAssetTypeNames();
}