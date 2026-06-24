package io.sclera.Repository;

import io.sclera.dto.GlobalQrcodeDTO;
import io.sclera.models.GlobalQrcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link GlobalQrcode} entities.
 * Ported from sclera-vdms-edge-server; native SQL converted to PostgreSQL.
 */
@Repository
public interface GlobalQrcodeRepository extends JpaRepository<GlobalQrcode, String> {

    // POSTGRES: MySQL "VALUE (...)" -> "VALUES (...)"
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO global_qrcode (id , image_url , location_id , device_id) VALUES (?1, ?2, ?3, ?4)", nativeQuery = true)
    void addGlobalQrcode(String global_qrcode_id, String image_url, String location_id, String device_id);

    /** Bound to {@code GlobalQrcode.getGlobalQrCodeLocation} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<GlobalQrcodeDTO> getGlobalQrCodeLocation(String seachkey, Integer pageno, Integer offset, String building_id, String floor_id);

    /** Bound to {@code GlobalQrcode.getGlobalQrCodeDevice} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<GlobalQrcodeDTO> getGlobalQrCodeDevice(String seachkey, Integer pageno, Integer offset, List<String> device_types);

    @Transactional
    @Query(value = "SELECT image_url FROM global_qrcode WHERE id = ?1", nativeQuery = true)
    String getImageurlByID(String globalQrCodeId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM global_qrcode WHERE id = ?1", nativeQuery = true)
    void deleteGlobalQrcodeById(String globalQrCodeId);

    @Transactional
    @Query(value = "SELECT id FROM global_qrcode WHERE device_id IS NULL AND location_id = ?1", nativeQuery = true)
    String getGlobalQrcodeIdByLocation(String location_id);

    @Transactional
    @Query(value = "SELECT id FROM global_qrcode WHERE location_id IS NULL AND device_id = ?1", nativeQuery = true)
    String getGlobalQrcodeIdByDevice(String device_id);

    @Transactional
    @Query(value = "SELECT id FROM global_qrcode WHERE location_id = ?1", nativeQuery = true)
    String getGlobalQrcodeIdByLocationId(String location_id);

    /** Bound to {@code GlobalQrcode.getGlobalQrCodesByIds} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<GlobalQrcodeDTO> getGlobalQrCodesByIds(List<String> qrcodes);

    // POSTGRES: MySQL "VALUE (...) ON DUPLICATE KEY UPDATE col = IFNULL(?2, col), ..."
    //        -> "VALUES (...) ON CONFLICT (id) DO UPDATE SET col = COALESCE(EXCLUDED.col, global_qrcode.col), ..."
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO global_qrcode (id , image_url , location_id , device_id) VALUES (?1, ?2, ?3, ?4) " +
            "ON CONFLICT (id) DO UPDATE SET image_url = COALESCE(EXCLUDED.image_url, global_qrcode.image_url), " +
            "location_id = EXCLUDED.location_id, device_id = EXCLUDED.device_id", nativeQuery = true)
    void upsertGlobalQrcode(String global_qrcode_id, String image_url, String location_id, String device_id);

    /** Bound to {@code GlobalQrcode.getQrcodeDetail} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<GlobalQrcodeDTO> getQrcodeDetail(String qrcodeid, String location_id, String device_id);

    /** Bound to {@code GlobalQrcode.getGlobalQrcodes} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<GlobalQrcodeDTO> getGlobalQrcodes(String seachkey, Integer pagesize, Integer offset);

    @Query(value = "SELECT COUNT(*) FROM global_qrcode WHERE device_id = ?1", nativeQuery = true)
    Integer getDeviceQrcodeCountByDeviceId(String device_id);

    /** Bound to {@code GlobalQrcode.getGlobalQrcodeById} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    GlobalQrcodeDTO getGlobalQrcodeById(String globalQrCodeId);

    /** Bound to {@code GlobalQrcode.getUntaggedGlobalQrcodes} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    Set<GlobalQrcodeDTO> getUntaggedGlobalQrcodes(String seachkey, Integer pagesize, Integer offset);

    /** Bound to {@code GlobalQrcode.getGlobalQrcodeDetails} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<GlobalQrcodeDTO> getGlobalQrcodeDetails();

    /** Bound to {@code GlobalQrcode.getUntaggedGlobalQrcodeDetails} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<GlobalQrcodeDTO> getUntaggedGlobalQrcodeDetails();

    /** Bound to {@code GlobalQrcode.getGlobalQrcodeLocationDetails} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<GlobalQrcodeDTO> getGlobalQrcodeLocationDetails(List<String> building_ids, List<String> floor_ids);

    /** Bound to {@code GlobalQrcode.getGlobalQrcodeDeviceDetails} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<GlobalQrcodeDTO> getGlobalQrcodeDeviceDetails(List<String> dockernames, List<String> device_types);
}
