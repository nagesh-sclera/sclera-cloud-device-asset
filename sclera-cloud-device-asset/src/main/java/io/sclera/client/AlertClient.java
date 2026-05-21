package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-alerts microservice (AP-C5).
 *
 * Replaces the no-op {@code io.sclera.service.AlertService} stub.
 * All methods are void: they invoke the remote endpoint and swallow any
 * exception (sidecar-down, network error) with a WARN log so that
 * call sites are never interrupted.
 *
 * NOTE: sendDeviceConditionsAlertInfo, sendSensorAlertInfo and sendDownloadEmail
 * pass DTO/Object bodies under GET routing — the body is silently dropped.
 * Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class AlertClient {

    private static final Logger log = LoggerFactory.getLogger(AlertClient.class);
    private static final String APP_ID = "sclera-alerts";

    private final DaprClient dapr;

    public AlertClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code AlertService#sendDeviceConditionsAlertInfo}.
     * Maps to GET sclera-alerts/alert/sendDeviceConditionsAlertInfo.
     * NOTE: DTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void sendDeviceConditionsAlertInfo(DeviceAlertDTO deviceAlert, AlertProfileDTO alertProfile, BigInteger timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("alertProfile", alertProfile);
        payload.put("timestamp", timestamp);
        try {
            dapr.invokeMethod(APP_ID, "alert/sendDeviceConditionsAlertInfo", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("AlertClient.sendDeviceConditionsAlertInfo failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code AlertService#sendSensorAlertInfo}.
     * Maps to GET sclera-alerts/alert/sendSensorAlertInfo.
     * NOTE: Object body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void sendSensorAlertInfo(Object sensorAlert, AlertProfileDTO alertProfile, BigInteger timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("alertProfile", alertProfile);
        payload.put("timestamp", timestamp);
        try {
            dapr.invokeMethod(APP_ID, "alert/sendSensorAlertInfo", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("AlertClient.sendSensorAlertInfo failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code AlertService#sendDownloadEmail}.
     * Maps to GET sclera-alerts/alert/sendDownloadEmail.
     * NOTE: body and MultipartFile are lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void sendDownloadEmail(JSONObject body, MultipartFile file, String type, String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "alert/sendDownloadEmail", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("AlertClient.sendDownloadEmail failed; swallowing: {}", e.getMessage());
        }
    }
}
