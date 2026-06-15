package io.sclera.models;


import jakarta.persistence.*;


/**
 * JPA entity holding per-VDMS presentation and configuration details such as weather data,
 * screen layout, device custom fields, and Corrigo layout. Linked one-to-one to a {@link Vdms}
 * and backed by the {@code vdms_details} table.
 */
@Entity
@Table(name = "vdms_details")
public class VdmsDetails {
    @Id
    private String id;

    @Column(length = 64)
    private String weather_city;

    @Column(length = 32)
    private String weather_zip_code;

    @Column(length = 32)
    private String weather_country_code;

    @Column(length = 32)
    private String weather_latitude;

    @Column(length = 32)
    private String weather_longitude;

    @Column(columnDefinition = "TEXT")
    private String weather_data;

    @Column(length = 32)
    private String weather_units;

    @Column(columnDefinition = "TEXT")
    private String layout_data;

    @Column(columnDefinition = "text")
    private String device_custom_fields;

    @Column(columnDefinition = "text")
    private String corrigo_layout_data;

    @OneToOne
    private Vdms vdms;


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

    public Vdms getVdms() {
        return vdms;
    }

    public void setVdms(Vdms vdms) {
        this.vdms = vdms;
    }
}
