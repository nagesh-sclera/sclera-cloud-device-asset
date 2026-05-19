package io.sclera.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.identity.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/vendoradmin")
public class VendorAdminController {
  @GetMapping("/deleteVendorsByOrganisationId")
  public void deleteVendorsByOrganisationId(@RequestParam String vendorOrgId) {
    // no-op
  }

  @GetMapping("/insertVendors")
  public void insertVendors(@RequestParam String vendor) {
    // no-op
  }
}
