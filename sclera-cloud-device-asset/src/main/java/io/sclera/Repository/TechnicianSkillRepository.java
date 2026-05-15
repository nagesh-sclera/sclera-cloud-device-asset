package io.sclera.Repository;

import io.sclera.dto.TechnicianSkillDTO;
import io.sclera.models.TechnicianSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill,String> {
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
    Integer createTechnicianSkill(String id, String name, String type, BigDecimal rating, Integer ranking, String createdBy, Long createdAt, String technicianId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE technician_skill SET name = ?2, type = ?3, rating = ?4, ranking = ?5, created_by = ?6, created_at = ?7, technician_id = ?8 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianSkill(String id, String name, String type, BigDecimal rating, Integer ranking, String createdBy, Long createdAt, String technicianId);

    @Query(name = "TechnicianSkill.getAll", nativeQuery = true)
    List<TechnicianSkillDTO> getAllTechnicianSkill();

    @Query(name = "TechnicianSkill.getById", nativeQuery = true)
    TechnicianSkillDTO getTechnicianSkillById(String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_skill WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianSkillById(String id);

    @Query(name = "TechnicianSkill.getByTechnicianId", nativeQuery = true)
    List<TechnicianSkillDTO> getSkillsByTechnicianId(String technicianId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8) " +
            "ON DUPLICATE KEY UPDATE " +
            "name = ?2, type = ?3, rating = ?4, ranking = ?5 "
            , nativeQuery = true)
    Integer upsertTechnicianSkill(String id, String name, String type, BigDecimal rating, Integer ranking, String createdBy, Long createdAt, String technicianId);

    @Query(value = "SELECT id FROM technician_skill WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechnicianSkillsByIds(List<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_skill WHERE id IN ?1", nativeQuery = true)
    int deleteTechnicianSkillsByIds(Set<String> ids);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_skill WHERE technician_id IN ?1", nativeQuery = true)
    int deleteTechnicianSkillsByTechnicianIds(Set<String> technicianIds);
}