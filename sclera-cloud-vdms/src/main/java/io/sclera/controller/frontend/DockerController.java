package io.sclera.controller.frontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.DockerDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.DockerService;
import io.sclera.util.ScleraUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;


@RequestMapping("/api")
@RestController
public class DockerController {

    @Autowired
    private DockerService dockerservice;

    @GetMapping("/user/{username}/vdms/{vdms_id}/docker/{docker_name}")
    public ResponseEntity<?> getDockerInfoByVdmsIdAndDockerName(@RequestParam String loggedInUser, @PathVariable String username, @PathVariable String vdms_id, @PathVariable String docker_name, HttpServletRequest httpServletRequest) {
        return dockerservice.getDockerInfoByVdmsIdAndDockerName(username, vdms_id, docker_name, httpServletRequest);
    }

    @GetMapping("/user/{username}/vdms/{vdms_id}/dockers")
    public ResponseEntity<?> getAllDockersByVdmsId(@RequestParam String loggedInUser, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return dockerservice.getAllDockersByVdmsId(username, vdms_id, httpServletRequest);
    }

    @GetMapping("/user/{email}/vdms/{vdms_id}/docker-names")
    public ResponseEntity<?> getNetworkNamesByVdmsId(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return dockerservice.getNetworkNamesByVdmsId(email, vdms_id, httpServletRequest);
    }

    @PostMapping("/user/{username}/vdms/{vdms_id}/docker")
    public ResponseEntity<?> addDockerByVdmsId(@RequestParam String loggedInUser, @RequestBody DockerDTO dockerdto, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return dockerservice.addDockerByVdmsId(dockerdto, username, vdms_id, httpServletRequest);
    }

    //    TS
    @DeleteMapping("/vdms/{vdms_id}/docker/{docker_name}/deletenetwork")
    public ResponseEntity<?> deleteDockerByDockerNameAndVdmsId(@RequestParam String loggedInUser, @PathVariable String vdms_id, @PathVariable String docker_name, HttpServletRequest httpServletRequest) {
        return dockerservice.deleteDockerByDockerNameAndVdmsId(vdms_id, docker_name, httpServletRequest);
    }

    //    TS
    @PostMapping("/user/vdms/docker/upsert")
    public ResponseEntity<?> upsertDockerByVdmsId(@RequestParam String loggedInUser, @RequestBody String dockerdto, HttpServletRequest httpServletRequest) {
        return dockerservice.upsertDockerByVdmsId(dockerdto, loggedInUser, httpServletRequest);
    }

    @GetMapping("/user/{email}/vendor/{vendor_email}/organisation-id")
    public ResponseEntity<?> inviteVendor(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vendor_email, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return dockerservice.inviteVendor(email, vendor_email, loggedInUser, httpServletRequest);
    }


    @GetMapping("/public/ip")
    public ResponseEntity<?> getPublicIp(HttpServletRequest httpServletRequest) {
        ResponseDTO responseDTO = ScleraUtils.generatePayload(httpServletRequest.getHeader("x-real-ip"), 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PutMapping("/user/{email}/vdms/{vdms_id}/docker/{name}/transfer/registered-vendor")
    public ResponseEntity<?> transferDockerToRegisteredVendorByVdmsIdAndDockerName(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String name, @RequestBody DockerDTO dockerdto, HttpServletRequest httpServletRequest) {
        return dockerservice.transferDockerToRegisteredVendorByVdmsIdAndDockerName(email, vdms_id, name, dockerdto, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{email}/vdms/{vdms_id}/docker/{name}/transfer/unregistered-vendor")
    public ResponseEntity<?> transferDockerToUnRegisteredVendorByVdmsIdAndDockerName(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String name, @RequestBody DockerDTO dockerdto, HttpServletRequest httpServletRequest) {
        return dockerservice.transferDockerToUnRegisteredVendorByVdmsIdAndDockerName(email, vdms_id, name, dockerdto, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{email}/vdms/{vdms_id}/docker/{name}/transfer/cancel")
    public ResponseEntity<?> cancelVendorTransferByVdmsIdAndDockerName(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return dockerservice.cancelVendorTransferByVdmsIdAndDockerName(email, vdms_id, name, loggedInUser, httpServletRequest);
    }

    @PutMapping("/organisation/{vendor_org_id}/vendor/{email}/vdms/{vdms_id}/docker/{name}/accept")
    public ResponseEntity<?> acceptCustomerRequestByVdmsIdAndDockerName(@RequestParam String loggedInUser, @PathVariable String vendor_org_id, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return dockerservice.acceptCustomerRequestByVdmsIdAndDockerName(vendor_org_id, email, vdms_id, name, loggedInUser, httpServletRequest);
    }

    @PutMapping("/organisation/{vendor_org_id}/vendor/{email}/vdms/{vdms_id}/docker/{name}/decline")
    public ResponseEntity<?> declineCustomerRequestByVdmsIdAndDockerName(@RequestParam String loggedInUser, @PathVariable String vendor_org_id, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return dockerservice.declineCustomerRequestByVdmsIdAndDockerName(vendor_org_id, email, vdms_id, name, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{email}/vdms/{vdms_id}/docker/proxy/profile/{id}")
    public ResponseEntity<?> modifyProxyProfileToDockerByVdmsIdAndNetworkName(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String id, @RequestBody Set<String> networks, HttpServletRequest httpServletRequest) {
        return dockerservice.modifyProxyProfileToDockerByVdmsIdAndNetworkName(email, vdms_id, id, networks, loggedInUser, httpServletRequest);
    }

    @PostMapping("/vdms/{vdms_id}/docker/syncDockerByVdmsId")
    public ResponseEntity<?> syncDockerByVdmsId(@RequestBody Set<DockerDTO> dockerDTOS, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return dockerservice.syncDockerByVdmsId(dockerDTOS, vdms_id, httpServletRequest);
    }

}