package io.sclera.Repository;

import io.sclera.dto.PropertyServiceRequestDTO;
import io.sclera.models.PropertyServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link PropertyServiceRequest} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface PropertyServiceRequestRepository extends JpaRepository<PropertyServiceRequest, String> {

    // POSTGRES: MySQL "VALUE (...) ON DUPLICATE KEY UPDATE label = ?2, options = ?3, type = ?4"
    //        -> "VALUES (...) ON CONFLICT (id) DO UPDATE SET label = EXCLUDED.label, options = EXCLUDED.options, type = EXCLUDED.type"
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO property_service_request (id, label, options, type, property_service_id) VALUES (?1,?2,?3,?4,?5) " +
            "ON CONFLICT (id) DO UPDATE SET label = EXCLUDED.label, options = EXCLUDED.options, type = EXCLUDED.type", nativeQuery = true)
    void upsertPropertyServiceRequest(String id, String label, String options, String type, String property_service_id);

    /** Bound to {@code PropertyServiceRequest.getPropertyServiceRequestsByServiceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyServiceRequestDTO> getPropertyServiceRequestsByServiceId(String property_service_id);
}
