package io.sclera.controller.multiTenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.MultiTenantVdmsService;
import io.sclera.service.VdmsVisibilityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/multiTenant")
@RestController
public class MultiTenantVdmsController {

    @Autowired
    private MultiTenantVdmsService multiTenantVdmsService;

    @Autowired
    private VdmsVisibilityService vdmsVisibilityService;

    @PutMapping(value = "/vdms/{vdmsId}/updateVdmsActivationStatus")
    public ResponseEntity<?> updateVdmsActivationStatus(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return multiTenantVdmsService.updateVdmsActivationStatus(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/multiTenantCheck")
    public ResponseEntity<ResponseDTO>getMultiTenantCheck(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return multiTenantVdmsService.getMultiTenantCheck(vdmsId, httpServletRequest);
    }

    @GetMapping("/user/{email}/visible/vdms")
    public ResponseEntity<?> getVisibleVdmsByEmail(@RequestParam String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsVisibilityService.getVisibleVdmsByEmail(email, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateVdmsDeploymentType")
    public ResponseEntity<?> updateVdmsDeploymentType(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return multiTenantVdmsService.updateVdmsDeploymentType(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/multiTenantVdmsInfo")
    public ResponseEntity<ResponseDTO>multiTenantVdmsInfo(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return multiTenantVdmsService.multiTenantVdmsInfo(vdmsId, httpServletRequest);
    }
}
