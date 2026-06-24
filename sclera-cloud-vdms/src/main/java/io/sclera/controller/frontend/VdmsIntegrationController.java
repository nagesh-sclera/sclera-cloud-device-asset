package io.sclera.controller.frontend;

import io.sclera.dto.VdmsIntegrationDTO;
import io.sclera.service.VdmsIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


@RequestMapping("/api")
@RestController
public class VdmsIntegrationController {

    @Autowired
    private VdmsIntegrationService vdmsIntegrationService;

    @PostMapping("/organisation/{org_id}/user/{email}/vdms/{vdms}/addVdmsIntegration")
    public ResponseEntity<?> addVdmsIntegrationByAndVdmsIdAndId(@RequestParam String loggedInUser, @RequestBody VdmsIntegrationDTO vdmsIntegrationDTO, @PathVariable String org_id,
                                                                @PathVariable String email, @PathVariable String vdms, HttpServletRequest httpServletRequest) {
        return vdmsIntegrationService.addVdmsIntegrationByAndVdmsIdAndId(vdmsIntegrationDTO, org_id, email, vdms, loggedInUser, httpServletRequest);
    }

    @GetMapping("/organisation/{org_id}/user/{email}/vdms/{vdms}/getAllVdmsIntegrations")
    public ResponseEntity<?> getAllVdmsIntegrationsByVdmsId(@RequestParam String loggedInUser, @PathVariable String org_id, @PathVariable String email, @PathVariable String vdms, @RequestParam("type") Integer type, HttpServletRequest httpServletRequest) {
        return vdmsIntegrationService.getAllVdmsIntegrationsByVdmsId(org_id, email, vdms, type, loggedInUser, httpServletRequest);
    }

    @PutMapping("/organisation/{org_id}/user/{email}/vdms/{vdms}/integration/{id}/active/{active}/updateVdmsIntegrationActivity")
    public ResponseEntity<?> updateVdmsIntegrationActivityByVdmsIdAndId(@RequestParam String loggedInUser, @PathVariable String org_id, @PathVariable String email, @PathVariable String vdms, @PathVariable String id,
                                                                        @PathVariable Integer active, HttpServletRequest httpServletRequest) {
        return vdmsIntegrationService.updateVdmsIntegrationActivityByVdmsIdAndId(org_id, email, vdms, id, active, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/organisation/{org_id}/user/{email}/vdms/{vdms}/integration/{id}/deleteVdmsIntegration")
    public ResponseEntity<?> deleteVdmsIntegrationByVdmsIdAndId(@RequestParam String loggedInUser, @PathVariable String org_id, @PathVariable String email, @PathVariable String vdms, @PathVariable String id, HttpServletRequest httpServletRequest) {
        return vdmsIntegrationService.deleteVdmsIntegrationByVdmsIdAndId(org_id, email, vdms, id, loggedInUser, httpServletRequest);
    }


}

