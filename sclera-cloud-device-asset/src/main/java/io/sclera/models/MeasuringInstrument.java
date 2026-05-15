package io.sclera.models;


import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.dto.touchscreen.SensorValueDTO;


@SqlResultSetMapping(
        name = "measuringinstrumentmapping",
        classes = {
                @ConstructorResult(
                        targetClass = MeasuringInstrumentDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "calculation_type", type = String.class),
                                @ColumnResult(name = "attribute", type = String.class),
                                @ColumnResult(name = "parameter", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "tags", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "timestamp", type = BigInteger.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "user_data_value", type = String.class),
                                @ColumnResult(name = "show_on_map", type = Integer.class),
                                @ColumnResult(name = "show_on_scan", type = Integer.class),
                                @ColumnResult(name = "measuring_entity", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "digital_twin_position", type = String.class),
                                @ColumnResult(name = "scale_type", type = String.class)

                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getInstrumentByDeviceId",
        query = "SELECT mi.id , mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter , mi.category , "
                + " mi.parameter , mi.value , mi.unit, mi.tags , mi.device_id, mi.timestamp, mi.sensor_type, mi.alert, mi.user_data_name, "
                + " mi.user_data_value, mi.show_on_map, mi.show_on_scan, mi.measuring_entity, b.name as building, f.name as floor, l.name as location," +
                "  l.id as location_id, mi.sub_category, mi.digital_twin_position, mi.scale_type "
                + " FROM measuring_instrument mi"
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + "  WHERE mi.device_id = ?1",
        resultSetMapping = "measuringinstrumentmapping"
)


//@NamedNativeQuery(
//        name = "MeasuringInstrument.getInstrumentByInstrumentId",
//        query = "SELECT mi.id , mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter , mi.category , mi.parameter , " +
//                " mi.value , mi.unit, mi.tags , mi.device_id, mi.timestamp, mi.sensor_type , mi.alert, mi.user_data_name, mi.user_data_value, " +
//                " mi.show_on_map, mi.show_on_scan , mi.measuring_entity, b.name as building, f.name as floor, l.name as location, l.id as location_id, " +
//                " mi.sub_category, mi.digital_twin_position, mi.scale_type "
//                + " FROM measuring_instrument mi"
//                + " LEFT JOIN device d ON mi.device_id = d.id"
//                + " LEFT JOIN location l ON d.location_id = l.id"
//                + " LEFT JOIN floor f ON l.floor_id = f.id"
//                + " LEFT JOIN building b ON f.building_id = b.id"
//                + " WHERE mi.id = ?1",
//        resultSetMapping = "measuringinstrumentmapping"
//
//)

@NamedNativeQuery(
        name = "MeasuringInstrument.getDaintreeMeasuringInstruments",
        query = "SELECT mi.id , mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter , mi.category , mi.parameter , mi.value ," +
                " mi.unit, mi.tags , mi.device_id, mi.timestamp, mi.sensor_type , mi.alert, mi.user_data_name, mi.user_data_value, mi.show_on_map, " +
                " mi.show_on_scan, mi.measuring_entity, b.name as building, f.name as floor, l.name as location, l.id as location_id, mi.sub_category, mi.digital_twin_position, mi.scale_type "
                + " FROM measuring_instrument mi"
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN measuring_instrument_attributes mia ON mia.measuring_instrument_id = mi.id" +
                " WHERE mia.protocol ='daintree'",
        resultSetMapping = "measuringinstrumentmapping"
)


@NamedNativeQuery(
        name = "MeasuringInstrument.getSiemensMeasuringInstruments",
        query = "SELECT mi.id , mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter , mi.category , mi.parameter , mi.value , mi.unit, " +
                " mi.tags , mi.device_id, mi.timestamp, mi.sensor_type , mi.alert, mi.user_data_name, mi.user_data_value, mi.show_on_map, mi.show_on_scan," +
                "  mi.measuring_entity, b.name as building, f.name as floor, l.name as location, l.id as location_id, mi.sub_category , mi.digital_twin_position, mi.scale_type "
                + " FROM measuring_instrument mi"
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN measuring_instrument_attributes mia ON mia.measuring_instrument_id = mi.id" +
                " WHERE mia.protocol ='siemens'",
        resultSetMapping = "measuringinstrumentmapping"
)

@NamedNativeQuery(
        name = "MeasuringInstrument.getMeasuringInstrumentsByDeviceId",
        query = "SELECT mi.id , mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter , mi.category , mi.parameter ," +
                " mi.value , mi.unit, mi.tags , mi.device_id, mi.timestamp, mi.sensor_type , mi.alert, mi.user_data_name, mi.user_data_value," +
                " mi.show_on_map, mi.show_on_scan, mi.measuring_entity, b.name as building, f.name as floor, l.name as location, l.id as location_id," +
                " mi.sub_category, mi.digital_twin_position, mi.scale_type "
                + " FROM measuring_instrument mi"
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN measuring_instrument_attributes mia ON mia.measuring_instrument_id = mi.id "
                + " WHERE mia.protocol ='siemens' AND mi.device_id = ?1 ",
        resultSetMapping = "measuringinstrumentmapping"
)

@SqlResultSetMapping(
        name = "measuringinstrumentmappings",
        classes = {
                @ConstructorResult(
                        targetClass = MeasuringInstrumentDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "calculation_type", type = String.class),
                                @ColumnResult(name = "attribute", type = String.class),
                                @ColumnResult(name = "parameter", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "tags", type = String.class),
                                @ColumnResult(name = "timestamp", type = BigInteger.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "user_data_value", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "show_on_map", type = Integer.class),
                                @ColumnResult(name = "show_on_scan", type = Integer.class),
                                @ColumnResult(name = "measuring_entity", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "scale_type", type = String.class)

                        })
        })


//Added pagination for getAllMonnitSensors
@NamedNativeQuery(
        name = "MeasuringInstrument.getAllMeasuringInstrumentDeviceByPagination",
        query = "SELECT ms.id, ms.type, ms.name, ms.description, ms.calculation_type, ms.attribute, ms.parameter, ms.category, ms.value,"
                + " ms.unit, ms.tags, ms.timestamp, ms.sensor_type, ms.alert, ms.user_data_name, ms.user_data_value, b.name as building, f.name as floor, l.name as location, "
                + " l.id as location_id, ms.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, ms.show_on_map, ms.show_on_scan, ms.measuring_entity, ms.sub_category, ms.scale_type "
                + " FROM measuring_instrument ms"
                + " LEFT JOIN device d ON ms.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or CONCAT_WS('',ms.user_data_name, ms.category, ms.name, ms.id, d.display_name ,d.user_data_name, l.name) LIKE CONCAT('%',?1,'%'))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "measuringinstrumentmappings"
)

@NamedNativeQuery(
        name = "MeasuringInstrument.getMeasuringInstrumentSensorById",
        query = "SELECT ms.id, ms.type, ms.name, ms.description, ms.calculation_type, ms.attribute, ms.parameter, ms.category, ms.value, ms.unit,"
                + " ms.tags, ms.timestamp, ms.sensor_type, ms.alert, ms.user_data_name, ms.user_data_value, b.name as building, f.name as floor,"
                + " l.name as location, l.id as location_id, ms.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, ms.show_on_map, ms.show_on_scan, ms.measuring_entity, ms.sub_category, ms.scale_type "
                + " FROM measuring_instrument ms"
                + " LEFT JOIN device d ON ms.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ms.id = ?1 ",
        resultSetMapping = "measuringinstrumentmappings"
)


//to get measuring instrument sensor info tagged to a device
@NamedNativeQuery(
        name = "MeasuringInstrument.getDeviceMeasuringInstrumentSensors",
        query = "SELECT ms.id, ms.type, ms.name, ms.description, ms.calculation_type, ms.attribute, ms.parameter, ms.category, ms.value, ms.unit,"
                + " ms.tags, ms.timestamp, ms.sensor_type, ms.alert, ms.user_data_name, ms.user_data_value, b.name as building, f.name as floor,"
                + " l.name as location, l.id as location_id, ms.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, ms.show_on_map, ms.show_on_scan, ms.measuring_entity, ms.sub_category, ms.scale_type "
                + " FROM measuring_instrument ms"
                + " LEFT JOIN device d ON ms.device_id = d.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ms.device_id = ?1",
        resultSetMapping = "measuringinstrumentmappings"
)


//@SqlResultSetMapping(
//		name = "devicemeasuringinstrumentmappingTS",
//		classes = {
//				@ConstructorResult(
//						targetClass = SensorDTO.class,
//						columns = {
//								@ColumnResult(name = "primary_id",type = String.class),
//								@ColumnResult(name = "name",type = String.class),
//								@ColumnResult(name = "alert",type = Boolean.class),
//								@ColumnResult(name = "category",type = String.class)
//						})
//		})
//
//@NamedNativeQuery(
//		name = "MeasuringInstrument.getmeasuringInstrumentsByDeviceIdTS",
//		query = "SELECT mi.id as primary_id, IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as name, mi.alert, mi.sensor_type as category"
//				+ " FROM measuring_instrument mi"
//				+ " LEFT JOIN device d ON mi.device_id = d.id"
//				+ " WHERE mi.device_id = ?1",
//		resultSetMapping = "devicemeasuringinstrumentmappingTS"
//)
//
//
//@SqlResultSetMapping(
//		name = "measuringinstrumentvaluemappingTS",
//		classes = {
//				@ConstructorResult(
//						targetClass = SensorValueDTO.class,
//						columns = {
//								@ColumnResult(name = "name",type = String.class),
//								@ColumnResult(name = "value",type = String.class),
//								@ColumnResult(name = "alert",type = Boolean.class),
//								@ColumnResult(name = "category",type = String.class)
//						})
//		})
////
//
//@NamedNativeQuery(
//		name = "MeasuringInstrument.getmeasuringInstrumentValuesByIdTS",
//		query = "SELECT IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as name," +
//				" IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, ''))," +
//				" mi.user_data_value) as value, mi.alert, mi.sensor_type as category"
//				+ " FROM measuring_instrument mi"
//				+ " WHERE mi.id = ?1",
//		resultSetMapping = "measuringinstrumentvaluemappingTS"
//)

@SqlResultSetMapping(
        name = "devicemeasuringinstrumentmapping",
        classes = {
                @ConstructorResult(
                        targetClass = SensorDTO.class,
                        columns = {
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "value", type = String.class)

                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getmeasuringInstrumentsByDeviceId",
        query = "SELECT mi.id as primary_id, null as secondary_id,IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as name, mi.alert, mi.sensor_type as category, 'measuring_instrument' as protocol, "
                + " IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, '')),mi.user_data_value) as value"
                + " FROM measuring_instrument mi"
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " WHERE mi.device_id = ?1 and mi.show_on_map = 1",
        resultSetMapping = "devicemeasuringinstrumentmapping"
)


@SqlResultSetMapping(
        name = "measuringinstrumentcategorysensormapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategorySensorDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "last_seen", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class)

                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getSensorCategoryByFloor",
        query = "SELECT mi.name,IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, '')),"
                + " mi.user_data_value) as value, mi.alert, false as alert, mi.sensor_type as category, mi.id as primary_id , null as secondary_id, 'measuring_instrument' as protocol,"
                + " l.id as location_id, mi.timestamp as last_seen, mi.device_id "
                + " FROM measuring_instrument mi"
                + " JOIN device d ON mi.device_id = d.id AND d.monitor = 1"
                + " JOIN location l ON d.location_id = l.id"
                + " JOIN floor f ON l.floor_id = f.id WHERE f.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1",
        resultSetMapping = "measuringinstrumentcategorysensormapping"
)

@NamedNativeQuery(
        name = "MeasuringInstrument.getSensorCategoryByFloorPagination",
        query = "SELECT mi.name,IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, '')),"
                + " mi.user_data_value) as value, mi.alert, false as alert, mi.sensor_type as category, mi.id as primary_id , null as secondary_id, 'measuring_instrument' as protocol,"
                + " l.id as location_id, mi.timestamp as last_seen, mi.device_id  "
                + " FROM measuring_instrument mi"
                + " JOIN device d ON mi.device_id = d.id AND d.monitor = 1"
                + " JOIN location l ON d.location_id = l.id"
                + " JOIN floor f ON l.floor_id = f.id WHERE f.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1"
                + " LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "measuringinstrumentcategorysensormapping"
)
@NamedNativeQuery(
        name = "MeasuringInstrument.getSensorCategoryByLocationPagination",
        query = "SELECT mi.name,IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, '')),"
                + " mi.user_data_value) as value, mi.alert, false as alert, mi.sensor_type as category, mi.id as primary_id , null as secondary_id, 'measuring_instrument' as protocol,"
                + " l.id as location_id, mi.timestamp as last_seen, mi.device_id  "
                + " FROM measuring_instrument mi"
                + " JOIN device d ON mi.device_id = d.id AND d.monitor = 1"
                + " JOIN location l ON d.location_id = l.id"
                + " JOIN floor f ON l.floor_id = f.id WHERE l.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1"
                + " LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "measuringinstrumentcategorysensormapping"
)

@SqlResultSetMapping(

        name = "measuringinstrumentdetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = MeasuringInstrumentDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "calculation_type", type = String.class),
                                @ColumnResult(name = "attribute", type = String.class),
                                @ColumnResult(name = "parameter", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "tags", type = String.class),
                                @ColumnResult(name = "timestamp", type = BigInteger.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "user_data_value", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
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
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "show_on_map", type = Integer.class),
                                @ColumnResult(name = "show_on_scan", type = Integer.class)

                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getMeasuringInstrumentSensorDetailsById",
        query = "SELECT mi.id, mi.type, mi.name, mi.description, mi.calculation_type, mi.attribute, mi.parameter, mi.category, mi.value,"
                + " mi.unit, mi.tags, mi.timestamp, mi.alert, mi.user_data_value, mi.user_data_name,  b.name as building, f.name as floor, l.name as location,"
                + " l.id as location_id, mi.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
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
                + " ph.phone as device_local_vendor_phone, mi.sensor_type,mi.show_on_map, mi.show_on_scan"
                + " FROM measuring_instrument mi"
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN phonebook ph ON d.local_vendor_id = ph.id"
                + " LEFT JOIN docker do ON d.docker_name = do.name AND d.docker_vdms_id = do.vdms_id"
                + " LEFT JOIN vdms v On do.vdms_id = v.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE mi.id = ?1",
        resultSetMapping = "measuringinstrumentdetailsmapping"

)

//@SqlResultSetMapping(
//		name = "analyticsmeasuringinstrumentsmappings",
//		classes = {
//				@ConstructorResult(
//						targetClass = SensorDTO.class,
//						columns = {
//								@ColumnResult(name = "primary_id",type = String.class),
//								@ColumnResult(name = "secondary_id",type = String.class),
//								@ColumnResult(name = "name",type = String.class),
//								@ColumnResult(name = "alert",type = Boolean.class),
//								@ColumnResult(name = "category",type = String.class),
//								@ColumnResult(name = "protocol",type = String.class),
//								@ColumnResult(name = "value",type = String.class),
//								@ColumnResult(name = "location_id",type = String.class),
//								@ColumnResult(name = "location_name",type = String.class),
//								@ColumnResult(name = "sensor_name",type = String.class),
//								@ColumnResult(name = "device_id",type = String.class),
//								@ColumnResult(name = "device_name",type = String.class),
//								@ColumnResult(name = "unit",type = String.class),
//								@ColumnResult(name = "last_seen",type = BigInteger.class)
//
//						})
//		})
//
//
//
//@NamedNativeQuery(
//		name = "MeasuringInstrument.getAnalyticsMeasuringInstruments",
//		query = "SELECT mi.id as primary_id, null as secondary_id, mi.sensor_type as category, IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as name,"
//				+ " mi.alert, mi.category, mi.value, l.id as location_id, l.name as location_name, mi.name as sensor_name, mi.device_id,IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, mi.unit, mi.timestamp as last_seen, 'measuring_instrument' as protocol "
//				+ " FROM  measuring_instrument mi "
//				+ " LEFT JOIN device d ON mi.device_id = d.id "
//				+ " LEFT JOIN location l ON d.location_id = l.id "
//				+ " WHERE (?1 = 'all' or mi.sensor_type = ?1) AND (?2 = 'null' or CONCAT_WS('',mi.id, mi.sensor_type, mi.name, l.id, l.name, d.id) LIKE CONCAT('%',?2,'%')) AND mi.device_id IS NOT NULL AND d.monitor = 1 "
//				+ " LIMIT ?3 OFFSET ?4",
//		resultSetMapping = "analyticsmeasuringinstrumentsmappings"
//)

@SqlResultSetMapping(
        name = "analyticsmeasuringinstrumentsmappings",
        classes = {
                @ConstructorResult(
                        targetClass = AnalyticSensorDTO.class,
                        columns = {
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "location_name", type = String.class),
                                @ColumnResult(name = "sensor_name", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "is_added", type = Integer.class),
                                @ColumnResult(name = "report_attribute_id", type = String.class)


                        })
        })


@NamedNativeQuery(
        name = "MeasuringInstrument.getAnalyticsMeasuringInstruments",
        query = "SELECT mi.id as primary_id, null as secondary_id, mi.sensor_type as category, IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as name,"
                + " mi.alert, mi.value, l.id as location_id, l.name as location_name, mi.name as sensor_name, mi.device_id,IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, mi.unit, mi.timestamp as last_seen, 'measuring_instrument' as protocol, "
                + " IF(r.primary_id = mi.id,1,0) as is_added, r.id as report_attribute_id  "
                + " FROM  measuring_instrument mi "
                + " LEFT JOIN device d ON mi.device_id = d.id "
                + " LEFT JOIN location l ON d.location_id = l.id "
                + " LEFT JOIN report_attributes r on r.primary_id = mi.id AND r.report_template_id = ?5 AND protocol = 'measuring_instrument' "
                + " WHERE (?1 = 'all' or mi.sensor_type = ?1) AND (?2 = 'null' or CONCAT_WS('',mi.id, mi.sensor_type, mi.name, l.id, l.name, d.id) LIKE CONCAT('%',?2,'%')) AND mi.device_id IS NOT NULL AND d.monitor = 1 "
                + " LIMIT ?3 OFFSET ?4",
        resultSetMapping = "analyticsmeasuringinstrumentsmappings"
)

@NamedNativeQuery(
        name = "MeasuringInstrument.getMeasuringInstrumentsByTemplateId",
        query = "SELECT mi.id as primary_id, null as secondary_id, mi.sensor_type as category, IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as name,"
                + " mi.alert, mi.category, mi.value, l.id as location_id, l.name as location_name, mi.name as sensor_name, mi.device_id,IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " mi.unit, mi.timestamp as last_seen, 'measuring_instrument' as protocol , 0  as is_added, r.id as report_attribute_id "
                + " FROM  measuring_instrument mi "
                + " LEFT JOIN device d ON mi.device_id = d.id "
                + " LEFT JOIN location l ON d.location_id = l.id "
                + " LEFT JOIN report_attributes r on r.primary_id = mi.id AND r.id = ?3"
                + " WHERE mi.id = ?1 AND (?2 = 'null' or CONCAT_WS('', mi.name, mi.user_data_name) LIKE CONCAT('%',?2,'%'))",
        resultSetMapping = "analyticsmeasuringinstrumentsmappings"
)


@SqlResultSetMapping(
        name = "measuringinstrumentsmappings",
        classes = {
                @ConstructorResult(
                        targetClass = ConditionsDTO.class,
                        columns = {
                                @ColumnResult(name = "alert_message", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class)
                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.listMeasuringIntrumentDevicesAlertMessagesByDevice",
        query = "select distinct(c.alert_message), m.device_id from measuring_instrument m " +
                "JOIN conditions c " +
                "ON m.id = c.measuring_instrument_id " +
                "where c.alert = 1 AND m.device_id IN ?1",
        resultSetMapping = "measuringinstrumentsmappings")

@SqlResultSetMapping(
        name = "measuringinstrumentalertdetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = SensorAlertDTO.class,
                        columns = {
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "primary_name", type = String.class),
                                @ColumnResult(name = "secondary_name", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "type", type = String.class) ,
                                @ColumnResult(name = "sub_category", type = String.class)

                        })
        })


@NamedNativeQuery(
        name = "MeasuringInstrument.getMeasuringInstrumentAlertDetails",
        query = "SELECT mi.id as primary_id , null as secondary_id, IF(mi.user_data_name IS NULL or mi.user_data_name = '', mi.name, mi.user_data_name) as primary_name, null as secondary_name,"
                + " mi.sensor_type as category, mi.value, mi.unit, 'measuring_instrument' as protocol, mi.device_id as device_id, mi.type, mi.sub_category"
                + " FROM measuring_instrument mi "
                + " LEFT JOIN device d ON mi.device_id = d.id"
                + " WHERE mi.id = ?1",
        resultSetMapping = "measuringinstrumentalertdetailsmapping"
)

@SqlResultSetMapping(
        name = "measuringinstrumentlocationsensormapping",
        classes = {
                @ConstructorResult(
                        targetClass = SensorDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class)

                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getSensorByDeviceId",
        query = "SELECT mi.name,IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, '')),"
                + " mi.user_data_value) as value, mi.alert, false as alert, mi.sensor_type as category, mi.id as primary_id , null as secondary_id, 'measuring_instrument' as protocol,"
                + " d.location_id as location_id, mi.timestamp as last_seen  "
                + " FROM measuring_instrument mi"
                + " JOIN device d ON mi.device_id = d.id AND d.monitor = 1 "
                + "WHERE (?1 = 'null' or d.id = ?1) AND mi.show_on_scan = 1",
        resultSetMapping = "measuringinstrumentlocationsensormapping"
)


@NamedNativeQuery(
        name = "MeasuringInstrument.getSensorByLocationId",
        query = "SELECT mi.name,IF(mi.user_data_value IS NULL or mi.user_data_value = '', CONCAT(mi.value, ' ', IFNULL(mi.unit, '')),"
                + " mi.user_data_value) as value, mi.alert, false as alert, mi.sensor_type as category, mi.id as primary_id , null as secondary_id, 'measuring_instrument' as protocol,"
                + " l.id as location_id, mi.timestamp as last_seen  "
                + " FROM measuring_instrument mi"
                + " JOIN device d ON mi.device_id = d.id AND d.monitor = 1"
                + " JOIN location l ON d.location_id = l.id"
                + " JOIN floor f ON l.floor_id = f.id WHERE  (?1 = 'null' or l.id = ?1) AND mi.show_on_scan = 1",
        resultSetMapping = "measuringinstrumentlocationsensormapping"
)

@SqlResultSetMapping(
        name = "integrationmeasuringinstrumentlocationsensormapping",
        classes = {
                @ConstructorResult(
                        targetClass = SensorDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "secondary_id", type = String.class),
                                @ColumnResult(name = "protocol", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "unit", type = String.class)

                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getIntegrationSensorByLocationId",
        query = "SELECT mi.name,IF(mi.user_data_value IS NULL or mi.user_data_value = '', mi.value,"
                + " mi.user_data_value) as value, mi.unit, mi.alert, false as alert, mi.sensor_type as category, mi.id as primary_id , null as secondary_id, 'measuring_instrument' as protocol,"
                + " l.id as location_id, mi.timestamp as last_seen  "
                + " FROM measuring_instrument mi"
                + " JOIN device d ON mi.device_id = d.id AND d.monitor = 1"
                + " JOIN location l ON d.location_id = l.id"
                + " JOIN floor f ON l.floor_id = f.id WHERE  (?1 = 'null' or l.id = ?1) AND mi.show_on_scan = 1 AND mi.show_on_map = 1",
        resultSetMapping = "integrationmeasuringinstrumentlocationsensormapping"
)


@SqlResultSetMapping(
        name = "measuringInstrumentAttributeMapping",
        classes = {
                @ConstructorResult(
                        targetClass = MeasuringInstrumentDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "attribute", type = String.class),
                                @ColumnResult(name = "scale_type", type = String.class),
                                @ColumnResult(name = "sensor_type", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class)
                        })
        })

@NamedNativeQuery(
        name = "MeasuringInstrument.getAllInstrument",
        query = "SELECT mi.id , mi.attribute, mi.scale_type ,mi.sensor_type, mi.sub_category"
                + " FROM measuring_instrument mi",
        resultSetMapping = "measuringInstrumentAttributeMapping"
)


@SqlResultSetMapping(
        name = "instrumentmapping",
        classes = {
                @ConstructorResult(
                        targetClass = MeasuringInstrumentDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "calculation_type", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "sensor_type", type = String.class),

                        })
        })
@NamedNativeQuery(
        name = "MeasuringInstrument.getInstrumentByInstrumentId",
        query = "SELECT mi.id , mi.type, mi.name,  mi.calculation_type,  mi.category , mi.value , mi.unit, mi.device_id,  mi.sensor_type " +
                " FROM measuring_instrument mi " +
                " WHERE mi.id = ?1",
        resultSetMapping = "instrumentmapping"

)


@Entity
public class MeasuringInstrument {

    @Id
    private String id;

    private String type;

    private String name;

    private String description;

    private String calculation_type;

    @Column(columnDefinition = "varchar(64) default 'static'")
    private String scale_type;

    @Column(columnDefinition = "TEXT")
    private String attribute;

    @Column(columnDefinition = "TEXT")
    private String parameter;

    @Column(columnDefinition = "varchar(255) default 'generic'")
    private String category;

    @Column(columnDefinition = "varchar(255) default 'generic'")
    private String sub_category;

    private String value;

    private String unit;

    @Column(columnDefinition = "TEXT")
    private String tags;

    private BigInteger timestamp;

    @Column(columnDefinition = "varchar(255) default 'generic'")
    private String sensor_type;


    @Column(columnDefinition = "boolean default false", length = 8)
    private Boolean alert;

    @Column(length = 64)
    private String user_data_value;

    @Column(length = 128)
    private String user_data_name;

    @Column(columnDefinition = "integer default 1")
    private Integer show_on_map;

    @Column(columnDefinition = "integer default 1")
    private Integer show_on_scan;

    @Column(length = 128, columnDefinition = "varchar(128) default 'device'")
    private String measuring_entity;

    private String digital_twin_position;

    @ManyToOne(fetch = FetchType.LAZY)
    private Device device;

    // removed: relation to Bucket-C entity Conditions (AP-C2)
    // removed: relation to Bucket-C entity History (AP-C6)

    @ManyToMany()
    @JoinTable(name = "measuring_instrument_location", joinColumns = @JoinColumn(name = "measuring_instrument_id"), inverseJoinColumns = @JoinColumn(name = "location_id"))
    private Set<Location> location;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measuring_instrument")
    private List<MeasuringInstrument_Attributes> measuringInstrumentAttributes;


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

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
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

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }


    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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

    public Set<Location> getLocation() {
        return location;
    }

    public void setLocation(Set<Location> location) {
        this.location = location;
    }

    public String getMeasuring_entity() {
        return measuring_entity;
    }

    public void setMeasuring_entity(String measuring_entity) {
        this.measuring_entity = measuring_entity;
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

    public List<MeasuringInstrument_Attributes> getMeasuringInstrumentAttributes() {
        return measuringInstrumentAttributes;
    }

    public void setMeasuringInstrumentAttributes(List<MeasuringInstrument_Attributes> measuringInstrumentAttributes) {
        this.measuringInstrumentAttributes = measuringInstrumentAttributes;
    }
}
