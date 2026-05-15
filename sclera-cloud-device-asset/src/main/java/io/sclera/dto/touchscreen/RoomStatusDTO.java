package io.sclera.dto.touchscreen;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomStatusDTO {

    private Integer device_status;
    private Integer sensor_alert_status;

    private String type;

    Set<SensorDTO> sensors;

    public Integer getDevice_status() {
        return device_status;
    }

    public void setDevice_status(Integer device_status) {
        this.device_status = device_status;
    }

    public Integer getSensor_alert_status() {
        return sensor_alert_status;
    }

    public void setSensor_alert_status(Integer sensor_alert_status) {
        this.sensor_alert_status = sensor_alert_status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<SensorDTO> getSensors() {
        return sensors;
    }

    public void setSensors(Set<SensorDTO> sensors) {
        this.sensors = sensors;
    }


    public RoomStatusDTO() {
    }

    public RoomStatusDTO(Integer device_status, Integer sensor_alert_status) {
        this.device_status = device_status;
        this.sensor_alert_status = sensor_alert_status;
    }

    @Override
    public String toString() {
        return "RoomStatusDTO{" +
                "device_status=" + device_status +
                ", sensor_alert_status=" + sensor_alert_status +
                ", type='" + type + '\'' +
                ", sensors=" + sensors +
                '}';
    }
}

