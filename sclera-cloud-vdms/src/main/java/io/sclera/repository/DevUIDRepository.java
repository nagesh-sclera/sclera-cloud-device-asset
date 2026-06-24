package io.sclera.repository;

import io.sclera.dto.DevUIDDTO;
import io.sclera.model.DevUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

@Repository
public interface DevUIDRepository extends JpaRepository<DevUID,String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO devuid(devuid,last_seen) VALUE(?1,?2)" ,nativeQuery = true)
    void addDevUID(String devuid, BigInteger currentTimeMillis);

    @Modifying
    @Transactional
    @Query(value = "UPDATE devuid SET last_seen = ?1 WHERE devuid = ?2" ,nativeQuery = true)
    void updateDevUID(BigInteger creation_time, String devuid);

    @Query(value = "SELECT devuid FROM devuid WHERE devuid = ?1" ,nativeQuery = true)
    String getDevUID(String devuid);

    @Query(nativeQuery = true)
    Set<DevUIDDTO> getAllDevUIDs(String key, String sortBy, int pageSize, int offset);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM devuid WHERE devuid IN ?1" ,nativeQuery = true)
    void deleteDevUIDs(Set<String> devUIDs);

    @Query(nativeQuery = true)
    Set<DevUIDDTO> getVisibleDevUIDs(String email, String key, String sort, int pageSize, int offset);
}
