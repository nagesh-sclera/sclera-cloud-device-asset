package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/socket")
public class SocketController {

    @GetMapping("/socketDeviceCount")
    public void socketDeviceCount() {
        // no-op
    }

    @GetMapping("/sockerDeviceCountByDocker")
    public void sockerDeviceCountByDocker(
            @RequestParam(required = false) String dockername,
            @RequestParam(required = false) String assignee) {
        // no-op
    }

    @GetMapping("/socketAiCallLogHistoryUpdate")
    public void socketAiCallLogHistoryUpdate(@RequestParam(required = false) String id) {
        // no-op
    }

    @GetMapping("/socketAiCallLogOngoingHistoryUpdate")
    public void socketAiCallLogOngoingHistoryUpdate(@RequestParam(required = false) String id) {
        // no-op
    }

    @GetMapping("/socketDeviceStatus")
    public void socketDeviceStatus(@RequestParam(required = false) String dto) {
        // no-op
    }

    @GetMapping("/socketOnlineDevice")
    public void socketOnlineDevice(@RequestParam(required = false) String deviceId) {
        // no-op
    }

    @GetMapping("/socketOfflineDevice")
    public void socketOfflineDevice(@RequestParam(required = false) String deviceId) {
        // no-op
    }

    @GetMapping("/socketDeviceUpdate")
    public void socketDeviceUpdate(@RequestParam(required = false) String devices) {
        // no-op
    }

    @GetMapping("/updateDeviceInterfaceStatus")
    public void updateDeviceInterfaceStatus(
            @RequestParam(required = false) String dto,
            @RequestParam(required = false) String a,
            @RequestParam(required = false) String b) {
        // no-op
    }

    @GetMapping("/socketMeasuringInstrumentSensorValueUpdate")
    public void socketMeasuringInstrumentSensorValueUpdate(
            @RequestParam(required = false) String deviceId) {
        // no-op
    }

    @GetMapping("/socketDockerInterfaceStatus")
    public void socketDockerInterfaceStatus(
            @RequestParam(required = false) String interfaceName,
            @RequestParam(required = false) String interfaceStatus,
            @RequestParam(required = false) String networkOrigin) {
        // no-op
    }
}
