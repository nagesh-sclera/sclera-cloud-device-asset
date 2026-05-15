package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class KNXInterface {
    @Id
    private String id;
    @ManyToOne
    private Docker docker;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Docker getDocker() { return docker; }
    public void setDocker(Docker docker) { this.docker = docker; }
}