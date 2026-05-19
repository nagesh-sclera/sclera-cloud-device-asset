package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.PowerSourceConnectionsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-integrations microservice (AP-C2).
 *
 * Replaces the stub {@code io.sclera.Repository.ConnectedDevicesRepository}
 * (13 StubLog methods).
 * Void methods swallow exceptions; list-returning methods return empty collections.
 *
 * NOTE: Set/body params are silently dropped under GET-only skeleton routing
 * (needs POST upgrade when scaffold supports verbs).
 */
@Component
public class ConnectedDevicesClient {

    private static final Logger log = LoggerFactory.getLogger(ConnectedDevicesClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public ConnectedDevicesClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code ConnectedDevicesRepository#addConnectedDevices}. */
    public void addConnectedDevices(String deviceId, String connectedDeviceId, String type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("connectedDeviceId", connectedDeviceId);
        payload.put("type", type);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/addConnectedDevices", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.addConnectedDevices failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code ConnectedDevicesRepository#getConnectedDevicesSpecifications}. Returns empty list on failure. */
    public List<ConnectedDevicesDTO> getConnectedDevicesSpecifications(String deviceId, Integer page, Integer size) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("page", page);
        payload.put("size", size);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getConnectedDevicesSpecifications", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getConnectedDevicesSpecifications failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#getConnectedSpecificationsByDeviceId}. Returns empty list on failure. */
    public List<ConnectedDevicesDTO> getConnectedSpecificationsByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getConnectedSpecificationsByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getConnectedSpecificationsByDeviceId failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#getAllInputConnectedSpecifications}. Returns empty list on failure. */
    public List<ConnectedDevicesDTO> getAllInputConnectedSpecifications(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getAllInputConnectedSpecifications", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getAllInputConnectedSpecifications failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#getAllOutputConnectedSpecifications}. Returns empty list on failure. */
    public List<ConnectedDevicesDTO> getAllOutputConnectedSpecifications(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getAllOutputConnectedSpecifications", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getAllOutputConnectedSpecifications failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#untagPowerSource}. */
    public void untagPowerSource(String specificationsId, String connectedSpecificationsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("specificationsId", specificationsId);
        payload.put("connectedSpecificationsId", connectedSpecificationsId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/untagPowerSource", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.untagPowerSource failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code ConnectedDevicesRepository#untagDevice}. */
    public void untagDevice(String specificationsId, String connectedSpecificationsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("specificationsId", specificationsId);
        payload.put("connectedSpecificationsId", connectedSpecificationsId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/untagDevice", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.untagDevice failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code ConnectedDevicesRepository#untagPowerSourceByDeviceId}. */
    public void untagPowerSourceByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/untagPowerSourceByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.untagPowerSourceByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code ConnectedDevicesRepository#getPowerSourceTopologyForDevice}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     * Returns empty list on failure.
     */
    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyForDevice(Set<String> deviceIds) {
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getPowerSourceTopologyForDevice", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getPowerSourceTopologyForDevice failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#getAllConnectedDevicesForLoadCalculation}. Returns empty list on failure. */
    public List<ConnectedDevicesDTO> getAllConnectedDevicesForLoadCalculation(String specificationId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("specificationId", specificationId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getAllConnectedDevicesForLoadCalculation", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getAllConnectedDevicesForLoadCalculation failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#getPowerSourceTopologyConnectionsCount}. Returns 0 on failure. */
    public Integer getPowerSourceTopologyConnectionsCount() {
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getPowerSourceTopologyConnectionsCount", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getPowerSourceTopologyConnectionsCount failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    /** Mirrors {@code ConnectedDevicesRepository#getPowerSourceTopologyByPagination}. Returns empty list on failure. */
    public List<PowerSourceConnectionsDTO> getPowerSourceTopologyByPagination(Integer pageSize, Integer offset) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pageSize", pageSize);
        payload.put("offset", offset);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/getPowerSourceTopologyByPagination", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.getPowerSourceTopologyByPagination failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** Mirrors {@code ConnectedDevicesRepository#deleteConnectedDevicesBySpecificationId}. */
    public void deleteConnectedDevicesBySpecificationId(String specificationsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("specificationsId", specificationsId);
        try {
            dapr.invokeMethod(APP_ID, "connectedDevices/deleteConnectedDevicesBySpecificationId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ConnectedDevicesClient.deleteConnectedDevicesBySpecificationId failed; swallowing: {}", e.getMessage());
        }
    }
}
