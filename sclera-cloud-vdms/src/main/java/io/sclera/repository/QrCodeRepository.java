package io.sclera.repository;

import io.sclera.dto.QrCodeDTO;
import io.sclera.model.QrCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode,String> {
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO qr_code (id ,image_url,creation_time,created_by) VALUE (?1,?2,?3,?4)", nativeQuery = true)
    void upsertGlobalQrcode(String qrcode_id ,String image_url, Long  creationTime,String email);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO qr_code (id, image_url, qr_code_link, creation_time, created_by, batch_id) " +
            "VALUES (?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addQrCode(String qrCodeId, String qrCodeImageUrl, String data, BigInteger creationTime, String email, String batchId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id=?1,location_id=?2,vdms_id=?3,updated_time=?4,updated_by=?5,qr_code_sync = ?6 WHERE id=?7", nativeQuery = true)
    void upsertQrcode(String devuid, String location, String vdmsid, Long updated_time, String updatedBy, Integer qrCodeSync, String qrCodeId);

    @Query(nativeQuery = true)
    QrCodeDTO getQrCodeDetailsByQrCodeId(String qrCodeId);

    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);


    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeDetailsByDeviceIds(String vdmsId, List<String> taggedIds);
    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeDetailsByLocationIds(String vdmsId, List<String> taggedIds);

    @Query(nativeQuery = true)
    List<QrCodeDTO> getTaggedDevicesByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<QrCodeDTO> getTaggedLocationsByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeDetailsByVdmsIdAndLocationId(String vdmsId,String locationId);

    @Query(value = "SELECT DISTINCT device_id FROM qr_code WHERE vdms_id = ?1 AND location_id IS NULL",nativeQuery = true)
    List<String> getQrCodeIdByVdmsIdAndDevice(String vdmsId);

    @Query(value = "SELECT DISTINCT location_id FROM qr_code WHERE vdms_id = ?1 AND device_id IS NULL",nativeQuery = true)
    List<String> getQrCodeIdByVdmsIdAndLocation(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = NULL WHERE device_id IN ?1 AND vdms_id=?2",nativeQuery = true)
    void updateQrCodeInfoBydeviceIdsAndVdmsId(List<String> taggedIds, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET location_id = NULL WHERE location_id IN ?1 AND vdms_id=?2",nativeQuery = true)
    void updateQrCodeInfoByLocationIdsAndVdmsId(List<String> taggedIds, String vdmsId);

    @Query(nativeQuery = true)
    QrCodeDTO getVdmsInfoByQrCodeId(String qrcodeId);

    @Query(value = "SELECT COUNT(id) FROM qr_code WHERE id = ?1", nativeQuery = true)
    int checkQrCodeId(String id);

    @Query(value = "SELECT SUBSTRING_INDEX(image_url, '/', -3) as imageUrl FROM qr_code WHERE batch_id = ?1", nativeQuery = true)
    List<String> getQrCodeImageByBatchId(String batchId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM qr_code WHERE batch_id IN ?1", nativeQuery = true)
    void deleteQrCodesByBatchIds(List<String> batchIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM qr_code WHERE vdms_id = ?1", nativeQuery = true)
    void deleteQrCodeByVdmsId(String vdmsId);

    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE vdms_id=?1", nativeQuery = true)
    Integer getQrcodeCountByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<QrCodeDTO> getAllQrCodeByVdmsId(String vdmsId, int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1 ,location_id = ?2,updated_by = ?3,updated_time = ?4,qr_code_sync = ?5 WHERE id = ?6", nativeQuery = true)
    void updateQrCodeDetailsById(String deviceId, String locationId, String updatedBy, String updatedTime,int qrCodeSync, String id);

    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE vdms_id = ?1 AND (updated_time >= ?2 OR creation_time >= ?2)", nativeQuery = true)
    int getQrCodeCountsByVdsId(String vdmsId, BigInteger updatedTime);

    @Query(nativeQuery = true)
    List<QrCodeDTO> getQrCodeRecordsByVdmsIdAndLastSyncTime(String vdmsId, BigInteger updatedTime, int pageSize, int offset);


    @Query(value = "SELECT id FROM qr_code WHERE vdms_id IS NULL AND location_id IS NULL AND device_id IS NULL LIMIT ?1 OFFSET ?2",nativeQuery = true)
    List<String> getUnTaggedQrCode(  int pageSize, int offset);

    // qr code sync changes
    @Query(nativeQuery = true)
    List<QrCodeDTO> getAllSyncQrCodeByVdmsId(String vdmsId, int pageSize, int offset);

    // qr code sync changes
    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET qr_code_sync = ?1,updated_time = ?2 WHERE vdms_id = ?3", nativeQuery = true)
    void updateQrCodeSync(int i,BigInteger updatedTime, String vdmsId, HttpServletRequest httpServletRequest);

    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE vdms_id IS NULL AND id = ?1", nativeQuery = true)
    int getAdcCheckByQrCodeId(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1 ,location_id = ?2, vdms_id = ?3,updated_by = ?4,updated_time = ?5,batch_id = ?6,qr_code_sync = ?7,customer_org_id = ?8 WHERE id = ?9", nativeQuery = true)
    void tagAdcQrCode(String deviceId, String locationId, String vdmsId, String updatedBy, BigInteger updatedTime, String batchId, int qrCodeSync, String orgId, String id);

    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE id = ?1", nativeQuery = true)
    int getQrCodeId(String qrCodeId);

    @Query(value = "SELECT adc_qr_code_check FROM qr_code WHERE id = ?1", nativeQuery = true)
    int getIsAdcTagged(String qrCodeId);

    @Query(value = "SELECT COUNT(*) FROM qr_code WHERE id = ?1 AND (device_id IS NOT NULL OR location_id IS NOT NULL)", nativeQuery = true)
    int getIsManagedAssetsTagged(String qrCodeId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE qr_code SET device_id = ?1, updated_by = ?2, updated_time = ?3, adc_qr_code_check = ?4 WHERE id = ?5", nativeQuery = true)
    void updateQrCodeById(String deviceId, String updatedBy, BigInteger updatedAt, int adcQrCodeCheck, String qrCodeId);
}


