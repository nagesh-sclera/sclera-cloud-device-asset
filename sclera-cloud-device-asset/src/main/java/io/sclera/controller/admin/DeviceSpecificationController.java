package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.dto.DeviceSpecificationDTO;
import io.sclera.dto.RemoteAgentServerDetailsDTO;
import io.sclera.service.DeviceInstalledAppsService;
import io.sclera.service.DeviceSpecificationService;
import io.sclera.client.RemoteDesktopSessionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST endpoints for device hardware/software specifications, installed applications
 * and remote desktop support sessions.
 * Delegates persistence and business logic to {@link DeviceSpecificationService} and
 * {@link DeviceInstalledAppsService}, and remote-support operations to {@link RemoteDesktopSessionClient}.
 */
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceSpecificationController {

    private static final Logger log = LoggerFactory.getLogger(DeviceSpecificationController.class);

    private final DeviceSpecificationService deviceSpecificationService;

    private final DeviceInstalledAppsService deviceInstalledAppsService;

    private final RemoteDesktopSessionClient remoteDesktopSessionService;

    public DeviceSpecificationController(DeviceSpecificationService deviceSpecificationService,
                                         DeviceInstalledAppsService deviceInstalledAppsService, RemoteDesktopSessionClient remoteDesktopSessionService) {
        this.deviceSpecificationService = deviceSpecificationService;
        this.deviceInstalledAppsService = deviceInstalledAppsService;
        this.remoteDesktopSessionService = remoteDesktopSessionService;
    }

    /**
     * Saves a full device specification JSON payload and returns the resolved device id.
     *
     * @param body                the full device specification JSON payload
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @param assignee            assignee filter for the specification (default "all")
     * @return the saved device id, or an empty body if none was produced
     */
    @PostMapping("/devicespecification")
    public ResponseEntity<String> receiveFullSpec(@RequestBody JSONObject body, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        log.info("receiveFullSpec assignee={}", assignee);
        try {
            String deviceId = deviceSpecificationService.saveFullJson(body, httpServletRequest, assignee);
            if (deviceId == null) {
                return ResponseEntity.ok().body(null);
            }
            return ResponseEntity.ok(deviceId);
        } catch (Exception e) {
            log.error("receiveFullSpec failed assignee={}: {}", assignee, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Upserts a delta (partial) device specification JSON payload.
     *
     * @param json the delta device specification JSON payload
     * @return a status message, or a bad-request response if the input is invalid or the device is not found
     */
    @PostMapping("/deltadevicespecs")
    public ResponseEntity<String> receiveDeltaJson(@RequestBody JSONObject json) {
        log.info("receiveDeltaJson called");
        try {
            String message = deviceSpecificationService.upsertDeltaJson(json);
            if (message == null) {
                return ResponseEntity.badRequest().body("Invalid input or device not found");
            }
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("receiveDeltaJson failed: {}", e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Returns the device specification for the given device.
     *
     * @param deviceId the device whose specification is requested
     * @return the device specification, or an empty specification if none exists
     */
    @GetMapping("/devicespecification/{deviceId}")
    public ResponseEntity<DeviceSpecificationDTO> getDeviceSpec(@PathVariable String deviceId) {
        log.info("getDeviceSpec deviceId={}", deviceId);
        try {
            DeviceSpecificationDTO dto = deviceSpecificationService.getSpecDtoByDeviceId(deviceId);
            if (dto == null) {
                return ResponseEntity.ok(DeviceSpecificationDTO.builder().build());
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("getDeviceSpec failed deviceId={}: {}", deviceId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the list of installed applications for the given device.
     *
     * @param deviceId the device whose installed applications are requested
     * @return the installed applications for the device
     */
    @GetMapping("/installedapps/{deviceId}")
    public ResponseEntity<List<DeviceInstalledAppsDTO>> getInstalledApps(@PathVariable String deviceId) {
        log.info("getInstalledApps deviceId={}", deviceId);
        try {
            List<DeviceInstalledAppsDTO> apps = deviceInstalledAppsService.getInstalledAppDTOs(deviceId);
            return ResponseEntity.ok(apps);
        } catch (Exception e) {
            log.error("getInstalledApps failed deviceId={}: {}", deviceId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the system updates for the given device.
     *
     * @param deviceId the device whose system updates are requested
     * @return the system updates as a JSON array
     */
    @GetMapping("/systemupdates/{deviceId}")
    public ResponseEntity<JSONArray> getSystemUpdates(@PathVariable String deviceId) {
        log.info("getSystemUpdates deviceId={}", deviceId);
        try {
            JSONArray systemUpdates = deviceSpecificationService.getSystemUpdatesArrayByDeviceId(deviceId);
            return ResponseEntity.ok(systemUpdates);
        } catch (Exception e) {
            log.error("getSystemUpdates failed deviceId={}: {}", deviceId, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Updates the remote-connect flag for a remote support session.
     *
     * @param json the JSON payload describing the remote-connect flag change
     * @return the result of the remote-connect flag update
     */
    @PostMapping("/remotesupport")
    public ResponseEntity<?> updateRemoteConnectFlag(@RequestBody JSONObject json) {
        log.info("updateRemoteConnectFlag called");
        try {
            return remoteDesktopSessionService.updateRemoteConnectFlag(json);
        } catch (Exception e) {
            log.error("updateRemoteConnectFlag failed: {}", e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Returns remote-connect information for the given device and user.
     *
     * @param deviceId the device whose remote-connect info is requested
     * @param username the user requesting the remote-connect info
     * @return the remote-connect information
     */
    @GetMapping("/remotesupport/device/{deviceId}")
    public ResponseEntity<?> getRemoteConnectInfo(@PathVariable String deviceId, @RequestParam String username) {
        log.info("getRemoteConnectInfo deviceId={} username={}", deviceId, username);
        try {
            return remoteDesktopSessionService.getRemoteConnectInfo(deviceId,username);
        } catch (Exception e) {
            log.error("getRemoteConnectInfo failed deviceId={}: {}", deviceId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns remote agent server session details for the given session id.
     *
     * @param id the remote session id
     * @return the remote agent server session details
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<RemoteAgentServerDetailsDTO> getRemoteSessions(@PathVariable String id) {
        log.info("getRemoteSessions id={}", id);
        try {
            RemoteAgentServerDetailsDTO sessions = remoteDesktopSessionService.getRemoteSessionDetails(id);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("getRemoteSessions failed id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates the acknowledgement/approval status of a remote support session.
     *
     * @param json the JSON payload describing the session approval change
     * @return a confirmation message
     */
    @PostMapping("/session/approval")
    public String updateAcknowledge(@RequestBody JSONObject json) {
        log.info("updateAcknowledge called");
        try {
            remoteDesktopSessionService.updateAcknowledge(json);
            return "Successfully updated session approval";
        } catch (Exception e) {
            log.error("updateAcknowledge failed: {}", e.getMessage(), e);
            throw e;
        }
    }

}