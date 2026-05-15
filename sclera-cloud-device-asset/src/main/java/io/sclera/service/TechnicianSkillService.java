package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.TechnicianSkillRepository;
import io.sclera.dto.TechnicianSkillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TechnicianSkillService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianSkillService.class);

    private final TechnicianSkillRepository technicianSkillRepository;

    @Autowired
    public TechnicianSkillService(TechnicianSkillRepository technicianSkillRepository) {
        this.technicianSkillRepository = technicianSkillRepository;
    }

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

    public TechnicianSkillDTO getTechnicianSkillById(String id) {
        return technicianSkillRepository.getTechnicianSkillById(id);
    }

    public List<TechnicianSkillDTO> getAllTechnicianSkill() {
        return technicianSkillRepository.getAllTechnicianSkill();
    }

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