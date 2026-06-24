package io.sclera.repository;

import io.sclera.dto.VdmsAccessVisibilityDTO;
import io.sclera.model.VdmsAccessVisibility;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VdmsAccessVisibilityRepository extends JpaRepository<VdmsAccessVisibility, String> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms_access_visibility WHERE email = ?1", nativeQuery = true)
    void deleteVdmsAccessVisibilityByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_access_visibility(id, devuid, email) VALUES(?1,?2,?3)", nativeQuery = true)
    void addVdmsAccessVisibilityByEmail(String id, String devuId, String email);

    @Query(nativeQuery = true)
    List<VdmsAccessVisibilityDTO> getVdmsAccessVisibilityByEmail(String email);

    @Query(value = "SELECT full_access FROM vdms_access_visibility WHERE email=?1 AND devuid IS NULL", nativeQuery = true)
    Integer getVdmsFullAccessByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_access_visibility(id, email,full_access) VALUES(?1,?2,?3)", nativeQuery = true)
    void addVdmsFullAccessVisibilityByEmail(String id, String email, Integer fullAccess);
}
