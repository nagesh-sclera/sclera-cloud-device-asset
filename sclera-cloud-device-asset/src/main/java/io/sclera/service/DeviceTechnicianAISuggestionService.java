package io.sclera.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import io.sclera.Repository.DeviceTechnicianAISuggestionRepository;
import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import io.sclera.dto.TechnicianDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages AI-generated technician suggestions for device types.
 *
 * <p>Persists and retrieves {@link DeviceTechnicianAISuggestionDTO} records that map a
 * device type (scoped by VDMS) to a set of suggested technicians, and resolves those
 * suggestions into full technician profiles for skill-profile recommendations.
 *
 * <p>Key collaborators:
 * <ul>
 *   <li>{@link DeviceTechnicianAISuggestionRepository} — persistence of the suggestion records.</li>
 *   <li>{@link TechnicianService} — resolves technician identifiers into skill-profile, primary-skill
 *       and availability details.</li>
 * </ul>
 */
@Service

public class DeviceTechnicianAISuggestionService {
    private static final Logger log = LoggerFactory.getLogger(DeviceTechnicianAISuggestionService.class);


    private final DeviceTechnicianAISuggestionRepository deviceTechnicianAISuggestionRepository;
    private final TechnicianService technicianService;

    @Autowired
    public DeviceTechnicianAISuggestionService(DeviceTechnicianAISuggestionRepository deviceTechnicianAiSuggestionRepository, TechnicianService technicianService) {
        this.deviceTechnicianAISuggestionRepository = deviceTechnicianAiSuggestionRepository;
        this.technicianService = technicianService;
    }

    /**
     * Inserts or updates a batch of technician suggestion records, skipping any that fail.
     *
     * @param deviceTechnicianAISuggestionDTOS the suggestion records to upsert
     * @return the set of identifiers that were successfully inserted or updated
     */
    public Set<String> upsertTechnicianSuggestion(List<DeviceTechnicianAISuggestionDTO> deviceTechnicianAISuggestionDTOS) {
        Set<String> insertedDeviceTechnicianSuggestionIds = new HashSet<>();
        if (deviceTechnicianAISuggestionDTOS != null && !deviceTechnicianAISuggestionDTOS.isEmpty()) {
            for (DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto : deviceTechnicianAISuggestionDTOS) {
                try {
                    Integer rowsAffected = deviceTechnicianAISuggestionRepository.upsertTechnicianSuggestion(
                            deviceTechnicianAiSuggestionDto.getId(),
                            deviceTechnicianAiSuggestionDto.getDeviceType(),
                            deviceTechnicianAiSuggestionDto.getTechnicians(),
                            deviceTechnicianAiSuggestionDto.getVdmsId()
                    );
                    if (rowsAffected != null && rowsAffected > 0) {
                        insertedDeviceTechnicianSuggestionIds.add(deviceTechnicianAiSuggestionDto.getId());
                    }
                } catch (Exception e) {
                    log.error("Error upserting Device Technician AI Suggestion with ID {}: {}", deviceTechnicianAiSuggestionDto.getId(), e.getMessage(), e);
                }
            }
        }
        log.info("Inserted or updated {} Device Technician AI Suggestion records.", insertedDeviceTechnicianSuggestionIds.size());
        return insertedDeviceTechnicianSuggestionIds;
    }

    /**
     * Creates a new technician suggestion record, assigning it a freshly generated identifier.
     *
     * @param deviceTechnicianAiSuggestionDto the suggestion to create
     * @param httpServletRequest the incoming HTTP request
     */
    public void createTechnicianSuggestion(DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto, HttpServletRequest httpServletRequest) {
        deviceTechnicianAiSuggestionDto.setId((Generators.timeBasedGenerator().generate().toString()));


        Integer rowsEffected = deviceTechnicianAISuggestionRepository.createTechnicianSuggestion(deviceTechnicianAiSuggestionDto.getId(), deviceTechnicianAiSuggestionDto.getDeviceType(), deviceTechnicianAiSuggestionDto.getTechnicians(), deviceTechnicianAiSuggestionDto.getVdmsId());


    }

    /**
     * Updates an existing technician suggestion record identified by its id, or logs when the id is absent.
     *
     * @param deviceTechnicianAiSuggestionDto the suggestion carrying the id and updated values
     * @param httpServletRequest the incoming HTTP request
     */
    public void updateTechnicianSuggestion(DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto, HttpServletRequest httpServletRequest) {
        if (deviceTechnicianAiSuggestionDto.getId() != null) {

            Integer updatedRows = deviceTechnicianAISuggestionRepository.updateTechnicianSuggestion(deviceTechnicianAiSuggestionDto.getId(), deviceTechnicianAiSuggestionDto.getDeviceType(), deviceTechnicianAiSuggestionDto.getTechnicians(), deviceTechnicianAiSuggestionDto.getVdmsId());


        } else {
            log.info("Device Technicial suggestion with this i does not exists");
        }
    }

    /**
     * Returns the technician suggestion record with the given identifier.
     *
     * @param id the suggestion identifier
     * @param httpServletRequest the incoming HTTP request
     * @return the matching suggestion, or {@code null} if none exists
     */
    public DeviceTechnicianAISuggestionDTO getdevicetechnicianbyid(String id, HttpServletRequest httpServletRequest) {
        return deviceTechnicianAISuggestionRepository.getdevicetechnicianbyid(id);
    }

    /**
     * Returns all technician suggestion records.
     *
     * @param httpServletRequest the incoming HTTP request
     * @return the list of all suggestion records
     */
    public List<DeviceTechnicianAISuggestionDTO> getAlldevicetechnician(HttpServletRequest httpServletRequest) {
        return deviceTechnicianAISuggestionRepository.getAlldevicetechnician();
    }

    // AI Suggestions for Skill Profiles
    /**
     * Resolves the AI-suggested technicians for a device type into full technician profiles.
     *
     * <p>Reads the stored JSON array of technician identifiers for the given device type and VDMS,
     * then loads each technician's skill-profile, primary-skill and availability details.
     *
     * @param deviceType the device type to look up suggestions for
     * @param vdmsId the VDMS scope identifier
     * @param httpServletRequest the incoming HTTP request
     * @return the resolved technician profiles, or an empty list if no suggestions exist
     */
    public List<TechnicianDTO> getDeviceTechnicianAISuggestionsByDeviceType(String deviceType, String vdmsId, HttpServletRequest httpServletRequest) {
        String techniciansJsonArray = deviceTechnicianAISuggestionRepository.getDeviceTechnicianAISuggestionByDeviceType(deviceType, vdmsId);

        if (techniciansJsonArray != null && !techniciansJsonArray.isEmpty()) {
            List<TechnicianDTO> deviceTechnicianAISuggestions = new ArrayList<>();

            ObjectMapper objectMapper = new ObjectMapper();
            List<String> technicianIdList = objectMapper.readValue(techniciansJsonArray, new TypeReference<>() {
            });

            for (String technicianId : technicianIdList) {
                TechnicianDTO technician = technicianService.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(technicianId);
                if (technician != null) {
                    deviceTechnicianAISuggestions.add(technician);
                }
            }

            return deviceTechnicianAISuggestions;
        }

        log.info("Technicians for the Device Type: {}, does not exist", deviceType);
        return Collections.emptyList();
    }
}
