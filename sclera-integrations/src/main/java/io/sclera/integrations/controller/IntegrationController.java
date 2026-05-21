package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.integrations.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/integration")
public class IntegrationController {
  @PostMapping("/updateCustomerOrgByIntegrationId")
  public void updateCustomerOrgByIntegrationId(@RequestParam(required=false) String customerOrgId) {
    // no-op
  }
}
