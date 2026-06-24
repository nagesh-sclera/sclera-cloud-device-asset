package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.DevUIDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;


@Validated
@RestController
public class DevUIDController {

    @Autowired
    private DevUIDService devUIDService;


    //TS
    @PutMapping(value = "/updateDevUID")
    public void upsertDevUID(@RequestBody String udi, HttpServletRequest httpServletRequest) {
        devUIDService.upsertDevUID(udi, httpServletRequest);
    }

    @GetMapping("/api/getDevUIDS")
    public ResponseEntity<?> getAllDevUIDs(@RequestParam(required = false, defaultValue = "all") String key,
                                           @RequestParam(required = false, defaultValue = "last_seen") String sort,
                                           @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                           @RequestParam(defaultValue = "500") @Min(1) @Max(1000) int pagesize,
                                           @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return devUIDService.getAllDevUIDs(key, sort, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/api/deleteDevUIDS")
    public ResponseEntity<?> deleteDevUIDs(@RequestBody Set<String> devUIDs, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return devUIDService.deleteDevUIDs(devUIDs, loggedInUser, httpServletRequest);
    }


}
