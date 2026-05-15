package io.sclera.vdms.controller;

import io.sclera.vdms.client.ScleraCloudDeviceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes read-only endpoints that vdms-service proxies back to sclera-cloud-device-asset
 * via Dapr service invocation — completing the 2-way communication loop.
 *
 * Routes:
 *   GET /vdms/asset/{username}/{vdmsId}/{dockerName}/device-count
 *   GET /vdms/asset/{username}/{vdmsId}/{dockerName}/devices
 */
@RestController
@RequestMapping("/vdms/asset")
public class AssetProxyController {

    private final ScleraCloudDeviceClient assetClient;

    public AssetProxyController(ScleraCloudDeviceClient assetClient) {
        this.assetClient = assetClient;
    }

    /**
     * GET /vdms/asset/{username}/{vdmsId}/{dockerName}/device-count
     *
     * Calls device-asset via Dapr: GET /user/{u}/vdms/{v}/docker/{d}/getdevicecount
     * Returns: {"total":N, "online":N, ...} as returned by device-asset.
     */
    @GetMapping("/{username}/{vdmsId}/{dockerName}/device-count")
    public ResponseEntity<Map<String, Object>> getDeviceCount(
            @PathVariable String username,
            @PathVariable String vdmsId,
            @PathVariable String dockerName) {

        Map<String, Object> result = assetClient.getDeviceCount(username, vdmsId, dockerName);
        if (result == null) {
            return ResponseEntity.status(502).body(Map.of("error", "no response from device-asset"));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /vdms/asset/{username}/{vdmsId}/{dockerName}/devices
     *
     * Calls device-asset via Dapr: GET /user/{u}/vdms/{v}/docker/{d}/devices
     * Returns the device array exactly as device-asset returns it.
     */
    @GetMapping("/{username}/{vdmsId}/{dockerName}/devices")
    public ResponseEntity<Object> getDevices(
            @PathVariable String username,
            @PathVariable String vdmsId,
            @PathVariable String dockerName) {

        Object result = assetClient.getDevices(username, vdmsId, dockerName);
        if (result == null) {
            return ResponseEntity.status(502).body(Map.of("error", "no response from device-asset"));
        }
        return ResponseEntity.ok(result);
    }
}
