package io.sclera.controller.frontend;

import io.sclera.service.ScleraFXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;


@RequestMapping("/api")
@RestController
public class ScleraFXController {

    @Autowired
    private ScleraFXService scleraFXService;

    @GetMapping("/sclerafx/versions")
    public ResponseEntity<?> getAllScleraFXVersions(@RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return scleraFXService.getAllScleraFXVersions(loggedInUser, httpServletRequest);
    }

    @PostMapping("/sclerafx/os/{os}")
    public ResponseEntity<?> uploadScleraToCloudByOS(@RequestParam String loggedInUser, @RequestParam("file") MultipartFile sclera_app, @PathVariable String os, HttpServletRequest httpServletRequest) throws IOException {
        return scleraFXService.uploadScleraToCloudByOS(sclera_app, os, loggedInUser, httpServletRequest);
    }

    @GetMapping("/sclerafx/email/{email}/os/{os}")
    public ResponseEntity<?> getScleraFXByOS(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String os, HttpServletRequest httpServletRequest) throws IOException {
        return scleraFXService.getScleraFXByOS(email, os, loggedInUser, httpServletRequest);
    }


}
