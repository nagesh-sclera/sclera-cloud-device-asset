package io.sclera.controller.frontend;

import io.sclera.dto.ProxyProfileDTO;
import io.sclera.service.ProxyProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


@RequestMapping("/api")
@RestController
public class ProxyProfileController {

    @Autowired
    private ProxyProfileService proxyProfileService;


    @GetMapping("/organisation/{customer_org_id}/proxy/profiles")
    public ResponseEntity<?> getProxyProfileByCustomerOrganisationId(@RequestParam String loggedInUser, @PathVariable String customer_org_id, HttpServletRequest httpServletRequest) {
        return proxyProfileService.getProxyProfileByCustomerOrganisationId(customer_org_id, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdms_id}/proxyProfile")
    public ResponseEntity<?> getProxyProfileByVdmsId(@PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return proxyProfileService.getVdmsProxyProfileByVdmsId(vdms_id, httpServletRequest);
    }

    @GetMapping("/admin/{admin_email}/proxy/profiles")
    public ResponseEntity<?> getGlobalProxyProfiles(@RequestParam String loggedInUser, @PathVariable String admin_email, HttpServletRequest httpServletRequest) {
        return proxyProfileService.getGlobalProxyProfiles(admin_email, loggedInUser, httpServletRequest);
    }

    @PostMapping("/organisation/{customer_org_id}/proxy/profile")
    public ResponseEntity<?> addProxyProfileByCustomerOrganisationId(@RequestParam String loggedInUser, @PathVariable String customer_org_id, @RequestBody ProxyProfileDTO proxyProfileDTO, HttpServletRequest httpServletRequest) {
        return proxyProfileService.addProxyProfileByCustomerOrganisationId(customer_org_id, proxyProfileDTO, loggedInUser, httpServletRequest);
    }

    @PostMapping("/admin/{admin_email}/addpropxyprofile")
    public ResponseEntity<?> addGlobalProxyProfileByAdminEmail(@RequestParam String loggedInUser, @PathVariable String admin_email, @RequestBody ProxyProfileDTO proxyProfileDTO, HttpServletRequest httpServletRequest) {
        return proxyProfileService.addGlobalProxyProfileByAdminEmail(admin_email, proxyProfileDTO, loggedInUser, httpServletRequest);
    }

    @PutMapping("/admin/{admin_email}/proxy/profile/{id}/updateproxyprofile")
    public ResponseEntity<?> updateGlobalProxyProfileByProxyProfileId(@RequestParam String loggedInUser, @PathVariable String admin_email, @PathVariable String id, @RequestBody ProxyProfileDTO proxyProfileDTO, HttpServletRequest httpServletRequest) {
        return proxyProfileService.updateGlobalProxyProfileByProxyProfileId(admin_email, proxyProfileDTO, id, loggedInUser, httpServletRequest);
    }

    @PutMapping("/organisation/{customer_org_id}/proxy/profile/{id}")
    public ResponseEntity<?> updateProxyProfileByProxyProfileId(@RequestParam String loggedInUser, @PathVariable String customer_org_id, @PathVariable String id, @RequestBody ProxyProfileDTO proxyProfileDTO, HttpServletRequest httpServletRequest) {
        return proxyProfileService.updateProxyProfileByProxyProfileId(customer_org_id, id, proxyProfileDTO, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/organisation/{customer_org_id}/proxy/profile/{id}")
    public ResponseEntity<?> deleteProxyProfileByProxyProfileId(@RequestParam String loggedInUser, @PathVariable String customer_org_id, @PathVariable String id, HttpServletRequest httpServletRequest) {
        return proxyProfileService.deleteProxyProfileByProxyProfileId(customer_org_id, id, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/admin/{admin_email}/proxy/profile/{id}")
    public ResponseEntity<?> deleteGlobalProxyProfileByProxyProfileId(@RequestParam String loggedInUser, @PathVariable String admin_email, @PathVariable String id, HttpServletRequest httpServletRequest) {
        return proxyProfileService.deleteGlobalProxyProfileByProxyProfileId(admin_email, id, loggedInUser, httpServletRequest);
    }


}
