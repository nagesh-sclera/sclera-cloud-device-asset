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
    @Query("SELECT new io.sclera.dto.AssetFieldDTO(af.id, af.name) " +
           "FROM AssetField af WHERE af.name IN :globalAssetFieldNames")
    List<AssetFieldDTO> getGlobalAssetFields(List<String> globalAssetFieldNames);

    /**
     * Marks the asset fields with the given ids as deleted.
     *
     * @param assetFieldIds the identifiers of the asset fields to flag as deleted
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE AssetField af SET af.isDeleted = true WHERE af.id IN :assetFieldIds")
    void deleteAssetFieldsByIds(Set<String> assetFieldIds);

    /**
     * Retrieves all asset fields.
     *
     * @return the list of all asset fields
     */
    @Query("SELECT new io.sclera.dto.AssetFieldDTO(" +
           "af.id, af.name, af.type, af.toolTip, af.defaultValue, " +
           "af.isActive, af.options, af.isDeleted, af.showInSection, af.createdAt) " +
           "FROM AssetField af WHERE af.isDeleted = false")
    List<AssetFieldDTO> getAllAssetFields();
}
