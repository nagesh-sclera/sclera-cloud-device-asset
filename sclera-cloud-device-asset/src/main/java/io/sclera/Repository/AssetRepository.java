package io.sclera.Repository;

import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.models.Asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

/**
 * Manages persistence and querying of {@link Asset} entities.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

    /**
     * Retrieves a page of assets filtered by import type and search key.
     *
     * @param importType the import type to filter by
     * @param searchKey  the search key to filter by, or {@code "null"} to skip
     * @param pageable   pagination parameters
     * @return the matching page of assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a WHERE a.import_type = :importType " +
           "AND (:searchKey = 'null' OR CONCAT(a.display_name, a.description) LIKE CONCAT('%', :searchKey, '%')) " +
           "ORDER BY a.display_name")
    List<AssetDTO> getPaginatedAssets(@Param("importType") String importType,
                                      @Param("searchKey") String searchKey,
                                      Pageable pageable);

    /**
     * Removes all asset records.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM asset", nativeQuery = true)
    void deleteAllRecords();

    /**
     * Retrieves the assets linked to the given device.
     *
     * @param deviceId the device identifier
     * @return the linked assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, adm.matchScore, " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a JOIN a.assetDeviceMappings adm " +
           "WHERE a.id IN (SELECT m.asset.id FROM AssetDeviceMapping m WHERE m.device.id = :deviceId)")
    List<AssetDTO> getLinkedAssets(@Param("deviceId") String deviceId);

    /**
     * Retrieves a page of unmapped assets.
     *
     * @param pageable pagination parameters
     * @return the matching page of unmapped assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a WHERE a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m) " +
           "AND a.isMatched = false")
    List<AssetDTO> getUnmappedAssets(Pageable pageable);

    /**
     * Counts all assets.
     *
     * @return the total number of assets
     */
    @Query("SELECT COUNT(a) FROM Asset a")
    Integer getTotalAssetCount();

    /**
     * Updates the matched products for the given asset.
     *
     * @param id              the asset identifier
     * @param matchedProducts the matched products to store
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET matched_products=?2 WHERE id=?1", nativeQuery = true)
    void saveMatchedProductsById(String id, String matchedProducts);

    /**
     * Retrieves a page of assets matching the given filter.
     *
     * @param filter   the filter to apply (column to order by)
     * @param pageable pagination parameters
     * @return the matching page of assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a")
    List<AssetDTO> getFilteredAssets(@Param("filter") String filter, Pageable pageable);

    /**
     * Returns the original keys of all assets.
     *
     * @return the original keys
     */
    @Query("SELECT a.originalKeys FROM Asset a")
    List<String> getOriginalKeys();

    /**
     * Removes the assets with the given ids.
     *
     * @param ids the identifiers of the assets to remove
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset WHERE id IN ?1", nativeQuery = true)
    void deleteAllById(ArrayList<String> ids);

    /**
     * Retrieves the assets with the given ids.
     *
     * @param idList the asset identifiers to match
     * @return the matching assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a WHERE a.id IN :idList")
    List<AssetDTO> getAssetsById(@Param("idList") List<String> idList);

    /**
     * Updates the matched flag for the given asset.
     *
     * @param matched the matched state to set
     * @param id      the asset identifier
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET is_matched=?1 WHERE id=?2", nativeQuery = true)
    void setMatched(Boolean matched, String id);

    /**
     * Retrieves assets that are matched but not yet mapped.
     *
     * @return the unmapped matched assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a WHERE a.isMatched = true " +
           "AND a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m)")
    List<AssetDTO> getUnmappedMatchedAssets();

    /**
     * Removes all matched asset records.
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset WHERE is_matched=1", nativeQuery = true)
    void deleteAllMatchedRecords();

    /**
     * Retrieves the sub-assets of the given parent asset.
     *
     * @param asset_id the parent asset identifier
     * @return the sub-assets of the parent asset
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a WHERE a.subsystem_parent_id = :asset_id")
    List<AssetDTO> getSubAssetsByParentId(@Param("asset_id") String asset_id);

    /**
     * Retrieves the unmapped assets among the given ids.
     *
     * @param idList the asset identifiers to match
     * @return the matching unmapped assets
     */
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
           "FROM Asset a WHERE a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m) " +
           "AND a.isMatched = false AND a.id IN :idList")
    List<AssetDTO> getUnmappedAssetsByIds(@Param("idList") List<String> idList);

    /**
     * Updates the matched products for the given asset.
     *
     * @param toString the matched products to store
     * @param asset_id the asset identifier
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET matched_products = ?1 WHERE id=?2", nativeQuery = true)
    void updateProductId(String toString, String asset_id);

    /**
     * Counts the sub-system assets of the given parent asset.
     *
     * @param parent_asset_id the parent asset identifier
     * @return the number of sub-system assets
     */
    ////update parent device subsystem count
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.subsystem_parent_id = ?1")
    Integer getParentAssetSubsystemCount(String parent_asset_id);

    /**
     * Updates the sub-system count for the given parent asset.
     *
     * @param parent_asset_id the parent asset identifier
     * @param subsystemCount  the sub-system count to set
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET subsystem_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateParentAssetSubsystemCount(String parent_asset_id, Integer subsystemCount);

    /**
     * Retrieves a page of sub-system parent assets for the given import type.
     *
     * @param importType the import type to filter by
     * @param pageable   pagination parameters
     * @return the matching sub-system parent assets
     */
    //parent asset sub system api - parent asset get
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.subsystem_count) " +
           "FROM Asset a WHERE a.subsystem_parent_id IS NULL AND a.import_type = :importType")
    List<AssetDTO> getSubSystemParentAssets(@Param("importType") String importType, Pageable pageable);

    /**
     * Retrieves a page of sub-system assets for the given parent asset.
     *
     * @param asset_id the parent asset identifier
     * @param pageable pagination parameters
     * @return the matching sub-system assets
     */
    //parent asset sub system api - sub system asset get
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.subsystem_count) " +
           "FROM Asset a WHERE a.subsystem_parent_id = :asset_id")
    List<AssetDTO> getSubSystemAssets(@Param("asset_id") String asset_id, Pageable pageable);

    /**
     * Retrieves a page of unmapped sub-system parent assets.
     *
     * @param pageable pagination parameters
     * @return the matching unmapped sub-system parent assets
     */
    //parent asset sub system api for unmapped assets - parent asset get
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.subsystem_count) " +
           "FROM Asset a WHERE a.subsystem_parent_id IS NULL " +
           "AND a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m) " +
           "AND a.isMatched = false")
    List<AssetDTO> getUnmappedSubSystemParentAssets(Pageable pageable);

    /**
     * Retrieves the unmapped sub-system parent assets among the given ids.
     *
     * @param asset_ids the asset identifiers to match
     * @return the matching unmapped sub-system parent assets
     */
    //parent asset sub system api for unmapped assets - parent asset get
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.subsystem_count) " +
           "FROM Asset a WHERE a.subsystem_parent_id IS NULL " +
           "AND a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m) " +
           "AND a.isMatched = false AND a.id IN :asset_ids")
    List<AssetDTO> getUnmappedSubSystemParentAssetsByAssetIds(@Param("asset_ids") List<String> asset_ids);

    /**
     * Updates the sub-system parent id for the given asset.
     *
     * @param asset_id            the asset identifier
     * @param subsystem_parent_id the sub-system parent id to set
     */
    //update sub system parent id
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET subsystem_parent_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateSubsystemParentId(String asset_id, String subsystem_parent_id);

    /**
     * Marks every asset as unmatched.
     */
    //set all assets to unmatched
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET is_matched=0", nativeQuery = true)
    void setAllAssetsToUnMatched();

    /**
     * Retrieves all assets for the given import type.
     *
     * @param importType the import type to filter by
     * @return the matching assets
     */
    //updated method to get all assets from db
    @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
           "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
           "a.ip_address, a.network_layer, a.serial_number, a.warranty, " +
           "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.subsystem_count) " +
           "FROM Asset a WHERE a.import_type = :importType")
    List<AssetDTO> getAllAssets(@Param("importType") String importType);

    /**
     * Returns the sub-system parent id of the given asset.
     *
     * @param asset_id the asset identifier
     * @return the sub-system parent id
     */
    //update parent device subsystem count
    @Query("SELECT a.subsystem_parent_id FROM Asset a WHERE a.id = ?1")
    String getSubsystemParentIdByAssetId(String asset_id);

    /**
     * Returns the ids of the sub-assets of the given parent asset.
     *
     * @param parent_asset_id the parent asset identifier
     * @return the sub-asset ids
     */
    //get sub asset ids by parent device id
    @Query("SELECT a.id FROM Asset a WHERE a.subsystem_parent_id = ?1")
    Set<String> getSubAssetIdByParentId(String parent_asset_id);

    /**
     * Updates the sub-system parent id for the given set of assets.
     *
     * @param subsystem_assets    the identifiers of the assets to update
     * @param subsystem_parent_id the sub-system parent id to set
     */
    //update set of subsystem device parent id
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET subsystem_parent_id = ?2 WHERE id IN ?1", nativeQuery = true)
    void updateSetOfSubsystemParentId(Set<String> subsystem_assets, String subsystem_parent_id);

    /**
     * Inserts an asset, updating its display name, description and type if the id already exists.
     *
     * @param id                  the asset identifier
     * @param display_name        the asset display name
     * @param description         the asset description
     * @param type                the asset type
     * @param mac_address         the asset MAC address
     * @param model               the asset model
     * @param vendor              the asset vendor
     * @param ip_address          the asset IP address
     * @param network_layer       the asset network layer
     * @param serial_number       the asset serial number
     * @param warranty            the asset warranty
     * @param original_keys       the asset original keys
     * @param custom_fields       the asset custom fields
     * @param subsystem_parent_id the sub-system parent id
     * @param is_matched          whether the asset is matched
     * @param matched_products    the matched products
     * @param vdms                the VDMS identifier
     * @param subsystem_count     the sub-system count
     * @param import_type         the import type
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO asset(id,display_name,description,type,mac_address,model,vendor,ip_address,network_layer,serial_number,warranty,original_keys,custom_fields,subsystem_parent_id,is_matched,matched_products,vdms_id,subsystem_count,import_type) VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19) ON CONFLICT (id) DO UPDATE SET display_name=EXCLUDED.display_name, description=EXCLUDED.description, type=EXCLUDED.type", nativeQuery = true)
    void assetUpsert(String id, String display_name, String description, String type, String mac_address, String model, String vendor, String ip_address, int network_layer, String serial_number, String warranty, String original_keys, String custom_fields, String subsystem_parent_id, boolean is_matched, String matched_products, String vdms, int subsystem_count, String import_type);


    /**
     * Counts the assets of the given import type optionally matching the search key.
     *
     * @param parent_asset_id the import type to filter by
     * @param searchKey       the search key to match, or {@code "null"} to ignore
     * @return the number of matching assets
     */
    @Query(value = "SELECT COUNT(*) FROM asset where import_type = ?1 AND ?2 = 'null' or CONCAT_WS('',display_name,description) LIKE CONCAT('%',?2,'%')", nativeQuery = true)
    Integer getAssetCount(String parent_asset_id, String searchKey);


    /**
     * Returns the distinct asset types.
     *
     * @return the unique device types
     */
    @Query("SELECT DISTINCT a.type FROM Asset a")
    List<String> getUniqueDeviceTypes();

    /**
     * Updates the type of assets whose type starts with the given prefix.
     *
     * @param type     the new type to set
     * @param idPrefix the type prefix to match
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset a SET a.type = ?1 WHERE a.type LIKE CONCAT(?2, '%') ", nativeQuery = true)
    void updateTypeByType(String type, String idPrefix);

    /**
     * Indicates whether any asset exists.
     *
     * @return {@code true} if at least one asset exists, otherwise {@code false}
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asset a")
    Boolean checkImportExists();

    /**
     * Sets the type to {@code generic} for assets with no type.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset a SET a.type ='generic' WHERE a.type IS NULL ", nativeQuery = true)
    void setTypeGeneric();

    /**
     * Updates assets of the given type to a new type.
     *
     * @param type    the existing type to match
     * @param generic the new type to set
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset a SET a.type = ?2 WHERE a.type = ?1 ", nativeQuery = true)
    void updateDeviceType(String type, String generic);
}
