package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsUserActivityDTO;
import io.sclera.service.VdmsUserActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vdms-user-activity")
public class VdmsUserActivityController {

    private final VdmsUserActivityService vdmsUserActivityService;

    public VdmsUserActivityController(VdmsUserActivityService vdmsUserActivityService) {
        this.vdmsUserActivityService = vdmsUserActivityService;
    }

    /**
     * Endpoint to add a new VDMS user activity.
     *
     * @param vdmsUserActivityDTO the DTO containing user activity details
     * @return ResponseEntity with the result of the operation
     */
    @PostMapping
    public ResponseEntity<ResponseDTO> addVdmsUserActivity(@RequestBody VdmsUserActivityDTO vdmsUserActivityDTO, HttpServletRequest httpServletRequest) {
        return vdmsUserActivityService.addVdmsUserActivity(vdmsUserActivityDTO, httpServletRequest);
    }
}
