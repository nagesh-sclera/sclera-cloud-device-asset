package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity linking a device to its historical record, used to track per-device
 * history within the asset-management domain.
 */
@Entity
@Table(name = "history")
public class History {
    @Id
    private Long id;

    @ManyToOne
    private Device device;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
}
