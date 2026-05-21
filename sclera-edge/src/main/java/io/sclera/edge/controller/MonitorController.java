package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @GetMapping("/deviceUpsertbyId")
    public void deviceUpsertbyId(
            @RequestParam(required = false) String dockerName,
            @RequestParam(required = false) String type) {
        // no-op
    }

    @GetMapping("/insertDevicesHistory")
    public void insertDevicesHistory(@RequestParam(required = false) String dockerName) {
        // no-op
    }
}
