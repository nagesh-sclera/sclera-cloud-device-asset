package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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