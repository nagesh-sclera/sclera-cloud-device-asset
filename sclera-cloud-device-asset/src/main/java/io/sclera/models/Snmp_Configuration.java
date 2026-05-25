package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Snmp_Configuration {
    @Id
    private Long id;

    @OneToOne
    private Device device;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
}
