package io.sclera.dto;

import java.math.BigInteger;

public class BacnetObjectDetailsDTO {
    private String id;
    private String name;
    private Integer type;
    private Integer instance;
    private String present_value;
    private Boolean validity;
    private String unit;
    private String state_text;
    private String user_data_name;
    private String user_data_value;
    private String category;
    private Boolean configuration;
    private String building;
    private String floor;
    private String location;
    private String location_id;
    private Boolean alert;
    private BigInteger last_seen;
    private String bacnet_device_id;
    private String bacnet_device_name;
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

    public BacnetObjectDetailsDTO() {}

    public BacnetObjectDetailsDTO(String id, String name, Integer type, Integer instance,
            String present_value, Boolean validity, String unit, String state_text,
            String user_data_name, String user_data_value, String category, Boolean configuration,
            String building, String floor, String location, String location_id, Boolean alert,
            BigInteger last_seen, String bacnet_device_id, String bacnet_device_name,
            String device_id, String device_name, String device_image_url_1,
            Integer device_monitor, Integer device_popup_notification, Integer device_email_alert,
            Integer device_sms_alert, String device_product_id, String docker_name,
            String docker_system_type, String vdms_id, String vendor_org_id, String customer_org_id,
            String device_model, String device_vendor, String device_type,
            Integer device_virtual_device_type, String device_warranty, Integer device_status,
            BigInteger device_last_seen_on, Boolean device_sensor_alert,
            Integer device_local_vendor_email_alert, Integer device_local_vendor_sms_alert,
            String device_global_image_url_1, String floor_id, String building_id,
            String device_local_vendor_name, String device_local_vendor_email,
            String device_local_vendor_extension, String device_local_vendor_phone) {
        this.id = id; this.name = name; this.type = type; this.instance = instance;
        this.present_value = present_value; this.validity = validity; this.unit = unit;
        this.state_text = state_text; this.user_data_name = user_data_name;
        this.user_data_value = user_data_value; this.category = category;
        this.configuration = configuration; this.building = building; this.floor = floor;
        this.location = location; this.location_id = location_id; this.alert = alert;
        this.last_seen = last_seen; this.bacnet_device_id = bacnet_device_id;
        this.bacnet_device_name = bacnet_device_name; this.device_id = device_id;
        this.device_name = device_name; this.device_image_url_1 = device_image_url_1;
        this.device_monitor = device_monitor; this.device_popup_notification = device_popup_notification;
        this.device_email_alert = device_email_alert; this.device_sms_alert = device_sms_alert;
        this.device_product_id = device_product_id; this.docker_name = docker_name;
        this.docker_system_type = docker_system_type; this.vdms_id = vdms_id;
        this.vendor_org_id = vendor_org_id; this.customer_org_id = customer_org_id;
        this.device_model = device_model; this.device_vendor = device_vendor;
        this.device_type = device_type; this.device_virtual_device_type = device_virtual_device_type;
        this.device_warranty = device_warranty; this.device_status = device_status;
        this.device_last_seen_on = device_last_seen_on; this.device_sensor_alert = device_sensor_alert;
        this.device_local_vendor_email_alert = device_local_vendor_email_alert;
        this.device_local_vendor_sms_alert = device_local_vendor_sms_alert;
        this.device_global_image_url_1 = device_global_image_url_1; this.floor_id = floor_id;
        this.building_id = building_id; this.device_local_vendor_name = device_local_vendor_name;
        this.device_local_vendor_email = device_local_vendor_email;
        this.device_local_vendor_extension = device_local_vendor_extension;
        this.device_local_vendor_phone = device_local_vendor_phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Integer getType() { return type; }
    public Integer getInstance() { return instance; }
    public String getPresent_value() { return present_value; }
    public Boolean getValidity() { return validity; }
    public String getUnit() { return unit; }
    public String getState_text() { return state_text; }
    public String getUser_data_name() { return user_data_name; }
    public String getUser_data_value() { return user_data_value; }
    public String getCategory() { return category; }
    public Boolean getConfiguration() { return configuration; }
    public String getBuilding() { return building; }
    public String getFloor() { return floor; }
    public String getLocation() { return location; }
    public String getLocation_id() { return location_id; }
    public Boolean getAlert() { return alert; }
    public BigInteger getLast_seen() { return last_seen; }
    public String getBacnet_device_id() { return bacnet_device_id; }
    public String getBacnet_device_name() { return bacnet_device_name; }
    public String getDevice_id() { return device_id; }
    public String getDevice_name() { return device_name; }
    public String getDevice_image_url_1() { return device_image_url_1; }
    public Integer getDevice_monitor() { return device_monitor; }
    public Integer getDevice_popup_notification() { return device_popup_notification; }
    public Integer getDevice_email_alert() { return device_email_alert; }
    public Integer getDevice_sms_alert() { return device_sms_alert; }
    public String getDevice_product_id() { return device_product_id; }
    public String getDocker_name() { return docker_name; }
    public String getDocker_system_type() { return docker_system_type; }
    public String getVdms_id() { return vdms_id; }
    public String getVendor_org_id() { return vendor_org_id; }
    public String getCustomer_org_id() { return customer_org_id; }
    public String getDevice_model() { return device_model; }
    public String getDevice_vendor() { return device_vendor; }
    public String getDevice_type() { return device_type; }
    public Integer getDevice_virtual_device_type() { return device_virtual_device_type; }
    public String getDevice_warranty() { return device_warranty; }
    public Integer getDevice_status() { return device_status; }
    public BigInteger getDevice_last_seen_on() { return device_last_seen_on; }
    public Boolean getDevice_sensor_alert() { return device_sensor_alert; }
    public Integer getDevice_local_vendor_email_alert() { return device_local_vendor_email_alert; }
    public Integer getDevice_local_vendor_sms_alert() { return device_local_vendor_sms_alert; }
    public String getDevice_global_image_url_1() { return device_global_image_url_1; }
    public String getFloor_id() { return floor_id; }
    public String getBuilding_id() { return building_id; }
    public String getDevice_local_vendor_name() { return device_local_vendor_name; }
    public String getDevice_local_vendor_email() { return device_local_vendor_email; }
    public String getDevice_local_vendor_extension() { return device_local_vendor_extension; }
    public String getDevice_local_vendor_phone() { return device_local_vendor_phone; }
}