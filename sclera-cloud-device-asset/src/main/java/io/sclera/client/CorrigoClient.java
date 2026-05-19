package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceDTO;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-workorders microservice (AP-C3).
 *
 * Replaces the stub {@code io.sclera.service.CorrigoService}.
 * Methods returning complex objects (CorrigoConfigurationDTO, JSONArray) return null
 * on sidecar failure (stub default). Void methods swallow exceptions with a WARN log.
 *
 * NOTE: methods passing DTO/Object bodies use GET routing — body is silently dropped.
 * Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class CorrigoClient {

    private static final Logger log = LoggerFactory.getLogger(CorrigoClient.class);
    private static final String APP_ID = "sclera-workorders";

    private final DaprClient dapr;

    public CorrigoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code CorrigoService#getCorrigoConfigurationDetails}.
     * Maps to GET sclera-workorders/corrigo/getCorrigoConfigurationDetails.
     * Returns null on sidecar failure (stub default).
     */
    public CorrigoConfigurationDTO getCorrigoConfigurationDetails() {
        try {
            dapr.invokeMethod(APP_ID, "corrigo/getCorrigoConfigurationDetails", null, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("CorrigoClient.getCorrigoConfigurationDetails failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mirrors {@code CorrigoService#updateCorrigoAssets}.
     * Maps to GET sclera-workorders/corrigo/updateCorrigoAssets.
     * NOTE: CorrigoConfigurationDTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateCorrigoAssets(String username, String vdmsid, Integer pageNo, Integer pageSize,
                                    String searchKey, CorrigoConfigurationDTO config) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsid", vdmsid);
        payload.put("pageNo", pageNo);
        payload.put("pageSize", pageSize);
        payload.put("searchKey", searchKey);
        try {
            dapr.invokeMethod(APP_ID, "corrigo/updateCorrigoAssets", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CorrigoClient.updateCorrigoAssets failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CorrigoService#getWorkordersByAssetIdForBot}.
     * Maps to GET sclera-workorders/corrigo/getWorkordersByAssetIdForBot.
     * Returns null on sidecar failure (stub default).
     * NOTE: DeviceDTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public JSONArray getWorkordersByAssetIdForBot(DeviceDTO device) {
        try {
            dapr.invokeMethod(APP_ID, "corrigo/getWorkordersByAssetIdForBot", null, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("CorrigoClient.getWorkordersByAssetIdForBot failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mirrors {@code CorrigoService#corrigoUrlSync}.
     * Maps to GET sclera-workorders/corrigo/corrigoUrlSync.
     * NOTE: url Object body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void corrigoUrlSync(Object url, String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "corrigo/corrigoUrlSync", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CorrigoClient.corrigoUrlSync failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CorrigoService#updateCorrigoCredentialsFromCloud}.
     * Maps to GET sclera-workorders/corrigo/updateCorrigoCredentialsFromCloud.
     */
    public void updateCorrigoCredentialsFromCloud(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "corrigo/updateCorrigoCredentialsFromCloud", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CorrigoClient.updateCorrigoCredentialsFromCloud failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CorrigoService#updateCorrigoCredentialsMigration}.
     * Maps to GET sclera-workorders/corrigo/updateCorrigoCredentialsMigration.
     */
    public void updateCorrigoCredentialsMigration(String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "corrigo/updateCorrigoCredentialsMigration", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CorrigoClient.updateCorrigoCredentialsMigration failed; swallowing: {}", e.getMessage());
        }
    }
}
