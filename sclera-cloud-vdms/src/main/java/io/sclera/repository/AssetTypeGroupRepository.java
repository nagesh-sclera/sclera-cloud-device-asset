package io.sclera.repository;


import io.sclera.model.AssetTypeGroup;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTypeGroupRepository extends JpaRepository<AssetTypeGroup, String> {

    @Query(value = "SELECT * FROM asset_type_group WHERE (?1 = 'all' OR CONCAT_WS('', name) LIKE CONCAT('%', ?1, '%')) ", nativeQuery = true)
    List<String> getAllAssetTypeGroup(String key, String sort);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asset_type_group (name) VALUES (?1)", nativeQuery = true)
    void addAssetTypeGroup(String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asset_type_group WHERE name IN ?1", nativeQuery = true)
    void deleteAssetTypeGroupByIds(List<String> assetTypeGroupIds);

    @Query(value = "SELECT COUNT(*) FROM asset_type_group WHERE name = ?1", nativeQuery = true)
    Integer checkAssetTypeGroupByName(String name);

}
