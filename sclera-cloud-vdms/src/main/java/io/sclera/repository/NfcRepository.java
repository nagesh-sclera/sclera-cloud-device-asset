package io.sclera.repository;

import io.sclera.dto.NfcDTO;
import io.sclera.model.NFC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface NfcRepository extends JpaRepository<NFC, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO nfc (id,location_id,device_id,uid,vdms_id,creation_time,created_by,nfc_sync_status) VALUE (?1,?2,?3,?4,?5,?6,?7,?8)", nativeQuery = true)
    void addNFC(String id, String locationId, String deviceId, String nfcUid, String vdmsId, Long creationTime, String createdBy, Integer nfcSyncStatus);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByDeviceIdAndVdmsId(String deviceId, String vdmsId);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByLocationIdAndVdmsId(String locationId, String vdmsId);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByVdmsIdTaggedByDevice(String vdmsId);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByDeviceIds(String vdmsId,List<String> deviceIds);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByVdmsIdTaggedByLocation(String vdmsId);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcDetailsByLocationIds(String vdmsId, List<String> deviceIds);

    @Query(value = "SELECT DISTINCT device_id FROM nfc WHERE vdms_id = ?1 AND location_id IS NULL AND device_id IS NOT NULL",nativeQuery = true)
    List<String> getNfcIdsByVdmsIdAndDevice(String vdmsId);

    @Query(value = "SELECT DISTINCT location_id FROM nfc WHERE vdms_id = ?1 AND device_id IS NULL AND location_id IS NOT NULL",nativeQuery = true)
    List<String> getNfcIdsByVdmsIdAndlocation(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM nfc WHERE device_id IN ?1 AND vdms_id = ?2",nativeQuery = true)
    void deleteNfcInfoByDeviceIdsAndVdmsId(List<String> taggedIds, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM nfc WHERE location_id IN ?1 AND vdms_id = ?2",nativeQuery = true)
    void deleteNfcInfoByLocationIdsAndVdmsId(List<String> taggedIds, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM nfc WHERE  vdms_id = ?1",nativeQuery = true)
    void deleteNfcDataByVdmsId(String vdmsId);

    @Query(value = "SELECT COUNT(*) FROM nfc WHERE vdms_id=?1", nativeQuery = true)
    Integer getNfcCountByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcRecordsByVdmsId(String vdmsId, int pageSize, int offset);

    @Query(value = "SELECT COUNT(id) FROM nfc WHERE id = ?1", nativeQuery = true)
    int checkNfcId(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE nfc SET device_id = ?1 ,location_id = ?2 WHERE id = ?3", nativeQuery = true)
    void updateNfcDetailsById(String deviceId, String locationId, String id);

    @Query(value = "SELECT COUNT(*) FROM nfc WHERE vdms_id = ?1", nativeQuery = true)
    int getNfcCountsByVdmsId(String vdmsId);


    @Query(value = "SELECT id FROM nfc WHERE vdms_id IS NULL AND location_id IS NULL AND device_id IS NULL LIMIT ?1 OFFSET ?2",nativeQuery = true)
    List<String> getUnTaggedNfc( int pageSize, int offset);

    @Query(nativeQuery = true)
    NfcDTO getNfcDetailsById(String id);

    @Query(nativeQuery = true)
    List<NfcDTO> getNfcSyncRecordsByVdmsId(String vdmsId,int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "UPDATE nfc SET nfc_sync_status = ?1,creation_time = ?2 WHERE vdms_id = ?3", nativeQuery = true)
    void updateNfcSyncByVdmsId(int i, BigInteger createdTime, String vdmsId);
}
