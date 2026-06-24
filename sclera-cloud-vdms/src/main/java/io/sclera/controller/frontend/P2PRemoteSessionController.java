package io.sclera.controller.frontend;

import io.sclera.service.P2PRemoteSessionService;
import io.sclera.util.SocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Random;


@RequestMapping("/api")
@RestController
public class P2PRemoteSessionController {

    @Autowired
    private P2PRemoteSessionService p2premotesessionService;


    @Autowired
    private SocketUtils socketUtils;

    @GetMapping("/vendor/{vendor_email}/vdms/{vdms_id}/p2p/session")
    public ResponseEntity<?> getPortAndSessionIdByVdmsIdAndVendorEmail(@RequestParam String loggedInUser, @PathVariable String vendor_email, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return p2premotesessionService.getPortAndSessionIdByVdmsIdAndVendorEmail(vendor_email, vdms_id, loggedInUser, httpServletRequest);
    }


    @GetMapping(value = "/random")
    public Integer random() {
        Random r = new Random();
        int low = 1;
        int high = 65535;
        Integer result;
        do {
            result = r.nextInt(high - low) + low;
        } while ((high - low) == 0);
        return result;
    }

    @GetMapping(value = "/websocket")
    public void test() {
        socketUtils.invokeWebSocketEndpoint("/topic/test", "Hello");
    }


}
