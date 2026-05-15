package io.sclera.models.compositeclass;

import java.io.Serializable;
import io.sclera.models.Bacnet_Device;

public class Bacnet_ObjectIds implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Bacnet_Device bacnet_device;

    public Bacnet_ObjectIds() {}

    public Bacnet_ObjectIds(String id, Bacnet_Device bacnet_device) {
        this.id = id;
        this.bacnet_device = bacnet_device;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Bacnet_Device getBacnet_device() { return bacnet_device; }
    public void setBacnet_device(Bacnet_Device bacnet_device) { this.bacnet_device = bacnet_device; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bacnet_device == null) ? 0 : bacnet_device.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Bacnet_ObjectIds other = (Bacnet_ObjectIds) obj;
        if (bacnet_device == null) {
            if (other.bacnet_device != null) return false;
        } else if (!bacnet_device.equals(other.bacnet_device)) return false;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        return true;
    }
}