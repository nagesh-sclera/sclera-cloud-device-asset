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

@Repository
public interface DeviceTypesRepository extends JpaRepository<DeviceTypes, String> {

    @Modifying
    @Transactional
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


    @Query(nativeQuery = true)
    List<DeviceTypesDTO> getAllDeviceTypes();

    @Query(value = "SELECT MAX(d.updated_timestamp) FROM device_types d",nativeQuery = true)
    BigInteger findMaxUpdatedTimestamp();

    @Query(nativeQuery = true)
    List<DeviceTypesDTO> getAllUpdatedDeviceTypes(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_types SET old_name = NULL WHERE old_name = ?1", nativeQuery = true)
    void deleteOldName(String oldName);
}


