package io.sclera.scheduler.service;

import io.sclera.scheduler.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Reads the active-VDMS list from vdms-service via the local Dapr sidecar
 * (GET /v1.0/invoke/{appId}/method/vdms/active). Used only by startup-sync — the
 * scheduler never reads the vdms table directly (DB-per-service).
 */
@Component
public class VdmsDirectoryClient {

    private static final Logger log = LoggerFactory.getLogger(VdmsDirectoryClient.class);
    private final RestClient http;
    private final String vdmsAppId;

    @Autowired
    public VdmsDirectoryClient(DaprProperties props,
                               @Value("${scheduler.vdms-app-id}") String vdmsAppId) {
        this("http://localhost:" + props.httpPort(), vdmsAppId);
    }

    // Test constructor — explicit base URL.
    public VdmsDirectoryClient(String daprBaseUrl, String vdmsAppId) {
        this.http = RestClient.builder().baseUrl(daprBaseUrl).build();
        this.vdmsAppId = vdmsAppId;
    }

    public List<VdmsActiveDto> fetchActiveVdms() {
        List<VdmsActiveDto> result = http.get()
            .uri("/v1.0/invoke/{appId}/method/vdms/active", vdmsAppId)
            .retrieve()
            .body(new ParameterizedTypeReference<List<VdmsActiveDto>>() {});
        int size = result == null ? 0 : result.size();
        log.info("Fetched {} active VDMS from {}", size, vdmsAppId);
        return result == null ? List.of() : result;
    }
}
