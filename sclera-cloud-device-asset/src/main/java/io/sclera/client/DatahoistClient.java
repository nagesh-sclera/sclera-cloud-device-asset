package io.sclera.client;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.DataHoistDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations.
 * Replaces the deleted {@code io.sclera.service.DataHoistService} stub.
 */
@Component
public class DatahoistClient {

    private static final Logger log = LoggerFactory.getLogger(DatahoistClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public DatahoistClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors the deleted DataHoistService#getDataHoistDeviceById. Returns empty set on failure. */
    public Set<DataHoistDTO> getDataHoistDeviceById(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        if (username != null) payload.put("username", username);
        if (vdmsId != null) payload.put("vdmsId", vdmsId);
        if (deviceId != null) payload.put("deviceId", deviceId);
        try {
            byte[] response = dapr.invokeMethod(APP_ID, "datahoist/getDataHoistDeviceById", payload, HttpExtension.GET, byte[].class).block();
            if (response == null || response.length == 0) return Collections.emptySet();
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(response, new TypeReference<Set<DataHoistDTO>>() {});
        } catch (Exception e) {
            log.warn("DatahoistClient.getDataHoistDeviceById failed; returning empty: {}", e.toString());
            return Collections.emptySet();
        }
    }
}
