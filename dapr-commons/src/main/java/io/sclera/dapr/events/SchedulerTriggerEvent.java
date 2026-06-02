package io.sclera.dapr.events;

/** Published when the Dapr Scheduler fires a job. runId is the idempotency key. */
public record SchedulerTriggerEvent(
    String jobName,
    String runId,
    long firedAtEpochMs) {}
