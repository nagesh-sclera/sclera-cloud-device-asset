package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.touchscreen.settings.UserDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertDTO {

    private Integer alert_type;

    private Integer email_alert;

    private Integer sms_alert;

    private Integer local_vendor_email_alert;

    private Integer local_vendor_sms_alert;

    private String local_vendor_name;

    private String local_vendor_email;

    private String local_vendor_extension;

    private String local_vendor_phone;

    private String vdms_id;

    private String customer_org_id;

    private String vendor_org_id;

    private String vendor_email;

    private String user_email;

    private String docker_name;

    private String docker_system_type;

    private Integer remote_access_otp;

    private BigInteger timestamp;

    private String timezone;

    private String device_name;

    private String product_id;

    private Integer device_monitor;

    private String building;

    private String floor;

    private String location;

    private String bacnet_device_name;

    private String bacnet_object_name;

    private String bacnet_object_category;

    private String lorawan_sensor_name;

    private String lorawan_sensor_attribute_name;

    private String lorawan_sensor_type;

    private String disruptive_sensor_name;

    private String disruptive_sensor_type;

    private String value;

    private String alert_message;

    private String unit;

    private String ticket_number;

    private Integer ticket_type;

    private String ticket_status;

    private String ticket_user_message;

    private String ticket_vendor_message;

    private String ticket_vendor_email;

    private String my_devices_sensor_attributes_name;

    private String my_devices_sensor_name;

    private String my_devices_sensor_type;

    private String monnit_sensor_name;

    private String monnit_sensor_category;

    private String pelican_sensor_attributes_name;

    private String pelican_sensor_name;

    private String pelican_sensor_type;

    private String knx_group_name;

    private String knx_group_category;

    private String snmp_object_name;

    private String snmp_object_category;

    private String daintree_device_name;
    private String daintree_point_category;
    private String daintree_point_name;
    private String measuring_instrument_name;
    private String measuring_instrument_category;
    private String service_name;
    private String qrcode_link;
    private String service_type;
    private SensorAlertDTO sensor;
    private DeviceAlertDTO device;
    private Set<UserDTO> profile_users;
    private VdmsDTO vdms;

    public AlertDTO() {
        super();
    }


    //Device Alert DTO
    public AlertDTO(Integer email_alert, Integer sms_alert, String vdms_id, String customer_org_id,
                    String vendor_org_id, String docker_name, String docker_system_type, String device_name,
                    String product_id, String building, String floor, String location, Integer device_monitor,
                    Integer local_vendor_email_alert, Integer local_vendor_sms_alert, String local_vendor_name,
                    String local_vendor_email, String local_vendor_extension, String local_vendor_phone) {
        super();
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.vdms_id = vdms_id;
        this.customer_org_id = customer_org_id;
        this.vendor_org_id = vendor_org_id;
        this.docker_name = docker_name;
        this.docker_system_type = docker_system_type;
        this.device_name = device_name;
        this.product_id = product_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.device_monitor = device_monitor;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    // Bacnet Alert DTO
    public AlertDTO(String bacnet_object_name, String bacnet_object_category, String unit, String bacnet_device_name,
                    String docker_name, String device_name, String product_id, Integer device_monitor, Integer email_alert, Integer sms_alert, String vendor_org_id, String vdms_id,
                    String docker_system_type, String customer_org_id, String building, String floor, String location, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone) {
        super();
        this.bacnet_object_name = bacnet_object_name;
        this.bacnet_object_category = bacnet_object_category;
        this.unit = unit;
        this.bacnet_device_name = bacnet_device_name;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.device_monitor = device_monitor;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.vendor_org_id = vendor_org_id;
        this.vdms_id = vdms_id;
        this.docker_system_type = docker_system_type;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    //Lorawan Alert DTO
    public AlertDTO(String lorawan_sensor_attribute_name, String unit, String lorawan_sensor_name, String lorawan_sensor_type,
                    String docker_name, Integer email_alert, Integer sms_alert, String device_name, String product_id, Integer device_monitor,
                    String docker_system_type, String vdms_id, String vendor_org_id, String customer_org_id,
                    String building, String floor, String location, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone) {
        super();
        this.lorawan_sensor_attribute_name = lorawan_sensor_attribute_name;
        this.unit = unit;
        this.lorawan_sensor_name = lorawan_sensor_name;
        this.lorawan_sensor_type = lorawan_sensor_type;
        this.docker_name = docker_name;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.device_name = device_name;
        this.product_id = product_id;
        this.device_monitor = device_monitor;
        this.docker_system_type = docker_system_type;
        this.vdms_id = vdms_id;
        this.vendor_org_id = vendor_org_id;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    //Disruptive Alert DTO
    public AlertDTO(String disruptive_sensor_name, String disruptive_sensor_type, String unit, String vdms_id, String docker_name,
                    String device_name, String product_id, Integer email_alert, Integer sms_alert, Integer device_monitor, String docker_system_type,
                    String vendor_org_id, String customer_org_id, String building, String floor, String location, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone) {
        super();
        this.disruptive_sensor_name = disruptive_sensor_name;
        this.disruptive_sensor_type = disruptive_sensor_type;
        this.unit = unit;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.device_monitor = device_monitor;
        this.docker_system_type = docker_system_type;
        this.vendor_org_id = vendor_org_id;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    //My Devices Alert DTO
    public AlertDTO(String my_devices_sensor_attribute_name, String unit, String my_devices_sensor_name, String my_devices_sensor_type,
                    String docker_name, String device_name, String product_id, Integer device_monitor,
                    String docker_system_type, String vdms_id, String vendor_org_id, Integer email_alert, Integer sms_alert, String customer_org_id,
                    String building, String floor, String location, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone) {
        super();
        this.my_devices_sensor_attributes_name = my_devices_sensor_attribute_name;
        this.unit = unit;
        this.my_devices_sensor_name = my_devices_sensor_name;
        this.my_devices_sensor_type = my_devices_sensor_type;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.device_monitor = device_monitor;
        this.docker_system_type = docker_system_type;
        this.vdms_id = vdms_id;
        this.vendor_org_id = vendor_org_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    //Ticket Alert DTO
    public AlertDTO(Integer email_alert, Integer sms_alert, String vdms_id, String customer_org_id,
                    String vendor_org_id, String docker_name, String docker_system_type, String device_name,
                    String product_id, String building, String floor, String location, Integer device_monitor,
                    String ticket_number, String ticket_status, String ticket_user_message, String ticket_vendor_message,
                    String ticket_vendor_email, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                    String local_vendor_name, String local_vendor_email, String local_vendor_extension, String local_vendor_phone) {
        super();
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.vdms_id = vdms_id;
        this.customer_org_id = customer_org_id;
        this.vendor_org_id = vendor_org_id;
        this.docker_name = docker_name;
        this.docker_system_type = docker_system_type;
        this.device_name = device_name;
        this.product_id = product_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.device_monitor = device_monitor;
        this.ticket_number = ticket_number;
        this.ticket_status = ticket_status;
        this.ticket_user_message = ticket_user_message;
        this.ticket_vendor_message = ticket_vendor_message;
        this.ticket_vendor_email = ticket_vendor_email;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    //Monnit Sensor
    public AlertDTO(Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email,
                    String local_vendor_extension, String local_vendor_phone, String monnit_sensor_name, String monnit_sensor_category,
                    String unit, String vdms_id, String docker_name, String device_name, String product_id, Integer email_alert, Integer sms_alert,
                    Integer device_monitor, String docker_system_type, String vendor_org_id, String customer_org_id, String building, String floor,
                    String location) {
        super();
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
        this.monnit_sensor_name = monnit_sensor_name;
        this.monnit_sensor_category = monnit_sensor_category;
        this.unit = unit;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.device_monitor = device_monitor;
        this.docker_system_type = docker_system_type;
        this.vendor_org_id = vendor_org_id;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
    }

    //Pelican Alert DTO
    public AlertDTO(String pelican_sensor_attribute_name, String unit, String pelican_sensor_name, String pelican_sensor_type,
                    String docker_name, String device_name, String product_id,
                    String docker_system_type, Integer device_monitor, String vdms_id, String vendor_org_id, Integer email_alert, Integer sms_alert, String customer_org_id,
                    String building, String floor, String location, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone) {
        super();
        this.pelican_sensor_attributes_name = pelican_sensor_attribute_name;
        this.unit = unit;
        this.pelican_sensor_name = pelican_sensor_name;
        this.pelican_sensor_type = pelican_sensor_type;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.docker_system_type = docker_system_type;
        this.device_monitor = device_monitor;
        this.vdms_id = vdms_id;
        this.vendor_org_id = vendor_org_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    // KNX Alert DTO
    public AlertDTO(String knx_group_name, String knx_group_category, String unit,
                    String docker_name, String device_name, String product_id, Integer device_monitor, Integer email_alert, Integer sms_alert, String vendor_org_id, String vdms_id,
                    String docker_system_type, String customer_org_id, String building, String floor, String location, Integer local_vendor_email_alert,
                    String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone, Integer local_vendor_sms_alert) {
        super();
        this.knx_group_name = knx_group_name;
        this.knx_group_category = knx_group_category;
        this.unit = unit;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.device_monitor = device_monitor;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.vendor_org_id = vendor_org_id;
        this.vdms_id = vdms_id;
        this.docker_system_type = docker_system_type;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
    }

    //Snmp Object Alert DTO
    public AlertDTO(String snmp_object_name, String snmp_object_category, String unit,
                    String docker_name, String device_name, String product_id, Integer device_monitor, Integer email_alert, Integer sms_alert, String vendor_org_id, String vdms_id,
                    String docker_system_type, String customer_org_id, String building, String floor, String location, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email, String local_vendor_extension,
                    String local_vendor_phone) {
        super();
        this.snmp_object_name = snmp_object_name;
        this.snmp_object_category = snmp_object_category;
        this.unit = unit;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.device_monitor = device_monitor;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.vendor_org_id = vendor_org_id;
        this.vdms_id = vdms_id;
        this.docker_system_type = docker_system_type;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
    }

    //Measuring Instrument DTO
    public AlertDTO(Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email,
                    String local_vendor_extension, String local_vendor_phone,
                    String unit, String vdms_id, String docker_name, String device_name, String product_id, Integer email_alert, Integer sms_alert,
                    Integer device_monitor, String docker_system_type, String vendor_org_id, String customer_org_id, String building, String floor,
                    String location, String measuring_instrument_name, String measuring_instrument_category) {
        super();
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
        this.unit = unit;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.device_monitor = device_monitor;
        this.docker_system_type = docker_system_type;
        this.vendor_org_id = vendor_org_id;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.measuring_instrument_name = measuring_instrument_name;
        this.measuring_instrument_category = measuring_instrument_category;
    }

    // Services Alert Info
    public AlertDTO(Integer email_alert, Integer sms_alert, String user_email, String device_name, String building, String floor, String location, String service_name) {
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.user_email = user_email;
        this.device_name = device_name;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.service_name = service_name;
    }

    //daintree
    public AlertDTO(String daintree_point_name, String unit, Integer local_vendor_email_alert,
                    Integer local_vendor_sms_alert, String local_vendor_name, String local_vendor_email,
                    String local_vendor_extension, String local_vendor_phone,
                    String vdms_id, String docker_name, String device_name, String product_id, Integer email_alert, Integer sms_alert,
                    Integer device_monitor, String docker_system_type, String vendor_org_id, String customer_org_id, String building, String floor,
                    String location, String daintree_device_name, String daintree_point_category) {
        super();
        this.daintree_point_name = daintree_point_name;
        this.unit = unit;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.local_vendor_name = local_vendor_name;
        this.local_vendor_email = local_vendor_email;
        this.local_vendor_extension = local_vendor_extension;
        this.local_vendor_phone = local_vendor_phone;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.device_name = device_name;
        this.product_id = product_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.device_monitor = device_monitor;
        this.docker_system_type = docker_system_type;
        this.vendor_org_id = vendor_org_id;
        this.customer_org_id = customer_org_id;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.daintree_device_name = daintree_device_name;
        this.daintree_point_category = daintree_point_category;
    }


    public Integer getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(Integer alert_type) {
        this.alert_type = alert_type;
    }

    public Integer getEmail_alert() {
        return email_alert;
    }

    public void setEmail_alert(Integer email_alert) {
        this.email_alert = email_alert;
    }

    public Integer getSms_alert() {
        return sms_alert;
    }

    public void setSms_alert(Integer sms_alert) {
        this.sms_alert = sms_alert;
    }

    public Integer getLocal_vendor_email_alert() {
        return local_vendor_email_alert;
    }

    public void setLocal_vendor_email_alert(Integer local_vendor_email_alert) {
        this.local_vendor_email_alert = local_vendor_email_alert;
    }

    public Integer getLocal_vendor_sms_alert() {
        return local_vendor_sms_alert;
    }

    public void setLocal_vendor_sms_alert(Integer local_vendor_sms_alert) {
        this.local_vendor_sms_alert = local_vendor_sms_alert;
    }

    public String getLocal_vendor_name() {
        return local_vendor_name;
    }

    public void setLocal_vendor_name(String local_vendor_name) {
        this.local_vendor_name = local_vendor_name;
    }

    public String getLocal_vendor_email() {
        return local_vendor_email;
    }

    public void setLocal_vendor_email(String local_vendor_email) {
        this.local_vendor_email = local_vendor_email;
    }

    public String getLocal_vendor_extension() {
        return local_vendor_extension;
    }

    public void setLocal_vendor_extension(String local_vendor_extension) {
        this.local_vendor_extension = local_vendor_extension;
    }

    public String getLocal_vendor_phone() {
        return local_vendor_phone;
    }

    public void setLocal_vendor_phone(String local_vendor_phone) {
        this.local_vendor_phone = local_vendor_phone;
    }

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public String getCustomer_org_id() {
        return customer_org_id;
    }

    public void setCustomer_org_id(String customer_org_id) {
        this.customer_org_id = customer_org_id;
    }

    public String getVendor_org_id() {
        return vendor_org_id;
    }

    public void setVendor_org_id(String vendor_org_id) {
        this.vendor_org_id = vendor_org_id;
    }

    public String getVendor_email() {
        return vendor_email;
    }

    public void setVendor_email(String vendor_email) {
        this.vendor_email = vendor_email;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
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

    public Integer getRemote_access_otp() {
        return remote_access_otp;
    }

    public void setRemote_access_otp(Integer remote_access_otp) {
        this.remote_access_otp = remote_access_otp;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Integer getDevice_monitor() {
        return device_monitor;
    }

    public void setDevice_monitor(Integer device_monitor) {
        this.device_monitor = device_monitor;
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

    public String getBacnet_device_name() {
        return bacnet_device_name;
    }

    public void setBacnet_device_name(String bacnet_device_name) {
        this.bacnet_device_name = bacnet_device_name;
    }

    public String getBacnet_object_name() {
        return bacnet_object_name;
    }

    public void setBacnet_object_name(String bacnet_object_name) {
        this.bacnet_object_name = bacnet_object_name;
    }

    public String getBacnet_object_category() {
        return bacnet_object_category;
    }

    public void setBacnet_object_category(String bacnet_object_category) {
        this.bacnet_object_category = bacnet_object_category;
    }

    public String getLorawan_sensor_name() {
        return lorawan_sensor_name;
    }

    public void setLorawan_sensor_name(String lorawan_sensor_name) {
        this.lorawan_sensor_name = lorawan_sensor_name;
    }

    public String getLorawan_sensor_attribute_name() {
        return lorawan_sensor_attribute_name;
    }

    public void setLorawan_sensor_attribute_name(String lorawan_sensor_attribute_name) {
        this.lorawan_sensor_attribute_name = lorawan_sensor_attribute_name;
    }

    public String getLorawan_sensor_type() {
        return lorawan_sensor_type;
    }

    public void setLorawan_sensor_type(String lorawan_sensor_type) {
        this.lorawan_sensor_type = lorawan_sensor_type;
    }

    public String getDisruptive_sensor_name() {
        return disruptive_sensor_name;
    }

    public void setDisruptive_sensor_name(String disruptive_sensor_name) {
        this.disruptive_sensor_name = disruptive_sensor_name;
    }

    public String getDisruptive_sensor_type() {
        return disruptive_sensor_type;
    }

    public void setDisruptive_sensor_type(String disruptive_sensor_type) {
        this.disruptive_sensor_type = disruptive_sensor_type;
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

    public String getTicket_number() {
        return ticket_number;
    }

    public void setTicket_number(String ticket_number) {
        this.ticket_number = ticket_number;
    }

    public Integer getTicket_type() {
        return ticket_type;
    }

    public void setTicket_type(Integer ticket_type) {
        this.ticket_type = ticket_type;
    }

    public String getTicket_status() {
        return ticket_status;
    }

    public void setTicket_status(String ticket_status) {
        this.ticket_status = ticket_status;
    }

    public String getTicket_user_message() {
        return ticket_user_message;
    }

    public void setTicket_user_message(String ticket_user_message) {
        this.ticket_user_message = ticket_user_message;
    }

    public String getTicket_vendor_message() {
        return ticket_vendor_message;
    }

    public void setTicket_vendor_message(String ticket_vendor_message) {
        this.ticket_vendor_message = ticket_vendor_message;
    }

    public String getTicket_vendor_email() {
        return ticket_vendor_email;
    }

    public void setTicket_vendor_email(String ticket_vendor_email) {
        this.ticket_vendor_email = ticket_vendor_email;
    }

    public String getMy_devices_sensor_attributes_name() {
        return my_devices_sensor_attributes_name;
    }

    public void setMy_devices_sensor_attributes_name(String my_devices_sensor_attributes_name) {
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
    }

    public String getMy_devices_sensor_name() {
        return my_devices_sensor_name;
    }

    public void setMy_devices_sensor_name(String my_devices_sensor_name) {
        this.my_devices_sensor_name = my_devices_sensor_name;
    }

    public String getMy_devices_sensor_type() {
        return my_devices_sensor_type;
    }

    public void setMy_devices_sensor_type(String my_devices_sensor_type) {
        this.my_devices_sensor_type = my_devices_sensor_type;
    }

    public String getMonnit_sensor_name() {
        return monnit_sensor_name;
    }

    public void setMonnit_sensor_name(String monnit_sensor_name) {
        this.monnit_sensor_name = monnit_sensor_name;
    }

    public String getMonnit_sensor_category() {
        return monnit_sensor_category;
    }

    public void setMonnit_sensor_category(String monnit_sensor_category) {
        this.monnit_sensor_category = monnit_sensor_category;
    }

    public String getPelican_sensor_attributes_name() {
        return pelican_sensor_attributes_name;
    }

    public void setPelican_sensor_attributes_name(String pelican_sensor_attributes_name) {
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
    }

    public String getPelican_sensor_name() {
        return pelican_sensor_name;
    }

    public void setPelican_sensor_name(String pelican_sensor_name) {
        this.pelican_sensor_name = pelican_sensor_name;
    }

    public String getPelican_sensor_type() {
        return pelican_sensor_type;
    }

    public void setPelican_sensor_type(String pelican_sensor_type) {
        this.pelican_sensor_type = pelican_sensor_type;
    }

    public String getKnx_group_name() {
        return knx_group_name;
    }

    public void setKnx_group_name(String knx_group_name) {
        this.knx_group_name = knx_group_name;
    }

    public String getKnx_group_category() {
        return knx_group_category;
    }

    public void setKnx_group_category(String knx_group_category) {
        this.knx_group_category = knx_group_category;
    }

    public String getSnmp_object_name() {
        return snmp_object_name;
    }

    public void setSnmp_object_name(String snmp_object_name) {
        this.snmp_object_name = snmp_object_name;
    }

    public String getSnmp_object_category() {
        return snmp_object_category;
    }

    public void setSnmp_object_category(String snmp_object_category) {
        this.snmp_object_category = snmp_object_category;
    }

    public String getMeasuring_instrument_name() {
        return measuring_instrument_name;
    }

    public void setMeasuring_instrument_name(String measuring_instrument_name) {
        this.measuring_instrument_name = measuring_instrument_name;
    }

    public String getMeasuring_instrument_category() {
        return measuring_instrument_category;
    }

    public void setMeasuring_instrument_category(String measuring_instrument_category) {
        this.measuring_instrument_category = measuring_instrument_category;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getQrcode_link() {
        return qrcode_link;
    }

    public void setQrcode_link(String qrcode_link) {
        this.qrcode_link = qrcode_link;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public String getDaintree_device_name() {
        return daintree_device_name;
    }

    public void setDaintree_device_name(String daintree_device_name) {
        this.daintree_device_name = daintree_device_name;
    }

    public String getDaintree_point_category() {
        return daintree_point_category;
    }

    public void setDaintree_point_category(String daintree_point_category) {
        this.daintree_point_category = daintree_point_category;
    }

    public String getDaintree_point_name() {
        return daintree_point_name;
    }

    public void setDaintree_point_name(String daintree_point_name) {
        this.daintree_point_name = daintree_point_name;
    }

    public SensorAlertDTO getSensor() {
        return sensor;
    }

    public void setSensor(SensorAlertDTO sensor) {
        this.sensor = sensor;
    }

    public DeviceAlertDTO getDevice() {
        return device;
    }

    public void setDevice(DeviceAlertDTO device) {
        this.device = device;
    }

    public Set<UserDTO> getProfile_users() {
        return profile_users;
    }

    public void setProfile_users(Set<UserDTO> profile_users) {
        this.profile_users = profile_users;
    }

    public VdmsDTO getVdms() {
        return vdms;
    }

    public void setVdms(VdmsDTO vdms) {
        this.vdms = vdms;
    }

    @Override
    public String toString() {
        return "AlertDTO{" +
                "alert_type=" + alert_type +
                ", email_alert=" + email_alert +
                ", sms_alert=" + sms_alert +
                ", local_vendor_email_alert=" + local_vendor_email_alert +
                ", local_vendor_sms_alert=" + local_vendor_sms_alert +
                ", local_vendor_name='" + local_vendor_name + '\'' +
                ", local_vendor_email='" + local_vendor_email + '\'' +
                ", local_vendor_extension='" + local_vendor_extension + '\'' +
                ", local_vendor_phone='" + local_vendor_phone + '\'' +
                ", vdms_id='" + vdms_id + '\'' +
                ", customer_org_id='" + customer_org_id + '\'' +
                ", vendor_org_id='" + vendor_org_id + '\'' +
                ", vendor_email='" + vendor_email + '\'' +
                ", user_email='" + user_email + '\'' +
                ", docker_name='" + docker_name + '\'' +
                ", docker_system_type='" + docker_system_type + '\'' +
                ", remote_access_otp=" + remote_access_otp +
                ", timestamp=" + timestamp +
                ", timezone='" + timezone + '\'' +
                ", device_name='" + device_name + '\'' +
                ", product_id='" + product_id + '\'' +
                ", device_monitor=" + device_monitor +
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                ", location='" + location + '\'' +
                ", bacnet_device_name='" + bacnet_device_name + '\'' +
                ", bacnet_object_name='" + bacnet_object_name + '\'' +
                ", bacnet_object_category='" + bacnet_object_category + '\'' +
                ", lorawan_sensor_name='" + lorawan_sensor_name + '\'' +
                ", lorawan_sensor_attribute_name='" + lorawan_sensor_attribute_name + '\'' +
                ", lorawan_sensor_type='" + lorawan_sensor_type + '\'' +
                ", disruptive_sensor_name='" + disruptive_sensor_name + '\'' +
                ", disruptive_sensor_type='" + disruptive_sensor_type + '\'' +
                ", value='" + value + '\'' +
                ", alert_message='" + alert_message + '\'' +
                ", unit='" + unit + '\'' +
                ", ticket_number='" + ticket_number + '\'' +
                ", ticket_type=" + ticket_type +
                ", ticket_status='" + ticket_status + '\'' +
                ", ticket_user_message='" + ticket_user_message + '\'' +
                ", ticket_vendor_message='" + ticket_vendor_message + '\'' +
                ", ticket_vendor_email='" + ticket_vendor_email + '\'' +
                ", my_devices_sensor_attributes_name='" + my_devices_sensor_attributes_name + '\'' +
                ", my_devices_sensor_name='" + my_devices_sensor_name + '\'' +
                ", my_devices_sensor_type='" + my_devices_sensor_type + '\'' +
                ", monnit_sensor_name='" + monnit_sensor_name + '\'' +
                ", monnit_sensor_category='" + monnit_sensor_category + '\'' +
                ", pelican_sensor_attributes_name='" + pelican_sensor_attributes_name + '\'' +
                ", pelican_sensor_name='" + pelican_sensor_name + '\'' +
                ", pelican_sensor_type='" + pelican_sensor_type + '\'' +
                ", knx_group_name='" + knx_group_name + '\'' +
                ", knx_group_category='" + knx_group_category + '\'' +
                ", snmp_object_name='" + snmp_object_name + '\'' +
                ", snmp_object_category='" + snmp_object_category + '\'' +
                ", daintree_device_name='" + daintree_device_name + '\'' +
                ", daintree_point_category='" + daintree_point_category + '\'' +
                ", daintree_point_name='" + daintree_point_name + '\'' +
                ", measuring_instrument_name='" + measuring_instrument_name + '\'' +
                ", measuring_instrument_category='" + measuring_instrument_category + '\'' +
                ", service_name='" + service_name + '\'' +
                ", qrcode_link='" + qrcode_link + '\'' +
                ", service_type='" + service_type + '\'' +
                ", sensor=" + sensor +
                ", device=" + device +
                ", profile_users=" + profile_users +
                ", vdms=" + vdms +
                '}';
    }
}
