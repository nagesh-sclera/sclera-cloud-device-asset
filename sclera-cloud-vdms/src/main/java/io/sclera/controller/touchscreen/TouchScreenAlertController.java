package io.sclera.controller.touchscreen;


import io.sclera.dto.TouchscreenAlertDTO;
import io.sclera.service.TouchScreenAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/touchscreen")
public class TouchScreenAlertController {

    @Autowired
    private TouchScreenAlertService touchScreenAlertService;

    @PostMapping(value = "/vdms/{vdmsId}/alertUser")
    public ResponseEntity<?> alertUser(@PathVariable String vdmsId, @RequestBody TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) throws IOException {
        return touchScreenAlertService.alertUser(vdmsId, touchscreenAlertDTO, httpServletRequest);
    }

    //Global Inspection Report
    @PostMapping(value = "/vdms/{vdmsId}/sendDownloadEmail")
    public ResponseEntity<?> sendDownloadEmail(@PathVariable String vdmsId,
                                               @RequestParam(name = "files", required = false) List<MultipartFile> files,
                                               @RequestParam(name = "fileType") String fileType, @RequestParam(name = "body") String body,
                                               HttpServletRequest httpServletRequest) throws IOException {
        return touchScreenAlertService.sendDownloadEmail(vdmsId, files, fileType, body, httpServletRequest);
    }

    @GetMapping(value = "/validatePreSignLink")
    public ResponseEntity<?> getRedirectionLink(@RequestParam String redirectUrl) {
        HttpHeaders headers = touchScreenAlertService.getRedirectionLink(redirectUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
