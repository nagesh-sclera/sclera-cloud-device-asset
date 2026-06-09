package io.sclera.interfaces;

import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import io.sclera.dto.TechnicianDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/** Service contract for {@link io.sclera.service.DeviceTechnicianAISuggestionService}. */
public interface DeviceTechnicianAISuggestionServiceInterface {

    Set<String> upsertTechnicianSuggestion(List<DeviceTechnicianAISuggestionDTO> deviceTechnicianAISuggestionDTOS);

    void createTechnicianSuggestion(DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto, HttpServletRequest httpServletRequest);

    void updateTechnicianSuggestion(DeviceTechnicianAISuggestionDTO deviceTechnicianAiSuggestionDto, HttpServletRequest httpServletRequest);

    DeviceTechnicianAISuggestionDTO getdevicetechnicianbyid(String id, HttpServletRequest httpServletRequest);

    List<DeviceTechnicianAISuggestionDTO> getAlldevicetechnician(HttpServletRequest httpServletRequest);

    List<TechnicianDTO> getDeviceTechnicianAISuggestionsByDeviceType(String deviceType, String vdmsId, HttpServletRequest httpServletRequest);
}
