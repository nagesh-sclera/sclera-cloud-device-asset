package io.sclera.scheduler.web.dto;

/** One run in the global activity feed (carries the job name, unlike per-job {@link RunView}). */
public record FeedRunView(
    String runId,
    String jobName,
    String status,
    boolean manual,
    String firedAt,
    String finishedAt,
    Long durationMs,
    String error) {}
