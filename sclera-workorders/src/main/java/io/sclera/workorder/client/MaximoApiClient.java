package io.sclera.workorder.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.exception.MaximoException;
import io.sclera.workorder.util.MaximoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Maximo REST API client — extracted from the monolith's APICallService god class
 * (originally lines 6611–6660 of {@code io.sclera.service.APICallService}).
 *
 * Only the two methods called by MaximoService are pulled in here:
 *   1. {@link #generateMaximoAccessToken} — OAuth2 client_credentials grant
 *   2. {@link #getAllWorkorders} — OSLC work-order query
 *
 * Behavior preserved:
 *   - generateMaximoAccessToken catches everything and returns null on failure
 *     (matches monolith — the caller handles the null and re-runs with forceCreate)
 *   - getAllWorkorders throws MaximoException(401) on auth failure so MaximoService
 *     can retry with a fresh token (this is the only thrown path)
 *   - All other failures inside getAllWorkorders are caught and logged, returning null
 *   - status_code is injected into the response JSON for downstream checks
 */
@Component
public class MaximoApiClient {

    private static final Logger log = LoggerFactory.getLogger(MaximoApiClient.class);

    private final RestClient httpClient;
    private final MaximoUtils maximoUtils;

    public MaximoApiClient(RestClient maximoHttpClient, MaximoUtils maximoUtils) {
        this.httpClient = maximoHttpClient;
        this.maximoUtils = maximoUtils;
    }

    /**
     * Calls the Maximo auth URL with {@code grant_type=client_credentials} and
     * the configured client_id / client_secret. Returns the {@code access_token}
     * from the response body, or null on any failure (matches monolith semantics).
     */
    public String generateMaximoAccessToken(MaximoConfigurationDTO maximoConfigurationDTO) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", maximoConfigurationDTO.getClientId());
            form.add("client_secret", maximoConfigurationDTO.getClientSecret());
            form.add("grant_type", "client_credentials");

            log.info("Requestbody (form-encoded): client_id, client_secret, grant_type=client_credentials");

            ResponseEntity<String> response = httpClient
                    .post()
                    .uri(maximoConfigurationDTO.getAuthUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().value() == 200) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                return responseBody.getString("access_token");
            }
        } catch (Exception e) {
            log.error("generateMaximoAccessToken failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Queries Maximo OSLC for work orders matching the filter built by
     * {@link MaximoUtils#buildParams}. On HTTP 401, throws {@link MaximoException}
     * with errorCode 401 so the service layer can refresh the token and retry.
     * Other failures are logged and yield null. The returned JSONObject has
     * {@code status_code} injected.
     */
    public JSONObject getAllWorkorders(String accessToken, String serverUrl, String workOrderId,
                                       MaximoDTO maximoDTO, Integer pageno, Integer pagesize) {
        try {
            Map<String, String> params = maximoUtils.buildParams(workOrderId, maximoDTO, pageno, pagesize);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(serverUrl);
            params.forEach(uriBuilder::queryParam);

            ResponseEntity<String> response = httpClient
                    .method(HttpMethod.GET)
                    .uri(uriBuilder.build().toUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toEntity(String.class);

            int statusCode = response.getStatusCode().value();
            if (statusCode == 401) {
                throw new MaximoException("Token Expired", 401, serverUrl);
            }

            String jsonString = response.getBody();
            log.info("**************jsonString:************{}", jsonString);
            JSONObject responseBody = jsonString == null ? new JSONObject() : JSON.parseObject(jsonString);
            responseBody.put("status_code", statusCode);
            log.info("responsebody get all workorders {}", responseBody);
            return responseBody;

        } catch (MaximoException me) {
            // re-throw — service layer needs the 401 signal
            throw me;
        } catch (Exception e) {
            log.error("getAllWorkorders failed: {}", e.getMessage());
        }
        return null;
    }
}
