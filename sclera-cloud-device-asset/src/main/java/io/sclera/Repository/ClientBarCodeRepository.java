package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.models.ClientBarCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link ClientBarCode} entities.
 */
@Repository
public interface ClientBarCodeRepository extends JpaRepository<ClientBarCode, String> {



    /**
     * Marks every client bar code record as deleted.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE ClientBarCode cbc SET cbc.isDeleted = true")
    void updateIsDeletedForAllClientBarCode();

    /**
     * Permanently removes all client bar code records flagged as deleted.
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ClientBarCode cbc WHERE cbc.isDeleted = true")
    void deleteOldClientBarCode();

    /**
     * Counts the client bar codes tagged to the given device.
     *
     * @param deviceId the device identifier
     * @return the number of client bar codes associated with the device
     */
    @Query("SELECT COUNT(cbc) FROM ClientBarCode cbc WHERE cbc.device.id = ?1")
    Integer getClientBarCodeCountByDeviceId(String deviceId);

    /**
     * Returns the location ids of client bar codes tagged to a location but not to a device.
     *
     * @param vdmsid the VDMS identifier
     * @return the location ids tagged to client bar codes
     */
    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT location_id FROM client_bar_code WHERE location_id IS NOT NULL AND device_id IS NULL", nativeQuery = true)
    JSONArray getLocationIdsTaggedToClientBarCode(String vdmsid);

    /**
     * Returns the device ids of client bar codes tagged to a device but not to a location.
     *
     * @param vdmsid the VDMS identifier
     * @return the device ids tagged to client bar codes
     */
    // NOT CONVERTED — stays native: JSONArray return type has no portable JPQL form
    @Query(value = "SELECT device_id FROM client_bar_code WHERE device_id IS NOT NULL AND location_id IS NULL", nativeQuery = true)
    JSONArray getDeviceIdsTaggedToClientBarCode(String vdmsid);

    /**
     * Retrieves client bar codes tagged to the given locations.
     *
     * @param locationIds the location identifiers to match
     * @return the set of matching client bar codes
     */
    @Query("SELECT new io.sclera.dto.ClientBarCodeDTO(cbc.id, cbc.device.id, cbc.location.id) " +
           "FROM ClientBarCode cbc WHERE cbc.location.id IN ?1")
    Set<ClientBarCodeDTO> getBarCodesByLocationIds(Set<String> locationIds);

    /**
     * Retrieves client bar codes tagged to the given devices.
     *
     * @param deviceIds the device identifiers to match
     * @return the set of matching client bar codes
     */
    @Query("SELECT new io.sclera.dto.ClientBarCodeDTO(cbc.id, cbc.device.id, cbc.location.id) " +
           "FROM ClientBarCode cbc WHERE cbc.device.id IN ?1")
    Set<ClientBarCodeDTO> getBarCodesByDeviceIds(Set<String> deviceIds);

    // -------------------------------------------------------------------------
    // Detail lookups (bound to @NamedNativeQuery on ClientBarCode entity)
    // -------------------------------------------------------------------------

    /** Bound to {@code ClientBarCode.getUnTaggedClientBarCode} @NamedNativeQuery (params: limit, offset). */
    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getUnTaggedClientBarCode(int limit, int offset);

    /** Bound to {@code ClientBarCode.getClientBarCodeDetailsByVdmsIdAndDeviceId} @NamedNativeQuery. */
    @Query(nativeQuery = true)
    List<ClientBarCodeDTO> getClientBarCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    /** ADC-tagging checks (mirrors ClientQrCodeRepository). */
    @Query(value = "SELECT COUNT(*) FROM client_bar_code WHERE client_bar_code_id = ?1", nativeQuery = true)
    int checkClientBarCodeId(String clientBarCodeId);

    /** Returns the row id of an existing client bar code by its business clientBarCodeId, or null if none. */
    @Query(value = "SELECT id FROM client_bar_code WHERE client_bar_code_id = ?1 LIMIT 1", nativeQuery = true)
    String findIdByClientBarCodeId(String clientBarCodeId);

    @Query(value = "SELECT COUNT(*) FROM client_bar_code WHERE vdms_id IS NULL AND client_bar_code_id = ?1", nativeQuery = true)
    int getAdcCheckByClientBarCodeId(String clientBarCodeId);
}
