package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolyLensDeviceAttributesDTO {

    private String id;

    private String name;

    private String display_name;

    private String value;

    private String unit;

    private String category;

    private String sub_category;

    private String user_data_value;

    private BigInteger timestamp;
    private String poly_lens_device_id;

    public PolyLensDeviceAttributesDTO() {
    }

    public PolyLensDeviceAttributesDTO(String id, String name, String display_name, String value, String unit, String category, String sub_category, String user_data_value, BigInteger timestamp, String poly_lens_device_id) {
        this.id = id;
        this.name = name;
        this.display_name = display_name;
        this.value = value;
        this.unit = unit;
        this.category = category;
        this.sub_category = sub_category;
        this.user_data_value = user_data_value;
        this.timestamp = timestamp;
        this.poly_lens_device_id = poly_lens_device_id;
    }

    public PolyLensDeviceAttributesDTO(String id, String name, String display_name, String value, String unit, String category, String sub_category, String user_data_value, BigInteger timestamp) {
        this.id = id;
        this.name = name;
        this.display_name = display_name;
        this.value = value;
        this.unit = unit;
        this.category = category;
        this.sub_category = sub_category;
        this.user_data_value = user_data_value;
        this.timestamp = timestamp;
    }

    public PolyLensDeviceAttributesDTO(String name, String display_name, String value, String unit, String user_data_value, BigInteger timestamp, String poly_lens_device_id) {
        this.name = name;
        this.display_name = display_name;
        this.value = value;
        this.unit = unit;
        this.user_data_value = user_data_value;
        this.timestamp = timestamp;
        this.poly_lens_device_id = poly_lens_device_id;
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }

    public String getUser_data_value() {
        return user_data_value;
    }

    public void setUser_data_value(String user_data_value) {
        this.user_data_value = user_data_value;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getPoly_lens_device_id() {
        return poly_lens_device_id;
    }

    public void setPoly_lens_device_id(String poly_lens_device_id) {
        this.poly_lens_device_id = poly_lens_device_id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    @Override
    public String toString() {
        return "PolyLensDeviceAttributesDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", display_name='" + display_name + '\'' +
                ", value='" + value + '\'' +
                ", unit='" + unit + '\'' +
                ", category='" + category + '\'' +
                ", sub_category='" + sub_category + '\'' +
                ", user_data_value='" + user_data_value + '\'' +
                ", timestamp=" + timestamp +
                ", poly_lens_device_id=" + poly_lens_device_id +
                '}';
    }
}
