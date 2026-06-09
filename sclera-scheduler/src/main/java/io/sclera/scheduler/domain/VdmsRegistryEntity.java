package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vdms_registry")
public class VdmsRegistryEntity {

    @Id
    @Column(name = "vdms_id")
    private String vdmsId;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected VdmsRegistryEntity() {}

    public VdmsRegistryEntity(String vdmsId, String timezone, boolean active) {
        this.vdmsId = vdmsId;
        this.timezone = timezone;
        this.active = active;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public String getVdmsId() { return vdmsId; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String t) { this.timezone = t; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
}
