package io.sclera.controller.mastervendor;

import io.sclera.service.DockerService;
import io.sclera.service.VdmsVisibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


@RequestMapping(value = "/api/vendor")
@RestController
public class MasterVendorController {

    @Autowired
    private DockerService dockerservice;

    @Autowired
    private VdmsVisibilityService vdmsvisibilityService;

    @PutMapping(value = "/organisation/{vendor_org_id}/vdms/{vdms_id}/docker/{name}/sync")
    public ResponseEntity<?> updateVdmsSyncByVendorOrgIdVdmsIdAndDockerName(@PathVariable String vendor_org_id, @PathVariable String vdms_id, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return dockerservice.updateVdmsSyncByVendorOrgIdVdmsIdAndDockerName(vendor_org_id, vdms_id, name, httpServletRequest);
    }

    @PostMapping(value = "/organisation/{organisation_id}/user/{email}/visible/vdms")
    public ResponseEntity<?> addVisibleVdmsByVendorOrganisationId(@RequestParam String loggedInUser,@PathVariable String organisation_id, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.addVisibleVdmsByVendorOrganisationId(organisation_id, email,loggedInUser,httpServletRequest);
    }

    @PostMapping(value = "/organisation/{orgId}/vendor/{email}/visible/vdms")
    public ResponseEntity<?> addVisibleVdmsByVendorOrganisationIdAndEmail(@RequestParam String loggedInUser,@PathVariable String orgId, @PathVariable String email, @RequestBody String body ,HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.addVisibleVdmsByVendorOrganisationIdAndEmail(orgId, email, body ,loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/{vendor_org_id}/vdms/sync")
    public ResponseEntity<?> addVdmsSyncByVendorOrganisationId(@PathVariable String vendor_org_id, HttpServletRequest httpServletRequest) {
        return dockerservice.addVdmsSyncByVendorOrganisationId(vendor_org_id, httpServletRequest);
    }

    @DeleteMapping(value = "/user/{email}/visible/vdms")
    public ResponseEntity<?> deleteVisibleVdmsByEmail(@RequestParam String loggedInUser,@PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.deleteVisibleVdmsByEmail(email,loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/user/vdms/{vdmsId}/docker/upsert")
    public ResponseEntity<?> upsertDockerByVdmsId(@PathVariable String vdmsId,@RequestBody String dockerdto, HttpServletRequest httpServletRequest) {
        return dockerservice.upsertDockerByVdmsIdAndDockerName(dockerdto,vdmsId, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/docker/{name}/tagMasterVendorToNetwork")
    public void tagMasterVendorToNetwork(@RequestParam String loggedInUser,@PathVariable String vdmsId ,@PathVariable String name ,@RequestBody String vendorOrgId, HttpServletRequest httpServletRequest){
        dockerservice.tagMasterVendorToNetwork(vdmsId ,name ,vendorOrgId,loggedInUser,httpServletRequest);
    }

    @PutMapping(value = "/organisation/{orgId}/untagVendorByOrganisationId")
    public void untagVendorByOrganisationId(@PathVariable String orgId ,HttpServletRequest httpServletRequest){
        dockerservice.untagVendorByOrganisationId(orgId ,httpServletRequest);
    }

    @GetMapping(value = "/user/{email}/visible/vdms")
    public ResponseEntity<?> getVisibleVdmsByEmail(@PathVariable String email,HttpServletRequest httpServletRequest) {
        return vdmsvisibilityService.getVisibleVdmsByEmailForVendor(email,httpServletRequest);
    }

}
