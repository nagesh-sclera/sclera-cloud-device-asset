package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import io.sclera.workorder.dto.VdmsDetailsDTO;
import io.sclera.workorder.exception.VdmsUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Cross-service client — calls vdms-service through the Dapr sidecar's
 * service-invocation API at {@code http://localhost:{daprPort}/v1.0/invoke/{appId}/method/...}.
 *
 * Used by the NEW sample method {@code MaximoService.getVdmsDetailsForMaximoConfig}.
 * Existing flows do not use this client.
 *
 * The Dapr sidecar handles service discovery (by app-id), retries, and mTLS.
 * From the app's perspective it's just a local HTTP call.
 */
@Component
public class VdmsClient {

    private static final Logger log = LoggerFactory.getLogger(VdmsClient.class);

    private final RestClient daprRestClient;
    private final DaprProperties props;

    public VdmsClient(RestClient daprRestClient, DaprProperties props) {
        this.daprRestClient = daprRestClient;
        this.props = props;
    }

    /**
     * Fetches the VDMS ID string from vdms-service.
     * Returns null on any error so callers can degrade gracefully.
     */
    public String getVdmsId() {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> resp = daprRestClient
                    .get()
                    .uri("/invoke/{appId}/method/api/v1/vdms-service/vdms/id", props.getVdmsAppId())
                    .retrieve()
                    .body(java.util.Map.class);
            return resp != null ? (String) resp.get("vdmsId") : null;
        } catch (RestClientException e) {
            log.warn("Could not fetch vdmsId from vdms-service via Dapr: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Fetches VDMS details by id from vdms-service.
     * Returns {@link Optional#empty()} when vdms-service returns 404.
     * Throws {@link VdmsUnavailableException} when the Dapr sidecar cannot reach
     * the downstream (sidecar down, app down, transport error).
     */
    public Optional<VdmsDetailsDTO> getVdmsDetails(String vdmsId) {
        log.debug("Invoking vdms-service via Dapr for vdmsId={}", vdmsId);
        try {
            VdmsDetailsDTO resp = daprRestClient
                    .get()
                    .uri("/invoke/{appId}/method/api/v1/vdms-service/vdms/{id}", props.getVdmsAppId(), vdmsId)
                    .retrieve()
                    .body(VdmsDetailsDTO.class);
            return Optional.ofNullable(resp);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("vdms-service unreachable via Dapr for vdmsId={}", vdmsId, e);
            throw new VdmsUnavailableException(
                    "Could not reach vdms-service via Dapr sidecar: " + e.getMessage(), e);
        }
    }
}
