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

/**
 * Manages persistence and querying of {@link AssetField} entities.
 */
@Repository
public interface AssetFieldRepository extends JpaRepository<AssetField, String> {

    /**
     * Retrieves the global asset fields matching the given field names.
     *
     * @param globalAssetFieldNames the global asset field names to match
     * @return the matching global asset fields
     */
    @Query(nativeQuery = true)
    List<AssetFieldDTO> getGlobalAssetFields(List<String> globalAssetFieldNames);

    /**
     * Marks the asset fields with the given ids as deleted.
     *
     * @param assetFieldIds the identifiers of the asset fields to flag as deleted
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_field SET is_deleted = true WHERE id IN ?1", nativeQuery = true)
    void deleteAssetFieldsByIds(Set<String> assetFieldIds);

    /**
     * Retrieves all asset fields.
     *
     * @return the list of all asset fields
     */
    @Query(nativeQuery = true)
    List<AssetFieldDTO> getAllAssetFields();
}
