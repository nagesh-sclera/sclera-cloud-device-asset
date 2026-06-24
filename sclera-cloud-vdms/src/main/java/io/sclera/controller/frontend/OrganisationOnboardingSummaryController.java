package io.sclera.controller.frontend;

import io.sclera.service.OrganisationOnboardingSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrganisationOnboardingSummaryController {

    @Autowired
    private OrganisationOnboardingSummaryService organisationOnboardingSummaryService;

    @GetMapping(value = "/organisation/{organisation_id}/organisationOnboardingSummary")
    private ResponseEntity<?> getOrganisationOnboardingSummaryByOrganisationId(@PathVariable String organisation_id) {
        return organisationOnboardingSummaryService.getOrganisationOnboardingSummaryByOrganisationId(organisation_id);
    }
}
