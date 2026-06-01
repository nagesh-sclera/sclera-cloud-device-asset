package io.sclera.dapr.events;

public record DeviceAuditEvent(
    String vdmsId,
    String deviceId,
    String action,
    String status,
    String message,
    String userEmail) {}
