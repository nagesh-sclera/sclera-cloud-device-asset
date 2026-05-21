package io.sclera.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/deleteVendorsByOrganisationId")
  public void deleteVendorsByOrganisationId(@RequestParam(required=false) String vendorOrgId) {
    // no-op
  }

  @PostMapping("/insertVendors")
  public void insertVendors(@RequestBody String vendor) {
    // no-op
  }
}
