package io.sclera.controller.admin;

import io.sclera.dto.TechnicianDTO;
import io.sclera.service.DeviceTechnicianAISuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class DeviceTechnicianAISuggestionController {

    private static final Logger log = LoggerFactory.getLogger(DeviceTechnicianAISuggestionController.class);

    @Autowired
    DeviceTechnicianAISuggestionService deviceTechnicianAISuggestionService;

    /**
     * Returns technicians recommended by the AI suggestion engine for a device type.
     *
     * @param vdmsid             owning VDMS id
     * @param deviceType         device type to find technician suggestions for
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return ranked list of suggested technicians
     */
    @GetMapping("/getdevicetechnicianaisuggestion")
    public ResponseEntity<List<TechnicianDTO>> getDeviceTechnicianAISuggestionByDeviceType(@RequestParam String vdmsid,
                                                                                           @RequestParam String deviceType,
                                                                                           HttpServletRequest httpServletRequest) {
        log.info("getDeviceTechnicianAISuggestionByDeviceType vdmsid={} deviceType={}", vdmsid, deviceType);
        try {
            List<TechnicianDTO> technicians = deviceTechnicianAISuggestionService.getDeviceTechnicianAISuggestionsByDeviceType(deviceType, vdmsid, httpServletRequest);
            return ResponseEntity.ok(technicians);
        } catch (Exception e) {
            log.error("getDeviceTechnicianAISuggestionByDeviceType failed vdmsid={}: {}", vdmsid, e.getMessage(), e);
            throw e;
        }
    }
}
