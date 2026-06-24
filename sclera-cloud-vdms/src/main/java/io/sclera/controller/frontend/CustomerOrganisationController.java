package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.CustomerOrganisationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organisations/{orgId}")
public class CustomerOrganisationController {

    @Autowired
    private CustomerOrganisationService customerOrganisationService;

    @GetMapping("/email/{email}/getVdmsFeaturesByOrgId")
    public ResponseEntity<ResponseDTO> getVdmsFeaturesByOrgId(@PathVariable String orgId,
                                                              @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return customerOrganisationService.getVdmsFeaturesByOrgId(orgId, loggedInUser, httpServletRequest);
    }
}
