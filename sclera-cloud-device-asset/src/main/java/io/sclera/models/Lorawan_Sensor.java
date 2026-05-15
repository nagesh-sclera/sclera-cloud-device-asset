package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;

import io.sclera.dto.ConditionsDTO;

import io.sclera.dto.LorawanSensorDTO;
import io.sclera.dto.LorawanSensorDetailsDTO;
import io.sclera.dto.touchscreen.SensorDTO;

@Entity

//******************************New Changes for Lorawan**********************************************
@SqlResultSetMapping(
        name = "lorawansensorsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LorawanSensorDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "app_key", type = String.class),
                                @ColumnResult(name = "sensor_device_id", type = String.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "is_battery_low", type = Boolean.class),
                                @ColumnResult(name = "configuration", type = Boolean.class),
                                @ColumnResult(name = "model_id", type = String.class),
                                @ColumnResult(name = "model_name", type = String.class),
                                @ColumnResult(name = "manufacturer", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "signal_strength", type = String.class),
                                @ColumnResult(name = "lorawan_device_type", type = Integer.class),
                                @ColumnResult(name = "app_session_key", type = String.class),
                                @ColumnResult(name = "network_session_key", type = String.class),
                                @ColumnResult(name = "sensor_device_address", type = String.class),
                                @ColumnResult(name = "serving_network_session_key", type = String.class),
                                @ColumnResult(name = "forwarding_network_session_key", type = String.class),
                                @ColumnResult(name = "network_key", type = String.class),
                                @ColumnResult(name = "sensor_device_profile_name", type = String.class),
                                @ColumnResult(name = "sensor_join_status", type = Integer.class),
                                @ColumnResult(name = "battery", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "device_image_url_1", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "sensor_info", type = String.class),
                                @ColumnResult(name = "sensor_device_profile_id", type = String.class),
                                @ColumnResult(name = "lorawan_configuration_id", type = String.class)

                        })
        })

//to be removed after pagination api works
@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensors",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration,"
                + " ls.model_id, ls.model_name, ls.manufacturer, ls.image_url,"
                + " l.name as location, f.name as floor, b.name as building, l.id as location_id, ls.last_seen, ls.signal_strength,"
                + " ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, ls.sensor_device_address, ls.serving_network_session_key,"
                + " ls.forwarding_network_session_key, ls.network_key, ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery,"
                + " ls.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info, ls.sensor_device_profile_id, ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id",
        resultSetMapping = "lorawansensorsmapping")

//Added pagination for getLorawanSensors
@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorsByPagination",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration,"
                + " ls.model_id, ls.model_name, ls.manufacturer, ls.image_url,"
                + " l.name as location, f.name as floor, b.name as building, l.id as location_id, ls.last_seen, ls.signal_strength,"
                + " ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, ls.sensor_device_address, ls.serving_network_session_key,"
                + " ls.forwarding_network_session_key, ls.network_key, ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery,"
                + " ls.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info, ls.sensor_device_profile_id,  ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or CONCAT_WS('', ls.model_name, ls.model_id, ls.manufacturer, ls.sensor_type, ls.name, ls.id, d.display_name , d.user_data_name, l.name) LIKE CONCAT('%',?1,'%'))"
                + " ORDER BY ls.lorawan_device_type, ls.sensor_join_status DESC, ls.last_seen DESC, ls.id"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "lorawansensorsmapping")


@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorById",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low,"
                + " ls.configuration, ls.model_id, ls.model_name, ls.manufacturer, ls.image_url,"
                + " l.name as location, f.name as floor, b.name as building, l.id as location_id, ls.last_seen, ls.signal_strength,"
                + " ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, ls.sensor_device_address, ls.serving_network_session_key,"
                + " ls.forwarding_network_session_key, ls.network_key, ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery,"
                + "	ls.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info,  ls.sensor_device_profile_id, ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls "
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ls.id = ?1",
        resultSetMapping = "lorawansensorsmapping")


//get lorawan sensor for touchscreen popup socket event
@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorByIdSocket",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low,"
                + " ls.configuration, ls.model_id, ls.model_name, ls.manufacturer, ls.image_url,"
                + " l.name as location, f.name as floor, b.name as building, l.id as location_id, ls.last_seen, ls.signal_strength,"
                + " ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, ls.sensor_device_address, ls.serving_network_session_key,"
                + " ls.forwarding_network_session_key, ls.network_key, ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery,"
                + "	ls.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info, ls.sensor_device_profile_id, ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls "
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ls.id = ?1 AND d.monitor = 1 AND d.popup_notification = 1 AND ls.device_id IS NOT NULL",
        resultSetMapping = "lorawansensorsmapping")


//get lorawan sensor info tagged to a device
@NamedNativeQuery(
        name = "Lorawan_Sensor.getDeviceLorawanSensors",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low,"
                + " ls.configuration, ls.model_id, ls.model_name, ls.manufacturer, ls.image_url,"
                + " l.name as location, f.name as floor, b.name as building, l.id as location_id, ls.last_seen, ls.signal_strength,"
                + " ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, ls.sensor_device_address, ls.serving_network_session_key,"
                + " ls.forwarding_network_session_key, ls.network_key, ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery,"
                + " ls.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info, ls.sensor_device_profile_id, ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ls.device_id = ?1",
        resultSetMapping = "lorawansensorsmapping")


//**************************New Changes for Lorawan*************************
//to be removed after pagination api works
@NamedNativeQuery(
        name = "Lorawan_Sensor.getNetworkLorawanSensors",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration,"
                + " ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, l.name as location, f.name as floor, b.name as building,"
                + " l.id as location_id, ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key,"
                + " ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key,"
                + " ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info, ls.sensor_device_profile_id, ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ls.configuration = 1 AND ls.lorawan_device_type = 2",
        resultSetMapping = "lorawansensorsmapping")

//Added pagination for getNetworkLorawanSensors
@NamedNativeQuery(
        name = "Lorawan_Sensor.getNetworkLorawanSensorsByPagination",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration,"
                + " ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, l.name as location, f.name as floor, b.name as building,"
                + " l.id as location_id, ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key,"
                + " ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key,"
                + " ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert, ls.sensor_info, ls.sensor_device_profile_id, ls.lorawan_configuration_id"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ls.configuration = 1 AND ls.lorawan_device_type = 2"
                + " AND (?1 = 'null' or CONCAT_WS('', ls.model_name, ls.model_id, ls.manufacturer, ls.sensor_type, ls.name, ls.id, d.display_name , d.user_data_name, l.name) LIKE CONCAT('%',?1,'%'))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "lorawansensorsmapping")


//Touchscreen New Code***************************************************************

@SqlResultSetMapping(
        name = "lorawansensorsmappingTS",
        classes = {
                @ConstructorResult(
                        targetClass = LorawanSensorDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "is_battery_low", type = Boolean.class),
                                @ColumnResult(name = "model_id", type = String.class),
                                @ColumnResult(name = "manufacturer", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "signal_strength", type = String.class),
                                @ColumnResult(name = "sensor_join_status", type = Integer.class),
                                @ColumnResult(name = "battery", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "device_image_url_1", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "sensor_device_profile_id", type = String.class),
                                @ColumnResult(name = "lorawan_configuration_id", type = String.class)
                        })
        })

//To be removed after pagination api works
@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorsTS",
        query = "SELECT ls.id , ls.name, ls.sensor_type, ls.is_battery_low, ls.model_id, ls.manufacturer,"
                + " l.name as location, f.name as floor, b.name as building, ls.last_seen, ls.signal_strength, ls.sensor_join_status, ls.battery, ls.device_id,"
                + "	IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, ls.alert"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or b.id = ?1) AND (?2 = 'null' or f.id = ?2) AND (?3 = 'null' or l.id = ?3) AND (?4 = 3 or ls.alert = ?4)"
                + " AND ls.configuration = 1 AND ls.device_id IS NOT NULL AND d.monitor = 1",
        resultSetMapping = "lorawansensorsmappingTS")

//Added pagination for getLorawanSensorsTS
@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorsPaginationTS",
        query = "SELECT ls.id , ls.name, ls.sensor_type, ls.is_battery_low, ls.model_id, ls.manufacturer,"
                + " l.name as location, f.name as floor, b.name as building, ls.last_seen, ls.signal_strength, ls.sensor_join_status, ls.battery, ls.device_id,"
                + "	IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1,ls.alert"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or b.id = ?1) AND (?2 = 'null' or f.id = ?2) AND (?3 = 'null' or l.id = ?3) AND (?4 = 3 or ls.alert = ?4)"
                + " AND ls.configuration = 1 AND ls.device_id IS NOT NULL AND d.monitor = 1"
                + " LIMIT ?5  OFFSET ?6",
        resultSetMapping = "lorawansensorsmappingTS")


//@SqlResultSetMapping(
//name = "devicelorawansensorsmappingTS",
//classes = {
//		@ConstructorResult(
//				targetClass = SensorDTO.class,
//				columns = {
//						@ColumnResult(name = "primary_id",type = String.class),
//						@ColumnResult(name = "name",type = String.class),
//						@ColumnResult(name = "alert",type = Boolean.class),
//						@ColumnResult(name = "category",type = String.class)
//				})
//})
//
//
//
//
//
//@NamedNativeQuery(
//name = "Lorawan_Sensor.getLorawanSensorsByDeviceIdTS",
//query = "SELECT ls.id as primary_id, ls.name, ls.alert, ls.sensor_type as category"
//		+ " FROM lorawan_sensor ls"
//		+ " LEFT JOIN device d ON ls.device_id = d.id"
//		+ " WHERE ls.device_id = ?1",
//		resultSetMapping = "devicelorawansensorsmappingTS")


//get lorawan sensor details by id for all required platforms
@SqlResultSetMapping(
        name = "lorawansensordetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LorawanSensorDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "app_key", type = String.class),
                                @ColumnResult(name = "sensor_device_id", type = String.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "is_battery_low", type = Boolean.class),
                                @ColumnResult(name = "configuration", type = Boolean.class),
                                @ColumnResult(name = "model_id", type = String.class),
                                @ColumnResult(name = "model_name", type = String.class),
                                @ColumnResult(name = "manufacturer", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "lorawan_device_type", type = Integer.class),
                                @ColumnResult(name = "app_session_key", type = String.class),
                                @ColumnResult(name = "network_session_key", type = String.class),
                                @ColumnResult(name = "sensor_device_address", type = String.class),
                                @ColumnResult(name = "serving_network_session_key", type = String.class),
                                @ColumnResult(name = "forwarding_network_session_key", type = String.class),
                                @ColumnResult(name = "network_key", type = String.class),
                                @ColumnResult(name = "sensor_device_profile_name", type = String.class),
                                @ColumnResult(name = "sensor_join_status", type = Integer.class),
                                @ColumnResult(name = "battery", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "signal_strength", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "device_image_url_1", type = String.class),
                                @ColumnResult(name = "device_monitor", type = Integer.class),
                                @ColumnResult(name = "device_popup_notification", type = Integer.class),
                                @ColumnResult(name = "device_email_alert", type = Integer.class),
                                @ColumnResult(name = "device_sms_alert", type = Integer.class),
                                @ColumnResult(name = "device_product_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "docker_system_type", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "device_model", type = String.class),
                                @ColumnResult(name = "device_vendor", type = String.class),
                                @ColumnResult(name = "device_type", type = String.class),
                                @ColumnResult(name = "device_virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "device_warranty", type = String.class),
                                @ColumnResult(name = "device_status", type = Integer.class),
                                @ColumnResult(name = "device_last_seen_on", type = BigInteger.class),
                                @ColumnResult(name = "device_sensor_alert", type = Boolean.class),
                                @ColumnResult(name = "device_local_vendor_email_alert", type = Integer.class),
                                @ColumnResult(name = "device_local_vendor_sms_alert", type = Integer.class),
                                @ColumnResult(name = "device_global_image_url_1", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "device_local_vendor_name", type = String.class),
                                @ColumnResult(name = "device_local_vendor_email", type = String.class),
                                @ColumnResult(name = "device_local_vendor_extension", type = String.class),
                                @ColumnResult(name = "device_local_vendor_phone", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "sensor_device_profile_id", type = String.class),
                                @ColumnResult(name = "lorawan_configuration_id", type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorDetailsById",
        query = "SELECT ls.id , ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration,"
                + " ls.model_id, ls.model_name, ls.manufacturer, ls.image_url,"
                + " ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, ls.sensor_device_address, ls.serving_network_session_key,"
                + " ls.forwarding_network_session_key, ls.network_key, ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery,"
                + " ls.last_seen, ls.signal_strength, b.name as building, f.name as floor, l.name as location, l.id as location_id, "
                + " ls.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, d.monitor as device_monitor, d.popup_notification as device_popup_notification,"
                + " d.email_alert as device_email_alert, d.sms_alert as device_sms_alert, d.product_id as device_product_id,"
                + " d.docker_name, do.system_type as docker_system_type, do.vdms_id, do.vendor_org_id, v.customer_org_id,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as device_model,"
                + " IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as device_vendor,"
                + " IF(d.user_data_type IS NULL OR d.user_data_type = '', d.type, d.user_data_type) as device_type,"
                + " d.virtual_device_type as device_virtual_device_type, d.warranty as device_warranty, d.status as device_status,"
                + " d.last_seen_on as device_last_seen_on,"
                + " IF(d.bacnet_status = 'alert' OR d.lorawan_status = 'alert' OR d.disruptive_status = 'alert' OR d.my_devices_status = 'alert' OR d.monnit_status OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.snmp_object_status = 'alert' OR d.measuring_instrument_status = 'alert' OR d.daintree_status = 'alert', 1, 0) as device_sensor_alert,"
                + " d.local_vendor_email_alert as device_local_vendor_email_alert, d.local_vendor_sms_alert as device_local_vendor_sms_alert,"
                + " p.global_image_url_1 as device_global_image_url_1, f.id as floor_id, b.id as building_id,"
                + " ph.vendor_name as device_local_vendor_name, ph.email as device_local_vendor_email, ph.value as device_local_vendor_extension,"
                + " ph.phone as device_local_vendor_phone, ls.alert"
                + " FROM lorawan_sensor ls"
                + " LEFT JOIN device d ON ls.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN phonebook ph ON d.local_vendor_id = ph.id"
                + " LEFT JOIN docker do ON d.docker_name = do.name AND d.docker_vdms_id = do.vdms_id"
                + " LEFT JOIN vdms v On do.vdms_id = v.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ls.id = ?1",
        resultSetMapping = "lorawansensordetailsmapping")


@SqlResultSetMapping(
        name = "lorawansensoralertmapping",
        classes = {
                @ConstructorResult(
                        targetClass = ConditionsDTO.class,
                        columns = {
                                @ColumnResult(name = "alert_message", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class)
                        })
        })

@NamedNativeQuery(
        name = "Lorawan_Sensor.listLorawanDevicesAlertMessagesByDevice",
        query = "select distinct(c.alert_message), ls.device_id from conditions c " +
                "JOIN lorawan_sensor_attributes lsa " +
                "ON lsa.name = c.lorawan_sensor_attributes_name AND lsa.lorawan_sensor_id = c.lorawan_sensor_attributes_lorawan_sensor_id " +
                "JOIN lorawan_sensor ls " +
                "ON ls.id = c.lorawan_sensor_attributes_lorawan_sensor_id " +
                "where c.alert = 1 AND ls.device_id IN ?1",
        resultSetMapping = "lorawansensoralertmapping")



@SqlResultSetMapping(
        name = "lorawansensorsexportmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LorawanSensorDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "app_key", type = String.class),
                                @ColumnResult(name = "sensor_device_id", type = String.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "is_battery_low", type = Boolean.class),
                                @ColumnResult(name = "configuration", type = Boolean.class),
                                @ColumnResult(name = "model_id", type = String.class),
                                @ColumnResult(name = "model_name", type = String.class),
                                @ColumnResult(name = "manufacturer", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "signal_strength", type = String.class),
                                @ColumnResult(name = "lorawan_device_type", type = Integer.class),
                                @ColumnResult(name = "app_session_key", type = String.class),
                                @ColumnResult(name = "network_session_key", type = String.class),
                                @ColumnResult(name = "sensor_device_address", type = String.class),
                                @ColumnResult(name = "serving_network_session_key", type = String.class),
                                @ColumnResult(name = "forwarding_network_session_key", type = String.class),
                                @ColumnResult(name = "network_key", type = String.class),
                                @ColumnResult(name = "sensor_device_profile_name", type = String.class),
                                @ColumnResult(name = "sensor_join_status", type = Integer.class),
                                @ColumnResult(name = "battery", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class)
                        })
        })

@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorsNotReporting",
        query = "SELECT ls.id, ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration, "
                + "ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, "
                + "ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, "
                + "ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key, "
                + "ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS device_name "
                + "FROM lorawan_sensor ls "
                + "LEFT JOIN device d ON ls.device_id = d.id "
                + "WHERE ls.last_seen <= ((UNIX_TIMESTAMP() * 1000) - 86400000) "
                + "AND ls.configuration = 1 "
                + "AND ls.lorawan_device_type = 2 "
                + "ORDER BY ls.last_seen DESC, ls.id",
        resultSetMapping = "lorawansensorsexportmapping")

@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorsReporting",
        query = "SELECT ls.id, ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration, "
                + "ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, "
                + "ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, "
                + "ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key, "
                + "ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS device_name "
                + "FROM lorawan_sensor ls "
                + "LEFT JOIN device d ON ls.device_id = d.id "
                + "WHERE ls.last_seen > ((UNIX_TIMESTAMP() * 1000) - 86400000) "
                + "AND ls.configuration = 1 "
                + "AND ls.lorawan_device_type = 2 "
                + "ORDER BY ls.last_seen DESC, ls.id",
        resultSetMapping = "lorawansensorsexportmapping")


@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanSensorsNotJoining",
        query = "SELECT ls.id, ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration, "
                + "ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, "
                + "ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, "
                + "ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key, "
                + "ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS device_name "
                + "FROM lorawan_sensor ls "
                + "LEFT JOIN device d ON ls.device_id = d.id "
                + " WHERE ls.sensor_join_status = 0 AND ls.configuration = 1 AND ls.lorawan_device_type = 2"
                + " ORDER BY ls.last_seen DESC, ls.id",
        resultSetMapping = "lorawansensorsexportmapping")

@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanGatewayNotReporting",
        query = "SELECT ls.id, ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration, "
                + "ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, "
                + "ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, "
                + "ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key, "
                + "ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS device_name "
                + "FROM lorawan_sensor ls "
                + "LEFT JOIN device d ON ls.device_id = d.id "
                + "WHERE ls.last_seen <= ((UNIX_TIMESTAMP() * 1000) - 86400000) "
                + "AND ls.configuration = 1 "
                + "AND ls.lorawan_device_type = 1 "
                + "ORDER BY ls.last_seen DESC, ls.id",
        resultSetMapping = "lorawansensorsexportmapping"
)

@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanGatewayReporting",
        query = "SELECT ls.id, ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration, "
                + "ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, "
                + "ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, "
                + "ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key, "
                + "ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS device_name "
                + "FROM lorawan_sensor ls "
                + "LEFT JOIN device d ON ls.device_id = d.id "
                + "WHERE ls.last_seen > ((UNIX_TIMESTAMP() * 1000) - 86400000) "
                + "AND ls.configuration = 1 "
                + "AND ls.lorawan_device_type = 1 "
                + "ORDER BY ls.last_seen DESC, ls.id",
        resultSetMapping = "lorawansensorsexportmapping"
)

@NamedNativeQuery(
        name = "Lorawan_Sensor.getLorawanGatewayNotConfigured",
        query = "SELECT ls.id, ls.name, ls.app_key, ls.sensor_device_id, ls.sensor_type, ls.is_battery_low, ls.configuration, "
                + "ls.model_id, ls.model_name, ls.manufacturer, ls.image_url, "
                + "ls.last_seen, ls.signal_strength, ls.lorawan_device_type, ls.app_session_key, ls.network_session_key, "
                + "ls.sensor_device_address, ls.serving_network_session_key, ls.forwarding_network_session_key, ls.network_key, "
                + "ls.sensor_device_profile_name, ls.sensor_join_status, ls.battery, ls.device_id, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS device_name "
                + "FROM lorawan_sensor ls "
                + "LEFT JOIN device d ON ls.device_id = d.id "
                + "WHERE ls.last_seen IS NULL "
                + "AND ls.configuration = 1 "
                + "AND ls.lorawan_device_type = 1 "
                + "ORDER BY ls.id",
        resultSetMapping = "lorawansensorsexportmapping"
)


public class Lorawan_Sensor {

    @Id
    private String id;

    @Column(length = 128)
    private String name;

    @Column(length = 64)
    private String app_key;

    @Column(length = 64)
    private String sensor_device_id;

    @Column(length = 64)
    private String sensor_type;

    @Column(columnDefinition = "boolean default false")
    private Boolean is_battery_low;

    @Column(columnDefinition = "boolean default false")
    private Boolean configuration;

    @Column(columnDefinition = "boolean default false")
    private Boolean is_deleted;

    private String model_id;

    private String model_name;

    private String manufacturer;

    private String image_url;

    private BigInteger last_seen;

    @Column(length = 64)
    private Integer signal_strength;

    @Column(length = 8)
    private Integer lorawan_device_type;

    @Column(length = 64)
    private String app_session_key;

    @Column(length = 64)
    private String network_session_key;

    @Column(length = 64)
    private String sensor_device_address;

    @Column(length = 64)
    private String serving_network_session_key;

    @Column(length = 64)
    private String forwarding_network_session_key;

    @Column(length = 64)
    private String network_key;

    @Column(length = 64)
    private String sensor_device_profile_name;

    @Column(length = 1, columnDefinition = "integer default 0")
    private Integer sensor_join_status;

    @Column(length = 64)
    private String battery;

    @Column(columnDefinition = "boolean default false")
    private Boolean alert;

    @Column(columnDefinition = "LONGTEXT")
    private String sensor_info;


    // TODO: replace with Dapr call when lorawan-sensor-attributes module is ready
    @javax.persistence.Transient
    private Set<Lorawan_Sensor_Attributes> lorawan_sensor_attributes;

    @ManyToOne
    private Device device;

    @ManyToOne
    private Vdms vdms;

    @ManyToOne
    private LorawanConfiguration lorawan_configuration;

    private String sensor_device_profile_id;

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

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getSensor_device_id() {
        return sensor_device_id;
    }

    public void setSensor_device_id(String sensor_device_id) {
        this.sensor_device_id = sensor_device_id;
    }

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }

    public Boolean getIs_battery_low() {
        return is_battery_low;
    }

    public void setIs_battery_low(Boolean is_battery_low) {
        this.is_battery_low = is_battery_low;
    }

    public Boolean getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Boolean configuration) {
        this.configuration = configuration;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getModel_id() {
        return model_id;
    }

    public void setModel_id(String model_id) {
        this.model_id = model_id;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

//	public Integer getEmail_alert() {
//		return email_alert;
//	}
//
//	public void setEmail_alert(Integer email_alert) {
//		this.email_alert = email_alert;
//	}
//
//	public Integer getSms_alert() {
//		return sms_alert;
//	}
//
//	public void setSms_alert(Integer sms_alert) {
//		this.sms_alert = sms_alert;
//	}

    public BigInteger getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(BigInteger last_seen) {
        this.last_seen = last_seen;
    }

    public Integer getSignal_strength() {
        return signal_strength;
    }

    public void setSignal_strength(Integer signal_strength) {
        this.signal_strength = signal_strength;
    }


//	public Lorawan_Gateway getLorawan_gateway() {
//		return lorawan_gateway;
//	}
//
//	public void setLorawan_gateway(Lorawan_Gateway lorawan_gateway) {
//		this.lorawan_gateway = lorawan_gateway;
//	}

    public Integer getLorawan_device_type() {
        return lorawan_device_type;
    }

    public void setLorawan_device_type(Integer lorawan_device_type) {
        this.lorawan_device_type = lorawan_device_type;
    }

    public String getApp_session_key() {
        return app_session_key;
    }

    public void setApp_session_key(String app_session_key) {
        this.app_session_key = app_session_key;
    }

    public String getNetwork_session_key() {
        return network_session_key;
    }

    public void setNetwork_session_key(String network_session_key) {
        this.network_session_key = network_session_key;
    }

    public String getSensor_device_address() {
        return sensor_device_address;
    }

    public void setSensor_device_address(String sensor_device_address) {
        this.sensor_device_address = sensor_device_address;
    }

    public String getServing_network_session_key() {
        return serving_network_session_key;
    }

    public void setServing_network_session_key(String serving_network_session_key) {
        this.serving_network_session_key = serving_network_session_key;
    }

    public String getForwarding_network_session_key() {
        return forwarding_network_session_key;
    }

    public void setForwarding_network_session_key(String forwarding_network_session_key) {
        this.forwarding_network_session_key = forwarding_network_session_key;
    }

    public String getNetwork_key() {
        return network_key;
    }

    public void setNetwork_key(String network_key) {
        this.network_key = network_key;
    }

    public String getSensor_device_profile_name() {
        return sensor_device_profile_name;
    }

    public void setSensor_device_profile_name(String sensor_device_profile_name) {
        this.sensor_device_profile_name = sensor_device_profile_name;
    }

    public Integer getSensor_join_status() {
        return sensor_join_status;
    }

    public void setSensor_join_status(Integer sensor_join_status) {
        this.sensor_join_status = sensor_join_status;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public String getSensor_info() {
        return sensor_info;
    }

    public void setSensor_info(String sensor_info) {
        this.sensor_info = sensor_info;
    }

    public Set<Lorawan_Sensor_Attributes> getLorawan_sensor_attributes() {
        return lorawan_sensor_attributes;
    }

    public void setLorawan_sensor_attributes(Set<Lorawan_Sensor_Attributes> lorawan_sensor_attributes) {
        this.lorawan_sensor_attributes = lorawan_sensor_attributes;
    }


//	public Location getLocation() {
//		return location;
//	}
//
//	public void setLocation(Location location) {
//		this.location = location;
//	}

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Vdms getVdms() {
        return vdms;
    }

    public void setVdms(Vdms vdms) {
        this.vdms = vdms;
    }

    public LorawanConfiguration getLorawan_configuration() {
        return lorawan_configuration;
    }

    public void setLorawan_configuration(LorawanConfiguration lorawan_configuration) {
        this.lorawan_configuration = lorawan_configuration;
    }
    public String getSensor_device_profile_id() {
        return sensor_device_profile_id;
    }

    public void setSensor_device_profile_id(String sensor_device_profile_id) {
        this.sensor_device_profile_id = sensor_device_profile_id;
    }

    public Lorawan_Sensor() {
        super();
    }

    public Lorawan_Sensor(String id) {
        super();
        this.id = id;
    }


}
