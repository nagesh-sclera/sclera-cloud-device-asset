package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.VdmsFeatureService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vdms/{vdmsId}/vdmsFeature")
public class VdmsFeatureController {

    @Autowired
    private VdmsFeatureService vdmsFeatureService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getVdmsFeatureDetailsByVdmsId(@PathVariable String vdmsId,
                                                                     @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                     HttpServletRequest httpServletRequest) {
        return vdmsFeatureService.getVdmsFeatureDetailsByVdmsId(vdmsId, loggedInUser, httpServletRequest);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> addVdmsFeatureByVdmsAndFeatureIds(@PathVariable String vdmsId, @RequestBody List<String> featureIds,
                                                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                         HttpServletRequest httpServletRequest) {
        return vdmsFeatureService.addVdmsFeatureByVdmsAndFeatureIds(vdmsId, featureIds, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO> deleteVdmsFeatureByVdmsId(@PathVariable String vdmsId,
                                                                 @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                 HttpServletRequest httpServletRequest) {
        return vdmsFeatureService.deleteVdmsFeatureByVdmsId(vdmsId, loggedInUser, httpServletRequest);
    }

}
