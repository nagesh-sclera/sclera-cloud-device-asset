package io.sclera.dapr.events;

/**
 * Published when the Dapr Scheduler fires a job. runId is the idempotency key.
 * vdmsId is the VDMS this fire targets; null for GLOBAL-scope jobs.
 * owner is the catalog owner of the job (e.g. "device-asset", "workorder");
 * subscribers route on it. Null when the scheduler cannot resolve the job's owner.
 */
public record SchedulerTriggerEvent(
    String jobName,
    String runId,
    String vdmsId,
    String owner,
    long firedAtEpochMs) {

    /** Backward-compatible constructor for per-VDMS jobs without a resolved owner. */
    public SchedulerTriggerEvent(String jobName, String runId, String vdmsId, long firedAtEpochMs) {
        this(jobName, runId, vdmsId, null, firedAtEpochMs);
    }

    /** Backward-compatible constructor for GLOBAL jobs (no VDMS dimension, no owner). */
    public SchedulerTriggerEvent(String jobName, String runId, long firedAtEpochMs) {
        this(jobName, runId, null, null, firedAtEpochMs);
    }
}
