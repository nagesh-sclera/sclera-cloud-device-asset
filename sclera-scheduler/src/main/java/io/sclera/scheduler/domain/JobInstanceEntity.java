package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_instance")
@IdClass(JobInstanceId.class)
public class JobInstanceEntity {

    @Id
    @Column(name = "job_name")
    private String jobName;

    @Id
    @Column(name = "vdms_id")
    private String vdmsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private JobInstanceState state = JobInstanceState.ENABLED;

    @Column(name = "snooze_until")
    private Instant snoozeUntil;

    @Column(name = "dapr_job_name", nullable = false)
    private String daprJobName;

    @Column(name = "next_fire_at")
    private Instant nextFireAt;

    @Column(name = "last_run_id")
    private UUID lastRunId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected JobInstanceEntity() {}

    public JobInstanceEntity(String jobName, String vdmsId, String daprJobName) {
        this.jobName = jobName;
        this.vdmsId = vdmsId;
        this.daprJobName = daprJobName;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public String getJobName() { return jobName; }
    public String getVdmsId() { return vdmsId; }
    public JobInstanceState getState() { return state; }
    public void setState(JobInstanceState s) { this.state = s; }
    public Instant getSnoozeUntil() { return snoozeUntil; }
    public void setSnoozeUntil(Instant t) { this.snoozeUntil = t; }
    public String getDaprJobName() { return daprJobName; }
    public void setDaprJobName(String n) { this.daprJobName = n; }
    public Instant getNextFireAt() { return nextFireAt; }
    public void setNextFireAt(Instant t) { this.nextFireAt = t; }
    public UUID getLastRunId() { return lastRunId; }
    public void setLastRunId(UUID id) { this.lastRunId = id; }
}
