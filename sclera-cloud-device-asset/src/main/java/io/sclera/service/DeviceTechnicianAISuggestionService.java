package io.sclera.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import io.sclera.Repository.DeviceTechnicianAISuggestionRepository;
import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import io.sclera.dto.TechnicianDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

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

    public void createTechnicianSuggestion(DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto, HttpServletRequest httpServletRequest) {
        deviceTechnicianAiSuggestionDto.setId((Generators.timeBasedGenerator().generate().toString()));


        Integer rowsEffected = deviceTechnicianAISuggestionRepository.createTechnicianSuggestion(deviceTechnicianAiSuggestionDto.getId(), deviceTechnicianAiSuggestionDto.getDeviceType(), deviceTechnicianAiSuggestionDto.getTechnicians(), deviceTechnicianAiSuggestionDto.getVdmsId());


    }

    public void updateTechnicianSuggestion(DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto, HttpServletRequest httpServletRequest) {
        if (deviceTechnicianAiSuggestionDto.getId() != null) {

            Integer updatedRows = deviceTechnicianAISuggestionRepository.updateTechnicianSuggestion(deviceTechnicianAiSuggestionDto.getId(), deviceTechnicianAiSuggestionDto.getDeviceType(), deviceTechnicianAiSuggestionDto.getTechnicians(), deviceTechnicianAiSuggestionDto.getVdmsId());


        } else {
            log.info("Device Technicial suggestion with this i does not exists");
        }
    }

    public DeviceTechnicianAISuggestionDTO getdevicetechnicianbyid(String id, HttpServletRequest httpServletRequest) {
        return deviceTechnicianAISuggestionRepository.getdevicetechnicianbyid(id);
    }

    public List<DeviceTechnicianAISuggestionDTO> getAlldevicetechnician(HttpServletRequest httpServletRequest) {
        return deviceTechnicianAISuggestionRepository.getAlldevicetechnician();
    }

    // AI Suggestions for Skill Profiles
    public List<TechnicianDTO> getDeviceTechnicianAISuggestionsByDeviceType(String deviceType, String vdmsId, HttpServletRequest httpServletRequest) throws JsonProcessingException {
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
