package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remotedesktopsession")
public class RemoteDesktopSessionController {

    @GetMapping("/updateRemoteConnectFlag")
    public String updateRemoteConnectFlag(@RequestParam(required = false) String json) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getRemoteConnectInfo")
    public String getRemoteConnectInfo(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String username) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getRemoteSessionDetails")
    public String getRemoteSessionDetails(@RequestParam(required = false) String id) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/updateAcknowledge")
    public void updateAcknowledge(@RequestParam(required = false) String json) {
        // no-op
    }
}
