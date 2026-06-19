package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import io.sclera.workorder.dto.UserActionLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Publishes audit entries to the Dapr pub/sub topic so vdms-service can
 * persist them in user_action_log asynchronously.
 *
 * Equivalent in behavior to the monolith's in-process {@code UserActionLogService.addUserAction}:
 *   - fire-and-forget (no return value)
 *   - swallows all transport errors (audit must never break the business path)
 *
 * Publishes to: http://localhost:{daprPort}/v1.0/publish/{pubsubName}/{topicName}
 * Dapr delivers the CloudEvent to vdms-service's /user-action-log-events subscription endpoint.
 */
@Component
public class UserActionLogClient {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogClient.class);

    private final RestClient daprRestClient;
    private final DaprProperties props;

    public UserActionLogClient(RestClient daprRestClient, DaprProperties props) {
        this.daprRestClient = daprRestClient;
        this.props = props;
    }

    /**
     * Same semantics as {@code UserActionLogService.addUserAction(email, type, action,
     * message, status, sub_type, primary_id)} in the monolith.
     * vdmsId is added because the audit table is partitioned by VDMS.
     */
    public void addUserAction(String email, String type, String action, String message,
                              String status, String subType, String primaryId, String vdmsId) {
        UserActionLogDTO dto = new UserActionLogDTO(email, type, action, message, status,
                subType, primaryId, vdmsId);
        // Carry the correlation id into the async event so the subscriber's audit-write
        // logs trace back to the originating request (pub/sub doesn't forward headers).
        dto.setRequestId(MDC.get("requestId"));
        try {
            daprRestClient
                    .post()
                    .uri("/publish/{pubsubName}/{topic}", props.getPubsubName(), props.getTopicName())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Published audit event type={} action={} primaryId={} to topic={}",
                    type, action, primaryId, props.getTopicName());
        } catch (RestClientException e) {
            log.error("Failed to publish audit event type={} action={} primaryId={} to topic={}",
                    type, action, primaryId, props.getTopicName(), e);
        }
    }
}
