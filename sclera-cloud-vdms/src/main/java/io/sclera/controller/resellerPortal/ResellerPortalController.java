package io.sclera.controller.resellerPortal;


import io.sclera.service.VdmsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/resellerPortal")
@Validated
@RestController
public class ResellerPortalController {

    @Autowired
    private VdmsService vdmsService;

    @GetMapping("/vdms/{vdms_id}/getResellerPortalVdmsInfoByVdmsId")
    public ResponseEntity<?> getResellerPortalVdmsInfoByVdmsId(@PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.getResellerPortalVdmsInfoByVdmsId(vdms_id, httpServletRequest);
    }
}
