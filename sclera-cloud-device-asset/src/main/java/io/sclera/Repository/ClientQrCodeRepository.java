package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.ClientQrCodeDTO;
import io.sclera.models.ClientQrCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

/**
 * Manages persistence and querying of {@link ClientQrCode} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface ClientQrCodeRepository extends JpaRepository<ClientQrCode, String> {

    @Query(value = "SELECT COUNT(cqc.device_id) FROM client_qr_code cqc LEFT JOIN device d ON d.id = cqc.device_id WHERE d.id = ?1", nativeQuery = true)
    Integer getClientQrCodeCountByDeviceId(String deviceId);

    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT location_id FROM client_qr_code WHERE vdms_id = ?1 AND location_id IS NOT NULL AND device_id IS NULL", nativeQuery = true)
    JSONArray getLocationIdsTaggedToClientQrCode(String vdmsid);

    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT device_id FROM client_qr_code WHERE vdms_id = ?1 AND device_id IS NOT NULL AND location_id IS NULL", nativeQuery = true)
    JSONArray getDeviceIdsTaggedToClientQrCode(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_qr_code SET is_deleted = true WHERE client_qr_code_id IN (?1)", nativeQuery = true)
    void updateIsDeletedForAllClientQrCodes(Set<String> clientQrCodeIds);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM client_qr_code WHERE is_deleted = true", nativeQuery = true)
    void deleteOldClientQrCodes();

    @Query(value = "SELECT MAX(updated_at) FROM client_qr_code WHERE device_id = ?1", nativeQuery = true)
    BigInteger maxUpdatedClientQrCodeTimeStamp(String id);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE device_id = ?1", nativeQuery = true)
    long countByDeviceId(String deviceId);

    /** ADC-tagging checks (brief contract; not present in edge source — synthesized). */
    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE client_qr_code_id = ?1", nativeQuery = true)
    int checkClientQrCodeId(String clientQrCodeId);

    @Query(value = "SELECT COALESCE((SELECT adc_client_qr_code_check FROM client_qr_code WHERE client_qr_code_id = ?1), 0)", nativeQuery = true)
    int getAdcCheckByClientQrCodeId(String clientQrCodeId);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code cqc JOIN device d ON d.id = cqc.device_id WHERE cqc.client_qr_code_id = ?1", nativeQuery = true)
    int getIsManagedAssetsTagged(String clientQrCodeId);

    // -------------------------------------------------------------------------
    // Detail lookups (bound to @NamedNativeQuery on ClientQrCode entity)
    // -------------------------------------------------------------------------

    /** Bound to {@code ClientQrCode.getUnTaggedClientQrCode} @NamedNativeQuery (params: limit, offset). */
    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getUnTaggedClientQrCode(int limit, int offset);

    /** Bound to {@code ClientQrCode.getClientQrCodeDetailsByVdmsIdAndDeviceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    /** Bound to {@code ClientQrCode.getClientQrCodeDetailsByVdmsIdAndLocationId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndLocationId(String vdmsId, String locationId);

    /** Returns the row id of an existing client QR code by its business clientQrCodeId, or null if none. */
    @Query(value = "SELECT id FROM client_qr_code WHERE client_qr_code_id = ?1 LIMIT 1", nativeQuery = true)
    String findIdByClientQrCodeId(String clientQrCodeId);
}
