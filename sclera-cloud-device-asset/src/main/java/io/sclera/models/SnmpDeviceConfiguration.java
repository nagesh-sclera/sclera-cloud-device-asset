package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class SnmpDeviceConfiguration {
    @Id
    private String id;
    @ManyToOne
    private Docker docker;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Docker getDocker() { return docker; }
    public void setDocker(Docker docker) { this.docker = docker; }
}