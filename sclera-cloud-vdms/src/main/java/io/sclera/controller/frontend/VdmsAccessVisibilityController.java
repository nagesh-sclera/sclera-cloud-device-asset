package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsAccessVisibilityDTO;
import io.sclera.service.VdmsAccessVisibilityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/user/{email}/vdms/access")
@RestController
public class VdmsAccessVisibilityController {

    @Autowired
    private VdmsAccessVisibilityService vdmsAccessVisibilityService;

    @PostMapping
    public ResponseEntity<ResponseDTO> replaceVdmsAccessVisibilityByEmail(@PathVariable String email, @RequestBody List<String> devUIds,
                                                                          @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return vdmsAccessVisibilityService.replaceVdmsAccessVisibilityByEmail(email, devUIds, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO> deleteVdmsAccessVisibilityByEmail(@PathVariable String email, @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return vdmsAccessVisibilityService.deleteVdmsAccessVisibilityByEmail(email, loggedInUser, httpServletRequest);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> getVdmsAccessVisibilityByEmail(@PathVariable String email, @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return vdmsAccessVisibilityService.getVdmsAccessVisibilityByEmail(email, loggedInUser, httpServletRequest);
    }
}
