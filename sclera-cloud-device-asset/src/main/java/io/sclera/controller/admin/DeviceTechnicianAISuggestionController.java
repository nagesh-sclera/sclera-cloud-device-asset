package io.sclera.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.TechnicianDTO;
import io.sclera.service.DeviceTechnicianAISuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
                                                                                           HttpServletRequest httpServletRequest) throws JsonProcessingException {
        List<TechnicianDTO> technicians = deviceTechnicianAISuggestionService.getDeviceTechnicianAISuggestionsByDeviceType(deviceType, vdmsid, httpServletRequest);
        return ResponseEntity.ok(technicians);
    }
}
