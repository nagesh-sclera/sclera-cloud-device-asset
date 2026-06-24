package io.sclera.controller.frontend;

import io.sclera.dto.VersionDTO;
import io.sclera.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.FileNotFoundException;


@RestController
public class VersionController {

    @Autowired
    private VersionService versionService;

    // Triggered by production team through script
    //whitelisted endpoint
    @GetMapping(value = "/getvdmsversion")
    public VersionDTO getVdmsVersion(HttpServletRequest httpServletRequest) {
        return versionService.getVdmsVersion(httpServletRequest);
    }

    @PutMapping(value = "/api/updateVdmsVersion")
    public ResponseEntity<?> updateVdmsVersion(@RequestBody String version, HttpServletRequest httpServletRequest) {
        return versionService.updateVdmsVersion(version, httpServletRequest);
    }


    @GetMapping(value = "/api/downloadsclera")
    public ResponseEntity<InputStreamResource> downloadScleraApp(HttpServletRequest httpServletRequest) throws FileNotFoundException {
        return versionService.downloadScleraApp(httpServletRequest);
    }

    @GetMapping(value = "/api/vdms/{vdmsId}/getvdmsversion")
    public ResponseEntity<?> getScleraResourceFile(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return versionService.getScleraResourceFile(vdmsId, httpServletRequest);
    }

}
