package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job")
public class JobEntity {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "schedule", nullable = false)
    private String schedule;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "trigger_topic", nullable = false)
    private String triggerTopic = "scheduler.trigger";

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private JobState state = JobState.ENABLED;

    @Column(name = "last_run_id")
    private UUID lastRunId;

    @Column(name = "next_fire_at")
    private Instant nextFireAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected JobEntity() {}

    public JobEntity(String name, String schedule, String owner,
                     String triggerTopic, JobState state) {
        this.name = name;
        this.schedule = schedule;
        this.owner = owner;
        this.triggerTopic = triggerTopic;
        this.state = state;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public String getName() { return name; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String s) { this.schedule = s; }
    public String getOwner() { return owner; }
    public void setOwner(String o) { this.owner = o; }
    public String getTriggerTopic() { return triggerTopic; }
    public void setTriggerTopic(String t) { this.triggerTopic = t; }
    public JobState getState() { return state; }
    public void setState(JobState s) { this.state = s; }
    public UUID getLastRunId() { return lastRunId; }
    public void setLastRunId(UUID id) { this.lastRunId = id; }
    public Instant getNextFireAt() { return nextFireAt; }
    public void setNextFireAt(Instant t) { this.nextFireAt = t; }
}
