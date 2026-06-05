package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.models.ClientBarCode;
import io.sclera.models.ClientNfc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link ClientBarCode} entities.
 */
@Repository
public interface ClientBarCodeRepository extends JpaRepository<ClientBarCode, String> {



    /**
     * Marks every client bar code record as deleted.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE client_bar_code SET is_deleted = true", nativeQuery = true)
    void updateIsDeletedForAllClientBarCode();

    /**
     * Permanently removes all client bar code records flagged as deleted.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM client_bar_code WHERE is_deleted = true", nativeQuery = true)
    void deleteOldClientBarCode();

    /**
     * Counts the client bar codes tagged to the given device.
     *
     * @param deviceId the device identifier
     * @return the number of client bar codes associated with the device
     */
    @Query(value = "SELECT COUNT(cbc.device_id) FROM client_bar_code cbc LEFT JOIN device d ON d.id = cbc.device_id WHERE d.id = ?1", nativeQuery = true)
    Integer getClientBarCodeCountByDeviceId(String deviceId);

    /**
     * Returns the location ids of client bar codes tagged to a location but not to a device.
     *
     * @param vdmsid the VDMS identifier
     * @return the location ids tagged to client bar codes
     */
    @Query(value = "SELECT location_id FROM client_bar_code WHERE location_id IS NOT NULL AND device_id IS NULL", nativeQuery = true)
    JSONArray getLocationIdsTaggedToClientBarCode(String vdmsid);

    /**
     * Returns the device ids of client bar codes tagged to a device but not to a location.
     *
     * @param vdmsid the VDMS identifier
     * @return the device ids tagged to client bar codes
     */
    @Query(value = "SELECT device_id FROM client_bar_code WHERE device_id IS NOT NULL AND location_id IS NULL", nativeQuery = true)
    JSONArray getDeviceIdsTaggedToClientBarCode(String vdmsid);

    /**
     * Retrieves client bar codes tagged to the given locations.
     *
     * @param locationIds the location identifiers to match
     * @return the set of matching client bar codes
     */
    @Query(nativeQuery = true)
    Set<ClientBarCodeDTO> getBarCodesByLocationIds(Set<String> locationIds);

    /**
     * Retrieves client bar codes tagged to the given devices.
     *
     * @param deviceIds the device identifiers to match
     * @return the set of matching client bar codes
     */
    @Query(nativeQuery = true)
    Set<ClientBarCodeDTO> getBarCodesByDeviceIds(Set<String> deviceIds);
}
