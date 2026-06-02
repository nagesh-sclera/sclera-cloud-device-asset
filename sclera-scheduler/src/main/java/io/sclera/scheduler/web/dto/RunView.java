package io.sclera.scheduler.web.dto;

public record RunView(
    String runId,
    String status,
    boolean manual,
    String firedAt,
    String finishedAt,
    Long durationMs,
    String error) {}
