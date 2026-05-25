package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "history")
public class History {
    @Id
    private Long id;

    @ManyToOne
    private Device device;

    @ManyToOne
    private Bacnet_Object bacnet_object;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    public Bacnet_Object getBacnet_object() { return bacnet_object; }
    public void setBacnet_object(Bacnet_Object bacnet_object) { this.bacnet_object = bacnet_object; }
}