package io.sclera.dapr;

public record PublishResult(boolean success, String eventId, String error) {}
