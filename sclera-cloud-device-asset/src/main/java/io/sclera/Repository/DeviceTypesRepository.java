package io.sclera.Repository;

import io.sclera.dto.DeviceTypesDTO;
import io.sclera.models.DeviceTypes;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.math.BigInteger;
import java.util.List;

/**
 * Manages persistence and querying of {@link DeviceTypes} entities.
 */
@Repository
public interface DeviceTypesRepository extends JpaRepository<DeviceTypes, String> {

    /**
     * Inserts a device type, updating its name and timestamp on id conflict.
     *
     * @param id the device type identifier
     * @param name the device type name
     * @param lastModified the updated timestamp
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: ON CONFLICT (id) DO UPDATE is already PG-portable; no JPQL equivalent for upsert
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value =
            "INSERT INTO device_types (id, name, updated_timestamp) " +
                    "VALUES (?1, ?2, ?3) " +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "name = EXCLUDED.name, " +
                    "updated_timestamp = EXCLUDED.updated_timestamp",
            nativeQuery = true)
    Integer upsert(String id,
                   String name,
                   BigInteger lastModified);


    /**
     * Retrieves all device type projections.
     *
     * @return the list of device type projections
     */
    @Query("SELECT new io.sclera.dto.DeviceTypesDTO(dt.id, dt.name, dt.updatedTimestamp) " +
           "FROM DeviceTypes dt")
    List<DeviceTypesDTO> getAllDeviceTypes();

    /**
     * Returns the most recent updated timestamp across all device types.
     *
     * @return the maximum updated timestamp
     */
    @Query("SELECT MAX(dt.updatedTimestamp) FROM DeviceTypes dt")
    BigInteger findMaxUpdatedTimestamp();

    /**
     * Retrieves device type projections where old_name is set (renamed types).
     * Note: the vdmsId parameter is accepted for API compatibility but is not used
     * as a filter — DeviceTypes has no vdms_id column.
     *
     * @param vdmsId the owning VDMS identifier (unused in query — no vdms_id column on DeviceTypes)
     * @return the list of updated device type projections
     */
    @Query("SELECT new io.sclera.dto.DeviceTypesDTO(dt.name, dt.oldName) " +
           "FROM DeviceTypes dt WHERE dt.oldName IS NOT NULL")
    List<DeviceTypesDTO> getAllUpdatedDeviceTypes(String vdmsId);

    /**
     * Clears the old name on device types matching the given old name.
     *
     * @param oldName the old name to clear
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceTypes dt SET dt.oldName = NULL WHERE dt.oldName = :oldName")
    void deleteOldName(String oldName);
}
