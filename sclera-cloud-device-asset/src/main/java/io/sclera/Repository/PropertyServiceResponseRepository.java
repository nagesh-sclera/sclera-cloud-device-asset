package io.sclera.Repository;

import io.sclera.dto.PropertyServiceResponseDTO;
import io.sclera.models.PropertyServiceResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

/**
 * Manages persistence and querying of {@link PropertyServiceResponse} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface PropertyServiceResponseRepository extends JpaRepository<PropertyServiceResponse, String> {

    // POSTGRES: MySQL "VALUE (...)" -> "VALUES (...)"
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO property_service_response (id, value, timestamp, property_qrcode_id, property_service_request_id) VALUES (?1,?2,?3,?4,?5)", nativeQuery = true)
    void addPropertyServiceResponse(String id, String value, BigInteger timestamp, String property_qrcode_id, String property_service_request_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE property_service_response SET value = ?3, alert = ?4, timestamp = ?5 WHERE property_qrcode_id = ?1 AND property_service_request_id = ?2", nativeQuery = true)
    void updatePropertyServiceResponse(String property_qrcode_id, String property_service_request_id, String value, Boolean alert, BigInteger timestamp);

    /** Bound to {@code PropertyServiceResponse.getPropertyServiceRequestResponsesByServiceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyServiceResponseDTO> getPropertyServiceRequestResponsesByServiceId(String property_service_id);

    /** Bound to {@code PropertyServiceResponse.getPropertyServiceRequestResponsesByIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyServiceResponseDTO> getPropertyServiceRequestResponsesByIds(String property_service_id, String id);

    @Query(value = "SELECT COUNT(*) FROM property_service_response WHERE property_qrcode_id = ?1 AND property_service_request_id = ?2", nativeQuery = true)
    Integer getPropertyServiceResponseCount(String property_qrcode_id, String property_service_request_id);

    /** Bound to {@code PropertyServiceResponse.getPropertyServiceResponses} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyServiceResponseDTO> getPropertyServiceResponses(String vdmsid);

    /** Bound to {@code PropertyServiceResponse.getPropertyServiceResponseById} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    PropertyServiceResponseDTO getPropertyServiceResponseById(String property_qrcode_id, String property_service_request_id);
}
