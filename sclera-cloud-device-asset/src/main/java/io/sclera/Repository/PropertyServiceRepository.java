package io.sclera.Repository;

import io.sclera.dto.PropertyServiceDTO;
import io.sclera.models.PropertyService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link PropertyService} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface PropertyServiceRepository extends JpaRepository<PropertyService, String> {

    // POSTGRES: MySQL "VALUE (...) ON DUPLICATE KEY UPDATE name = ?2"
    //        -> "VALUES (...) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name"
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO property_service (id, name, vdms_id) VALUES (?1,?2,?3) " +
            "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name", nativeQuery = true)
    void upsertPropertyService(String id, String name, String vdmsid);

    /** Bound to {@code PropertyService.getPropertyServicesById} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    PropertyServiceDTO getPropertyServicesById(String vdmsid, String id);

    /** Bound to {@code PropertyService.getPropertyServices} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyServiceDTO> getPropertyServices();
}
