package io.sclera.controller.admin;

import io.sclera.dto.TechnicianDTO;
import io.sclera.service.DeviceTechnicianAISuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceTechnicianAISuggestionController {

    @Autowired
    DeviceTechnicianAISuggestionService deviceTechnicianAISuggestionService;

    @GetMapping("/user/{username}/vdms/{vdmsid}/getdevicetechnicianaisuggestion")
    public ResponseEntity<List<TechnicianDTO>> getDeviceTechnicianAISuggestionByDeviceType(@PathVariable String username,
                                                                                           @PathVariable String vdmsid,
                                                                                           @RequestParam String deviceType,
                                                                                           HttpServletRequest httpServletRequest) {
        List<TechnicianDTO> technicians = deviceTechnicianAISuggestionService.getDeviceTechnicianAISuggestionsByDeviceType(deviceType, vdmsid, httpServletRequest);
        return ResponseEntity.ok(technicians);
    }
}
