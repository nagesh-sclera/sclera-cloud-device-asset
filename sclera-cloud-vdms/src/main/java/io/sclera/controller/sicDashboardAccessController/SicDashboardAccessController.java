package io.sclera.controller.sicDashboardAccessController;


import io.sclera.service.VdmsVisibilityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sicDashboardAccess")
public class SicDashboardAccessController {

    @Autowired
    private VdmsVisibilityService vdmsVisibilityService;

    @GetMapping("/user/{email}/visible/vdms")
    public ResponseEntity<?> getVisibleVdmsByEmail(@PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsVisibilityService.getVisibleVdmsByUserEmail(email, httpServletRequest);
    }

}
