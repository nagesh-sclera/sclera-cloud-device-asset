package io.sclera.integrations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class KNXInterface {
    @Id
    private String id;

    // PG-port: scalar FKs (Docker entity lives in cloud-device-asset).
    @Column(name = "docker_name")
    private String docker_name;

    @Column(name = "docker_vdms_id")
    private String docker_vdms_id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocker_name() { return docker_name; }
    public void setDocker_name(String docker_name) { this.docker_name = docker_name; }
    public String getDocker_vdms_id() { return docker_vdms_id; }
    public void setDocker_vdms_id(String docker_vdms_id) { this.docker_vdms_id = docker_vdms_id; }
}
