package io.sclera.integrations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ConnectedDevices {

    @Id
    private String id;

    @Column
    private String connected_specifications_id;

    // PG-port: scalar FK (Specifications entity lives in cloud-device-asset).
    @Column(name = "specifications_id")
    private String specifications_id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnected_specifications_id() { return connected_specifications_id; }
    public void setConnected_specifications_id(String connected_specifications_id) { this.connected_specifications_id = connected_specifications_id; }
    public String getSpecifications_id() { return specifications_id; }
    public void setSpecifications_id(String specifications_id) { this.specifications_id = specifications_id; }
}
