package io.sclera.dto;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;


public class MeasuringInstrumentDTO {
    private String id;
    private String type;
    private String name;
    private String description;
    private String calculation_type;
    private String attribute;
    private String parameter;
    private String category;
    private String value;
    private String unit;
    private String tags;
    private BigInteger timestamp;
    private Boolean alert;
    private String user_data_value;
    private String user_data_name;
    private String alert_message;
    private String device_id;
    private String building;
    private String floor;
    private String location;
    private String location_id;
    private String device_name;
    private String device_image_url_1;
    private Integer value_changed_status;
    private String sensor_type;
    private Integer show_on_map;
    private Integer show_on_scan;
    private String measuring_entity;
    private String previous_value;
    private String sub_category;
    private String digital_twin_position;

    private String scale_type;


    private List<MeasuringInstrumentAttributesDTO> measuring_instrument_attributes;


    private Set<LocationDTO> locations;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCalculation_type() {
        return calculation_type;
    }

    public void setCalculation_type(String calculation_type) {
        this.calculation_type = calculation_type;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public Integer getValue_changed_status() {
        return value_changed_status;
    }

    public void setValue_changed_status(Integer value_changed_status) {
        this.value_changed_status = value_changed_status;
    }

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public String getUser_data_value() {
        return user_data_value;
    }

    public void setUser_data_value(String user_data_value) {
        this.user_data_value = user_data_value;
    }

    public String getUser_data_name() {
        return user_data_name;
    }

    public void setUser_data_name(String user_data_name) {
        this.user_data_name = user_data_name;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
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

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevice_image_url_1() {
        return device_image_url_1;
    }

    public void setDevice_image_url_1(String device_image_url_1) {
        this.device_image_url_1 = device_image_url_1;
    }

    public Integer getShow_on_map() {
        return show_on_map;
    }

    public void setShow_on_map(Integer show_on_map) {
        this.show_on_map = show_on_map;
    }

    public Integer getShow_on_scan() {
        return show_on_scan;
    }

    public void setShow_on_scan(Integer show_on_scan) {
        this.show_on_scan = show_on_scan;
    }

    public String getMeasuring_entity() {
        return measuring_entity;
    }

    public void setMeasuring_entity(String measuring_entity) {
        this.measuring_entity = measuring_entity;
    }

    public String getPrevious_value() {
        return previous_value;
    }

    public void setPrevious_value(String previous_value) {
        this.previous_value = previous_value;
    }

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }

    public String getDigital_twin_position() {
        return digital_twin_position;
    }

    public void setDigital_twin_position(String digital_twin_position) {
        this.digital_twin_position = digital_twin_position;
    }

    public String getScale_type() {
        return scale_type;
    }

    public void setScale_type(String scale_type) {
        this.scale_type = scale_type;
    }

    public List<MeasuringInstrumentAttributesDTO> getMeasuring_instrument_attributes() {
        return measuring_instrument_attributes;
    }

    public void setMeasuring_instrument_attributes(List<MeasuringInstrumentAttributesDTO> measuring_instrument_attributes) {
        this.measuring_instrument_attributes = measuring_instrument_attributes;
    }

    public Set<LocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(Set<LocationDTO> locations) {
        this.locations = locations;
    }


    public MeasuringInstrumentDTO() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MeasuringInstrumentDTO(String id, String type, String name, String description, String calculation_type,
                                  String attribute, String parameter, String category, String value, String unit, String tags,
                                  String device_id, BigInteger timestamp, String sensor_type, Boolean alert, String user_data_name,
                                  String user_data_value, Integer show_on_map, Integer show_on_scan, String measuring_entity, String building,
                                  String floor, String location, String location_id, String sub_category, String digital_twin_position, String scale_type) {
        super();
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.calculation_type = calculation_type;
        this.attribute = attribute;
        this.parameter = parameter;
        this.category = category;
        this.value = value;
        this.unit = unit;
        this.tags = tags;
        this.device_id = device_id;
        this.timestamp = timestamp;
        this.sensor_type = sensor_type;
        this.alert = alert;
        this.user_data_name = user_data_name;
        this.user_data_value = user_data_value;
        this.show_on_map = show_on_map;
        this.show_on_scan = show_on_scan;
        this.measuring_entity = measuring_entity;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.location_id = location_id;
        this.sub_category = sub_category;
        this.digital_twin_position = digital_twin_position;
        this.scale_type = scale_type;

    }


    public MeasuringInstrumentDTO(String id, String type, String name, String description, String calculation_type,
                                  String attribute, String parameter, String category, String value, String unit, String tags,
                                  BigInteger timestamp, String sensor_type, Boolean alert, String user_data_value, String user_data_name,
                                  String building, String floor, String location, String location_id, String device_id,
                                  String device_name, Integer show_on_map, Integer show_on_scan) {
        super();
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.calculation_type = calculation_type;
        this.attribute = attribute;
        this.parameter = parameter;
        this.category = category;
        this.value = value;
        this.unit = unit;
        this.tags = tags;
        this.timestamp = timestamp;
        this.sensor_type = sensor_type;
        this.alert = alert;
        this.user_data_value = user_data_value;
        this.user_data_name = user_data_name;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.location_id = location_id;
        this.device_id = device_id;
        this.device_name = device_name;
        this.show_on_map = show_on_map;
        this.show_on_scan = show_on_scan;
    }

    public MeasuringInstrumentDTO(String id, String type, String name, String description, String calculation_type,
                                  String attribute, String parameter, String category, String value, String unit, String tags,
                                  BigInteger timestamp, String sensor_type, Boolean alert, String user_data_value, String user_data_name,
                                  String building, String floor, String location, String location_id, String device_id,
                                  String device_name, Integer show_on_map, Integer show_on_scan, String measuring_entity, String sub_category, String scale_type) {
        super();
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.calculation_type = calculation_type;
        this.attribute = attribute;
        this.parameter = parameter;
        this.category = category;
        this.value = value;
        this.unit = unit;
        this.tags = tags;
        this.timestamp = timestamp;
        this.sensor_type = sensor_type;
        this.alert = alert;
        this.user_data_value = user_data_value;
        this.user_data_name = user_data_name;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.location_id = location_id;
        this.device_id = device_id;
        this.device_name = device_name;
        this.show_on_map = show_on_map;
        this.show_on_scan = show_on_scan;
        this.measuring_entity = measuring_entity;
        this.sub_category = sub_category;
        this.scale_type = scale_type;

    }

    public MeasuringInstrumentDTO(String id, String attribute, String scale_type, String sensor_type, String sub_category) {
        this.id = id;
        this.attribute = attribute;
        this.scale_type = scale_type;
        this.sensor_type = sensor_type;
        this.sub_category = sub_category;
    }


    public MeasuringInstrumentDTO(String id, String type, String name, String calculation_type, String category, String value, String unit, String device_id, String sensor_type) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.calculation_type = calculation_type;
        this.category = category;
        this.value = value;
        this.unit = unit;
        this.device_id = device_id;
        this.sensor_type = sensor_type;
    }

    @Override
    public String toString() {
        return "MeasuringInstrumentDTO{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", calculation_type='" + calculation_type + '\'' +
                ", attribute='" + attribute + '\'' +
                ", parameter='" + parameter + '\'' +
                ", category='" + category + '\'' +
                ", value='" + value + '\'' +
                ", unit='" + unit + '\'' +
                ", tags='" + tags + '\'' +
                ", timestamp=" + timestamp +
                ", alert=" + alert +
                ", user_data_value='" + user_data_value + '\'' +
                ", user_data_name='" + user_data_name + '\'' +
                ", alert_message='" + alert_message + '\'' +
                ", device_id='" + device_id + '\'' +
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                ", location='" + location + '\'' +
                ", location_id='" + location_id + '\'' +
                ", device_name='" + device_name + '\'' +
                ", device_image_url_1='" + device_image_url_1 + '\'' +
                ", value_changed_status=" + value_changed_status +
                ", sensor_type='" + sensor_type + '\'' +
                ", show_on_map=" + show_on_map +
                ", show_on_scan=" + show_on_scan +
                ", measuring_entity='" + measuring_entity + '\'' +
                ", previous_value='" + previous_value + '\'' +
                ", sub_category='" + sub_category + '\'' +
                ", digital_twin_position='" + digital_twin_position + '\'' +
                ", scale_type='" + scale_type + '\'' +
                ", measuring_instrument_attributes=" + measuring_instrument_attributes +
                ", locations=" + locations +
                '}';
    }

}
