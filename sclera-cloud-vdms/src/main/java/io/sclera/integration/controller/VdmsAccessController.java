package io.sclera.integration.controller;

import io.sclera.integration.service.VdmsAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class VdmsAccessController {

    @Autowired
    public VdmsAccessService vdmsAccessService;


    @GetMapping(value = "/users/{username}/vdms")
    public ResponseEntity<?> getVdmsAccessByUsername(@PathVariable String username, @RequestParam(required = false, defaultValue = "all") String key,
                                                     @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "100") int pageSize,
                                                     HttpServletRequest httpServletRequest) {
        return vdmsAccessService.getVdmsAccessByUsername(username, key, pageNo, pageSize, httpServletRequest);
    }


}
