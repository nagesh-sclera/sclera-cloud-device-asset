package io.sclera.service.impl;
import io.sclera.service.*;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.TechnicianAvailabilityRepository;
import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.service.TechnicianAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages technician availability records, providing create, update, upsert,
 * delete and query operations over availability windows (date/time ranges,
 * all-day flags, recurrence frequency and conditions) for technicians.
 * <p>
 * Persistence is delegated to {@link TechnicianAvailabilityRepository}, and
 * availability data is exchanged via {@link TechnicianAvailabilityDTO}.
 */
@Service
public class TechnicianAvailabilityServiceImpl implements TechnicianAvailabilityService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianAvailabilityServiceImpl.class);

    private final TechnicianAvailabilityRepository technicianAvailabilityRepository;

    @Autowired
    public TechnicianAvailabilityServiceImpl(TechnicianAvailabilityRepository technicianAvailabilityRepository) {
        this.technicianAvailabilityRepository = technicianAvailabilityRepository;
    }

    /**
     * Inserts or updates each supplied availability record, skipping any that fail.
     *
     * @param technicianAvailabilityDTOS the availability records to upsert; null or empty yields an empty result
     * @return the set of IDs that were successfully inserted or updated
     */
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

    /**
     * Deletes the availability records whose IDs exist in the store, resolving existence first.
     *
     * @param technicianAvailabilityDTOS the records identifying which availability entries to delete
     * @return the set of IDs that existed and were deleted; empty when none matched
     */
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

    /**
     * Creates a new availability record, assigning it a generated time-based UUID.
     *
     * @param technicianAvailabilityDto the availability record to create
     */
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

    /**
     * Updates an existing availability record when a non-empty ID is supplied.
     *
     * @param technicianAvailabilityDto the availability record carrying the ID and updated values
     */
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


    /**
     * Returns the availability record matching the given ID.
     *
     * @param id the availability record identifier
     * @param httpServletRequest the originating HTTP request
     * @return the matching availability record, or null if none exists
     */
    public TechnicianAvailabilityDTO getTechnicianAvailabilityById(String id, HttpServletRequest httpServletRequest) {
        return  technicianAvailabilityRepository.getTechnicianAvailabilityById(id);
    }

    /**
     * Returns all technician availability records.
     *
     * @param httpServletRequest the originating HTTP request
     * @return the list of all availability records
     */
    public List<TechnicianAvailabilityDTO> getAllTechnicianAvailability(HttpServletRequest httpServletRequest) {
        return technicianAvailabilityRepository.getAllTechnicianAvailability();
    }

    /**
     * Returns a technician's availability records that fall within the given time range.
     *
     * @param technicianId the technician identifier
     * @param startTime the inclusive start of the time range
     * @param endTime the inclusive end of the time range
     * @param httpServletRequest the originating HTTP request
     * @return the list of availability records within the range
     */
    public List<TechnicianAvailabilityDTO> getAvailabilityInRange(String technicianId, String startTime, String endTime,HttpServletRequest httpServletRequest) {
        return technicianAvailabilityRepository.getTechnicianAvailabilityInRange(technicianId, startTime, endTime);
    }
}