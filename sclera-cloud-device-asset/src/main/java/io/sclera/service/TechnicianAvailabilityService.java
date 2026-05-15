package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.TechnicianAvailabilityRepository;
import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.dto.TechnicianSkillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TechnicianAvailabilityService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianAvailabilityService.class);

    private final TechnicianAvailabilityRepository technicianAvailabilityRepository;

    @Autowired
    public TechnicianAvailabilityService(TechnicianAvailabilityRepository technicianAvailabilityRepository) {
        this.technicianAvailabilityRepository = technicianAvailabilityRepository;
    }

    public Set<String> upsertTechnicianAvailability(List<TechnicianAvailabilityDTO> technicianAvailabilityDTOS) {
        Set<String> insertedTechnicianAvailabilityIds = new HashSet<>();
        if(technicianAvailabilityDTOS != null && !technicianAvailabilityDTOS.isEmpty()) {
            for (TechnicianAvailabilityDTO technicianAvailabilityDTO : technicianAvailabilityDTOS) {
                try {
                    Integer rowsAffected = technicianAvailabilityRepository.upsertTechnicianAvailability(
                            technicianAvailabilityDTO.getId(),
                            technicianAvailabilityDTO.getStartDate(),
                            technicianAvailabilityDTO.getEndDate(),
                            technicianAvailabilityDTO.getStartTime(),
                            technicianAvailabilityDTO.getEndTime(),
                            technicianAvailabilityDTO.getIsAllDay(),
                            technicianAvailabilityDTO.getFrequency(),
                            technicianAvailabilityDTO.getCondition(),
                            technicianAvailabilityDTO.getTechnicianId());
                    if (rowsAffected != null && rowsAffected > 0) {
                        insertedTechnicianAvailabilityIds.add(technicianAvailabilityDTO.getId());
                    }
                } catch (Exception e) {
                    log.error("Error upserting TechnicianAvailability with ID {}: {}", technicianAvailabilityDTO.getId(), e.getMessage(), e);
                }
            }
        }
        log.info("Inserted or updated {} Technician Availability records.", insertedTechnicianAvailabilityIds.size());
        return insertedTechnicianAvailabilityIds;
    }

    public Set<String> deleteTechnicianAvailabilityById(List<TechnicianAvailabilityDTO> technicianAvailabilityDTOS) {
        Set<String> existingIds = Set.of();
        if (technicianAvailabilityDTOS != null && !technicianAvailabilityDTOS.isEmpty()) {
            List<String> allIds = technicianAvailabilityDTOS.stream()
                    .map(TechnicianAvailabilityDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!allIds.isEmpty()) {
                try {
                    existingIds = this.findExistingTechnicianAvailabilityByIds(allIds);
                    if (existingIds != null && !existingIds.isEmpty()) {
                        this.deleteTechnicianAvailabilityByIds(existingIds);
                        return existingIds;
                    }
                } catch (Exception e) {
                    log.error("Error deleting Technician Availability records: {}", e.getMessage(), e);
                }
            }
        }
        return existingIds;
    }

    private Set<String> findExistingTechnicianAvailabilityByIds(List<String> ids) {
        Set<String> existingIds = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            existingIds = technicianAvailabilityRepository.findExistingTechnicianAvailabilityByIds(ids);
        }
        return existingIds;
    }

    private void deleteTechnicianAvailabilityByIds(Set<String> ids) {
        int noOfRecordsDeleted = technicianAvailabilityRepository.deleteTechnicianAvailabilityByIds(ids);
        log.info("No of ids got to delete : {}, Number of TechnicianAvailabilityByIds records got deleted : {} ", ids.size(), noOfRecordsDeleted);
    }

    public void createTechnicianAvailability(TechnicianAvailabilityDTO technicianAvailabilityDto) {
        technicianAvailabilityDto.setId(Generators.timeBasedGenerator().generate().toString());

        Integer rows = technicianAvailabilityRepository.createTechnicianAvailability(
                technicianAvailabilityDto.getId(),
                technicianAvailabilityDto.getStartDate(),
                technicianAvailabilityDto.getEndDate(),
                technicianAvailabilityDto.getStartTime(),
                technicianAvailabilityDto.getEndTime(),
                technicianAvailabilityDto.getIsAllDay(),
                technicianAvailabilityDto.getFrequency(),
                technicianAvailabilityDto.getCondition(),
                technicianAvailabilityDto.getTechnicianId());
        if (rows < 1) {
            throw new RuntimeException("An error occurred while inserting technician availability");
        }

    }

    public void updateTechnicianAvailability(TechnicianAvailabilityDTO technicianAvailabilityDto) {
        if (technicianAvailabilityDto.getId() != null && !technicianAvailabilityDto.getId().isEmpty()) {
            Integer rows = technicianAvailabilityRepository.updateTechnicianAvailability(
                    technicianAvailabilityDto.getId(),
                    technicianAvailabilityDto.getStartDate(),
                    technicianAvailabilityDto.getEndDate(),
                    technicianAvailabilityDto.getStartTime(),
                    technicianAvailabilityDto.getEndTime(),
                    technicianAvailabilityDto.getIsAllDay(),
                    technicianAvailabilityDto.getFrequency(),
                    technicianAvailabilityDto.getCondition(),
                    technicianAvailabilityDto.getTechnicianId());
            if (rows < 1) {
                throw new RuntimeException("An error occurred while updating technician availability");
            }
        }
    }


    public TechnicianAvailabilityDTO getTechnicianAvailabilityById(String id, HttpServletRequest httpServletRequest) {
        return  technicianAvailabilityRepository.getTechnicianAvailabilityById(id);
    }

    public List<TechnicianAvailabilityDTO> getAllTechnicianAvailability(HttpServletRequest httpServletRequest) {
        return technicianAvailabilityRepository.getAllTechnicianAvailability();
    }

    public List<TechnicianAvailabilityDTO> getAvailabilityInRange(String technicianId, String startTime, String endTime,HttpServletRequest httpServletRequest) {
        return technicianAvailabilityRepository.getTechnicianAvailabilityInRange(technicianId, startTime, endTime);
    }
}