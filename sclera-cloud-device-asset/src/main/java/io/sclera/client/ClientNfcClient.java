package io.sclera.client;

import com.alibaba.fastjson.JSONArray;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-integrations microservice (AP-C2).
 *
 * Replaces {@code io.sclera.service.ClientNfcService}.
 * All methods return documented safe defaults on sidecar failure.
 */
@Component
public class ClientNfcClient {

    private static final Logger log = LoggerFactory.getLogger(ClientNfcClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public ClientNfcClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code ClientNfcService#getDeviceIdsTaggedToClientNfc}. Returns empty JSONArray on failure. */
    public JSONArray getDeviceIdsTaggedToClientNfc(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "clientNfc/getDeviceIdsTaggedToClientNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientNfcClient.getDeviceIdsTaggedToClientNfc failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code ClientNfcService#getClientNfcCountByDeviceId}. Returns 0 on failure. */
    public Integer getClientNfcCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "clientNfc/getClientNfcCountByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientNfcClient.getClientNfcCountByDeviceId failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    /** Mirrors {@code ClientNfcService#getLocationIdsTaggedToClientNfc}. Returns empty JSONArray on failure. */
    public JSONArray getLocationIdsTaggedToClientNfc(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "clientNfc/getLocationIdsTaggedToClientNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientNfcClient.getLocationIdsTaggedToClientNfc failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code ClientNfcService#syncAllClientNfc}. */
    public void syncAllClientNfc(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "clientNfc/syncAllClientNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientNfcClient.syncAllClientNfc failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code ClientNfcService#syncClientNfc}. */
    public void syncClientNfc(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "clientNfc/syncClientNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientNfcClient.syncClientNfc failed; swallowing: {}", e.getMessage());
        }
    }
}
