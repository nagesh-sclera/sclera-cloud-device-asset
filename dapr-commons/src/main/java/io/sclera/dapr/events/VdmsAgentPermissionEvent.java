package io.sclera.dapr.events;

public record VdmsAgentPermissionEvent(String vdmsId, String agentId, String permission) {}
