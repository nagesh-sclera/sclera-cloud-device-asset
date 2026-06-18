package io.sclera.controller.admin;

import io.sclera.dto.TechnicianDTO;
import io.sclera.service.DeviceTechnicianAISuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Device Technician AI Suggestions", description = "Return AI-suggested technicians for a given device type.")
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
    @Operation(summary = "Get AI technician suggestions",
            description = "Returns technicians recommended by the AI suggestion engine for the given device type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Technician suggestions returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getdevicetechnicianaisuggestion")
    public ResponseEntity<List<TechnicianDTO>> getDeviceTechnicianAISuggestionByDeviceType(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device type to find technician suggestions for") @RequestParam String deviceType,
            HttpServletRequest httpServletRequest) {
        log.info("getDeviceTechnicianAISuggestionByDeviceType vdmsid={} deviceType={}", vdmsid, deviceType);
        List<TechnicianDTO> technicians = deviceTechnicianAISuggestionService.getDeviceTechnicianAISuggestionsByDeviceType(deviceType, vdmsid, httpServletRequest);
        return ResponseEntity.ok(technicians);
    }
}
