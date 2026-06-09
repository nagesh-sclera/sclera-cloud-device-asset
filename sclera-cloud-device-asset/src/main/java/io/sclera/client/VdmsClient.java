package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.State;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Internal client for vdms-service. Calls are routed via the local Dapr sidecar.
 *
 * <p>Read-through cache: each GET call first checks the {@code statestore-vdmscache}
 * Dapr state store (Redis-backed, 5-min TTL). On miss it invokes vdms-service and
 * writes the response back to the cache. Pub/sub continues to use the {@code pubsub} component.
 */
@Service
public class VdmsClient {

    private static final Logger log = LoggerFactory.getLogger(VdmsClient.class);
    private static final String VDMS_APP_ID = "vdms-service";
    private static final String PUBSUB_NAME = "pubsub";
    private static final String CACHE_STORE = "statestore-vdmscache";

    private final DaprClient dapr;
    private final DaprEventPublisher publisher;

    public VdmsClient(DaprClient dapr, DaprEventPublisher publisher) {
        this.dapr = dapr;
        this.publisher = publisher;
    }

    // ── Service invocation (cached) ───────────────────────────────────────────

    /** Fetches the VDMS id from vdms-service via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getVdmsId() {
        return readThroughCache("vdms-id", "vdms/id");
    }

    /** Fetches VDMS details from vdms-service via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getVdmsDetails() {
        return readThroughCache("vdms-details", "vdms/details");
    }

    /** Fetches the VDMS master record from vdms-service via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getMaster() {
        return readThroughCache("vdms-master", "vdms/master");
    }

    /** Fetches whether a secondary device exists from vdms-service via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getHasSecondaryDevice() {
        return readThroughCache("vdms-has-secondary-device", "vdms/has-secondary-device");
    }

    /** Fetches the secondary device id from vdms-service via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getSecondaryDeviceId() {
        return readThroughCache("vdms-secondary-device-id", "vdms/secondary-device-id");
    }

    /** Fetches the customer organisation id for the given VDMS id via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getCustomerOrgId(String vdmsId) {
        return readThroughCache("vdms-customer-org-id:" + vdmsId,
                                "vdms/customer-org-id/" + vdmsId);
    }

    /** Fetches ADC sync details from vdms-service via the read-through cache. Returns null on origin failure. */
    public Map<String, Object> getSyncDetailsForAdc() {
        return readThroughCache("vdms-sync-details-for-adc", "vdms/sync-details-for-adc");
    }

    // ── Pub/Sub ───────────────────────────────────────────────────────────────

    /**
     * Publish an event to a Dapr topic via DaprEventPublisher.
     *
     * <p>Never throws; returns a PublishResult indicating success or failure.
     */
    public PublishResult publishEvent(String topic, Object payload) {
        return publisher.publish(PUBSUB_NAME, topic, payload);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> readThroughCache(String cacheKey, String vdmsPath) {
        // 1. Cache lookup
        try {
            State cached = dapr.getState(CACHE_STORE, cacheKey, Map.class).block();
            if (cached != null && cached.getValue() != null) {
                return (Map<String, Object>) cached.getValue();
            }
        } catch (Exception e) {
            log.debug("VdmsClient cache lookup failed for {}: {}", cacheKey, e.getMessage());
        }
        // 2. Origin invoke
        Map<String, Object> fresh;
        try {
            fresh = (Map<String, Object>) dapr.invokeMethod(
                VDMS_APP_ID, vdmsPath, null, HttpExtension.GET, Map.class
            ).block();
        } catch (Exception e) {
            log.warn("VdmsClient invoke failed for {}: {}", vdmsPath, e.getMessage());
            return null;
        }
        // 3. Cache write (best-effort)
        if (fresh != null) {
            try {
                dapr.saveState(CACHE_STORE, cacheKey, fresh).block();
            } catch (Exception e) {
                log.debug("VdmsClient cache write failed for {}: {}", cacheKey, e.getMessage());
            }
        }
        return fresh;
    }
}
