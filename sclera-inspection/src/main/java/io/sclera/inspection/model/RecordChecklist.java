package io.sclera.inspection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RecordChecklist {
    @Id
    private String id;

    @Column(name = "device_id")
    private String device_id;

    @Column(name = "location_id")
    private String location_id;

    @Column(name = "building_id")
    private String building_id;

    @Column(name = "floor_id")
    private String floor_id;

    @Column(name = "is_removed")
    private Integer is_removed;

    @Column(name = "record_type")
    private String record_type;

    @Column(name = "status")
    private String status;

    @Column(name = "inspection_record_id")
    private String inspection_record_id;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String image_urls;

    @Column(name = "updated_email")
    private String updated_email;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDevice_id() { return device_id; }
    public void setDevice_id(String device_id) { this.device_id = device_id; }
    public String getLocation_id() { return location_id; }
    public void setLocation_id(String location_id) { this.location_id = location_id; }
    public String getBuilding_id() { return building_id; }
    public void setBuilding_id(String building_id) { this.building_id = building_id; }
    public String getFloor_id() { return floor_id; }
    public void setFloor_id(String floor_id) { this.floor_id = floor_id; }
    public Integer getIs_removed() { return is_removed; }
    public void setIs_removed(Integer is_removed) { this.is_removed = is_removed; }
    public String getRecord_type() { return record_type; }
    public void setRecord_type(String record_type) { this.record_type = record_type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInspection_record_id() { return inspection_record_id; }
    public void setInspection_record_id(String inspection_record_id) { this.inspection_record_id = inspection_record_id; }
    public String getImage_urls() { return image_urls; }
    public void setImage_urls(String image_urls) { this.image_urls = image_urls; }
    public String getUpdated_email() { return updated_email; }
    public void setUpdated_email(String updated_email) { this.updated_email = updated_email; }
}