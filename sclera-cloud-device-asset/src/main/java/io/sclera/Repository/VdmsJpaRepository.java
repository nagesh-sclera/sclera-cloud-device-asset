package io.sclera.Repository;

import io.sclera.models.Vdms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data access to the local {@code vdms} table for multi-VDMS reads, CRUD,
 * and referential-integrity checks used when deleting a VDMS.
 */
@Repository
public interface VdmsJpaRepository extends JpaRepository<Vdms, String> {

    @Query("SELECT v.id FROM Vdms v")
    List<String> findAllIds();

    @Query("SELECT COUNT(b) FROM Building b WHERE b.vdms.id = :vdmsId")
    long countBuildingsByVdmsId(@Param("vdmsId") String vdmsId);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.vdms.id = :vdmsId")
    long countAssetsByVdmsId(@Param("vdmsId") String vdmsId);
}
