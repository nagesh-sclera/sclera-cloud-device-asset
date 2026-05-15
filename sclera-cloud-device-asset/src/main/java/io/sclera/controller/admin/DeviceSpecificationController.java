package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.dto.DeviceSpecificationDTO;
import io.sclera.dto.RemoteAgentServerDetailsDTO;
import io.sclera.service.DeviceInstalledAppsService;
import io.sclera.service.DeviceSpecificationService;
import io.sclera.service.RemoteDesktopSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/api")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceSpecificationController {

    private final DeviceSpecificationService deviceSpecificationService;

    private final DeviceInstalledAppsService deviceInstalledAppsService;

    private final RemoteDesktopSessionService remoteDesktopSessionService;

    public DeviceSpecificationController(DeviceSpecificationService deviceSpecificationService,
                                         DeviceInstalledAppsService deviceInstalledAppsService, RemoteDesktopSessionService remoteDesktopSessionService) {
        this.deviceSpecificationService = deviceSpecificationService;
        this.deviceInstalledAppsService = deviceInstalledAppsService;
        this.remoteDesktopSessionService = remoteDesktopSessionService;
    }

    @PostMapping("/devicespecification")
    public ResponseEntity<String> receiveFullSpec(@RequestBody JSONObject body, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        String deviceId = deviceSpecificationService.saveFullJson(body, httpServletRequest, assignee);
        if (deviceId == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(deviceId);
    }


    @PostMapping("/deltadevicespecs")
    public ResponseEntity<String> receiveDeltaJson(@RequestBody JSONObject json) {
        String message = deviceSpecificationService.upsertDeltaJson(json);
        if (message == null) {
            return ResponseEntity.badRequest().body("Invalid input or device not found");
        }
        return ResponseEntity.ok(message);
    }


    @GetMapping("/devicespecification/{deviceId}")
    public ResponseEntity<DeviceSpecificationDTO> getDeviceSpec(@PathVariable String deviceId) {
        DeviceSpecificationDTO dto = deviceSpecificationService.getSpecDtoByDeviceId(deviceId);
        if (dto == null) {
            return ResponseEntity.ok(DeviceSpecificationDTO.builder().build());
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/installedapps/{deviceId}")
    public ResponseEntity<List<DeviceInstalledAppsDTO>> getInstalledApps(@PathVariable String deviceId) {
        List<DeviceInstalledAppsDTO> apps = deviceInstalledAppsService.getInstalledAppDTOs(deviceId);
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/systemupdates/{deviceId}")
    public ResponseEntity<JSONArray> getSystemUpdates(@PathVariable String deviceId) {
        JSONArray systemUpdates = deviceSpecificationService.getSystemUpdatesArrayByDeviceId(deviceId);
        return ResponseEntity.ok(systemUpdates);
    }


    @PostMapping("/remotesupport")
    public ResponseEntity<?> updateRemoteConnectFlag(@RequestBody JSONObject json) {
        return remoteDesktopSessionService.updateRemoteConnectFlag(json);
    }


    @GetMapping("/remotesupport/user/{username}/device/{deviceId}")
    public ResponseEntity<?> getRemoteConnectInfo(@PathVariable String deviceId, @PathVariable String username) {
        return remoteDesktopSessionService.getRemoteConnectInfo(deviceId,username);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<RemoteAgentServerDetailsDTO> getRemoteSessions(@PathVariable String id) {
        RemoteAgentServerDetailsDTO sessions = remoteDesktopSessionService.getRemoteSessionDetails(id);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/session/approval")
    public String updateAcknowledge(@RequestBody JSONObject json) {
        remoteDesktopSessionService.updateAcknowledge(json);
        return "Successfully updated session approval";
    }

}