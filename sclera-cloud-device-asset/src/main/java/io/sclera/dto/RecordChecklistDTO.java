package io.sclera.dto;

public class RecordChecklistDTO {
    private String building_id;
    private String record_type;
    private String inspection_record_id;
    private String location_id;
    private String floor_id;

    public String getBuilding_id() { return building_id; }
    public void setBuilding_id(String v) { this.building_id = v; }
    public String getRecord_type() { return record_type; }
    public void setRecord_type(String v) { this.record_type = v; }
    public String getInspection_record_id() { return inspection_record_id; }
    public void setInspection_record_id(String v) { this.inspection_record_id = v; }
    public String getLocation_id() { return location_id; }
    public void setLocation_id(String v) { this.location_id = v; }
    public String getFloor_id() { return floor_id; }
    public void setFloor_id(String v) { this.floor_id = v; }
}
