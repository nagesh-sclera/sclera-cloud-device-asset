package io.sclera.Repository;

import io.sclera.dto.ClientNfcDTO;
import io.sclera.models.ClientNfc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Manages persistence and querying of {@link ClientNfc} entities (client NFC tags).
 * Mirrors {@code ClientQrCodeRepository}; native SQL uses PostgreSQL.
 */
@Repository
public interface ClientNfcRepository extends JpaRepository<ClientNfc, String> {

    /** Bound to {@code ClientNfc.getClientNfcDetailsByVdmsIdAndDeviceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    /** Returns the number of client NFC rows for the given nfcId (existence check). */
    @Query(value = "SELECT COUNT(*) FROM client_nfc WHERE nfc_id = ?1", nativeQuery = true)
    int checkClientNfcId(String nfcId);

    /** Tags/untags an existing client NFC record by its nfcId. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE client_nfc SET device_id = ?1, location_id = ?2, vdms_id = ?3, batch_id = ?4 WHERE nfc_id = ?5", nativeQuery = true)
    void tagClientNfc(String deviceId, String locationId, String vdmsId, String batchId, String nfcId);

    /** Inserts a new client NFC record. */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_nfc (id, nfc_id, creation_time, created_by, device_id, location_id, vdms_id, batch_id, is_deleted) "
            + "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, false) ON CONFLICT (id) DO NOTHING", nativeQuery = true)
    void addClientNFC(String id, String nfcId, java.math.BigInteger addedAt, String addedBy, String deviceId, String locationId, String vdmsId, String batchId);

    @Query(value = "SELECT COUNT(*) FROM client_nfc WHERE vdms_id IS NULL AND nfc_id = ?1", nativeQuery = true)
    int getAdcCheckByClientNfcId(String nfcId);
}
