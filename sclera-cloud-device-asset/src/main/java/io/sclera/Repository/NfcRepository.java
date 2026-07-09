package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.NfcDTO;
import io.sclera.models.Nfc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link Nfc} entities (generated NFC tags).
 * Mirrors {@code QrCodeRepository}; native SQL uses PostgreSQL.
 */
@Repository
public interface NfcRepository extends JpaRepository<Nfc, String> {

    /** Bound to {@code Nfc.getNfcDetailsByVdmsIdAndDeviceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    /** Returns a page of NFC ids with no vdms/device/location assigned (well-formed untagged query). */
    @Query(value = "SELECT id FROM nfc WHERE vdms_id IS NULL AND location_id IS NULL AND device_id IS NULL LIMIT ?1 OFFSET ?2", nativeQuery = true)
    List<String> getUnTaggedNfc(int pageSize, int offset);

    /** Bound to {@code Nfc.getNfcDetailsById} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    NfcDTO getNfcDetailsById(String id);

    /** Returns the number of NFC rows with the given id (existence check). */
    @Query(value = "SELECT COUNT(*) FROM nfc WHERE id = ?1", nativeQuery = true)
    int checkNfcId(String id);

    /** Tags/untags a generated NFC by setting its device/location assignment. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE nfc SET device_id = ?1, location_id = ?2 WHERE id = ?3", nativeQuery = true)
    void updateNfcDetailsById(String deviceId, String locationId, String id);

    // -------------------------------------------------------------------------
    // Read/enrichment queries (local; formerly served by sclera-integrations via NfcClient).
    // Mirror QrCodeRepository.
    // -------------------------------------------------------------------------

    /** Count of NFC tags assigned to a device (mirrors QrCodeRepository.getQrCodeCountByDeviceId). */
    @Query(value = "SELECT COUNT(n.device_id) FROM nfc n LEFT JOIN device d ON d.id = n.device_id WHERE d.id = ?1", nativeQuery = true)
    Integer getQrNfcCountByDeviceId(String deviceId);

    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT device_id FROM nfc WHERE vdms_id = ?1 AND device_id IS NOT NULL AND location_id IS NULL", nativeQuery = true)
    JSONArray getDeviceIdsTaggedToNfc(String vdmsId);

    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT location_id FROM nfc WHERE vdms_id = ?1 AND location_id IS NOT NULL AND device_id IS NULL", nativeQuery = true)
    JSONArray getLocationIdsTaggedToNfc(String vdmsId);

    /** Bound to {@code Nfc.getNfcsByDeviceIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<NfcDTO> getNfcsByDeviceIds(Set<String> deviceIds);

    /** Bound to {@code Nfc.getNfcsByLocationIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<NfcDTO> getNfcsByLocationIds(Set<String> locationIds);
}
