package io.sclera.controller.vdmshealth;

import com.alibaba.fastjson2.JSONObject;
import io.sclera.service.VdmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
public class VdmsHealthController {

    @Autowired
    private VdmsService vdmsService;

    @GetMapping(value = "/getVdmsStatus")
    public ResponseEntity<?> getVdmsStatus(@RequestParam String loggedInUser, @RequestParam String key, HttpServletRequest httpServletRequest) {
        return vdmsService.getVdmsStatus(key, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/getStatus")
    public ResponseEntity<?> getStatus(@RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return vdmsService.getStatus(loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/getVdms")
    public ResponseEntity<?> getVdms(@RequestParam String loggedInUser, @RequestParam String status,@RequestParam String key, HttpServletRequest httpServletRequest) {
        return vdmsService.getVdms(status,key, loggedInUser, httpServletRequest);
    }
}
