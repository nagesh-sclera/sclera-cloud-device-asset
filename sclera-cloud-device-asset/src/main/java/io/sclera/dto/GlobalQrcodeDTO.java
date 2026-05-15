package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalQrcodeDTO {

    private String id;

    private String image_url;

    private String location_id;

    private String device_id;

    private String qrcode_type;

    private String device_name;
    private String docker_name;

    private String location_name;

    private String floor_name;

    private String building_name;


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

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }


    public String getQrcode_type() {
        return qrcode_type;
    }

    public void setQrcode_type(String qrcode_type) {
        this.qrcode_type = qrcode_type;
    }

    public String getDocker_name() {
        return docker_name;
    }

    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
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

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public GlobalQrcodeDTO(String id, String image_url, String location_id, String location_name, String floor_name, String building_name) {
        this.id = id;
        this.image_url = image_url;
        this.location_id = location_id;
        this.location_name = location_name;
        this.floor_name = floor_name;
        this.building_name = building_name;
    }

    public GlobalQrcodeDTO(String id, String image_url, String device_id, String device_name, String docker_name, String location_name, String floor_name, String building_name) {
        this.id = id;
        this.image_url = image_url;
        this.device_id = device_id;
        this.device_name = device_name;
        this.docker_name = docker_name;
        this.location_name = location_name;
        this.floor_name = floor_name;
        this.building_name = building_name;
    }

    public GlobalQrcodeDTO(String id, String image_url, String location_id, String device_id, String docker_name) {
        this.id = id;
        this.image_url = image_url;
        this.location_id = location_id;
        this.device_id = device_id;
        this.docker_name = docker_name;
    }


    public GlobalQrcodeDTO(String id, String image_url, String device_id, String device_name, String docker_name,
                           String location_id, String location_name, String floor_name, String building_name) {
        this.id = id;
        this.image_url = image_url;
        this.location_id = location_id;
        this.device_id = device_id;
        this.device_name = device_name;
        this.docker_name = docker_name;
        this.location_name = location_name;
        this.floor_name = floor_name;
        this.building_name = building_name;
    }

    @Override
    public String toString() {
        return "GlobalQrcodeDTO{" +
                "id='" + id + '\'' +
                ", image_url='" + image_url + '\'' +
                ", location_id='" + location_id + '\'' +
                ", device_id='" + device_id + '\'' +
                ", qrcode_type='" + qrcode_type + '\'' +
                ", device_name='" + device_name + '\'' +
                ", docker_name='" + docker_name + '\'' +
                ", location_name='" + location_name + '\'' +
                ", floor_name='" + floor_name + '\'' +
                ", building_name='" + building_name + '\'' +
                '}';
    }
    public GlobalQrcodeDTO() {
        super();
        // TODO Auto-generated constructor stub
    }
}
