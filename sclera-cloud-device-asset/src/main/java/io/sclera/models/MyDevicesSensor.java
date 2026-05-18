package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/** STUB: non-AP-C1 entity (no @Entity to keep out of schema) */
@Entity
public class MyDevicesSensor {
    @Id
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @ManyToOne
    private Device device;

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
}
