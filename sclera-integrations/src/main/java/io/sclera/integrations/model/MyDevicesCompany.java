package io.sclera.integrations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MyDevicesCompany {
    @Id
    private String id;

    @Column
    private String name;

    @Column(name = "vdms_id")
    private String vdms_id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVdms_id() { return vdms_id; }
    public void setVdms_id(String vdms_id) { this.vdms_id = vdms_id; }
}
