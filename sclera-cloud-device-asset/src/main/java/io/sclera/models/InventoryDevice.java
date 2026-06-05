package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * JPA entity associating an inventory tracking identifier with a device, used to manage
 * devices held in inventory within the asset-management domain.
 */
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
