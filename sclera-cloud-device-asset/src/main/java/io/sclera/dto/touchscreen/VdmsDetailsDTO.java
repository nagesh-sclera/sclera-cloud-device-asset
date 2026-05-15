package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VdmsDetailsDTO {

    private String id;

    private String weather_city;

    private String weather_zip_code;

    private String weather_country_code;

    private String weather_latitude;

    private String weather_longitude;

    private String weather_data;

    private String weather_units;
    
    private String layout_data;

    private String vdms_id;

    private String device_custom_fields;

    private String corrigo_layout_data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWeather_city() {
        return weather_city;
    }

    public void setWeather_city(String weather_city) {
        this.weather_city = weather_city;
    }

    public String getWeather_zip_code() {
        return weather_zip_code;
    }

    public void setWeather_zip_code(String weather_zip_code) {
        this.weather_zip_code = weather_zip_code;
    }

    public String getWeather_country_code() {
        return weather_country_code;
    }

    public void setWeather_country_code(String weather_country_code) {
        this.weather_country_code = weather_country_code;
    }

    public String getWeather_latitude() {
        return weather_latitude;
    }

    public void setWeather_latitude(String weather_latitude) {
        this.weather_latitude = weather_latitude;
    }

    public String getWeather_longitude() {
        return weather_longitude;
    }

    public void setWeather_longitude(String weather_longitude) {
        this.weather_longitude = weather_longitude;
    }

    public String getWeather_data() {
        return weather_data;
    }

    public void setWeather_data(String weather_data) {
        this.weather_data = weather_data;
    }

    public String getWeather_units() {
        return weather_units;
    }

    public void setWeather_units(String weather_units) {
        this.weather_units = weather_units;
    }
    
    public String getLayout_data() {
        return layout_data;
    }

    public void setLayout_data(String layout_data) {
        this.layout_data = layout_data;
    }
    
    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public String getDevice_custom_fields() {
        return device_custom_fields;
    }

    public void setDevice_custom_fields(String device_custom_fields) {
        this.device_custom_fields = device_custom_fields;
    }

    public String getCorrigo_layout_data() {
        return corrigo_layout_data;
    }

    public void setCorrigo_layout_data(String corrigo_layout_data) {
        this.corrigo_layout_data = corrigo_layout_data;
    }

    public VdmsDetailsDTO() {
    }

    public VdmsDetailsDTO(String id, String weather_city, String weather_zip_code, String weather_country_code, String weather_latitude, String weather_longitude, String weather_data, String weather_units, String vdms_id) {
        this.id = id;
        this.weather_city = weather_city;
        this.weather_zip_code = weather_zip_code;
        this.weather_country_code = weather_country_code;
        this.weather_latitude = weather_latitude;
        this.weather_longitude = weather_longitude;
        this.weather_data = weather_data;
        this.weather_units = weather_units;
        this.vdms_id = vdms_id;
    }

    public VdmsDetailsDTO(String id, String layout_data,String vdms_id, String corrigo_layout_data) {
        this.id = id;
        this.layout_data = layout_data;
        this.vdms_id = vdms_id;
        this.corrigo_layout_data = corrigo_layout_data;
    }

    public VdmsDetailsDTO(String id,String device_custom_fields) {
        this.id = id;
        this.device_custom_fields = device_custom_fields;

    }
}
