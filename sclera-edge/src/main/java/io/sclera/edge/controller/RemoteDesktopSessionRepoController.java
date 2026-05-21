package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remotedesktopsessionrepo")
public class RemoteDesktopSessionRepoController {

    @GetMapping("/deleteByDeviceId")
    public void deleteByDeviceId(@RequestParam(required = false) String deviceId) {
        // no-op
    }
}
