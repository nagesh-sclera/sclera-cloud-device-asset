package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.BuildFilesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buildFiles")
public class BuildFilesController {

    @Autowired
    private BuildFilesService buildFilesService;

    @GetMapping
    private ResponseEntity<ResponseDTO> getBuildFiles(@RequestParam String type, HttpServletRequest httpServletRequest) {
        return buildFilesService.getBuildFiles(type, httpServletRequest);
    }

    @PutMapping
    private ResponseEntity<ResponseDTO> updateBuildFiles(@RequestParam String type, @RequestBody String version, HttpServletRequest httpServletRequest) {
        return buildFilesService.updateBuildFiles(type, version, httpServletRequest);
    }
}
