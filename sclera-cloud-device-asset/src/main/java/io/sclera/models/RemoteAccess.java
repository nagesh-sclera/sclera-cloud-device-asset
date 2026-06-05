package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * JPA entity linking a Docker instance to remote-access configuration, used to manage
 * remote connectivity to devices within the asset-management domain.
 */
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