package io.sclera.Repository;

import io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO;
import io.sclera.models.Asset;
import io.sclera.models.AssetDeviceMapping;
import io.sclera.models.Device;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetDeviceMappingRepository extends JpaRepository<AssetDeviceMapping, String> {

  @Modifying
  @Transactional
  @Query(value = "INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id) VALUES(?1,?2,?3,?4) ON DUPLICATE KEY UPDATE match_score=?2", nativeQuery = true)
  void saveNewAssetMapping(String id, Integer match_score, Asset asset, Device device);

  @Query(value = "SELECT asset_id FROM asset_device_mapping WHERE device_id IN ?1", nativeQuery = true)
  List<String> findByDeviceIds(List<String> collect);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM asset_device_mapping", nativeQuery = true)
  void deleteAllRecords();

  @Query(value = "SELECT CASE WHEN COUNT(*)>0 THEN 'true' ELSE 'false' END AS bool FROM asset_device_mapping WHERE asset_id=?1", nativeQuery = true)
  String findByAssetId(String asset_id);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM asset_device_mapping WHERE device_id=?1", nativeQuery = true)
  void deleteByDeviceId(String new_device_id);

  @Query(nativeQuery = true)
  List<AssetDeviceMappingDTO> findMappings();
}
