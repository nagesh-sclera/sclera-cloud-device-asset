package io.sclera.controller;


import io.sclera.service.TrustedOriginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TrustedOriginController {

    @Autowired
    private TrustedOriginService trustedOriginService;

    @PostMapping(value = "/addOrigin")
    public ResponseEntity<?> addOrigin(@RequestBody List<String> origin,
                                       HttpServletRequest httpServletRequest) {
        return trustedOriginService.addOrigin(origin, httpServletRequest);
    }

    @GetMapping(value = "/getOrigins")
    public ResponseEntity<?> getAllOrigins(HttpServletRequest httpServletRequest) {
        return trustedOriginService.getAllOrigins(httpServletRequest);
    }

    @DeleteMapping(value = "/deleteOrigin")
    public ResponseEntity<?> deleteOrigin(@RequestBody List<String> origin, HttpServletRequest httpServletRequest) {
        return trustedOriginService.deleteOrigin(origin, httpServletRequest);
    }

}
