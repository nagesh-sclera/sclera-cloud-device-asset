package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.TechnicianSkillRepository;
import io.sclera.dto.TechnicianSkillDTO;
import io.sclera.interfaces.TechnicianSkillServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages persistence of technician skill records.
 *
 * <p>Provides create, update, bulk upsert, lookup, and bulk delete operations,
 * delegating all data access to {@link TechnicianSkillRepository} and exchanging
 * data through {@link TechnicianSkillDTO}.
 */
@Service
public class TechnicianSkillService implements TechnicianSkillServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(TechnicianSkillService.class);

    private final TechnicianSkillRepository technicianSkillRepository;

    @Autowired
    public TechnicianSkillService(TechnicianSkillRepository technicianSkillRepository) {
        this.technicianSkillRepository = technicianSkillRepository;
    }

    /**
     * Inserts or updates each supplied technician skill record, skipping any that fail.
     *
     * @param technicianSkillDTOs the technician skills to upsert; may be {@code null} or empty
     * @return the set of IDs that were successfully inserted or updated
     */
    public Set<String> upsertTechnicianSkill(List<TechnicianSkillDTO> technicianSkillDTOs) {
        Set<String> insertedTechnicianSkillIds = new HashSet<>();
        if (technicianSkillDTOs != null && !technicianSkillDTOs.isEmpty()) {
            for (TechnicianSkillDTO technicianSkillDTO : technicianSkillDTOs) {
                try {
                    Integer rowsAffected = technicianSkillRepository.upsertTechnicianSkill(
                            technicianSkillDTO.getId(),
                            technicianSkillDTO.getName(),
                            technicianSkillDTO.getType(),
                            technicianSkillDTO.getRating(),
                            technicianSkillDTO.getRanking(),
                            technicianSkillDTO.getCreatedBy(),
                            technicianSkillDTO.getCreatedAt(),
                            technicianSkillDTO.getTechnicianId()
                    );
                    if (rowsAffected != null && rowsAffected > 0) {
                        insertedTechnicianSkillIds.add(technicianSkillDTO.getId());
                    }
                } catch (Exception e) {
                    log.error("Error upserting TechnicianSkill with ID {}: {}", technicianSkillDTO.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("Inserted or updated {} Technician Skills records.", insertedTechnicianSkillIds.size());
        return insertedTechnicianSkillIds;
    }

    /**
     * Creates a new technician skill record, assigning it a freshly generated time-based ID.
     *
     * @param technicianSkillDto the technician skill to create
     */
    public void createTechnicianSkill(TechnicianSkillDTO technicianSkillDto) {
        technicianSkillDto.setId(Generators.timeBasedGenerator().generate().toString());
        technicianSkillRepository.createTechnicianSkill(
                technicianSkillDto.getId(),
                technicianSkillDto.getName(),
                technicianSkillDto.getType(),
                technicianSkillDto.getRating(),
                technicianSkillDto.getRanking(),
                technicianSkillDto.getCreatedBy(),
                technicianSkillDto.getCreatedAt(),
                technicianSkillDto.getTechnicianId()
        );
    }

    /**
     * Updates an existing technician skill record; logs and skips the update when the ID is {@code null}.
     *
     * @param technicianSkillDto the technician skill carrying the ID and updated values
     */
    public void updateTechnicianSkill(TechnicianSkillDTO technicianSkillDto) {
        if (technicianSkillDto.getId() != null) {
            technicianSkillRepository.updateTechnicianSkill(
                    technicianSkillDto.getId(),
                    technicianSkillDto.getName(),
                    technicianSkillDto.getType(),
                    technicianSkillDto.getRating(),
                    technicianSkillDto.getRanking(),
                    technicianSkillDto.getCreatedBy(),
                    technicianSkillDto.getCreatedAt(),
                    technicianSkillDto.getTechnicianId()
            );
        } else {
            log.info("technicianSkillId is null");
        }
    }

    /**
     * Retrieves a single technician skill record by its ID.
     *
     * @param id the technician skill ID
     * @return the matching technician skill, or {@code null} if none exists
     */
    public TechnicianSkillDTO getTechnicianSkillById(String id) {
        return technicianSkillRepository.getTechnicianSkillById(id);
    }

    /**
     * Retrieves all technician skill records.
     *
     * @return the list of all technician skills
     */
    public List<TechnicianSkillDTO> getAllTechnicianSkill() {
        return technicianSkillRepository.getAllTechnicianSkill();
    }

    /**
     * Deletes the technician skill records whose IDs are present and currently exist in the store.
     *
     * @param technicianSkillDTOS the technician skills identifying the records to delete; may be {@code null} or empty
     * @return the set of IDs that existed and were deleted; empty when nothing matched or an error occurred
     */
    public Set<String> deleteTechnicianSkillsById(List<TechnicianSkillDTO> technicianSkillDTOS) {
        Set<String> existingIds = Set.of();
        if (technicianSkillDTOS != null && !technicianSkillDTOS.isEmpty()) {
            List<String> allIds = technicianSkillDTOS.stream()
                  .map(TechnicianSkillDTO::getId)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

            if (!allIds.isEmpty()) {
                try {
                    existingIds = this.findExistingTechnicianSkillsByIds(allIds);
                    if (existingIds != null && !existingIds.isEmpty()) {
                        this.deleteTechnicianSkillsByIds(existingIds);
                        return existingIds;
                    }
                } catch (Exception e) {
                    log.error("Error deleting TechnicianSkills: {}", e.getMessage(), e);
                }
            }
        }
        return existingIds;
    }

    private Set<String> findExistingTechnicianSkillsByIds(List<String> ids) {
        Set<String> existingIds = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            existingIds = technicianSkillRepository.findExistingTechnicianSkillsByIds(ids);
        }
        return existingIds;
    }

    private void deleteTechnicianSkillsByIds(Set<String> ids) {
        int noOfRecordsDeleted = technicianSkillRepository.deleteTechnicianSkillsByIds(ids);
        log.info("No of ids got to delete : {}, Number of TechnicianSkillsByIds records got deleted : {} ", ids.size(), noOfRecordsDeleted);
    }
}