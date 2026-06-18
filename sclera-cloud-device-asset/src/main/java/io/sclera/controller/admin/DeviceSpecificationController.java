package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.dto.DeviceSpecificationDTO;
import io.sclera.dto.RemoteAgentServerDetailsDTO;
import io.sclera.service.DeviceInstalledAppsService;
import io.sclera.service.DeviceSpecificationService;
import io.sclera.client.RemoteDesktopSessionClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Device Specifications", description = "Manage device hardware/software specifications, installed applications and remote desktop support sessions.")
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
    @Operation(summary = "Save full device specification",
            description = "Saves a full device specification JSON payload and returns the resolved device id. Returns an empty body when no device id is produced.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specification saved"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/devicespecification")
    public ResponseEntity<String> receiveFullSpec(
            @RequestBody JSONObject body, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee filter for the specification") @RequestParam(defaultValue = "all") String assignee) {
        log.info("receiveFullSpec assignee={}", assignee);
        String deviceId = deviceSpecificationService.saveFullJson(body, httpServletRequest, assignee);
        if (deviceId == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(deviceId);
    }


    /**
     * Upserts a delta (partial) device specification JSON payload.
     *
     * @param json the delta device specification JSON payload
     * @return a status message, or a bad-request response if the input is invalid or the device is not found
     */
    @Operation(summary = "Upsert delta device specification",
            description = "Upserts a delta (partial) device specification JSON payload. Returns a bad-request response when the input is invalid or the device is not found.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delta specification upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid input or device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/deltadevicespecs")
    public ResponseEntity<String> receiveDeltaJson(@RequestBody JSONObject json) {
        log.info("receiveDeltaJson called");
        String message = deviceSpecificationService.upsertDeltaJson(json);
        if (message == null) {
            return ResponseEntity.badRequest().body("Invalid input or device not found");
        }
        return ResponseEntity.ok(message);
    }


    /**
     * Returns the device specification for the given device.
     *
     * @param deviceId the device whose specification is requested
     * @return the device specification, or an empty specification if none exists
     */
    @Operation(summary = "Get device specification by device id",
            description = "Returns the device specification for the given device. Returns an empty specification when none exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specification returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/devicespecification/{deviceId}")
    public ResponseEntity<DeviceSpecificationDTO> getDeviceSpec(
            @Parameter(description = "Device whose specification is requested") @PathVariable String deviceId) {
        log.info("getDeviceSpec deviceId={}", deviceId);
        DeviceSpecificationDTO dto = deviceSpecificationService.getSpecDtoByDeviceId(deviceId);
        if (dto == null) {
            return ResponseEntity.ok(DeviceSpecificationDTO.builder().build());
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns the list of installed applications for the given device.
     *
     * @param deviceId the device whose installed applications are requested
     * @return the installed applications for the device
     */
    @Operation(summary = "Get installed applications by device id",
            description = "Returns the list of installed applications for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Installed applications returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/installedapps/{deviceId}")
    public ResponseEntity<List<DeviceInstalledAppsDTO>> getInstalledApps(
            @Parameter(description = "Device whose installed applications are requested") @PathVariable String deviceId) {
        log.info("getInstalledApps deviceId={}", deviceId);
        List<DeviceInstalledAppsDTO> apps = deviceInstalledAppsService.getInstalledAppDTOs(deviceId);
        return ResponseEntity.ok(apps);
    }

    /**
     * Returns the system updates for the given device.
     *
     * @param deviceId the device whose system updates are requested
     * @return the system updates as a JSON array
     */
    @Operation(summary = "Get system updates by device id",
            description = "Returns the system updates for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "System updates returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/systemupdates/{deviceId}")
    public ResponseEntity<JSONArray> getSystemUpdates(
            @Parameter(description = "Device whose system updates are requested") @PathVariable String deviceId) {
        log.info("getSystemUpdates deviceId={}", deviceId);
        JSONArray systemUpdates = deviceSpecificationService.getSystemUpdatesArrayByDeviceId(deviceId);
        return ResponseEntity.ok(systemUpdates);
    }


    /**
     * Updates the remote-connect flag for a remote support session.
     *
     * @param json the JSON payload describing the remote-connect flag change
     * @return the result of the remote-connect flag update
     */
    @Operation(summary = "Update remote-connect flag",
            description = "Updates the remote-connect flag for a remote support session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remote-connect flag updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/remotesupport")
    public ResponseEntity<?> updateRemoteConnectFlag(@RequestBody JSONObject json) {
        log.info("updateRemoteConnectFlag called");
        return remoteDesktopSessionService.updateRemoteConnectFlag(json);
    }


    /**
     * Returns remote-connect information for the given device and user.
     *
     * @param deviceId the device whose remote-connect info is requested
     * @param username the user requesting the remote-connect info
     * @return the remote-connect information
     */
    @Operation(summary = "Get remote-connect info by device id",
            description = "Returns remote-connect information for the given device and user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remote-connect info returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/remotesupport/device/{deviceId}")
    public ResponseEntity<?> getRemoteConnectInfo(
            @Parameter(description = "Device whose remote-connect info is requested") @PathVariable String deviceId,
            @Parameter(description = "User requesting the remote-connect info") @RequestParam String username) {
        log.info("getRemoteConnectInfo deviceId={} username={}", deviceId, username);
        return remoteDesktopSessionService.getRemoteConnectInfo(deviceId, username);
    }

    /**
     * Returns remote agent server session details for the given session id.
     *
     * @param id the remote session id
     * @return the remote agent server session details
     */
    @Operation(summary = "Get remote session details by id",
            description = "Returns remote agent server session details for the given session id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session details returned"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/sessions/{id}")
    public ResponseEntity<RemoteAgentServerDetailsDTO> getRemoteSessions(
            @Parameter(description = "Remote session id") @PathVariable String id) {
        log.info("getRemoteSessions id={}", id);
        RemoteAgentServerDetailsDTO sessions = remoteDesktopSessionService.getRemoteSessionDetails(id);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Updates the acknowledgement/approval status of a remote support session.
     *
     * @param json the JSON payload describing the session approval change
     * @return a confirmation message
     */
    @Operation(summary = "Update session approval",
            description = "Updates the acknowledgement/approval status of a remote support session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session approval updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/session/approval")
    public String updateAcknowledge(@RequestBody JSONObject json) {
        log.info("updateAcknowledge called");
        remoteDesktopSessionService.updateAcknowledge(json);
        return "Successfully updated session approval";
    }

}
