package io.sclera.client;

import com.alibaba.fastjson.JSONArray;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.NfcDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-integrations microservice (AP-C2).
 *
 * Replaces {@code io.sclera.service.NfcService}.
 * All methods return documented safe defaults on sidecar failure.
 */
@Component
public class NfcClient {

    private static final Logger log = LoggerFactory.getLogger(NfcClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public NfcClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code NfcService#getDeviceIdsTaggedToNfc}. Returns empty JSONArray on failure. */
    public JSONArray getDeviceIdsTaggedToNfc(String nfcId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("nfcId", nfcId);
        try {
            dapr.invokeMethod(APP_ID, "nfc/getDeviceIdsTaggedToNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.getDeviceIdsTaggedToNfc failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code NfcService#getNfcsByDeviceIds}. Returns empty set on failure. */
    public Set<NfcDTO> getNfcsByDeviceIds(Set<String> deviceIds) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceIds", deviceIds);
        try {
            dapr.invokeMethod(APP_ID, "nfc/getNfcsByDeviceIds", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.getNfcsByDeviceIds failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /** Mirrors {@code NfcService#getQrNfcCountByDeviceId}. Returns 0 on failure. */
    public Integer getQrNfcCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "nfc/getQrNfcCountByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.getQrNfcCountByDeviceId failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    /** Mirrors {@code NfcService#getLocationIdsTaggedToNfc}. Returns empty JSONArray on failure. */
    public JSONArray getLocationIdsTaggedToNfc(String nfcId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("nfcId", nfcId);
        try {
            dapr.invokeMethod(APP_ID, "nfc/getLocationIdsTaggedToNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.getLocationIdsTaggedToNfc failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code NfcService#getNfcsByLocationIds}. Returns empty set on failure. */
    public Set<NfcDTO> getNfcsByLocationIds(Set<String> locationIds) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locationIds", locationIds);
        try {
            dapr.invokeMethod(APP_ID, "nfc/getNfcsByLocationIds", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.getNfcsByLocationIds failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /** Mirrors {@code NfcService#syncAllNfc}. */
    public void syncAllNfc(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "nfc/syncAllNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.syncAllNfc failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code NfcService#syncNfc}. */
    public void syncNfc(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "nfc/syncNfc", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("NfcClient.syncNfc failed; swallowing: {}", e.getMessage());
        }
    }
}
