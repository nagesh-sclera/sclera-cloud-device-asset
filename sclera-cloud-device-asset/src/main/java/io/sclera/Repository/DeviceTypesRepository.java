package io.sclera.Repository;

import io.sclera.dto.DeviceTypesDTO;
import io.sclera.models.DeviceTypes;
import javax.transaction.Transactional;

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
    @Query(value =
            "INSERT INTO device_types (id, name, updated_timestamp) " +
                    "VALUES (?1, ?2, ?3) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "name = ?2, " +
                    "updated_timestamp = ?3",
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


