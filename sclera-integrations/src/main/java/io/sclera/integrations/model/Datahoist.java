package io.sclera.integrations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Datahoist {
    @Id
    private String id;

    @Column(name = "device_id")
    private String device_id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDevice_id() { return device_id; }
    public void setDevice_id(String device_id) { this.device_id = device_id; }
}
