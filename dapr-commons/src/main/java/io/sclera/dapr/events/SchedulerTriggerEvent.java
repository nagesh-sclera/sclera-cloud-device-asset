package io.sclera.dapr.events;

/**
 * Published when the Dapr Scheduler fires a job. runId is the idempotency key.
 * vdmsId is the VDMS this fire targets; null for GLOBAL-scope jobs.
 */
public record SchedulerTriggerEvent(
    String jobName,
    String runId,
    String vdmsId,
    long firedAtEpochMs) {

    /** Backward-compatible constructor for GLOBAL jobs (no VDMS dimension). */
    public SchedulerTriggerEvent(String jobName, String runId, long firedAtEpochMs) {
        this(jobName, runId, null, firedAtEpochMs);
    }
}
