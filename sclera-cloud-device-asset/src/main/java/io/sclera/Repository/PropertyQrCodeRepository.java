package io.sclera.Repository;

import io.sclera.dto.PropertyQrcodeDTO;
import io.sclera.models.PropertyQrcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link PropertyQrcode} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface PropertyQrCodeRepository extends JpaRepository<PropertyQrcode, String> {

    // POSTGRES: MySQL "VALUE (...)" -> "VALUES (...)"
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO property_qrcode (id, image_url, property_service_id, location_id) VALUES (?1,?2,?3,?4)", nativeQuery = true)
    void addPropertyQrcode(String id, String image_url, String property_service_id, String location_id);

    /** Bound to {@code PropertyQrcode.getPropertyServiceLocations} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyQrcodeDTO> getPropertyServiceLocations(String property_service_id);

    /** Bound to {@code PropertyQrcode.getPropertyQrcode} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    PropertyQrcodeDTO getPropertyQrcode(String property_service_id, String location_id);

    /** Bound to {@code PropertyQrcode.getPropertyQrcodeByFloor} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<PropertyQrcodeDTO> getPropertyQrcodeByFloor(String building_id, String floor_id, String location_id, String property_service_id);

    @Query(value = "SELECT property_service_id FROM property_qrcode WHERE location_id = ?1", nativeQuery = true)
    Set<String> getPropertyServicesByLocationId(String location_id);
}
