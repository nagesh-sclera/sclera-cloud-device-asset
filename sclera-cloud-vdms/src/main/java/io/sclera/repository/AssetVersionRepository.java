package io.sclera.repository;

import io.sclera.dto.VersionDTO;
import io.sclera.model.AssetVersion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetVersionRepository extends JpaRepository<AssetVersion, String> {
    @Query(nativeQuery = true)
    VersionDTO getAssetVersion();

    @Modifying
    @Transactional
    @Query(value = "UPDATE asset_version SET version = ?1" ,nativeQuery = true)
    void updateAssetVersion(String assetVersion);
}

