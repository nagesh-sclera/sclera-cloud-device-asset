
package io.sclera.dto;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecificationsDTO {

    private String id;
    private String key_name;
    private String key_value;
    private String key_unit;
    private String connected_device;
    private String device_id;
    private String connected_specifications_id;

    private JSONObject power;


    public JSONObject getPower() {
        return power;
    }

    public void setPower(JSONObject power) {
        this.power = power;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public String getKey_value() {
        return key_value;
    }

    public void setKey_value(String key_value) {
        this.key_value = key_value;
    }

    public String getKey_unit() {
        return key_unit;
    }

    public void setKey_unit(String key_unit) {
        this.key_unit = key_unit;
    }

    public String getConnected_device() {
        return connected_device;
    }

    public void setConnected_device(String connected_device) {
        this.connected_device = connected_device;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }


    public String getConnected_specifications_id() {
        return connected_specifications_id;
    }

    public void setConnected_specifications_id(String connected_specifications_id) {
        this.connected_specifications_id = connected_specifications_id;
    }


    public SpecificationsDTO() {
    }

    public SpecificationsDTO(String id, String key_name, String key_value, String key_unit, String device_id) {
        this.id = id;
        this.key_name = key_name;
        this.key_value = key_value;
        this.key_unit = key_unit;
        this.device_id = device_id;
    }


    public SpecificationsDTO(String id, String key_name, String device_id) {
        this.id = id;
        this.key_name = key_name;
        this.device_id = device_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecificationsDTO)) return false;
        SpecificationsDTO that = (SpecificationsDTO) o;
        return getId().equals(that.getId()) && getKey_name().equals(that.getKey_name()) && getDevice_id().equals(that.getDevice_id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getKey_name(), getDevice_id());
    }

    @Override
    public String toString() {
        return "SpecificationsDTO{" +
                "id='" + id + '\'' +
                ", key_name='" + key_name + '\'' +
                ", key_value='" + key_value + '\'' +
                ", key_unit='" + key_unit + '\'' +
                ", connected_device='" + connected_device + '\'' +
                ", device_id='" + device_id + '\'' +
                ", connected_specifications_id='" + connected_specifications_id + '\'' +
                ", power=" + power +
                '}';
    }
}

