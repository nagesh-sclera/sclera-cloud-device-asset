package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.models.ClientBarCode;
import io.sclera.models.ClientNfc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Set;

@Repository
public interface ClientBarCodeRepository extends JpaRepository<ClientBarCode, String> {



    @Modifying
    @Transactional
    @Query(value = "UPDATE client_bar_code SET is_deleted = true", nativeQuery = true)
    void updateIsDeletedForAllClientBarCode();

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM client_bar_code WHERE is_deleted = true", nativeQuery = true)
    void deleteOldClientBarCode();

    @Query(value = "SELECT COUNT(cbc.device_id) FROM client_bar_code cbc LEFT JOIN device d ON d.id = cbc.device_id WHERE d.id = ?1", nativeQuery = true)
    Integer getClientBarCodeCountByDeviceId(String deviceId);

    @Query(value = "SELECT location_id FROM client_bar_code WHERE location_id IS NOT NULL AND device_id IS NULL", nativeQuery = true)
    JSONArray getLocationIdsTaggedToClientBarCode(String vdmsid);

    @Query(value = "SELECT device_id FROM client_bar_code WHERE device_id IS NOT NULL AND location_id IS NULL", nativeQuery = true)
    JSONArray getDeviceIdsTaggedToClientBarCode(String vdmsid);

    @Query(nativeQuery = true)
    Set<ClientBarCodeDTO> getBarCodesByLocationIds(Set<String> locationIds);

    @Query(nativeQuery = true)
    Set<ClientBarCodeDTO> getBarCodesByDeviceIds(Set<String> deviceIds);
}
