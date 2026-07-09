package io.sclera.service;

import io.sclera.dto.TechnicianAvailabilityDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/** Service contract for {@link io.sclera.service.TechnicianAvailabilityService}. */
public interface TechnicianAvailabilityService {
    Set<String> upsertTechnicianAvailability(List<TechnicianAvailabilityDTO> technicianAvailabilityDTOS);

    Set<String> deleteTechnicianAvailabilityById(List<TechnicianAvailabilityDTO> technicianAvailabilityDTOS);

    void createTechnicianAvailability(TechnicianAvailabilityDTO technicianAvailabilityDto);

    void updateTechnicianAvailability(TechnicianAvailabilityDTO technicianAvailabilityDto);

    TechnicianAvailabilityDTO getTechnicianAvailabilityById(String id, HttpServletRequest httpServletRequest);

    List<TechnicianAvailabilityDTO> getAllTechnicianAvailability(HttpServletRequest httpServletRequest);

    List<TechnicianAvailabilityDTO> getAvailabilityInRange(String technicianId, String startTime, String endTime, HttpServletRequest httpServletRequest);
}
