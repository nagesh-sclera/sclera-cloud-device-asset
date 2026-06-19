package io.sclera.workorder.service.impl;

import io.sclera.workorder.service.UserActionLogService;
import io.sclera.workorder.client.UserActionLogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Default {@link UserActionLogService} — publishes audit events via {@link UserActionLogClient}. */
@Service
public class UserActionLogServiceImpl implements UserActionLogService {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogServiceImpl.class);

    private final UserActionLogClient userActionLogClient;

    public UserActionLogServiceImpl(UserActionLogClient userActionLogClient) {
        this.userActionLogClient = userActionLogClient;
    }

    /** {@inheritDoc} Best-effort: delegates to the Dapr client and logs (without rethrowing) on failure. */
    @Override
    public void addUserAction(String username, String vdmsId, String type, String action,
                              String message, String status, String subType, String primaryId) {
        try {
            userActionLogClient.addUserAction(username, type, action, message, status, subType, primaryId, vdmsId);
        } catch (Exception e) {
            log.error("Failed to publish audit event type={} action={} primaryId={}: {}",
                    type, action, primaryId, e.getMessage());
        }
    }
}
