package io.sclera.Repository;

import io.sclera.dto.AssetFieldDTO;
import io.sclera.models.AssetField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface AssetFieldRepository extends JpaRepository<AssetField, String> {

    @Query(nativeQuery = true)
    List<AssetFieldDTO> getGlobalAssetFields(List<String> globalAssetFieldNames);

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_field SET is_deleted = true WHERE id IN ?1", nativeQuery = true)
    void deleteAssetFieldsByIds(Set<String> assetFieldIds);

    @Query(nativeQuery = true)
    List<AssetFieldDTO> getAllAssetFields();
}
