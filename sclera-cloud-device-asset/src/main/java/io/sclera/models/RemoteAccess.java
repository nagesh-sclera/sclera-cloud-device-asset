package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class RemoteAccess {
    @Id
    private String id;
    @OneToOne
    private Docker docker;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Docker getDocker() { return docker; }
    public void setDocker(Docker docker) { this.docker = docker; }
}