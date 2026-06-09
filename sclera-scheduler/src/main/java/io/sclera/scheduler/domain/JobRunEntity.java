package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_run")
public class JobRunEntity {

    @Id
    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RunStatus status;

    @Column(name = "manual", nullable = false)
    private boolean manual;

    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error")
    private String error;

    @Column(name = "vdms_id")
    private String vdmsId;

    protected JobRunEntity() {}

    public JobRunEntity(UUID runId, String jobName, RunStatus status,
                        boolean manual, Instant firedAt) {
        this(runId, jobName, status, manual, firedAt, null);
    }

    public JobRunEntity(UUID runId, String jobName, RunStatus status,
                        boolean manual, Instant firedAt, String vdmsId) {
        this.runId = runId;
        this.jobName = jobName;
        this.status = status;
        this.manual = manual;
        this.firedAt = firedAt;
        this.vdmsId = vdmsId;
    }

    public UUID getRunId() { return runId; }
    public String getJobName() { return jobName; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus s) { this.status = s; }
    public boolean isManual() { return manual; }
    public Instant getFiredAt() { return firedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant t) { this.finishedAt = t; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long d) { this.durationMs = d; }
    public String getError() { return error; }
    public void setError(String e) { this.error = e; }
    public String getVdmsId() { return vdmsId; }
}
