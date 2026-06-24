package io.sclera.controller.frontend;

import io.sclera.dto.IocDto;
import io.sclera.service.IocService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class IocController {

    @Autowired
    public IocService iocService;

    @GetMapping(value = "/user/{email}/getAllIocDetails")
    public ResponseEntity<?> getAllIocDetails(@PathVariable String email, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.getAllIocDetails(email, loggedInUser, httpServletRequest);

    }

    @GetMapping(value = "/user/{email}/getIocDetailsByIocId")
    public ResponseEntity<?> getIocDetailsByIocId(@PathVariable String email, @RequestParam String iocId, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.getIocDetailsByIocId(email, iocId, loggedInUser, httpServletRequest);

    }

    @GetMapping(value = "/organisation/{orgId}/user/{email}/getAllIocDetailsByOrgId")
    public ResponseEntity<?> getAllIocDetailsByOrgId(@PathVariable String orgId, @PathVariable String email, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.getAllIocDetailsByOrgId(email, orgId, loggedInUser, httpServletRequest);

    }


    @GetMapping(value = "/organisation/{orgId}/user/{email}/getIocDetailsByIocIdAndOrgId")
    public ResponseEntity<?> getIocDetailsByIocIdAndOrgId(@PathVariable String orgId, @PathVariable String email, @RequestParam String iocId, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.getIocDetailsByIocIdAndOrgId(email, orgId, iocId, loggedInUser, httpServletRequest);

    }

    @PostMapping(value = "/organisation/{orgId}/user/{email}/addIocData")
    public ResponseEntity<?> addIocData(@PathVariable String orgId, @PathVariable String email, @RequestBody IocDto iocData, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.addIocData(orgId, email, iocData, loggedInUser, httpServletRequest);

    }

    @PutMapping(value = "/organisation/{orgId}/user/{email}/editIocData")
    public ResponseEntity<?> editIocData(@PathVariable String orgId, @PathVariable String email, @RequestBody IocDto iocData, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.editIocData(orgId, email, iocData, loggedInUser, httpServletRequest);

    }

    @DeleteMapping(value = "/organisation/{orgId}/user/{email}/iocId/{iocId}/deleteIocData")
    public ResponseEntity<?> deleteIocData(@PathVariable String orgId, @PathVariable String email, @PathVariable String iocId, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.deleteIocData(orgId, email, iocId, loggedInUser, httpServletRequest);

    }

    @GetMapping(value = "/user/{email}/getAllOrgIdAndCompanyName")
    public ResponseEntity<?> getAllOrgIdAndCompanyName(@PathVariable String email, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return iocService.getAllOrgIdAndCompanyName(email, loggedInUser, httpServletRequest);

    }
}
