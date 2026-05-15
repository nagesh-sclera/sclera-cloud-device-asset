package io.sclera.models;


import javax.persistence.*;

import io.sclera.dto.touchscreen.VdmsDetailsDTO;


@SqlResultSetMapping(
        name = "weathermapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "weather_city", type = String.class),
                                @ColumnResult(name = "weather_zip_code", type = String.class),
                                @ColumnResult(name = "weather_country_code", type = String.class),
                                @ColumnResult(name = "weather_latitude", type = String.class),
                                @ColumnResult(name = "weather_longitude", type = String.class),
                                @ColumnResult(name = "weather_data", type = String.class),
                                @ColumnResult(name = "weather_units", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "VdmsDetails.getWeatherData",
        query = "SELECT w.id,w.weather_city,w.weather_zip_code,w.weather_country_code,w.weather_latitude,w.weather_longitude,w.weather_data, w.weather_units,w.vdms_id"
                + " FROM vdms_details w ",
        resultSetMapping = "weathermapping"
)

@SqlResultSetMapping(
        name = "layoutmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "layout_data", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "corrigo_layout_data", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "VdmsDetails.getVdmsLayoutData",
        query = "SELECT vd.id,vd.layout_data,vd.vdms_id,vd.corrigo_layout_data"
                + " FROM vdms_details vd ",
        resultSetMapping = "layoutmapping"
)

@SqlResultSetMapping(
        name = "devicecustomfieldsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "device_custom_fields", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "VdmsDetails.getVdmsDeviceCustomFields",
        query = "SELECT vd.id,vd.device_custom_fields"
                + " FROM vdms_details vd ",
        resultSetMapping = "devicecustomfieldsmapping"
)

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

    @Column(columnDefinition = "LONGTEXT")
    private String device_custom_fields;

    @Column(columnDefinition = "LONGTEXT")
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
