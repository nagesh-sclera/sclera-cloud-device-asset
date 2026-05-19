package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-audit microservice.
 *
 * Replaces the no-op {@code io.sclera.service.SyslogService} stub.
 * Returns an empty {@link JSONObject} on any exception (sidecar-down,
 * network error) so that call sites receive the same documented default
 * as the original stub.
 *
 * Path and verb are aligned with SyslogController (GET-only camelCase
 * skeleton endpoint).
 */
@Component
public class SyslogClient {

    private static final Logger log = LoggerFactory.getLogger(SyslogClient.class);
    private static final String APP_ID = "sclera-audit";

    private final DaprClient dapr;

    public SyslogClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code SyslogService#getSyslogExcludeDeviceIds}.
     * Maps to GET sclera-audit/syslog/getSyslogExcludeDeviceIds.
     * Documented default on failure: {@code new JSONObject()} (empty object).
     */
    public JSONObject getSyslogExcludeDeviceIds(String username, String vdmsid,
                                                 String dockerName, String profileType) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsid", vdmsid);
        payload.put("docker_name", dockerName);
        payload.put("profile_type", profileType);
        try {
            JSONObject result = dapr.invokeMethod(APP_ID, "syslog/getSyslogExcludeDeviceIds",
                    payload, HttpExtension.GET, JSONObject.class).block();
            return result != null ? result : new JSONObject();
        } catch (Exception e) {
            log.warn("SyslogClient.getSyslogExcludeDeviceIds failed; swallowing: {}", e.getMessage());
            return new JSONObject();
        }
    }
}
