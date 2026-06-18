package io.sclera.integrations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigInteger;

/**
 * A sensor tagged to a device. Owned by the integrations service (sensors are an
 * integration concern); related back to the device in cloud-device-asset only by
 * the {@code device_id} value passed in from the caller. Lives in the
 * integrations_svc schema (ddl-auto creates the table).
 */
@Entity
@Table(name = "device_sensor")
public class DeviceSensor {

    @Id
    private String id;

    @Column(name = "device_id")
    private String device_id;

    @Column(name = "vdms_id")
    private String vdms_id;

    private String name;
    private String type;

    // "value" is reserved in some dialects — map to a safe column name.
    @Column(name = "sensor_value")
    private String value;

    private String unit;
    private String status;
    private String protocol;

    @Column(name = "created_timestamp")
    private BigInteger created_timestamp;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDevice_id() { return device_id; }
    public void setDevice_id(String device_id) { this.device_id = device_id; }
    public String getVdms_id() { return vdms_id; }
    public void setVdms_id(String vdms_id) { this.vdms_id = vdms_id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public BigInteger getCreated_timestamp() { return created_timestamp; }
    public void setCreated_timestamp(BigInteger created_timestamp) { this.created_timestamp = created_timestamp; }
}
