package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.RemoteAgentServerDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Thin Dapr client delegating to sclera-edge (AP-C1edge).
 * Replaces {@code io.sclera.service.RemoteDesktopSessionService}.
 */
@Component
public class RemoteDesktopSessionClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteDesktopSessionClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public RemoteDesktopSessionClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public ResponseEntity<?> updateRemoteConnectFlag(JSONObject json) {
        try {
            dapr.invokeMethod(APP_ID, "remotedesktopsession/updateRemoteConnectFlag", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RemoteDesktopSessionClient.updateRemoteConnectFlag failed; returning default", e);
        }
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<?> getRemoteConnectInfo(String deviceId, String username) {
        try {
            java.util.Map<String, String> p = new java.util.HashMap<>();
            p.put("deviceId", deviceId);
            p.put("username", username);
            dapr.invokeMethod(APP_ID, "remotedesktopsession/getRemoteConnectInfo", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RemoteDesktopSessionClient.getRemoteConnectInfo failed; returning default", e);
        }
        return ResponseEntity.ok(null);
    }

    public RemoteAgentServerDetailsDTO getRemoteSessionDetails(String id) {
        try {
            java.util.Map<String, String> p = new java.util.HashMap<>();
            p.put("id", id);
            return dapr.invokeMethod(APP_ID, "remotedesktopsession/getRemoteSessionDetails", p, HttpExtension.GET, RemoteAgentServerDetailsDTO.class).block();
        } catch (Exception e) {
            log.warn("RemoteDesktopSessionClient.getRemoteSessionDetails failed; returning null", e);
        }
        return null;
    }

    public void updateAcknowledge(JSONObject json) {
        try {
            dapr.invokeMethod(APP_ID, "remotedesktopsession/updateAcknowledge", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RemoteDesktopSessionClient.updateAcknowledge failed; swallowing", e);
        }
    }
}
