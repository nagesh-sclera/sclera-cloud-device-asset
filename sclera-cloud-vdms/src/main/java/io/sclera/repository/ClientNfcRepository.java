package io.sclera.repository;

import io.sclera.dto.ClientNfcDTO;
import io.sclera.model.ClientNfc;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface ClientNfcRepository extends JpaRepository<ClientNfc, String>  {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_nfc (id ,nfc_id,creation_time,created_by,device_id,location_id,vdms_id,batch_id,client_nfc_sync) VALUE (?1,?2,?3,?4,?5,?6,?7,?8,?9)", nativeQuery = true)
    void addClientNFC(String id, String nfc_id, BigInteger addedAt, String addedBy, String deviceId, String locationId, String vdmsId, String batchId, int clientNfcSync);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByDeviceIdAndVdmsId(String deviceId, String vdmsId);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByLocationIdAndVdmsId(String locationId, String vdmsId);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByVdmsIdTaggedByDevice(String vdmsId);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByDeviceIds(String vdmsId,List<String> deviceIds);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByVdmsIdTaggedByLocation(String vdmsId);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByLocationIds(String vdmsId, List<String> deviceIds);

    @Query(value = "SELECT DISTINCT device_id FROM client_nfc WHERE vdms_id = ?1 AND location_id IS NULL AND device_id IS NOT NULL",nativeQuery = true)
    List<String> getClientNfcIdsByVdmsIdAndDevice(String vdmsId);

    @Query(value = "SELECT DISTINCT location_id FROM client_nfc WHERE vdms_id = ?1 AND device_id IS NULL AND location_id IS NOT NULL",nativeQuery = true)
    List<String> getClientNfcIdsByVdmsIdAndlocation(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM client_nfc WHERE device_id IN ?1 AND vdms_id = ?2",nativeQuery = true)
    void deleteClientNfcInfoByDeviceIdsAndVdmsId(List<String> taggedIds, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM client_nfc WHERE location_id IN ?1 AND vdms_id = ?2",nativeQuery = true)
    void deleteClientNfcInfoByLocationIdsAndVdmsId(List<String> taggedIds, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM client_nfc WHERE  vdms_id = ?1",nativeQuery = true)
    void deleteClientNfcDataByVdmsId(String vdmsId);



    @Query(value = "SELECT COUNT(nfc_id) FROM client_nfc WHERE nfc_id = ?1", nativeQuery = true)
    int checkNfcId(String clientNfcId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_nfc SET device_id=?1,location_id=?2,vdms_id=?3,batch_id=?4,client_nfc_sync = ?5 WHERE nfc_id=?6", nativeQuery = true)
    void tagClientNfc(String deviceId, String locationId, String vdmsId, String batchId, int clientNfcSync, String clientNfcId);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByVdmsIdAndDeviceIds(String vdmsId, List<String> deviceId);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcDetailsByVdmsIdAndLocationIds(String vdmsId, List<String> locationId);

    @Query(nativeQuery = true)
    ClientNfcDTO getClientNfcDetailsByNfcId(String nfc_id);

    @Query(nativeQuery = true)
    List<ClientNfcDTO> getClientNfcRecordsByVdmsId(String vdmsId, int pageSize, int offset);

    @Query(value = "SELECT COUNT(*) FROM client_nfc WHERE vdms_id= ?1", nativeQuery = true)
    int getClientNfcCounts(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_nfc SET device_id = ?1,location_id = ?2,nfc_id = ?3,client_nfc_sync = ?4 WHERE id = ?5", nativeQuery = true)
    void updateClientNfcDetailsById(String deviceId, String locationId, String nfcId,int nfcSync, String id);

    @Query(value = "SELECT COUNT(*) FROM client_nfc WHERE vdms_id = ?1 AND id = ?2", nativeQuery = true)
    int checkClientNfcByVdmsIdAndId(String vdmsId, String id);

    @Query(value = "SELECT id FROM client_nfc WHERE  AND location_id IS NULL AND device_id IS NULL LIMIT ?1 OFFSET ?2",nativeQuery = true)
    List<String> getUnTaggedClientNfc( int pageSize, int offset);



    @Query(nativeQuery = true)
    List<ClientNfcDTO> getSyncClientNfcRecordsByVdmsId(String vdmsId, int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_nfc SET client_nfc_sync = ?1,creation_time = ?2 WHERE vdms_id = ?3", nativeQuery = true)
    void updateClientNfcSyncByVdmsId(int i, BigInteger creationTime, String vdmsId);

    @Query(value = "SELECT COUNT(*) FROM client_nfc WHERE vdms_id IS NULL AND nfc_id = ?1", nativeQuery = true)
    int getAdcCheckByClientNfcId(String nfcId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_nfc SET device_id = ?1,location_id = ?2,vdms_id = ?3,updated_at = ?4,updated_by = ?5,batch_id = ?6,client_nfc_sync = ?7, customer_org_id = ?8 WHERE nfc_id = ?9", nativeQuery = true)
    void tagAdcClientNfc(String deviceId, String locationId, String vdmsId, BigInteger updatedAt, String updatedBy, String batchId, int clientNfcSync, String orgId, String clientNfcId);

    @Query(value = "SELECT COUNT(*) FROM client_nfc WHERE nfc_id=?1", nativeQuery = true)
    int getClientNfcId(String clientNfcId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_nfc (id, nfc_id, creation_time, created_by, device_id, adc_client_nfc_check) VALUE (?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addAdcClientNfc(String id, String nfcId, BigInteger addedAt, String createdBy,String deviceId, int adcClientNfcCheck);

    @Query(value = "SELECT adc_client_nfc_check FROM client_nfc WHERE nfc_id = ?1", nativeQuery = true)
    int getIsAdcTagged(String clientNfcId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_nfc SET device_id = ?1, updated_by = ?2, updated_at = ?3 WHERE nfc_id = ?4", nativeQuery = true)
    void updateClientNfcById(String deviceId, String updatedBy, BigInteger updatedAt, String clientNfcId);
}
