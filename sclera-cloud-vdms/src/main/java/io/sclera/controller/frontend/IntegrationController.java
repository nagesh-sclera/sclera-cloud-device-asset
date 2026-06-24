package io.sclera.controller.frontend;

import io.sclera.dto.IntegrationDTO;
import io.sclera.service.IntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Set;


@RequestMapping("/api")
@RestController
public class IntegrationController {

    @Autowired
    private IntegrationService integrationService;

    @GetMapping("/getIntegrations")
    public ResponseEntity<?> getDistinctIntegrations(@RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return integrationService.getDistinctIntegrations(loggedInUser, httpServletRequest);
    }

    @GetMapping("/integration/getIntegrationNames")
    public ResponseEntity<?> getIntegrationNames(@RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return integrationService.getIntegrationNames(loggedInUser, httpServletRequest);
    }

    @GetMapping("/getIntegrationsByCategory")
    public ResponseEntity<?> getIntegrationsByCategory(@RequestParam String loggedInUser, @RequestParam(required = false, defaultValue = "all") Set<String> categories, HttpServletRequest httpServletRequest) {
        return integrationService.getIntegrationsByCategory(categories, loggedInUser, httpServletRequest);
    }

    @GetMapping("/integration/{id}/getIntegration")
    public ResponseEntity<?> getIntegrationDataByIntegrationId(@RequestParam String loggedInUser, @PathVariable String id, HttpServletRequest httpServletRequest) {
        return integrationService.getIntegrationDataByIntegrationId(id, loggedInUser, httpServletRequest);
    }

    @PostMapping("/upsertIntegration")
    public void upsertIntegration(@RequestParam String loggedInUser, @RequestBody IntegrationDTO integrationDTO, HttpServletRequest httpServletRequest) throws IOException {
        integrationService.upsertIntegration(integrationDTO, loggedInUser, httpServletRequest);
    }

}
