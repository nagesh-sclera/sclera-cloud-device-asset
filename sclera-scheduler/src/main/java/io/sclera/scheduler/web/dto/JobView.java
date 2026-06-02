package io.sclera.scheduler.web.dto;

public record JobView(
    String name,
    String schedule,
    String owner,
    String state,
    String lastStatus,     // null if never run
    Long lastDurationMs,
    String lastFiredAt,     // ISO-8601, null if never run
    String nextFireAt) {}   // ISO-8601, null if unknown
