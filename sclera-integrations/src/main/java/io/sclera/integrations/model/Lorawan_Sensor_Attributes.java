package io.sclera.integrations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Lorawan_Sensor_Attributes {
    @Id
    private String id;

    @Column(name = "lorawan_sensor_id")
    private String lorawan_sensor_id;

    @Column
    private String name;

    @Column
    private String value;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLorawan_sensor_id() { return lorawan_sensor_id; }
    public void setLorawan_sensor_id(String lorawan_sensor_id) { this.lorawan_sensor_id = lorawan_sensor_id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
