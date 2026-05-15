package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceAlertDTO {


    private String docker_name;
    private String docker_system_type;
    private String name;
    private Integer device_monitor;
    private String building;
    private String floor;
    private String location;
    private String id;
    private String product_id;
    private String image_url;
    private String local_vendor_id;
    private PhonebookAddressDto local_vendor;
    private String template_type;
    private BigInteger alert_time;

    private String alert_message;

    private String type;

    public String getDocker_name() {
        return docker_name;
    }

    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
    }

    public String getDocker_system_type() {
        return docker_system_type;
    }

    public void setDocker_system_type(String docker_system_type) {
        this.docker_system_type = docker_system_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDevice_monitor() {
        return device_monitor;
    }

    public void setDevice_monitor(Integer device_monitor) {
        this.device_monitor = device_monitor;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }


    public String getLocal_vendor_id() {
        return local_vendor_id;
    }

    public void setLocal_vendor_id(String local_vendor_id) {
        this.local_vendor_id = local_vendor_id;
    }

    public PhonebookAddressDto getLocal_vendor() {
        return local_vendor;
    }

    public void setLocal_vendor(PhonebookAddressDto local_vendor) {
        this.local_vendor = local_vendor;
    }

    public String getTemplate_type() {
        return template_type;
    }

    public void setTemplate_type(String template_type) {
        this.template_type = template_type;
    }

    public BigInteger getAlert_time() {
        return alert_time;
    }

    public void setAlert_time(BigInteger alert_time) {
        this.alert_time = alert_time;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public DeviceAlertDTO(String id, String docker_name, String docker_system_type, String name, String building, String floor, String location, Integer device_monitor, String product_id, String image_url, String type) {
        this.id = id;
        this.docker_name = docker_name;
        this.docker_system_type = docker_system_type;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.device_monitor = device_monitor;
        this.product_id = product_id;
        this.image_url = image_url;
        this.type = type;

    }

    public DeviceAlertDTO(String id, String docker_name, String docker_system_type, String name, String building, String floor, String location, Integer device_monitor,
                         String product_id, String image_url, String local_vendor_id, String type) {
        this.id = id;
        this.docker_name = docker_name;
        this.docker_system_type = docker_system_type;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.device_monitor = device_monitor;
        this.product_id = product_id;
        this.image_url = image_url;
        this.local_vendor_id = local_vendor_id;
        this.type = type;

    }

    @Override
    public String toString() {
        return "DeviceAlertDTO{" +
                "docker_name='" + docker_name + '\'' +
                ", docker_system_type='" + docker_system_type + '\'' +
                ", name='" + name + '\'' +
                ", device_monitor=" + device_monitor +
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                ", location='" + location + '\'' +
                ", id='" + id + '\'' +
                ", product_id='" + product_id + '\'' +
                ", image_url='" + image_url + '\'' +
                ", local_vendor_id='" + local_vendor_id + '\'' +
                ", local_vendor=" + local_vendor +
                ", template_type='" + template_type + '\'' +
                ", alert_time=" + alert_time +
                '}';
    }
}
