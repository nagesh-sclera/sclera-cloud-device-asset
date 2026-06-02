package io.sclera.dapr.events;

/** Published by the job's owning service after running the work. */
public record SchedulerResultEvent(
    String jobName,
    String runId,
    String status,      // "SUCCESS" | "FAILED"
    long durationMs,
    String error) {}    // null when SUCCESS
