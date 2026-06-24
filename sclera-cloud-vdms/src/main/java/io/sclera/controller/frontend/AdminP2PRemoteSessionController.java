package io.sclera.controller.frontend;

import io.sclera.service.AdminP2PRemoteSessionService;
import io.sclera.util.SocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


@RequestMapping("/api")
@RestController
public class AdminP2PRemoteSessionController {

    @Autowired
    private AdminP2PRemoteSessionService adminP2PRemoteSessionService;


    @GetMapping("/admin/{admin_email}/devUID/{devUID}/port/{access_port}/startvdmsaccess")
    public ResponseEntity<?> getPortAndSessionIdByAdminEmailAndDevUID(@PathVariable String admin_email, @PathVariable String devUID, @PathVariable String access_port, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return adminP2PRemoteSessionService.getPortAndSessionIdByAdminEmailAndDevUID(admin_email, devUID, access_port, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/admin/{admin_email}/devUID/clearsessions")
    public ResponseEntity<?> clearSessionsByAdminEmail(@PathVariable String admin_email, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return adminP2PRemoteSessionService.clearSessionsByAdminEmail(admin_email, loggedInUser, httpServletRequest);
    }

}
