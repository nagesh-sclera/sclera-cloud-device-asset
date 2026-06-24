package io.sclera.controller.frontend;

import io.sclera.service.VdmsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api")
public class ScleraAgentController {

    @Autowired
    private VdmsService vdmsService;

    @GetMapping(value = "/organisations/{orgId}/user/{email}/getScleraAgentVdmsInfo")
    public ResponseEntity<?> getScleraAgentVdmsInfo(@PathVariable String orgId, @PathVariable String email, @RequestParam String key,
                                                    @RequestParam int pageNo, @RequestParam int pageSize, HttpServletRequest httpServletRequest) {
        return vdmsService.getScleraAgentVdmsInfo(orgId, email, pageNo, pageSize, key, httpServletRequest);
    }
}
