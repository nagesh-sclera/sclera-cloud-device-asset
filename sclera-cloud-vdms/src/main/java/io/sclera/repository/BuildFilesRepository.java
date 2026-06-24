package io.sclera.repository;

import io.sclera.dto.BuildFilesDTO;
import io.sclera.model.BuildFiles;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildFilesRepository extends JpaRepository<BuildFiles, String> {

    @Query(nativeQuery = true)
    BuildFilesDTO getBuildFiles(String type);


    @Modifying
    @Transactional
    @Query(value = "UPDATE build_files SET version = ?1 WHERE type = ?2", nativeQuery = true)
    void updateVersionByType(String version, String type);
}
