package io.sclera.Repository;

import io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO;
import io.sclera.models.AssetDeviceMapping;
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
   * Inserts a new asset-to-device mapping, updating only the match score if the id already exists.
   *
   * @param id the mapping identifier
   * @param match_score the match score between the asset and device
   * @param asset_id the mapped asset identifier
   * @param device_id the mapped device identifier
   */
  @Modifying
  @Transactional
  // NOT CONVERTED — stays native: the INSERT ... ON CONFLICT upsert is ALREADY valid PostgreSQL (no MySQL
  // constructs). An entity save()/find-or-create is strictly worse here: AssetDeviceMapping has an ASSIGNED
  // @Id, so Spring Data routes save() to merge(), which does a SELECT-before-insert that eagerly materialises
  // the @ManyToOne asset + the device's large eager association graph just to set one column. The FK params
  // were changed from Asset/Device entities to scalar ids — entity params do not bind in Hibernate 7 native
  // queries and these are plain FK columns.
  @Query(value = "INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id) VALUES(?1,?2,?3,?4) ON CONFLICT (id) DO UPDATE SET match_score=EXCLUDED.match_score", nativeQuery = true)
  void saveNewAssetMapping(String id, Integer match_score, String asset_id, String device_id);

  /**
   * Returns the asset ids mapped to any of the given devices.
   *
   * @param collect the device identifiers to match
   * @return the asset ids mapped to the given devices
   */
  @Query("SELECT m.asset.id FROM AssetDeviceMapping m WHERE m.device.id IN ?1")
  List<String> findByDeviceIds(List<String> collect);

  /**
   * Removes all asset-device mapping records.
   */
  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("DELETE FROM AssetDeviceMapping m")
  void deleteAllRecords();

  /**
   * Indicates whether any mapping exists for the given asset.
   *
   * @param asset_id the asset identifier
   * @return {@code "true"} if a mapping exists for the asset, otherwise {@code "false"}
   */
  @Query("SELECT CASE WHEN COUNT(m) > 0 THEN 'true' ELSE 'false' END FROM AssetDeviceMapping m WHERE m.asset.id = ?1")
  String findByAssetId(String asset_id);

  /**
   * Removes the mapping for the given device.
   *
   * @param new_device_id the device identifier whose mapping is removed
   */
  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("DELETE FROM AssetDeviceMapping m WHERE m.device.id = ?1")
  void deleteByDeviceId(String new_device_id);

  /**
   * Retrieves all asset-device mappings.
   *
   * @return the list of asset-device mappings
   */
  @Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO(m.asset.id, m.device.id)"
      + " FROM AssetDeviceMapping m")
  List<AssetDeviceMappingDTO> findMappings();
}
