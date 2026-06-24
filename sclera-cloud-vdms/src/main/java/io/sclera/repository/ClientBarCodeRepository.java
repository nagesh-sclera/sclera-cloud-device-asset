package io.sclera.repository;

import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.model.ClientBarCode;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ClientBarCodeRepository extends JpaRepository<ClientBarCode, String> {

    @Query(value = "SELECT COUNT(*) FROM client_bar_code WHERE vdms_id= ?1", nativeQuery = true)
    Integer getClientBarCodeCountByVdmsId(String vdmsId);


    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getAllClientBarCodeByVdmsId(String vdmsId, @Min(1) @Max(1000) int pageSize, int offset);


    @Query(nativeQuery = true)
    ClientBarCodeDTO getClientBarCodeDetailsByClientBarCodeId(String clientBarCodeId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE client_bar_code SET device_id=?1,location_id=?2,vdms_id=?3,updated_at=?4,updated_by=?5,batch_id=?6,bar_code_sync=?7 WHERE client_bar_code_id=?8", nativeQuery = true)
    void tagClientBarCode(String deviceId, String locationId, String vdmsId, BigInteger updatedAt, String loggedInUser, String batchId, int barCodeSync, String clientBarCodeId);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_bar_code (id ,client_bar_code_id,added_at,added_by,device_id,location_id,vdms_id,batch_id,bar_code_sync) VALUE (?1,?2,?3,?4,?5,?6,?7,?8,?9)", nativeQuery = true)
    void addClientBarCode(String id, String clientBarCodeId, BigInteger addedAt, String email, String deviceId, String locationId, String vdmsId, String batchId, int barCodeSync);


    @Query(nativeQuery = true)
    ClientBarCodeDTO getBarCodeDataByClientBarCodeId(String clientBarCodeId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM client_bar_code WHERE vdms_id = ?1", nativeQuery = true)
    void deleteClientBarCodeByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getClientBarCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getClientBarCodeDetailsByVdmsIdAndLocationId(String vdmsId, String locationId);

    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getBarCodeRecordsByVdmsIdAndLastSyncTime(String vdmsId, BigInteger lastSyncTime, int pageSize, int offset);

    @Query(value = "SELECT COUNT(*) FROM client_bar_code WHERE vdms_id = ?1 AND (updated_at >= ?2 OR added_at >= ?2)", nativeQuery = true)
    int getBarCodeCountsByVdsId(String vdmsId, BigInteger lastSyncTime);

    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_bar_code SET bar_code_sync = ?1 WHERE vdms_id = ?2", nativeQuery = true)
    void updateBarCodeSyncByVdmsId(int i, String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_bar_code SET device_id = ?1,location_id = ?2,vdms_id = ?3,updated_at = ?4,updated_by = ?5,batch_id = ?6,bar_code_sync = ?7,customer_org_id = ?8 WHERE client_bar_code_id = ?9", nativeQuery = true)
    void tagAdcClientBarCode(String deviceId, String locationId, String vdmsId, BigInteger updatedAt, String loggedInUser, String batchId, int barCodeSync, String orgId, String clientBarCodeId);

    @Query(value = "SELECT adc_bar_code_check FROM client_bar_code WHERE client_bar_code_id=?1", nativeQuery = true)
    int getIsAdcTagged(String clientBarCodeId);

    @Query(value = "SELECT count(*) FROM client_bar_code WHERE client_bar_code_id=?1", nativeQuery = true)
    int getClientBarCodeId(String clientBarCodeId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO client_bar_code (id ,client_bar_code_id,added_at,added_by,device_id,adc_bar_code_check) VALUE (?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addAdcClientBarCode(String id, String clientBarCodeId, BigInteger addedAt, String addedBy,String deviceId, int adcBarCodeCheck);

    @Query(value = "SELECT COUNT(*) FROM client_bar_code WHERE vdms_id IS NULL AND client_bar_code_id = ?1", nativeQuery = true)
    int getAdcCheckByClientBarCodeId(String clientBarCodeId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE client_bar_code SET device_id = ?1, updated_by = ?2, updated_at = ?3 WHERE client_bar_code_id = ?4", nativeQuery = true)
    void updateClientBarCodeById(String deviceId, String updatedBy, BigInteger updatedAt, String clientBarCodeId);
}
