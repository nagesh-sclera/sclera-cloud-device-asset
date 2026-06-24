package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AlertDTO {
	
		/*  Alert-type Details
			2 - Vendor requested Remote Access ( Send alert to User )
			8 - Ticket Alert
			*/


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
	private String device_user_data_name;
	private String product_id;
	private String building;
	private String floor;
	private String location;
	private String alert_message;
	private String bacnet_device_name;
	private String bacnet_object_name;
	private String bacnet_object_user_data_name;
	private String bacnet_object_category;
	private String lorawan_gateway_name;
	private String lorawan_sensor_name;
	private String lorawan_sensor_attribute_name;
	private String lorawan_sensor_type;
	private String disruptive_sensor_name;
	private String disruptive_sensor_user_data_name;
	private String disruptive_sensor_type;
	private String value;
	private String user_data_value;
	private String unit;
	private String ticket_number;
	private Integer ticket_type;
	private String ticket_status;
	private String ticket_user_message;
	private String ticket_vendor_message;
	private String ticket_vendor_email;
	private String my_devices_sensor_name;
	private String my_devices_sensor_attributes_name;
	private String my_devices_sensor_type;
	private String datahoist_sensor_name;
	private String datahoist_sensor_type;
	private String datahoist_sensor_attribute;
	private String monnit_sensor_name;
	private String monnit_sensor_category;
	private String pelican_sensor_name;
	private String pelican_sensor_type;
	private String pelican_sensor_attributes_name;
	private String knx_group_name;
	private String knx_group_category;
	private String snmp_object_name;
	private String snmp_object_category;
	private String measuring_instrument_name;
	private String measuring_instrument_category;
	private String service_name;
	private String qrcode_link;
	private String service_type;
}