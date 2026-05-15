package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyQrcodeDTO {
    private String id;
    private String image_url;
    private String location_id;
    private String property_service_id;

    private String location_name;
    private String floor_name;
    private String building_name;

    private String property_service_name;

    private Set<PropertyServiceResponseDTO> property_service_responses;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getProperty_service_id() {
        return property_service_id;
    }

    public void setProperty_service_id(String property_service_id) {
        this.property_service_id = property_service_id;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getFloor_name() {
        return floor_name;
    }

    public void setFloor_name(String floor_name) {
        this.floor_name = floor_name;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String building_name) {
        this.building_name = building_name;
    }

    public String getProperty_service_name() {
        return property_service_name;
    }

    public void setProperty_service_name(String property_service_name) {
        this.property_service_name = property_service_name;
    }

    public Set<PropertyServiceResponseDTO> getProperty_service_responses() {
        return property_service_responses;
    }

    public void setProperty_service_responses(Set<PropertyServiceResponseDTO> property_service_responses) {
        this.property_service_responses = property_service_responses;
    }

    public PropertyQrcodeDTO() {

    }

    public PropertyQrcodeDTO(String id, String image_url,  String property_service_id,String location_id) {
        this.id = id;
        this.image_url = image_url;
        this.property_service_id = property_service_id;
        this.location_id = location_id;
    }

    public PropertyQrcodeDTO(String id, String image_url, String location_id, String property_service_id, String location_name, String floor_name, String building_name, String property_service_name) {
        this.id = id;
        this.image_url = image_url;
        this.location_id = location_id;
        this.property_service_id = property_service_id;
        this.location_name = location_name;
        this.floor_name = floor_name;
        this.building_name = building_name;
        this.property_service_name = property_service_name;
    }
}
