package io.sclera.client;

import com.alibaba.fastjson.JSONArray;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ClientQrCodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-integrations microservice (AP-C2).
 *
 * Replaces {@code io.sclera.service.ClientQrCodeService}.
 * All methods return documented safe defaults on sidecar failure.
 *
 * NOTE: Set body params in upsertClientQrCodesInBatch are silently dropped
 * under GET-only skeleton routing (needs POST upgrade).
 */
@Component
public class ClientQrCodeClient {

    private static final Logger log = LoggerFactory.getLogger(ClientQrCodeClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public ClientQrCodeClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code ClientQrCodeRepository#countByDeviceId}. Returns 0 on failure. */
    public Integer countByDeviceId(String deviceId) {
        return getClientQrCodeCountByDeviceId(deviceId);
    }

    /** Mirrors {@code ClientQrCodeService#getDeviceIdsTaggedToClientQrCode}. Returns empty JSONArray on failure. */
    public JSONArray getDeviceIdsTaggedToClientQrCode(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/getDeviceIdsTaggedToClientQrCode", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.getDeviceIdsTaggedToClientQrCode failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code ClientQrCodeService#getClientQrCodeCountByDeviceId}. Returns 0 on failure. */
    public Integer getClientQrCodeCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/getClientQrCodeCountByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.getClientQrCodeCountByDeviceId failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    /** Mirrors {@code ClientQrCodeService#maxUpdatedClientQrCodeTimeStamp}. Returns null on failure. */
    public BigInteger maxUpdatedClientQrCodeTimeStamp(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/maxUpdatedClientQrCodeTimeStamp", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.maxUpdatedClientQrCodeTimeStamp failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    /** Mirrors {@code ClientQrCodeService#getLocationIdsTaggedToClientQrCode}. Returns empty JSONArray on failure. */
    public JSONArray getLocationIdsTaggedToClientQrCode(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/getLocationIdsTaggedToClientQrCode", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.getLocationIdsTaggedToClientQrCode failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code ClientQrCodeService#syncClientQrCodes}. Returns empty set on failure. */
    public Set<ClientQrCodeDTO> syncClientQrCodes(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/syncClientQrCodes", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.syncClientQrCodes failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /**
     * Mirrors {@code ClientQrCodeService#upsertClientQrCodesInBatch}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void upsertClientQrCodesInBatch(Set<ClientQrCodeDTO> dtos) {
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/upsertClientQrCodesInBatch", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.upsertClientQrCodesInBatch failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code ClientQrCodeService#syncAllClientQrCodes}. */
    public void syncAllClientQrCodes(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "clientQrCode/syncAllClientQrCodes", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ClientQrCodeClient.syncAllClientQrCodes failed; swallowing: {}", e.getMessage());
        }
    }
}
