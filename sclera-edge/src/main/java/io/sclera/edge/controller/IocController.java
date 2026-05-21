package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ioc")
public class IocController {

    @GetMapping("/sendDeviceAlertDataIOC")
    public void sendDeviceAlertDataIOC(
            @RequestParam(required = false) String deviceConditionsDTO,
            @RequestParam(required = false) String deviceAlert,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertProfile,
            @RequestParam(required = false) String timestamp) {
        // no-op
    }

    @GetMapping("/sendDigitalTwinData")
    public void sendDigitalTwinData(@RequestParam(required = false) String deviceIds) {
        // no-op
    }

    @GetMapping("/sendSensorValueDataToIOC")
    public void sendSensorValueDataToIOC(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String sensorValue) {
        // no-op
    }
}
