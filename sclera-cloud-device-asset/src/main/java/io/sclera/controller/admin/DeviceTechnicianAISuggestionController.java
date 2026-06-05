package io.sclera.controller.admin;

import io.sclera.dto.TechnicianDTO;
import io.sclera.service.DeviceTechnicianAISuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST endpoint that returns AI-suggested technicians for a given device type.
 * Delegates to {@link DeviceTechnicianAISuggestionService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceTechnicianAISuggestionController {

    @Autowired
    DeviceTechnicianAISuggestionService deviceTechnicianAISuggestionService;

    /**
     * Returns technicians recommended by the AI suggestion engine for a device type.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param deviceType         device type to find technician suggestions for
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return ranked list of suggested technicians
     */
    @GetMapping("/user/{username}/vdms/{vdmsid}/getdevicetechnicianaisuggestion")
    public ResponseEntity<List<TechnicianDTO>> getDeviceTechnicianAISuggestionByDeviceType(@PathVariable String username,
                                                                                           @PathVariable String vdmsid,
                                                                                           @RequestParam String deviceType,
                                                                                           HttpServletRequest httpServletRequest) {
        List<TechnicianDTO> technicians = deviceTechnicianAISuggestionService.getDeviceTechnicianAISuggestionsByDeviceType(deviceType, vdmsid, httpServletRequest);
        return ResponseEntity.ok(technicians);
    }
}
