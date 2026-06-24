package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.QrCodeDTO;
import io.sclera.models.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link QrCode} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, String> {

    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT location_id FROM qr_code WHERE vdms_id = ?1 AND location_id IS NOT NULL AND device_id IS NULL", nativeQuery = true)
    JSONArray getLocationIdsTaggedToQrCode(String vdmsId);

    @Query(value = "SELECT COUNT(qc.device_id) FROM qr_code qc LEFT JOIN device d ON d.id = qc.device_id WHERE d.id = ?1", nativeQuery = true)
    Integer getQrCodeCountByDeviceId(String deviceId);

    /** Bound to {@code QrCode.getQrCodesByDeviceIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceId);

    /** Bound to {@code QrCode.getQrCodesByLocationIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds);

    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT device_id FROM qr_code WHERE vdms_id = ?1 AND device_id IS NOT NULL AND location_id IS NULL", nativeQuery = true)
    JSONArray getDeviceIdsTaggedToQrCode(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET is_deleted = true", nativeQuery = true)
    void updateIsDeletedForAllQrCodes();

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM qr_code WHERE is_deleted = true", nativeQuery = true)
    void deleteOldQrCodes();

    @Query(value = "SELECT MAX(updated_time) FROM qr_code WHERE device_id = ?1", nativeQuery = true)
    BigInteger getMaxUpdatedQrCodeTimeStamp(String id);

    /** Bound to {@code QrCode.getQrCodeDetailsByIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<QrCodeDTO> getQrCodeDetailsByIds(Set<String> qrcodeIds);

    /** Bound to {@code QrCode.getClientQrCodeDetailsByIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<QrCodeDTO> getClientQrCodeDetailsByIds(Set<String> clientQrcodeIds);

    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE device_id = ?1", nativeQuery = true)
    long countByDeviceId(String deviceId);

    /** ADC-tagging checks (brief contract; not present in edge source — synthesized). */
    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE id = ?1", nativeQuery = true)
    int checkQrCodeId(String qrCodeId);

    @Query(value = "SELECT COALESCE((SELECT adc_qr_code_check FROM qr_code WHERE id = ?1), 0)", nativeQuery = true)
    int getAdcCheckByQrCodeId(String qrCodeId);

    @Query(value = "SELECT COUNT(*) FROM qr_code qc JOIN device d ON d.id = qc.device_id WHERE qc.id = ?1", nativeQuery = true)
    int getIsManagedAssetsTagged(String qrCodeId);

    // -------------------------------------------------------------------------
    // Methods added for Phase 4 generation task
    // -------------------------------------------------------------------------

    /** Inserts a new QR code row generated during bulk or single export. */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO qr_code (id, image_url, qr_code_link, creation_time, created_by, batch_id, is_deleted) "
            + "VALUES (?1, ?2, ?3, ?4, ?5, ?6, false) ON CONFLICT (id) DO NOTHING", nativeQuery = true)
    void addQrCode(String id, String imageUrl, String qrCodeLink, BigInteger creationTime, String createdBy, String batchId);

    /** Bound to {@code QrCode.getQrCodeDetailsByQrCodeId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    QrCodeDTO getQrCodeDetailsByQrCodeId(String qrCodeId);

    /** Bound to {@code QrCode.getQrCodeDetailsByVdmsIdAndDeviceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    /** Bound to {@code QrCode.getQrCodeDetailsByVdmsIdAndLocationId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeDetailsByVdmsIdAndLocationId(String vdmsId, String locationId);

    /** Returns a page of QR code ids that have no device or location assigned. */
    @Query(value = "SELECT id FROM qr_code WHERE device_id IS NULL AND location_id IS NULL LIMIT ?1 OFFSET ?2", nativeQuery = true)
    List<String> getUnTaggedQrCode(int pageSize, int offset);

    /** Returns count of QR codes for a vdmsId updated after lastSyncTime. */
    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE vdms_id = ?1 AND updated_time > ?2", nativeQuery = true)
    int getQrCodeCountsByVdsId(String vdmsId, BigInteger lastSyncTime);

    /** Sets or updates device/location assignment (tagging) for a QR code, with ADC context. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1, location_id = ?2, vdms_id = ?3, updated_by = ?4, "
            + "updated_time = ?5, batch_id = ?6, adc_qr_code_check = ?7, customer_org_id = ?8 "
            + "WHERE id = ?9", nativeQuery = true)
    void tagAdcQrCode(String deviceId, String locationId, String vdmsId, String updatedBy,
                      BigInteger updatedAt, String batchId, int adcQrCodeCheck, String orgId, String id);

    /** Updates device/location/updatedBy/updatedTime on an existing QR code row. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1, location_id = ?2, updated_by = ?3, "
            + "updated_time = ?4, adc_qr_code_check = ?5 WHERE id = ?6", nativeQuery = true)
    void updateQrCodeDetailsById(String deviceId, String locationId, String updatedBy,
                                 String updatedTime, int adcQrCodeCheck, String id);

    /** Upserts (tag/untag) device/location/vdmsId on a QR code. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1, location_id = ?2, vdms_id = ?3, "
            + "updated_time = ?4, updated_by = ?5, adc_qr_code_check = ?6 WHERE id = ?7", nativeQuery = true)
    void upsertQrcode(String deviceId, String locationId, String vdmsId, Long updatedTime,
                      String loggedInUser, int adcQrCodeCheck, String id);

    /** Updates device assignment and sync flag for a single QR code. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1, updated_by = ?2, updated_time = ?3, "
            + "adc_qr_code_check = ?4 WHERE id = ?5", nativeQuery = true)
    void updateQrCodeById(String deviceId, String updatedBy, BigInteger updatedAt, int adcQrCodeCheck, String id);

    /** Returns 1 if the QR code id exists, 0 otherwise. Alias for checkQrCodeId. */
    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE id = ?1", nativeQuery = true)
    int getQrCodeId(String qrCodeId);

    /** Returns whether the ADC-tagged QR code is assigned to a device or location (1 = tagged, 0 = not). */
    @Query(value = "SELECT CASE WHEN (device_id IS NOT NULL OR location_id IS NOT NULL) THEN 1 ELSE 0 END "
            + "FROM qr_code WHERE id = ?1", nativeQuery = true)
    int getIsAdcTagged(String qrCodeId);
}
