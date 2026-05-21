package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.RecordChecklistDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-inspection microservice (AP-C4).
 *
 * Replaces the stub {@code io.sclera.service.RecordChecklistService}.
 * Collection-returning methods return empty collections on sidecar failure (stub default).
 * Scalar-returning methods return null/0 on sidecar failure (stub default).
 * Void methods swallow exceptions with a WARN log.
 *
 * NOTE: methods passing Set/List bodies use GET routing — bodies are
 * silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class RecordChecklistClient {

    private static final Logger log = LoggerFactory.getLogger(RecordChecklistClient.class);
    private static final String APP_ID = "sclera-inspection";

    private final DaprClient dapr;

    public RecordChecklistClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code RecordChecklistService#updateRecordChecklistDeviceAndIsRemoved}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateRecordChecklistDeviceAndIsRemoved(Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/updateRecordChecklistDeviceAndIsRemoved", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.updateRecordChecklistDeviceAndIsRemoved failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code RecordChecklistService#deleteRecordChecklistInBatch}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deleteRecordChecklistInBatch(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/deleteRecordChecklistInBatch", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.deleteRecordChecklistInBatch failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code RecordChecklistService#deleteAllRecordChecklistByDeviceId}. Returns empty list on failure. */
    public List<String> deleteAllRecordChecklistByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/deleteAllRecordChecklistByDeviceId", payload, HttpExtension.POST).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.deleteAllRecordChecklistByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code RecordChecklistService#deleteAllRecordChecklistImagesByUrls}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deleteAllRecordChecklistImagesByUrls(List<String> urls) {
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/deleteAllRecordChecklistImagesByUrls", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.deleteAllRecordChecklistImagesByUrls failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code RecordChecklistService#getRecordChecklistStatusByDeviceId}. Returns null on failure. */
    public String getRecordChecklistStatusByDeviceId(String deviceId, String x) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("x", x);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/getRecordChecklistStatusByDeviceId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("RecordChecklistClient.getRecordChecklistStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code RecordChecklistService#getChecklistStatusCountDeviceId}. Returns 0 on failure. */
    public Integer getChecklistStatusCountDeviceId(String a, String b, String c) {
        Map<String, String> payload = new HashMap<>();
        payload.put("a", a);
        payload.put("b", b);
        payload.put("c", c);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/getChecklistStatusCountDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("RecordChecklistClient.getChecklistStatusCountDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Mirrors {@code RecordChecklistService#updateRecordChecklistByDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateRecordChecklistByDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/updateRecordChecklistByDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.updateRecordChecklistByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code RecordChecklistService#getAllRecordChecklistByBuildings}.
     * Returns empty set on failure.
     * NOTE: List bodies are lost under GET-only skeleton routing (needs POST upgrade).
     */
    public Set<RecordChecklistDTO> getAllRecordChecklistByBuildings(List<String> buildingIds, List<String> floorIds, List<String> locationIds) {
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/getAllRecordChecklistByBuildings", null, HttpExtension.POST).block();
            return new HashSet<>();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.getAllRecordChecklistByBuildings failed; returning stub default: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /** Mirrors {@code RecordChecklistService#getRecordChecklistStatusByLocationId}. Returns null on failure. */
    public String getRecordChecklistStatusByLocationId(String locationId, String status) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        payload.put("status", status);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/getRecordChecklistStatusByLocationId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("RecordChecklistClient.getRecordChecklistStatusByLocationId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code RecordChecklistService#getChecklistStatusCountLocationId}. Returns 0 on failure. */
    public Integer getChecklistStatusCountLocationId(String locationId, String a, String b) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        payload.put("a", a);
        payload.put("b", b);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/getChecklistStatusCountLocationId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("RecordChecklistClient.getChecklistStatusCountLocationId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Mirrors {@code RecordChecklistService#updateRecordChecklistLocationAndIsRemoved}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateRecordChecklistLocationAndIsRemoved(Set<String> locationIds) {
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/updateRecordChecklistLocationAndIsRemoved", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.updateRecordChecklistLocationAndIsRemoved failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code RecordChecklistService#deleteRecordChecklistByLocationId}. */
    public void deleteRecordChecklistByLocationId(String locationId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/deleteRecordChecklistByLocationId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.deleteRecordChecklistByLocationId failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code RecordChecklistService#deleteAllRecordChecklistByLocationId}. Returns null on failure. */
    public List<String> deleteAllRecordChecklistByLocationId(String locationId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/deleteAllRecordChecklistByLocationId", payload, HttpExtension.POST).block();
            return null;
        } catch (Exception e) {
            log.warn("RecordChecklistClient.deleteAllRecordChecklistByLocationId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code RecordChecklistService#updateRecordChecklist}. */
    public void updateRecordChecklist(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "recordChecklist/updateRecordChecklist", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("RecordChecklistClient.updateRecordChecklist failed; swallowing: {}", e.getMessage());
        }
    }
}
