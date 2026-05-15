package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorAlertDTO {

    private String primary_id;
    private String secondary_id;
    private String primary_name;
    private String secondary_name;
    private String category;
    private String value;
    private String alert_message;
    private String unit;
    private String protocol;
    private String device_id;

    private String type;

    private String tertiary_id;
    private String tertiary_name;

    //sensor alert changes
    private String priority;
    private String sub_category;

    public String getPrimary_id() {
        return primary_id;
    }

    public void setPrimary_id(String primary_id) {
        this.primary_id = primary_id;
    }

    public String getSecondary_id() {
        return secondary_id;
    }

    public void setSecondary_id(String secondary_id) {
        this.secondary_id = secondary_id;
    }

    public String getPrimary_name() {
        return primary_name;
    }

    public void setPrimary_name(String primary_name) {
        this.primary_name = primary_name;
    }

    public String getSecondary_name() {
        return secondary_name;
    }

    public void setSecondary_name(String secondary_name) {
        this.secondary_name = secondary_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTertiary_id() {
        return tertiary_id;
    }

    public void setTertiary_id(String tertiary_id) {
        this.tertiary_id = tertiary_id;
    }

    public String getTertiary_name() {
        return tertiary_name;
    }

    public void setTertiary_name(String tertiary_name) {
        this.tertiary_name = tertiary_name;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }

    public SensorAlertDTO() {
    }


    public SensorAlertDTO(String primary_id, String secondary_id,
                          String primary_name, String secondary_name, String category, String value,
                          String unit, String protocol, String device_id) {

        this.primary_id = primary_id;
        this.secondary_id = secondary_id;
        this.primary_name = primary_name;
        this.secondary_name = secondary_name;
        this.category = category;
        this.value = value;
        this.unit = unit;
        this.protocol = protocol;
        this.device_id = device_id;
    }

    public SensorAlertDTO(String primary_id, String secondary_id, String primary_name, String secondary_name, String category, String value, String unit, String protocol, String device_id, String type,String sub_category) {
        this.primary_id = primary_id;
        this.secondary_id = secondary_id;
        this.primary_name = primary_name;
        this.secondary_name = secondary_name;
        this.category = category;
        this.value = value;
        this.unit = unit;
        this.protocol = protocol;
        this.device_id = device_id;
        this.type = type;
        this.sub_category = sub_category;
    }

    public SensorAlertDTO(String primary_id, String secondary_id, String primary_name, String secondary_name, String protocol, String tertiary_id, String tertiary_name) {
        this.primary_id = primary_id;
        this.secondary_id = secondary_id;
        this.primary_name = primary_name;
        this.secondary_name = secondary_name;
        this.protocol = protocol;
        this.tertiary_id = tertiary_id;
        this.tertiary_name = tertiary_name;
    }
}
