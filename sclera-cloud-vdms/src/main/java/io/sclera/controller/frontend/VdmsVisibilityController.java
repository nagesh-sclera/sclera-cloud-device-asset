package io.sclera.controller.frontend;

import io.sclera.dto.QuickSearchDTO;
import io.sclera.service.VdmsVisibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;


@RequestMapping("/api")
@RestController
public class VdmsVisibilityController {

    @Autowired
    private VdmsVisibilityService vdmsvisibilityService;

    @PostMapping("/user/{email}/visible/vdms")
    public ResponseEntity<?> replaceVisibleVdmsByEmail(@RequestParam String loggedInUser, @RequestBody Set<QuickSearchDTO> vdmsdtos, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.replaceNewVisibleVdmsByEmail(vdmsdtos, email, loggedInUser, httpServletRequest);
    }

    @PostMapping("/organisation/{organisation_id}/user/{email}/visible/vdms")
    public ResponseEntity<?> addVisibleVdmsByVendorOrganisationId(@RequestParam String loggedInUser, @PathVariable String organisation_id, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.addVisibleVdmsByVendorOrganisationId(organisation_id, email, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/user/{email}/visible/vdms")
    public ResponseEntity<?> deleteVisibleVdmsByEmail(@RequestParam String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.deleteVisibleVdmsByEmail(email, loggedInUser, httpServletRequest);
    }

    @GetMapping("/user/{email}/visible/vdms")
    public ResponseEntity<?> getVisibleVdmsByEmail(@RequestParam String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.getVisibleVdmsByEmail(email, loggedInUser, httpServletRequest);
    }

    @GetMapping("/user/{email}/visible/getVisibleVdmsId")
    public ResponseEntity<?> getVisibleVdmsIdByEmail(@RequestParam String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.getVisibleVdmsIdByEmail(email, loggedInUser, httpServletRequest);
    }
}