package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeasuringInstrumentDetailsDTO {
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
    private String sensor_type;
    private String user_data_value;
    private String user_data_name;

    private String alert_message;

    private String building;

    private String floor;

    private String location;

    private String location_id;

    private String device_id;
    private String device_name;
    private String device_image_url_1;

    private Integer device_monitor;
    private Integer device_popup_notification;

    private Integer device_email_alert;
    private Integer device_sms_alert;
    private String device_product_id;
    private String docker_name;
    private String docker_system_type;
    private String vdms_id;
    private String vendor_org_id;
    private String customer_org_id;
    private String device_model;
    private String device_vendor;
    private String device_type;
    private Integer device_virtual_device_type;
    private String device_warranty;
    private Integer device_status;
    private BigInteger device_last_seen_on;
    private Boolean device_sensor_alert;
    private Integer device_local_vendor_email_alert;
    private Integer device_local_vendor_sms_alert;

    private String device_global_image_url_1;
    private String floor_id;
    private String building_id;
    private String device_local_vendor_name;
    private String device_local_vendor_email;
    private String device_local_vendor_extension;
    private String device_local_vendor_phone;
    private Integer show_on_map;

    private Integer show_on_scan;


    private List<MeasuringInstrumentAttributesDTO> measuring_instrument_attributes;

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

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
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

    public Integer getDevice_monitor() {
        return device_monitor;
    }

    public void setDevice_monitor(Integer device_monitor) {
        this.device_monitor = device_monitor;
    }

    public Integer getDevice_popup_notification() {
        return device_popup_notification;
    }

    public void setDevice_popup_notification(Integer device_popup_notification) {
        this.device_popup_notification = device_popup_notification;
    }

    public Integer getDevice_email_alert() {
        return device_email_alert;
    }

    public void setDevice_email_alert(Integer device_email_alert) {
        this.device_email_alert = device_email_alert;
    }

    public Integer getDevice_sms_alert() {
        return device_sms_alert;
    }

    public void setDevice_sms_alert(Integer device_sms_alert) {
        this.device_sms_alert = device_sms_alert;
    }

    public String getDevice_product_id() {
        return device_product_id;
    }

    public void setDevice_product_id(String device_product_id) {
        this.device_product_id = device_product_id;
    }

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

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public String getVendor_org_id() {
        return vendor_org_id;
    }

    public void setVendor_org_id(String vendor_org_id) {
        this.vendor_org_id = vendor_org_id;
    }

    public String getCustomer_org_id() {
        return customer_org_id;
    }

    public String getDevice_model() {
        return device_model;
    }

    public void setDevice_model(String device_model) {
        this.device_model = device_model;
    }

    public String getDevice_vendor() {
        return device_vendor;
    }

    public void setDevice_vendor(String device_vendor) {
        this.device_vendor = device_vendor;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public Integer getDevice_virtual_device_type() {
        return device_virtual_device_type;
    }

    public void setDevice_virtual_device_type(Integer device_virtual_device_type) {
        this.device_virtual_device_type = device_virtual_device_type;
    }

    public String getDevice_warranty() {
        return device_warranty;
    }

    public void setDevice_warranty(String device_warranty) {
        this.device_warranty = device_warranty;
    }

    public Integer getDevice_status() {
        return device_status;
    }

    public void setDevice_status(Integer device_status) {
        this.device_status = device_status;
    }

    public BigInteger getDevice_last_seen_on() {
        return device_last_seen_on;
    }

    public void setDevice_last_seen_on(BigInteger device_last_seen_on) {
        this.device_last_seen_on = device_last_seen_on;
    }

    public Boolean getDevice_sensor_alert() {
        return device_sensor_alert;
    }

    public void setDevice_sensor_alert(Boolean device_sensor_alert) {
        this.device_sensor_alert = device_sensor_alert;
    }

    public Integer getDevice_local_vendor_email_alert() {
        return device_local_vendor_email_alert;
    }

    public void setDevice_local_vendor_email_alert(Integer device_local_vendor_email_alert) {
        this.device_local_vendor_email_alert = device_local_vendor_email_alert;
    }

    public Integer getDevice_local_vendor_sms_alert() {
        return device_local_vendor_sms_alert;
    }

    public void setDevice_local_vendor_sms_alert(Integer device_local_vendor_sms_alert) {
        this.device_local_vendor_sms_alert = device_local_vendor_sms_alert;
    }

    public String getDevice_global_image_url_1() {
        return device_global_image_url_1;
    }

    public void setDevice_global_image_url_1(String device_global_image_url_1) {
        this.device_global_image_url_1 = device_global_image_url_1;
    }

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }

    public String getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(String building_id) {
        this.building_id = building_id;
    }

    public String getDevice_local_vendor_name() {
        return device_local_vendor_name;
    }

    public void setDevice_local_vendor_name(String device_local_vendor_name) {
        this.device_local_vendor_name = device_local_vendor_name;
    }

    public String getDevice_local_vendor_email() {
        return device_local_vendor_email;
    }

    public void setDevice_local_vendor_email(String device_local_vendor_email) {
        this.device_local_vendor_email = device_local_vendor_email;
    }

    public String getDevice_local_vendor_extension() {
        return device_local_vendor_extension;
    }

    public void setDevice_local_vendor_extension(String device_local_vendor_extension) {
        this.device_local_vendor_extension = device_local_vendor_extension;
    }

    public String getDevice_local_vendor_phone() {
        return device_local_vendor_phone;
    }

    public void setDevice_local_vendor_phone(String device_local_vendor_phone) {
        this.device_local_vendor_phone = device_local_vendor_phone;
    }

    public void setCustomer_org_id(String customer_org_id) {
        this.customer_org_id = customer_org_id;
    }

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
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

    public List<MeasuringInstrumentAttributesDTO> getMeasuring_instrument_attributes() {
        return measuring_instrument_attributes;
    }

    public void setMeasuring_instrument_attributes(List<MeasuringInstrumentAttributesDTO> measuring_instrument_attributes) {
        this.measuring_instrument_attributes = measuring_instrument_attributes;
    }

    public MeasuringInstrumentDetailsDTO() {
    }

    public MeasuringInstrumentDetailsDTO(String id, String type, String name, String description, String calculation_type,
                                         String attribute, String parameter, String category, String value, String unit,
                                         String tags, BigInteger timestamp, Boolean alert, String user_data_value, String user_data_name,
                                         String building, String floor, String location, String location_id,
                                         String device_id, String device_name, String device_image_url_1, Integer device_monitor, Integer device_popup_notification,
                                         Integer device_email_alert, Integer device_sms_alert, String device_product_id, String docker_name,
                                         String docker_system_type, String vdms_id, String vendor_org_id, String customer_org_id,
                                         String device_model, String device_vendor, String device_type, Integer device_virtual_device_type,
                                         String device_warranty, Integer device_status, BigInteger device_last_seen_on, Boolean device_sensor_alert,
                                         Integer device_local_vendor_email_alert, Integer device_local_vendor_sms_alert,
                                         String device_global_image_url_1, String floor_id, String building_id, String device_local_vendor_name,
                                         String device_local_vendor_email, String device_local_vendor_extension, String device_local_vendor_phone,
                                         String sensor_type, Integer show_on_map, Integer show_on_scan) {
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
        this.alert = alert;
        this.user_data_value = user_data_value;
        this.user_data_name = user_data_name;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.location_id = location_id;
        this.device_id = device_id;
        this.device_name = device_name;
        this.device_image_url_1 = device_image_url_1;
        this.device_monitor = device_monitor;
        this.device_popup_notification = device_popup_notification;
        this.device_email_alert = device_email_alert;
        this.device_sms_alert = device_sms_alert;
        this.device_product_id = device_product_id;
        this.docker_name = docker_name;
        this.docker_system_type = docker_system_type;
        this.vdms_id = vdms_id;
        this.vendor_org_id = vendor_org_id;
        this.customer_org_id = customer_org_id;
        this.device_model = device_model;
        this.device_vendor = device_vendor;
        this.device_type = device_type;
        this.device_virtual_device_type = device_virtual_device_type;
        this.device_warranty = device_warranty;
        this.device_status = device_status;
        this.device_last_seen_on = device_last_seen_on;
        this.device_sensor_alert = device_sensor_alert;
        this.device_local_vendor_email_alert = device_local_vendor_email_alert;
        this.device_local_vendor_sms_alert = device_local_vendor_sms_alert;
        this.device_global_image_url_1 = device_global_image_url_1;
        this.floor_id = floor_id;
        this.building_id = building_id;
        this.device_local_vendor_name = device_local_vendor_name;
        this.device_local_vendor_email = device_local_vendor_email;
        this.device_local_vendor_extension = device_local_vendor_extension;
        this.device_local_vendor_phone = device_local_vendor_phone;
        this.sensor_type = sensor_type;
        this.show_on_map = show_on_map;
        this.show_on_scan = show_on_scan;

    }

    @Override
    public String toString() {
        return "MeasuringInstrumentDetailsDTO{" +
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
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                ", location='" + location + '\'' +
                ", location_id='" + location_id + '\'' +
                ", device_id='" + device_id + '\'' +
                ", device_name='" + device_name + '\'' +
                ", device_image_url_1='" + device_image_url_1 + '\'' +
                ", device_monitor=" + device_monitor +
                ", device_popup_notification=" + device_popup_notification +
                ", device_email_alert=" + device_email_alert +
                ", device_sms_alert=" + device_sms_alert +
                ", device_product_id='" + device_product_id + '\'' +
                ", docker_name='" + docker_name + '\'' +
                ", docker_system_type='" + docker_system_type + '\'' +
                ", vdms_id='" + vdms_id + '\'' +
                ", vendor_org_id='" + vendor_org_id + '\'' +
                ", customer_org_id='" + customer_org_id + '\'' +
                ", device_model='" + device_model + '\'' +
                ", device_vendor='" + device_vendor + '\'' +
                ", device_type='" + device_type + '\'' +
                ", device_virtual_device_type=" + device_virtual_device_type +
                ", device_warranty='" + device_warranty + '\'' +
                ", device_status=" + device_status +
                ", device_last_seen_on=" + device_last_seen_on +
                ", device_sensor_alert=" + device_sensor_alert +
                ", device_local_vendor_email_alert=" + device_local_vendor_email_alert +
                ", device_local_vendor_sms_alert=" + device_local_vendor_sms_alert +
                ", device_global_image_url_1='" + device_global_image_url_1 + '\'' +
                ", floor_id='" + floor_id + '\'' +
                ", building_id='" + building_id + '\'' +
                ", device_local_vendor_name='" + device_local_vendor_name + '\'' +
                ", device_local_vendor_email='" + device_local_vendor_email + '\'' +
                ", device_local_vendor_extension='" + device_local_vendor_extension + '\'' +
                ", device_local_vendor_phone='" + device_local_vendor_phone + '\'' +
                '}';
    }
}
