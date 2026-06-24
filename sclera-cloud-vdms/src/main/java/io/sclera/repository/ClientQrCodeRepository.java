package io.sclera.repository;

import io.sclera.dto.ClientQrCodeDTO;
import io.sclera.model.ClientQrCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ClientQrCodeRepository extends JpaRepository<ClientQrCode, String> {


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_qr_code (id ,client_qr_code_id,added_at,added_by,device_id,location_id,vdms_id,batch_id,client_qr_code_sync) VALUE (?1,?2,?3,?4,?5,?6,?7,?8,?9)", nativeQuery = true)
    void addClientQrCode(String qrCodeId, String clientQrCodeId, BigInteger addedAt, String addedBy, String deviceId, String locationId, String vdmsId, String batchId, int clientQrCodeSync);

    @Query(value = "SELECT COUNT(client_qr_code_id) FROM client_qr_code WHERE client_qr_code_id = ?1", nativeQuery = true)
    int checkQrCodeId(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_qr_code SET device_id=?1,location_id=?2,vdms_id=?3,updated_at=?4,updated_by=?5,batch_id=?6,client_qr_code_sync = ?7 WHERE client_qr_code_id=?8", nativeQuery = true)
    void tagClientQrCode(String deviceId, String locationId, String vdmsId, BigInteger updated_time, String updatedBy, String batchId, int clientQrCodeSync, String clientQrCodeId);

//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM client_qr_code WHERE client_qr_code_id=?1", nativeQuery = true)
//    void deleteClientQrCodeDetails(String clientQrCodeId);

//    @Query(nativeQuery = true)
//    ClientQrCodeDTO getClientQrCodeByVdmsIdAndQrCodeId(String vdmsId, String clientQrCodeId);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String id);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndLocationId(String vdmsId, String id);

    @Query(nativeQuery = true)
    ClientQrCodeDTO getQrCodeDataByClientQrCodeId(String clientQrCodeId);

    @Query(nativeQuery = true)
    ClientQrCodeDTO getClientQrCodeDetailsByClientQrCodeId(String clientQrCodeId);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndDeviceIds(String vdmsId, List<String> id);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndLocationIds(String vdmsId, List<String> id);

    @Query(value = "SELECT DISTINCT device_id FROM client_qr_code WHERE vdms_id = ?1 AND location_id IS NULL", nativeQuery = true)
    List<String> getClientQrCodeDeviceIdsByVdmsId(String vdmsId);

    @Query(value = "SELECT DISTINCT location_id FROM client_qr_code WHERE vdms_id = ?1 AND device_id IS NULL", nativeQuery = true)
    List<String> getClientQrCodeLocationIdsByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeTaggedDevicesDetailsByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeTaggedLocationsDetailsByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM client_qr_code WHERE vdms_id = ?1", nativeQuery = true)
    void deleteClientQrCodeByVdmsId(String vdmsId);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE vdms_id = ?1", nativeQuery = true)
    Integer getClientQrCodeCountByVdmsID(String vdmsId);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getAllClientQrCodeByVdmsId(String vdmsId, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getClientQrCodeRecordsByVdmsIdAndLastSyncTime(String vdmsId, BigInteger updatedTime, int pageSize, int offset);

    @Transactional
    @Modifying
    @Query(value = "UPDATE client_qr_code SET device_id = ?1,location_id = ?2,updated_at = ?3,updated_by = ?4,client_qr_code_sync=?5 WHERE id = ?6", nativeQuery = true)
    void updateClientQrCodeDetailsById(String deviceId, String locationId, BigInteger updatedTime, String updatedBy, int clientQrCodeSync, String id);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE vdms_id = ?1 AND id = ?2", nativeQuery = true)
    int checkClientQrCodeByVdmsIdAndId(String vdmsId, String id);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE vdms_id =?1 AND  (updated_at >= ?2 OR added_at >= ?2)", nativeQuery = true)
    int getClientQrCodeCounts(String vdmsId, BigInteger updated_at);

    @Query(value = "SELECT id FROM client_qr_code WHERE vdms_id IS NULL AND location_id IS NULL AND device_id IS NULL LIMIT ?1 OFFSET ?2",nativeQuery = true)
    List<String> getUnTaggedClientQrCode( int pageSize, int offset);

    // qr code sync change
    @Query(nativeQuery = true)
    List<ClientQrCodeDTO> getAllSyncClientQrCodeByVdmsId(String vdmsId, int pageSize, int offset);

    // qr code sync change
    @Modifying
    @Transactional
    @Query(value = "UPDATE client_qr_code SET client_qr_code_sync = ?1,updated_at = ?2 WHERE vdms_id = ?3", nativeQuery = true)
    void updateClientQrCodeSyncByVdmsId(int i, BigInteger updatedTime, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_qr_code SET device_id = ?1,location_id = ?2,vdms_id = ?3,updated_at = ?4,updated_by = ?5,batch_id = ?6,client_qr_code_sync = ?7, customer_org_id = ?8 WHERE client_qr_code_id = ?9", nativeQuery = true)
    void tagAdcClientQrCode(String deviceId, String locationId, String vdmsId, BigInteger updatedAt, String updatedBy, String batchId, int clientQrCodeSync, String orgId,String clientQrCodeId);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE vdms_id IS NULL AND client_qr_code_id = ?1", nativeQuery = true)
    int getAdcCheckByClientQrCodeId(String clientQrCodeId);

    @Query(value = "SELECT COUNT(*) FROM client_qr_code WHERE client_qr_code_id=?1", nativeQuery = true)
    int getClientQrCodeId(String clientQrCodeId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_qr_code (id ,client_qr_code_id,added_at,added_by,device_id,adc_client_qr_code_check) VALUE (?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addAdcClientQrCode(String id, String clientQrCodeId, BigInteger addedAt, String addedBy,String deviceId, int adcClientQrCodeCheck);

    @Query(value = "SELECT adc_client_qr_code_check FROM client_qr_code WHERE client_qr_code_id=?1", nativeQuery = true)
    int getIsAdcTagged(String clientQrCodeId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_qr_code SET device_id = ?1, updated_at = ?2, updated_by = ?3 WHERE client_qr_code_id = ?4", nativeQuery = true)
    void updateClientQrCodeById(String deviceId, BigInteger updatedAt, String updatedBy, String clientQrCodeId);
}
