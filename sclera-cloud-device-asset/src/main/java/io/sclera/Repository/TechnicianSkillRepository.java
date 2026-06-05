package io.sclera.Repository;

import io.sclera.dto.TechnicianSkillDTO;
import io.sclera.models.TechnicianSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Manages persistence and querying of {@link TechnicianSkill} records.
 */
@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill,String> {
    /**
     * Inserts a new technician skill row with the given values.
     *
     * @param id           skill identifier
     * @param name         skill name
     * @param type         skill type
     * @param rating       skill rating
     * @param ranking      skill ranking
     * @param createdBy    identifier of the creating user
     * @param createdAt    creation timestamp as an epoch value
     * @param technicianId owning technician identifier
     * @return the number of rows inserted
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
    Integer createTechnicianSkill(String id, String name, String type, BigDecimal rating, Integer ranking, String createdBy, Long createdAt, String technicianId);

    /**
     * Updates the skill row identified by the given id with new values.
     *
     * @param id           skill identifier
     * @param name         skill name
     * @param type         skill type
     * @param rating       skill rating
     * @param ranking      skill ranking
     * @param createdBy    identifier of the creating user
     * @param createdAt    creation timestamp as an epoch value
     * @param technicianId owning technician identifier
     * @return the number of rows updated
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE technician_skill SET name = ?2, type = ?3, rating = ?4, ranking = ?5, created_by = ?6, created_at = ?7, technician_id = ?8 WHERE id = ?1", nativeQuery = true)
    Integer updateTechnicianSkill(String id, String name, String type, BigDecimal rating, Integer ranking, String createdBy, Long createdAt, String technicianId);

    /**
     * Returns all technician skill records.
     *
     * @return the list of skill projections
     */
    @Query(name = "TechnicianSkill.getAll", nativeQuery = true)
    List<TechnicianSkillDTO> getAllTechnicianSkill();

    /**
     * Returns the skill record with the given id.
     *
     * @param id skill identifier
     * @return the matching skill projection
     */
    @Query(name = "TechnicianSkill.getById", nativeQuery = true)
    TechnicianSkillDTO getTechnicianSkillById(String id);

    /**
     * Deletes the skill row with the given id.
     *
     * @param id skill identifier
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_skill WHERE id = ?1", nativeQuery = true)
    void deleteTechnicianSkillById(String id);

    /**
     * Returns all skill records belonging to the given technician.
     *
     * @param technicianId owning technician identifier
     * @return the matching skill projections
     */
    @Query(name = "TechnicianSkill.getByTechnicianId", nativeQuery = true)
    List<TechnicianSkillDTO> getSkillsByTechnicianId(String technicianId);

    /**
     * Inserts the skill row or updates it on id conflict.
     *
     * @param id           skill identifier
     * @param name         skill name
     * @param type         skill type
     * @param rating       skill rating
     * @param ranking      skill ranking
     * @param createdBy    identifier of the creating user
     * @param createdAt    creation timestamp as an epoch value
     * @param technicianId owning technician identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "name = EXCLUDED.name, type = EXCLUDED.type, rating = EXCLUDED.rating, ranking = EXCLUDED.ranking"
            , nativeQuery = true)
    Integer upsertTechnicianSkill(String id, String name, String type, BigDecimal rating, Integer ranking, String createdBy, Long createdAt, String technicianId);

    /**
     * Returns the subset of the supplied ids that already exist.
     *
     * @param ids candidate skill identifiers
     * @return the identifiers found in the table
     */
    @Query(value = "SELECT id FROM technician_skill WHERE id IN ?1", nativeQuery = true)
    Set<String> findExistingTechnicianSkillsByIds(List<String> ids);

    /**
     * Deletes the skill rows matching the given ids.
     *
     * @param ids skill identifiers to delete
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_skill WHERE id IN ?1", nativeQuery = true)
    int deleteTechnicianSkillsByIds(Set<String> ids);

    /**
     * Deletes all skill rows belonging to the given technicians.
     *
     * @param technicianIds technician identifiers whose skills are removed
     * @return the number of rows deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM technician_skill WHERE technician_id IN ?1", nativeQuery = true)
    int deleteTechnicianSkillsByTechnicianIds(Set<String> technicianIds);
}