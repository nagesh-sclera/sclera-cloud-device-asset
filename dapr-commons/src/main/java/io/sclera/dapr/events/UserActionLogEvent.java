package io.sclera.dapr.events;

/**
 * Audit entry published by sclera-workorders to the {@code user-action-log-events} topic;
 * consumed by vdms-service, which owns the user_action_log table.
 * Field names match io.sclera.workorder.dto.UserActionLogDTO so Jackson binds the CloudEvent data.
 */
public record UserActionLogEvent(
    String email,
    String type,
    String action,
    String message,
    String status,
    String subType,
    String primaryId,
    String vdmsId,
    String requestId) {
}
