package io.sclera.workorder.service;

/**
 * Emits user-action (audit) events for workorder operations.
 *
 * <p>Best-effort: publishing failures are logged and swallowed so the audit path
 * never breaks the business path. Events are delivered to vdms-service via Dapr pub/sub.
 */
public interface UserActionLogService {

    /**
     * Publishes a single audit event.
     *
     * @param username  acting user's identifier / e-mail
     * @param vdmsId    VDMS the action is scoped to
     * @param type      event type (e.g. {@code tickets}, {@code maximo})
     * @param action    action performed (e.g. {@code ADD}, {@code UPDATE}, {@code DELETE})
     * @param message   human-readable description
     * @param status    outcome (e.g. {@code success}, {@code failed})
     * @param subType   event sub-type, may be {@code null}
     * @param primaryId id of the primary entity the action concerns
     */
    void addUserAction(String username, String vdmsId, String type, String action,
                       String message, String status, String subType, String primaryId);
}
