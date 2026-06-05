package io.sclera.Repository;

import io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO;
import io.sclera.models.Asset;
import io.sclera.models.AssetDeviceMapping;
import io.sclera.models.Device;
import java.util.List;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Manages persistence and querying of {@link AssetDeviceMapping} entities.
 */
@Repository
public interface AssetDeviceMappingRepository extends JpaRepository<AssetDeviceMapping, String> {

  /**
   * Inserts a new asset-to-device mapping, updating the match score if the id already exists.
   *
   * @param id the mapping identifier
   * @param match_score the match score between the asset and device
   * @param asset the mapped asset
   * @param device the mapped device
   */
  @Modifying
  @Transactional
  // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
  @Query(value = "INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id) VALUES(?1,?2,?3,?4) ON CONFLICT (id) DO UPDATE SET match_score=EXCLUDED.match_score", nativeQuery = true)
  void saveNewAssetMapping(String id, Integer match_score, Asset asset, Device device);

  /**
   * Returns the asset ids mapped to any of the given devices.
   *
   * @param collect the device identifiers to match
   * @return the asset ids mapped to the given devices
   */
  @Query(value = "SELECT asset_id FROM asset_device_mapping WHERE device_id IN ?1", nativeQuery = true)
  List<String> findByDeviceIds(List<String> collect);

  /**
   * Removes all asset-device mapping records.
   */
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM asset_device_mapping", nativeQuery = true)
  void deleteAllRecords();

  /**
   * Indicates whether any mapping exists for the given asset.
   *
   * @param asset_id the asset identifier
   * @return {@code "true"} if a mapping exists for the asset, otherwise {@code "false"}
   */
  @Query(value = "SELECT CASE WHEN COUNT(*)>0 THEN 'true' ELSE 'false' END AS bool FROM asset_device_mapping WHERE asset_id=?1", nativeQuery = true)
  String findByAssetId(String asset_id);

  /**
   * Removes the mapping for the given device.
   *
   * @param new_device_id the device identifier whose mapping is removed
   */
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM asset_device_mapping WHERE device_id=?1", nativeQuery = true)
  void deleteByDeviceId(String new_device_id);

  /**
   * Retrieves all asset-device mappings.
   *
   * @return the list of asset-device mappings
   */
  @Query(nativeQuery = true)
  List<AssetDeviceMappingDTO> findMappings();
}
