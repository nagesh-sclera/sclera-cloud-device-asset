package io.sclera.Repository;

import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.models.Asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

    @Query(nativeQuery = true)
    List<AssetDTO> getPaginatedAssets(Integer limit, Integer offset, String importType, String searchKey);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM asset", nativeQuery = true)
    void deleteAllRecords();

    @Query(nativeQuery = true)
    List<AssetDTO> getLinkedAssets(String id);

    @Query(nativeQuery = true)
    List<AssetDTO> getUnmappedAssets(Integer pageSize, Integer offset);

    @Query(value = "SELECT COUNT(a.id) FROM asset a", nativeQuery = true)
    Integer getTotalAssetCount();

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET matched_products=?2 WHERE id=?1", nativeQuery = true)
    void saveMatchedProductsById(String id, String matchedProducts);

    @Query(nativeQuery = true)
    List<AssetDTO> getFilteredAssets(String filter, Integer pageSize, Integer offset);

    @Query(value = "SELECT original_keys FROM asset LIMIT 1", nativeQuery = true)
    String getOriginalKeys();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset WHERE id IN ?1", nativeQuery = true)
    void deleteAllById(ArrayList<String> ids);

    @Query(nativeQuery = true)
    List<AssetDTO> getAssetsById(List<String> idList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET is_matched=?1 WHERE id=?2", nativeQuery = true)
    void setMatched(Boolean matched, String id);

    @Query(nativeQuery = true)
    List<AssetDTO> getUnmappedMatchedAssets();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset WHERE is_matched=1", nativeQuery = true)
    void deleteAllMatchedRecords();

    @Query(nativeQuery = true)
    List<AssetDTO> getSubAssetsByParentId(String asset_id);

    @Query(nativeQuery = true)
    List<AssetDTO> getUnmappedAssetsByIds(List<String> idList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET matched_products = ?1 WHERE id=?2", nativeQuery = true)
    void updateProductId(String toString, String asset_id);

    ////update parent device subsystem count
    @Query(value = "SELECT COUNT(*) FROM asset where subsystem_parent_id = ?1", nativeQuery = true)
    Integer getParentAssetSubsystemCount(String parent_asset_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET subsystem_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateParentAssetSubsystemCount(String parent_asset_id, Integer subsystemCount);

    //parent asset sub system api - parent asset get
    @Query(nativeQuery = true)
    List<AssetDTO> getSubSystemParentAssets(Integer pageSize, Integer offset, String importType);

    //parent asset sub system api - sub system asset get
    @Query(nativeQuery = true)
    List<AssetDTO> getSubSystemAssets(String asset_id, Integer pageSize, Integer offset);

    //parent asset sub system api for unmapped assets - parent asset get
    @Query(nativeQuery = true)
    List<AssetDTO> getUnmappedSubSystemParentAssets(Integer pageSize, Integer offset);

    //parent asset sub system api for unmapped assets - parent asset get
    @Query(nativeQuery = true)
    List<AssetDTO> getUnmappedSubSystemParentAssetsByAssetIds(List<String> asset_ids);

    //update sub system parent id
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET subsystem_parent_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateSubsystemParentId(String asset_id, String subsystem_parent_id);

    //set all assets to unmatched
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET is_matched=0", nativeQuery = true)
    void setAllAssetsToUnMatched();

    //updated method to get all assets from db
    @Query(nativeQuery = true)
    List<AssetDTO> getAllAssets(String importType);

    //update parent device subsystem count
    @Query(value = "SELECT subsystem_parent_id FROM asset where id = ?1", nativeQuery = true)
    String getSubsystemParentIdByAssetId(String asset_id);

    //get sub asset ids by parent device id
    @Query(value = "SELECT id FROM asset where subsystem_parent_id = ?1", nativeQuery = true)
    Set<String> getSubAssetIdByParentId(String parent_asset_id);

    //update set of subsystem device parent id
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset SET subsystem_parent_id = ?2 WHERE id IN ?1", nativeQuery = true)
    void updateSetOfSubsystemParentId(Set<String> subsystem_assets, String subsystem_parent_id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asset(id,display_name,description,type,mac_address,model,vendor,ip_address,network_layer,serial_number,warranty,original_keys,custom_fields,subsystem_parent_id,is_matched,matched_products,vdms_id,subsystem_count,import_type) VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19) ON DUPLICATE KEY UPDATE display_name=?2, description=?3, type=?4", nativeQuery = true)
    void assetUpsert(String id, String display_name, String description, String type, String mac_address, String model, String vendor, String ip_address, int network_layer, String serial_number, String warranty, String original_keys, String custom_fields, String subsystem_parent_id, boolean is_matched, String matched_products, String vdms, int subsystem_count, String import_type);


    @Query(value = "SELECT COUNT(*) FROM asset where import_type = ?1 AND ?2 = 'null' or CONCAT_WS('',display_name,description) LIKE CONCAT('%',?2,'%')", nativeQuery = true)
    Integer getAssetCount(String parent_asset_id, String searchKey);


    @Transactional
    @Query(value = "SELECT DISTINCT(a.type) FROM asset a ", nativeQuery = true)
    List<String> getUniqueDeviceTypes();

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset a SET a.type = ?1 WHERE a.type LIKE CONCAT(?2, '%') ", nativeQuery = true)
    void updateTypeByType(String type, String idPrefix);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM asset)", nativeQuery = true)
    Boolean checkImportExists();

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset a SET a.type ='generic' WHERE a.type IS NULL ", nativeQuery = true)
    void setTypeGeneric();

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset a SET a.type = ?2 WHERE a.type = ?1 ", nativeQuery = true)
    void updateDeviceType(String type, String generic);
}
