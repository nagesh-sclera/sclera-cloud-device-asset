package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceSensorsDTO {

    private String id;
    private String name;
    private String location;
    private String location_id;
    private String type;
    private Integer virtual_device_type;
    private Integer status;
    private String position;
    private Boolean sensor_alert;
    private String global_qrcode_id;
    private List<SensorDTO> sensors;
    private String nfc_id;
    private String barcode_id;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getVirtual_device_type() {
        return virtual_device_type;
    }

    public void setVirtual_device_type(Integer virtual_device_type) {
        this.virtual_device_type = virtual_device_type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<SensorDTO> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorDTO> sensors) {
        this.sensors = sensors;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getSensor_alert() {
        return sensor_alert;
    }

    public void setSensor_alert(Boolean sensor_alert) {
        this.sensor_alert = sensor_alert;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getGlobal_qrcode_id() {
        return global_qrcode_id;
    }

    public void setGlobal_qrcode_id(String global_qrcode_id) {
        this.global_qrcode_id = global_qrcode_id;
    }

    public  String getNfc_id(){
        return nfc_id;
    }


    public void setNfc_id(String nfc_id) {
        this.nfc_id = nfc_id;

    }

    public String getBarcode_id() {
        return barcode_id;
    }

    public void setBarcode_id(String barcode_id) {
        this.barcode_id = barcode_id;
    }

    public DeviceSensorsDTO() {
        super();
    }

    public DeviceSensorsDTO(String id, String name, String location, String location_id, String type, Integer virtual_device_type,
                            Integer status, String position, Boolean sensor_alert) {
        super();
        this.id = id;
        this.name = name;
        this.location = location;
        this.location_id = location_id;
        this.type = type;
        this.virtual_device_type = virtual_device_type;
        this.status = status;
        this.position = position;
        this.sensor_alert = sensor_alert;

    }


}
