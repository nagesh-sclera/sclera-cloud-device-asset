package io.sclera.inspection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CheckListRecord {
    @Id
    private String id;

    @Column(name = "device_id")
    private String device_id;

    @Column(name = "location_id")
    private String location_id;

    @Column(name = "is_removed")
    private Integer is_removed;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDevice_id() { return device_id; }
    public void setDevice_id(String device_id) { this.device_id = device_id; }
    public String getLocation_id() { return location_id; }
    public void setLocation_id(String location_id) { this.location_id = location_id; }
    public Integer getIs_removed() { return is_removed; }
    public void setIs_removed(Integer is_removed) { this.is_removed = is_removed; }}
