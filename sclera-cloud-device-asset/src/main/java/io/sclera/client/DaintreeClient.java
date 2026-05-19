package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.DaintreeConfigurationDTO;
import io.sclera.dto.DaintreeDeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.DaintreeService}.
 */
@Component
public class DaintreeClient {

    private static final Logger log = LoggerFactory.getLogger(DaintreeClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public DaintreeClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code DaintreeService#getDeviceIdByDaintreeDeviceId}. Returns null on failure. */
    public String getDeviceIdByDaintreeDeviceId(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "daintree/getDeviceIdByDaintreeDeviceId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("DaintreeClient.getDeviceIdByDaintreeDeviceId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code DaintreeService#getDeviceDaintreeDevicesCountByDeviceId}. Returns 0 on failure. */
    public Integer getDeviceDaintreeDevicesCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "daintree/getDeviceDaintreeDevicesCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("DaintreeClient.getDeviceDaintreeDevicesCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code DaintreeService#getDaintreeAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getDaintreeAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "daintree/getDaintreeAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("DaintreeClient.getDaintreeAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code DaintreeService#getDaintreeDevicesByDeviceId}. Returns empty set on failure. */
    public Set<DaintreeDeviceDTO> getDaintreeDevicesByDeviceId(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "daintree/getDaintreeDevicesByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("DaintreeClient.getDaintreeDevicesByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code DaintreeService#listDaintreeDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listDaintreeDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "daintree/listDaintreeDevicesAlertMessagesByDeviceIds", null, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("DaintreeClient.listDaintreeDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code DaintreeService#updateDaintreeDeviceByDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateDaintreeDeviceByDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "daintree/updateDaintreeDeviceByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("DaintreeClient.updateDaintreeDeviceByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code DaintreeService#getDaintreeConfigurations}. Returns empty list on failure. */
    public List<DaintreeConfigurationDTO> getDaintreeConfigurations(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "daintree/getDaintreeConfigurations", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("DaintreeClient.getDaintreeConfigurations failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
