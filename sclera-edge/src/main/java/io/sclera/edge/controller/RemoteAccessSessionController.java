package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/remoteaccesssession")
public class RemoteAccessSessionController {

    @GetMapping("/getAllRemoteAccessSessions")
    public List<String> getAllRemoteAccessSessions() {
        return Defaults.emptyList();
    }

    @GetMapping("/stopRemoteAccess")
    public void stopRemoteAccess(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String networkName,
            @RequestParam(required = false) String ipAddress) {
        // no-op
    }
}
