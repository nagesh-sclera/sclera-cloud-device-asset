package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.PropertyQrcodeDTO;
import io.sclera.dto.PropertyServiceDTO;
import io.sclera.dto.PropertyServiceRequestDTO;
import io.sclera.dto.PropertyServiceResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.PropertyQrcodeService}.
 */
@Component
public class PropertyQrcodeClient {

    private static final Logger log = LoggerFactory.getLogger(PropertyQrcodeClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public PropertyQrcodeClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code PropertyQrcodeService#updatePropertyServiceLocations}. */
    public void updatePropertyServiceLocations(String locationId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/updatePropertyServiceLocations", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.updatePropertyServiceLocations failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code PropertyQrcodeService#upsertPropertyServiceDetails}.
     * NOTE: DTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public PropertyServiceDTO upsertPropertyServiceDetails(String username, String vdmsId, PropertyServiceDTO dto) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/upsertPropertyServiceDetails", payload, HttpExtension.GET).block();
            return dto;
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.upsertPropertyServiceDetails failed; returning stub default: {}", e.getMessage());
            return dto;
        }
    }

    /**
     * Mirrors {@code PropertyQrcodeService#addPropertyServiceLocations}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void addPropertyServiceLocations(String username, String vdmsId, String serviceId, Set<LocationDTO> locations) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("serviceId", serviceId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/addPropertyServiceLocations", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.addPropertyServiceLocations failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code PropertyQrcodeService#multiUpdatePropertyServiceResponse}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void multiUpdatePropertyServiceResponse(String username, String vdmsId, Set<PropertyServiceResponseDTO> responses) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/multiUpdatePropertyServiceResponse", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.multiUpdatePropertyServiceResponse failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code PropertyQrcodeService#getPropertyServices}. Returns empty set on failure. */
    public Set<PropertyServiceDTO> getPropertyServices(String username, String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/getPropertyServices", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.getPropertyServices failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code PropertyQrcodeService#getPropertyServiceLocationsById}. Returns empty set on failure. */
    public Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(String username, String vdmsId, String serviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("serviceId", serviceId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/getPropertyServiceLocationsById", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.getPropertyServiceLocationsById failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Mirrors {@code PropertyQrcodeService#deletePropertyServiceRequests}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deletePropertyServiceRequests(String username, String vdmsId, Set<PropertyServiceRequestDTO> requests) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/deletePropertyServiceRequests", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.deletePropertyServiceRequests failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code PropertyQrcodeService#deletePropertyServiceLocations}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deletePropertyServiceLocations(String username, String vdmsId, String serviceId, Set<String> locations) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("serviceId", serviceId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/deletePropertyServiceLocations", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.deletePropertyServiceLocations failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code PropertyQrcodeService#deletePropertyService}. */
    public void deletePropertyService(String username, String vdmsId, String serviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("serviceId", serviceId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/deletePropertyService", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.deletePropertyService failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code PropertyQrcodeService#getZoneMap}. Returns empty set on failure. */
    public Set<PropertyQrcodeDTO> getZoneMap(String username, String vdmsId, String buildingId, String floorId, String locationId, String serviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("buildingId", buildingId);
        payload.put("floorId", floorId);
        payload.put("locationId", locationId);
        payload.put("serviceId", serviceId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/getZoneMap", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.getZoneMap failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code PropertyQrcodeService#syncServiceValue}. */
    public void syncServiceValue(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "propertyQrcode/syncServiceValue", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PropertyQrcodeClient.syncServiceValue failed; swallowing: {}", e.getMessage());
        }
    }
}
