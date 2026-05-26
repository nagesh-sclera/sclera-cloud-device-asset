package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class GlobalInspectionRelation {
    @Id
    private String id;

    @ManyToOne
    private Device device;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
}