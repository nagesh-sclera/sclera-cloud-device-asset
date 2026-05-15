package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryDTO {

    private String id;

    private Integer type;

    private String name;

    private Integer device_alarm;

    private String device_display_name;

    private String device_user_data_name;

    private String device_ip_address;

    private String device_old_ip_address;

    private String remote_session_type;

    private Integer private_port;

    private String public_ip_address;

    private String vendor_email;

    private String bacnet_device_name;

    private String bacnet_object_name;

    private String bacnet_object_user_data_name;

    private String bacnet_object_category;

    private String lorawan_sensor_name;

    private String lorawan_sensor_type;

    private String disruptive_sensor_name;

    private String disruptive_sensor_user_data_name;

    private String disruptive_sensor_type;

    private String snmp_device_name;

    private String value;

//	private String user_data_value;

    private String unit;

    private String alert_message;

    private String ticket_number;

    private Integer ticket_type;

    private String ticket_status;

    private String ticket_user_message;

    private String ticket_vendor_message;

    private BigInteger timestamp;

    private String vdms_id;

    private String docker_name;

    private String device_id;

    private String bacnet_device_id;
    private String bacnet_object_id;

    private String lorawan_sensor_id;
    private String lorawan_sensor_attributes_name;

    private String ticket_id;

    private String disruptive_sensor_id;

    private String snmp_device_id;

    private String location;

    private String my_devices_sensor_name;
    private String my_devices_sensor_user_data_name;
    private String my_devices_sensor_type;
    private String my_devices_sensor_id;
    private String my_devices_sensor_attributes_name;

    private String monnit_sensor_id;
    private String monnit_sensor_name;
    private String monnit_sensor_user_data_name;
    private String monnit_sensor_category;

    private String pelican_sensor_name;
    private String pelican_sensor_user_data_name;
    private String pelican_sensor_type;
    private String pelican_sensor_id;
    private String pelican_sensor_attributes_name;

    private String knx_device_address;
    private String knx_group_address;
    private String knx_group_user_data_name;
    private String knx_group_category;
    private String knx_device_name;
    private String knx_group_name;
    
    private String snmp_object_oid;
    private String snmp_device_configuration_id;
    private String snmp_object_name;
    private String snmp_object_category;

    private String measuring_instrument_id;
    private String measuring_instrument_name;
    private String measuring_instrument_user_data_name;
    private String measuring_instrument_category;

    private String daintree_point_id;
    private String daintree_point_name;
    private String daintree_point_category;
    private String daintree_device_id;
    private String daintree_device_name;
    private String daintree_device_user_data_name;

    private String ecobee_sensor_name;
    private String ecobee_sensor_user_data_name;
    private String ecobee_sensor_type;
    private String ecobee_sensor_id;
    private String ecobee_sensor_attributes_name;

    private String created_email;
    private String status;

    private String modbus_device_name;
    private String modbus_register_name;
    private String modbus_register_user_data_name;
    private String modbus_register_category;
    private String modbus_register_id;

    private String sub_type;
    private String assignee_email;
    private String assignee_name;
    private String device_name;
    private Integer count;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDevice_alarm() {
        return device_alarm;
    }

    public void setDevice_alarm(Integer device_alarm) {
        this.device_alarm = device_alarm;
    }

    public String getDevice_display_name() {
        return device_display_name;
    }

    public void setDevice_display_name(String device_display_name) {
        this.device_display_name = device_display_name;
    }

    public String getDevice_user_data_name() {
        return device_user_data_name;
    }

    public void setDevice_user_data_name(String device_user_data_name) {
        this.device_user_data_name = device_user_data_name;
    }

    public String getDevice_ip_address() {
        return device_ip_address;
    }

    public void setDevice_ip_address(String device_ip_address) {
        this.device_ip_address = device_ip_address;
    }

    public String getDevice_old_ip_address() {
        return device_old_ip_address;
    }

    public void setDevice_old_ip_address(String device_old_ip_address) {
        this.device_old_ip_address = device_old_ip_address;
    }

    public String getRemote_session_type() {
        return remote_session_type;
    }

    public void setRemote_session_type(String remote_session_type) {
        this.remote_session_type = remote_session_type;
    }

    public Integer getPrivate_port() {
        return private_port;
    }

    public void setPrivate_port(Integer private_port) {
        this.private_port = private_port;
    }

    public String getPublic_ip_address() {
        return public_ip_address;
    }

    public void setPublic_ip_address(String public_ip_address) {
        this.public_ip_address = public_ip_address;
    }

    public String getVendor_email() {
        return vendor_email;
    }

    public void setVendor_email(String vendor_email) {
        this.vendor_email = vendor_email;
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

    public String getBacnet_object_user_data_name() {
        return bacnet_object_user_data_name;
    }

    public void setBacnet_object_user_data_name(String bacnet_object_user_data_name) {
        this.bacnet_object_user_data_name = bacnet_object_user_data_name;
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

    public String getDisruptive_sensor_user_data_name() {
        return disruptive_sensor_user_data_name;
    }

    public void setDisruptive_sensor_user_data_name(String disruptive_sensor_user_data_name) {
        this.disruptive_sensor_user_data_name = disruptive_sensor_user_data_name;
    }

    public String getDisruptive_sensor_type() {
        return disruptive_sensor_type;
    }

    public void setDisruptive_sensor_type(String disruptive_sensor_type) {
        this.disruptive_sensor_type = disruptive_sensor_type;
    }

    public String getSnmp_device_name() {
        return snmp_device_name;
    }

    public void setSnmp_device_name(String snmp_device_name) {
        this.snmp_device_name = snmp_device_name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

//	public String getUser_data_value() {
//		return user_data_value;
//	}
//
//	public void setUser_data_value(String user_data_value) {
//		this.user_data_value = user_data_value;
//	}

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
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

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public String getDocker_name() {
        return docker_name;
    }

    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getBacnet_device_id() {
        return bacnet_device_id;
    }

    public void setBacnet_device_id(String bacnet_device_id) {
        this.bacnet_device_id = bacnet_device_id;
    }

    public String getBacnet_object_id() {
        return bacnet_object_id;
    }

    public void setBacnet_object_id(String bacnet_object_id) {
        this.bacnet_object_id = bacnet_object_id;
    }

    public String getLorawan_sensor_id() {
        return lorawan_sensor_id;
    }

    public void setLorawan_sensor_id(String lorawan_sensor_id) {
        this.lorawan_sensor_id = lorawan_sensor_id;
    }

    public String getLorawan_sensor_attributes_name() {
        return lorawan_sensor_attributes_name;
    }

    public void setLorawan_sensor_attributes_name(String lorawan_sensor_attributes_name) {
        this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name;
    }

    public String getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(String ticket_id) {
        this.ticket_id = ticket_id;
    }

    public String getDisruptive_sensor_id() {
        return disruptive_sensor_id;
    }

    public void setDisruptive_sensor_id(String disruptive_sensor_id) {
        this.disruptive_sensor_id = disruptive_sensor_id;
    }

    public String getSnmp_device_id() {
        return snmp_device_id;
    }

    public void setSnmp_device_id(String snmp_device_id) {
        this.snmp_device_id = snmp_device_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMy_devices_sensor_name() {
        return my_devices_sensor_name;
    }

    public void setMy_devices_sensor_name(String my_devices_sensor_name) {
        this.my_devices_sensor_name = my_devices_sensor_name;
    }

    public String getMy_devices_sensor_user_data_name() {
        return my_devices_sensor_user_data_name;
    }

    public void setMy_devices_sensor_user_data_name(String my_devices_sensor_user_data_name) {
        this.my_devices_sensor_user_data_name = my_devices_sensor_user_data_name;
    }

    public String getMy_devices_sensor_type() {
        return my_devices_sensor_type;
    }

    public void setMy_devices_sensor_type(String my_devices_sensor_type) {
        this.my_devices_sensor_type = my_devices_sensor_type;
    }

    public String getMy_devices_sensor_id() {
        return my_devices_sensor_id;
    }

    public void setMy_devices_sensor_id(String my_devices_sensor_id) {
        this.my_devices_sensor_id = my_devices_sensor_id;
    }

    public String getMy_devices_sensor_attributes_name() {
        return my_devices_sensor_attributes_name;
    }

    public void setMy_devices_sensor_attributes_name(String my_devices_sensor_attributes_name) {
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
    }

    public String getMonnit_sensor_id() {
        return monnit_sensor_id;
    }

    public void setMonnit_sensor_id(String monnit_sensor_id) {
        this.monnit_sensor_id = monnit_sensor_id;
    }

    public String getMonnit_sensor_name() {
        return monnit_sensor_name;
    }

    public void setMonnit_sensor_name(String monnit_sensor_name) {
        this.monnit_sensor_name = monnit_sensor_name;
    }

    public String getMonnit_sensor_user_data_name() {
        return monnit_sensor_user_data_name;
    }

    public void setMonnit_sensor_user_data_name(String monnit_sensor_user_data_name) {
        this.monnit_sensor_user_data_name = monnit_sensor_user_data_name;
    }

    public String getMonnit_sensor_category() {
        return monnit_sensor_category;
    }

    public void setMonnit_sensor_category(String monnit_sensor_category) {
        this.monnit_sensor_category = monnit_sensor_category;
    }

    public String getPelican_sensor_name() {
        return pelican_sensor_name;
    }

    public void setPelican_sensor_name(String pelican_sensor_name) {
        this.pelican_sensor_name = pelican_sensor_name;
    }

    public String getPelican_sensor_user_data_name() {
        return pelican_sensor_user_data_name;
    }

    public void setPelican_sensor_user_data_name(String pelican_sensor_user_data_name) {
        this.pelican_sensor_user_data_name = pelican_sensor_user_data_name;
    }

    public String getPelican_sensor_type() {
        return pelican_sensor_type;
    }

    public void setPelican_sensor_type(String pelican_sensor_type) {
        this.pelican_sensor_type = pelican_sensor_type;
    }

    public String getPelican_sensor_id() {
        return pelican_sensor_id;
    }

    public void setPelican_sensor_id(String pelican_sensor_id) {
        this.pelican_sensor_id = pelican_sensor_id;
    }

    public String getPelican_sensor_attributes_name() {
        return pelican_sensor_attributes_name;
    }

    public void setPelican_sensor_attributes_name(String pelican_sensor_attributes_name) {
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
    }

    public String getKnx_device_address() {
        return knx_device_address;
    }

    public void setKnx_device_address(String knx_device_address) {
        this.knx_device_address = knx_device_address;
    }

    public String getKnx_group_address() {
        return knx_group_address;
    }

    public void setKnx_group_address(String knx_group_address) {
        this.knx_group_address = knx_group_address;
    }

    public String getKnx_group_user_data_name() {
        return knx_group_user_data_name;
    }

    public void setKnx_group_user_data_name(String knx_group_user_data_name) {
        this.knx_group_user_data_name = knx_group_user_data_name;
    }

    public String getKnx_group_category() {
        return knx_group_category;
    }

    public void setKnx_group_category(String knx_group_category) {
        this.knx_group_category = knx_group_category;
    }

    public String getKnx_device_name() {
        return knx_device_name;
    }

    public void setKnx_device_name(String knx_device_name) {
        this.knx_device_name = knx_device_name;
    }

    public String getKnx_group_name() {
        return knx_group_name;
    }

    public void setKnx_group_name(String knx_group_name) {
        this.knx_group_name = knx_group_name;
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

    public String getSnmp_object_oid() {
        return snmp_object_oid;
    }

    public void setSnmp_object_oid(String snmp_object_oid) {
        this.snmp_object_oid = snmp_object_oid;
    }

    public String getSnmp_device_configuration_id() {
		return snmp_device_configuration_id;
	}

	public void setSnmp_device_configuration_id(String snmp_device_configuration_id) {
		this.snmp_device_configuration_id = snmp_device_configuration_id;
	}

    public String getMeasuring_instrument_id() {
        return measuring_instrument_id;
    }

    public void setMeasuring_instrument_id(String measuring_instrument_id) {
        this.measuring_instrument_id = measuring_instrument_id;
    }

    public String getMeasuring_instrument_name() {
        return measuring_instrument_name;
    }

    public void setMeasuring_instrument_name(String measuring_instrument_name) {
        this.measuring_instrument_name = measuring_instrument_name;
    }

    public String getMeasuring_instrument_user_data_name() {
        return measuring_instrument_user_data_name;
    }

    public void setMeasuring_instrument_user_data_name(String measuring_instrument_user_data_name) {
        this.measuring_instrument_user_data_name = measuring_instrument_user_data_name;
    }

    public String getMeasuring_instrument_category() {
        return measuring_instrument_category;
    }

    public void setMeasuring_instrument_category(String measuring_instrument_category) {
        this.measuring_instrument_category = measuring_instrument_category;
    }

    public String getDaintree_point_id() {
        return daintree_point_id;
    }

    public void setDaintree_point_id(String daintree_point_id) {
        this.daintree_point_id = daintree_point_id;
    }

    public String getDaintree_point_name() {
        return daintree_point_name;
    }

    public void setDaintree_point_name(String daintree_point_name) {
        this.daintree_point_name = daintree_point_name;
    }

    public String getDaintree_point_category() {
        return daintree_point_category;
    }

    public void setDaintree_point_category(String daintree_point_category) {
        this.daintree_point_category = daintree_point_category;
    }

    public String getDaintree_device_id() {
        return daintree_device_id;
    }

    public void setDaintree_device_id(String daintree_device_id) {
        this.daintree_device_id = daintree_device_id;
    }

    public String getDaintree_device_name() {
        return daintree_device_name;
    }

    public void setDaintree_device_name(String daintree_device_name) {
        this.daintree_device_name = daintree_device_name;
    }

    public String getDaintree_device_user_data_name() {
        return daintree_device_user_data_name;
    }

    public void setDaintree_device_user_data_name(String daintree_device_user_data_name) {
        this.daintree_device_user_data_name = daintree_device_user_data_name;
    }


    public String getEcobee_sensor_name() {
        return ecobee_sensor_name;
    }

    public void setEcobee_sensor_name(String ecobee_sensor_name) {
        this.ecobee_sensor_name = ecobee_sensor_name;
    }

    public String getEcobee_sensor_user_data_name() {
        return ecobee_sensor_user_data_name;
    }

    public void setEcobee_sensor_user_data_name(String ecobee_sensor_user_data_name) {
        this.ecobee_sensor_user_data_name = ecobee_sensor_user_data_name;
    }

    public String getEcobee_sensor_type() {
        return ecobee_sensor_type;
    }

    public void setEcobee_sensor_type(String ecobee_sensor_type) {
        this.ecobee_sensor_type = ecobee_sensor_type;
    }

    public String getEcobee_sensor_id() {
        return ecobee_sensor_id;
    }

    public void setEcobee_sensor_id(String ecobee_sensor_id) {
        this.ecobee_sensor_id = ecobee_sensor_id;
    }

    public String getEcobee_sensor_attributes_name() {
        return ecobee_sensor_attributes_name;
    }

    public void setEcobee_sensor_attributes_name(String ecobee_sensor_attributes_name) {
        this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name;
    }

    public String getCreated_email() {
        return created_email;
    }

    public void setCreated_email(String created_email) {
        this.created_email = created_email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModbus_device_name() {
        return modbus_device_name;
    }

    public void setModbus_device_name(String modbus_device_name) {
        this.modbus_device_name = modbus_device_name;
    }

    public String getModbus_register_name() {
        return modbus_register_name;
    }

    public void setModbus_register_name(String modbus_register_name) {
        this.modbus_register_name = modbus_register_name;
    }

    public String getModbus_register_user_data_name() {
        return modbus_register_user_data_name;
    }

    public void setModbus_register_user_data_name(String modbus_register_user_data_name) {
        this.modbus_register_user_data_name = modbus_register_user_data_name;
    }

    public String getModbus_register_category() {
        return modbus_register_category;
    }

    public void setModbus_register_category(String modbus_register_category) {
        this.modbus_register_category = modbus_register_category;
    }
    public String getModbus_register_id() {
        return modbus_register_id;
    }

    public void setModbus_register_id(String modbus_register_id) {
        this.modbus_register_id = modbus_register_id;
    }

    public String getSub_type() {
        return sub_type;
    }

    public void setSub_type(String sub_type) {
        this.sub_type = sub_type;
    }

    public String getAssignee_email() {
        return assignee_email;
    }

    public void setAssignee_email(String assignee_email) {
        this.assignee_email = assignee_email;
    }

    public String getAssignee_name() {
        return assignee_name;
    }

    public void setAssignee_name(String assignee_name) {
        this.assignee_name = assignee_name;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public HistoryDTO() {
        super();
    }

    public HistoryDTO(String id, Integer type, String name, Integer device_alarm, String device_ip_address, String device_old_ip_address, String remote_session_type,
                      Integer private_port, String public_ip_address, String vendor_email, BigInteger timestamp, String ticket_number,
                      Integer ticket_type, String ticket_status, String ticket_user_message, String ticket_vendor_message,
                      String value, String unit, String alert_message, String bacnet_device_name, String bacnet_object_name,
                      String bacnet_object_category, String lorawan_sensor_name, String lorawan_sensor_attributes_name, String lorawan_sensor_type,
                      String disruptive_sensor_name, String disruptive_sensor_type, String my_devices_sensor_name,
                      String my_devices_sensor_attributes_name, String my_devices_sensor_type, String monnit_sensor_name, String monnit_sensor_category,
                      String pelican_sensor_name, String pelican_sensor_attributes_name, String pelican_sensor_type, String knx_group_category,
                       String knx_group_name, String snmp_object_name,String snmp_object_category, String measuring_instrument_name, String measuring_instrument_category,
                      String daintree_point_id, String daintree_point_category, String daintree_point_name, String daintree_device_id, String daintree_device_name,
                      String daintree_device_user_data_name,String ecobee_sensor_name, String ecobee_sensor_attributes_name, String ecobee_sensor_type,
                      String created_email, String status, String modbus_device_name, String modbus_register_name, String modbus_register_category,String sub_type) {
        super();
        this.id = id;
        this.type = type;
        this.name = name;
        this.device_alarm = device_alarm;
        this.device_ip_address = device_ip_address;
        this.device_old_ip_address = device_old_ip_address;
        this.remote_session_type = remote_session_type;
        this.private_port = private_port;
        this.public_ip_address = public_ip_address;
        this.vendor_email = vendor_email;
        this.timestamp = timestamp;
        this.ticket_number = ticket_number;
        this.ticket_type = ticket_type;
        this.ticket_status = ticket_status;
        this.ticket_user_message = ticket_user_message;
        this.ticket_vendor_message = ticket_vendor_message;
        this.value = value;
        this.unit = unit;
        this.alert_message = alert_message;
        this.bacnet_device_name = bacnet_device_name;
        this.bacnet_object_name = bacnet_object_name;
        this.bacnet_object_category = bacnet_object_category;
        this.lorawan_sensor_name = lorawan_sensor_name;
        this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name;
        this.lorawan_sensor_type = lorawan_sensor_type;
        this.disruptive_sensor_name = disruptive_sensor_name;
        this.disruptive_sensor_type = disruptive_sensor_type;
        this.my_devices_sensor_name = my_devices_sensor_name;
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
        this.my_devices_sensor_type = my_devices_sensor_type;
        this.monnit_sensor_name = monnit_sensor_name;
        this.monnit_sensor_category = monnit_sensor_category;
        this.pelican_sensor_name = pelican_sensor_name;
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
        this.pelican_sensor_type = pelican_sensor_type;
        this.knx_group_category = knx_group_category;
        this.knx_group_name = knx_group_name;
        this.snmp_object_name = snmp_object_name;
        this.snmp_object_category = snmp_object_category;
        this.measuring_instrument_name = measuring_instrument_name;
        this.measuring_instrument_category = measuring_instrument_category;
        this.daintree_point_id = daintree_point_id;
        this.daintree_point_category = daintree_point_category;
        this.daintree_point_name = daintree_point_name;
        this.daintree_device_id =daintree_device_id;
        this.daintree_device_name= daintree_device_name;
        this.daintree_device_user_data_name = daintree_device_user_data_name;
        this.ecobee_sensor_name = ecobee_sensor_name;
        this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name;
        this.ecobee_sensor_type = ecobee_sensor_type;
        this.created_email = created_email;
        this.status = status;
        this.modbus_device_name = modbus_device_name;
        this.modbus_register_name = modbus_register_name;
        this.modbus_register_category = modbus_register_category;
        this.sub_type = sub_type;
    }

    public HistoryDTO(String id, Integer type, String name, Integer device_alarm, String device_display_name,
                      String device_user_data_name, String device_ip_address, String device_old_ip_address, String remote_session_type, Integer private_port, String public_ip_address,
                      String vendor_email, String bacnet_device_name, String bacnet_object_name,
                      String bacnet_object_user_data_name, String bacnet_object_category, String lorawan_sensor_name,
                      String lorawan_sensor_type, String disruptive_sensor_name, String disruptive_sensor_user_data_name, String disruptive_sensor_type,
                      String value, String unit, String alert_message, String ticket_number, Integer ticket_type,
                      String ticket_status, String ticket_user_message, String ticket_vendor_message,
                      BigInteger timestamp, String vdms_id, String docker_name, String device_id, String bacnet_device_id,
                      String bacnet_object_id, String lorawan_sensor_id, String lorawan_sensor_attributes_name,
                      String ticket_id, String disruptive_sensor_id, String location, String my_devices_sensor_name,
                      String my_devices_sensor_user_data_name, String my_devices_sensor_type, String my_devices_sensor_id,
                      String my_devices_sensor_attributes_name, String monnit_sensor_id, String monnit_sensor_name,
                      String monnit_sensor_user_data_name, String monnit_sensor_category, String pelican_sensor_name, String pelican_sensor_user_data_name,
                      String pelican_sensor_type, String pelican_sensor_id, String pelican_sensor_attributes_name,
                      String knx_group_user_data_name, String knx_group_category, String knx_device_address, String knx_group_address,
                      String knx_group_name, String snmp_object_name,String snmp_object_category, String snmp_object_oid,
                      String snmp_device_configuration_id, String measuring_instrument_id, String measuring_instrument_name,
                      String measuring_instrument_user_data_name, String measuring_instrument_category, String daintree_point_id, String daintree_point_category, String daintree_point_name, String daintree_device_id,
                      String daintree_device_name, String daintree_device_user_data_name, String ecobee_sensor_name, String ecobee_sensor_user_data_name,
                      String ecobee_sensor_type, String ecobee_sensor_id, String ecobee_sensor_attributes_name, String modbus_device_name, String modbus_register_name,
                      String modbus_register_user_data_name, String modbus_register_category, String modbus_register_id) {
        super();
        this.id = id;
        this.type = type;
        this.name = name;
        this.device_alarm = device_alarm;
        this.device_display_name = device_display_name;
        this.device_user_data_name = device_user_data_name;
        this.device_ip_address = device_ip_address;
        this.device_old_ip_address = device_old_ip_address;
        this.remote_session_type = remote_session_type;
        this.private_port = private_port;
        this.public_ip_address = public_ip_address;
        this.vendor_email = vendor_email;
        this.bacnet_device_name = bacnet_device_name;
        this.bacnet_object_name = bacnet_object_name;
        this.bacnet_object_user_data_name = bacnet_object_user_data_name;
        this.bacnet_object_category = bacnet_object_category;
        this.lorawan_sensor_name = lorawan_sensor_name;
        this.lorawan_sensor_type = lorawan_sensor_type;
        this.disruptive_sensor_name = disruptive_sensor_name;
        this.disruptive_sensor_user_data_name = disruptive_sensor_user_data_name;
        this.disruptive_sensor_type = disruptive_sensor_type;
        this.value = value;
        this.unit = unit;
        this.alert_message = alert_message;
        this.ticket_number = ticket_number;
        this.ticket_type = ticket_type;
        this.ticket_status = ticket_status;
        this.ticket_user_message = ticket_user_message;
        this.ticket_vendor_message = ticket_vendor_message;
        this.timestamp = timestamp;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.device_id = device_id;
        this.bacnet_device_id = bacnet_device_id;
        this.bacnet_object_id = bacnet_object_id;
        this.lorawan_sensor_id = lorawan_sensor_id;
        this.lorawan_sensor_attributes_name = lorawan_sensor_attributes_name;
        this.ticket_id = ticket_id;
        this.disruptive_sensor_id = disruptive_sensor_id;
        this.location = location;
        this.my_devices_sensor_name = my_devices_sensor_name;
        this.my_devices_sensor_user_data_name = my_devices_sensor_user_data_name;
        this.my_devices_sensor_type = my_devices_sensor_type;
        this.my_devices_sensor_id = my_devices_sensor_id;
        this.my_devices_sensor_attributes_name = my_devices_sensor_attributes_name;
        this.monnit_sensor_id = monnit_sensor_id;
        this.monnit_sensor_name = monnit_sensor_name;
        this.monnit_sensor_user_data_name = monnit_sensor_user_data_name;
        this.monnit_sensor_category = monnit_sensor_category;
        this.pelican_sensor_name = pelican_sensor_name;
        this.pelican_sensor_user_data_name = pelican_sensor_user_data_name;
        this.pelican_sensor_type = pelican_sensor_type;
        this.pelican_sensor_id = pelican_sensor_id;
        this.pelican_sensor_attributes_name = pelican_sensor_attributes_name;
		this.knx_group_user_data_name = knx_group_user_data_name;
		this.knx_group_category = knx_group_category;
		this.knx_device_address = knx_device_address;
		this.knx_group_address = knx_group_address;
        this.knx_group_name = knx_group_name;
        this.snmp_object_name = snmp_object_name;
        this.snmp_object_category = snmp_object_category;
        this.snmp_object_oid = snmp_object_oid;
        this.snmp_device_configuration_id = snmp_device_configuration_id;
        this.measuring_instrument_id = measuring_instrument_id;
        this.measuring_instrument_name = measuring_instrument_name;
        this.measuring_instrument_user_data_name = measuring_instrument_user_data_name;
        this.measuring_instrument_category = measuring_instrument_category;
        this.daintree_point_id = daintree_point_id;
        this.daintree_point_category = daintree_point_category;
        this.daintree_point_name = daintree_point_name;
        this.daintree_device_id =daintree_device_id;
        this.daintree_device_name= daintree_device_name;
        this.daintree_device_user_data_name = daintree_device_user_data_name;
        this.ecobee_sensor_name = ecobee_sensor_name;
        this.ecobee_sensor_user_data_name = ecobee_sensor_user_data_name;
        this.ecobee_sensor_type = ecobee_sensor_type;
        this.ecobee_sensor_id = ecobee_sensor_id;
        this.ecobee_sensor_attributes_name = ecobee_sensor_attributes_name;
        this.modbus_device_name = modbus_device_name;
        this.modbus_register_name = modbus_register_name;
        this.modbus_register_user_data_name = modbus_register_user_data_name;
        this.modbus_register_category = modbus_register_category;
        this.modbus_register_id = modbus_register_id;
    }

    public HistoryDTO(String id, String vendor_email, String ticket_number, Integer ticket_type, String ticket_status,
                      String ticket_user_message, String ticket_vendor_message, BigInteger timestamp, String ticket_id) {
        super();
        this.id = id;
        this.vendor_email = vendor_email;
        this.ticket_number = ticket_number;
        this.ticket_type = ticket_type;
        this.ticket_status = ticket_status;
        this.ticket_user_message = ticket_user_message;
        this.ticket_vendor_message = ticket_vendor_message;
        this.timestamp = timestamp;
        this.ticket_id = ticket_id;
    }

    public HistoryDTO(Integer type, Integer device_alarm, String device_ip_address, String device_old_ip_address,
                      BigInteger timestamp, String device_id) {
        super();
        this.type = type;
        this.device_alarm = device_alarm;
        this.device_ip_address = device_ip_address;
        this.device_old_ip_address = device_old_ip_address;
        this.timestamp = timestamp;
        this.device_id = device_id;
    }

    public HistoryDTO(String assignee_email, String assignee_name, String device_id,
                      String device_name, BigInteger timestamp, String docker_name) {
        this.assignee_email = assignee_email;
        this.assignee_name = assignee_name;
        this.device_id = device_id;
        this.device_name = device_name;
        this.timestamp = timestamp;
        this.docker_name = docker_name;
    }

    public HistoryDTO(String assignee_name, Integer count, String assignee_email) {
        this.assignee_name = assignee_name;
        this.count = count;
        this.assignee_email = assignee_email;
    }

    @Override
    public String toString() {
        return "HistoryDTO{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", device_id='" + device_id + '\'' +
                ", created_email='" + created_email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }


}
