package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class InventoryDevice {

    @Id
    private String tracking_id;

    @OneToOne
    private Device device;

    public String getTracking_id() { return tracking_id; }
    public void setTracking_id(String tracking_id) { this.tracking_id = tracking_id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

}
