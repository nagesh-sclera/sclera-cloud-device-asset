package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationAlertDTO {

    private String id;
    private String name;

    private String floor_id;

    private String floor_name;

    private String building_id;

    private String building_name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }

    public String getFloor_name() {
        return floor_name;
    }

    public void setFloor_name(String floor_name) {
        this.floor_name = floor_name;
    }

    public String getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(String building_id) {
        this.building_id = building_id;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String building_name) {
        this.building_name = building_name;
    }

    public LocationAlertDTO() {
    }

    public LocationAlertDTO(String id, String name, String floor_id, String floor_name,
                            String building_id, String building_name) {
        this.id = id;
        this.name = name;
        this.floor_id = floor_id;
        this.floor_name = floor_name;
        this.building_id = building_id;
        this.building_name = building_name;
    }
}

