package io.sclera.repository;

import io.sclera.dto.ScleraFXDTO;
import io.sclera.model.ScleraFX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface ScleraFXRepository extends JpaRepository<ScleraFX ,String> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE sclerafx SET version = ?1 WHERE os = ?2" ,nativeQuery = true)
    void updateScleraFXVersionByOs(String version, String os);


    @Query(value = "SELECT version FROM sclerafx WHERE os = ?1" ,nativeQuery = true)
    String getScleraFXVersionByOS(String os);

    @Query(value = "SELECT COUNT(os) FROM sclerafx " ,nativeQuery = true)
    Integer checkScleraFXVersion();


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO sclerafx VALUES ('windows','1.0.0'),('ubuntu','1.0.0'),('mac','1.0.0')" ,nativeQuery = true)
    void insertDefaultScleraFXVersions();

    @Query(nativeQuery = true)
    List<ScleraFXDTO> getAllScleraFXVersions();
}
