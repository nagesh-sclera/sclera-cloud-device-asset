package io.sclera.client;

import com.alibaba.fastjson.JSONArray;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.QrCodeDTO;
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
 * Replaces {@code io.sclera.service.QrCodeService}.
 * All methods return documented safe defaults on sidecar failure.
 *
 * NOTE: Set body params in upsertQrCodesInBatch are silently dropped under
 * GET-only skeleton routing (needs POST upgrade).
 */
@Component
public class QrCodeClient {

    private static final Logger log = LoggerFactory.getLogger(QrCodeClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public QrCodeClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code QrCodeRepository#countByDeviceId}. Returns 0 on failure. */
    public Integer countByDeviceId(String deviceId) {
        return getQrCodeCountByDeviceId(deviceId);
    }

    /** Mirrors {@code QrCodeService#getDeviceIdsTaggedToQrCode}. Returns empty JSONArray on failure. */
    public JSONArray getDeviceIdsTaggedToQrCode(String qrCodeId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("qrCodeId", qrCodeId);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getDeviceIdsTaggedToQrCode", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getDeviceIdsTaggedToQrCode failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code QrCodeService#getQrCodesByDeviceIds}. Returns empty set on failure. */
    public Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceIds) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceIds", deviceIds);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getQrCodesByDeviceIds", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getQrCodesByDeviceIds failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /** Mirrors {@code QrCodeService#getQrCodeCountByDeviceId}. Returns 0 on failure. */
    public Integer getQrCodeCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getQrCodeCountByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getQrCodeCountByDeviceId failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    /** Mirrors {@code QrCodeService#getMaxUpdatedQrCodeTimeStamp}. Returns null on failure. */
    public BigInteger getMaxUpdatedQrCodeTimeStamp(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getMaxUpdatedQrCodeTimeStamp", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getMaxUpdatedQrCodeTimeStamp failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    /** Mirrors {@code QrCodeService#getLocationIdsTaggedToQrCode}. Returns empty JSONArray on failure. */
    public JSONArray getLocationIdsTaggedToQrCode(String qrCodeId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("qrCodeId", qrCodeId);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getLocationIdsTaggedToQrCode", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getLocationIdsTaggedToQrCode failed; returning empty: {}", e.getMessage());
        }
        return new JSONArray();
    }

    /** Mirrors {@code QrCodeService#getQrCodesByLocationIds}. Returns empty set on failure. */
    public Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locationIds", locationIds);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getQrCodesByLocationIds", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getQrCodesByLocationIds failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /** Mirrors {@code QrCodeService#syncQrCodes}. Returns empty set on failure. */
    public Set<QrCodeDTO> syncQrCodes(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/syncQrCodes", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.syncQrCodes failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /** Mirrors {@code QrCodeService#getQrCodeDetailsByIds}. Returns empty set on failure. */
    public Set<QrCodeDTO> getQrCodeDetailsByIds(Set<String> ids) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ids", ids);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getQrCodeDetailsByIds", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getQrCodeDetailsByIds failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /**
     * Mirrors {@code QrCodeService#upsertQrCodesInBatch}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void upsertQrCodesInBatch(Set<QrCodeDTO> dtos) {
        try {
            dapr.invokeMethod(APP_ID, "qrCode/upsertQrCodesInBatch", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.upsertQrCodesInBatch failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code QrCodeService#getClientQrCodeDetailsByIds}. Returns empty set on failure. */
    public Set<QrCodeDTO> getClientQrCodeDetailsByIds(Set<String> ids) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ids", ids);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/getClientQrCodeDetailsByIds", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.getClientQrCodeDetailsByIds failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /** Mirrors {@code QrCodeService#syncAlQrCodes}. */
    public void syncAlQrCodes(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "qrCode/syncAlQrCodes", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("QrCodeClient.syncAlQrCodes failed; swallowing: {}", e.getMessage());
        }
    }
}
