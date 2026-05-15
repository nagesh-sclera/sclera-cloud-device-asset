package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.dto.touchscreen.SensorValueDTO;
import io.sclera.models.compositeclass.Bacnet_ObjectIds;
import io.sclera.models.compositeclass.DockerIds;

@Entity
@IdClass(Bacnet_ObjectIds.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Bacnet_Device.class)


//New Changes ***************************************************************
@SqlResultSetMapping(
        name = "bacnetobjectmapping",
        classes = {
                @ConstructorResult(
                        targetClass = BacnetObjectDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "type", type = Integer.class),
                                @ColumnResult(name = "instance", type = Integer.class),
                                @ColumnResult(name = "present_value", type = String.class),
                                @ColumnResult(name = "validity", type = Boolean.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "state_text", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "user_data_value", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "configuration", type = Boolean.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "bacnet_device_id", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "device_image_url_1", type = String.class),
                                // ============================ begin root changed on 19-Apr-2022 5:32:28 PM step  =====================================//
                                @ColumnResult(name = "cov_subscription", type = Integer.class),
                                @ColumnResult(name = "high_limit", type = String.class),
                                @ColumnResult(name = "low_limit", type = String.class),
                                @ColumnResult(name = "present_value_data_type", type = Integer.class),
                                // ============================ end root changed on 19-Apr-2022 5:32:28 PM step  =====================================//
                                @ColumnResult(name = "show_on_map", type = Integer.class),
                                @ColumnResult(name = "bacnet_device_name", type = String.class),
                                @ColumnResult(name = "show_on_scan", type = Integer.class),
                                @ColumnResult(name = "off_normal", type = Boolean.class),
                                @ColumnResult(name = "normal", type = Boolean.class),
                                @ColumnResult(name = "fault", type = Boolean.class)
                        })
        })


//to be removed after pagination api works
@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectList",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, l.id as location_id, bo.bacnet_device_id,"
                + " bo.last_seen, bo.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type, "
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.bacnet_device_id = ?1",
        resultSetMapping = "bacnetobjectmapping"
)

//Added pagination for getBacnetObjectList
@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectsByBacnetDeviceIdPagination",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name,"
                + " bo.user_data_value, bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, bo.bacnet_device_id,"
                + " l.id as location_id, bo.last_seen, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type, "
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.bacnet_device_id = ?1"
                + " AND (?2 = 'null' or CONCAT_WS('', bo.name, bo.category, bo.user_data_name, d.display_name, d.user_data_name, l.name) LIKE CONCAT('%',?2,'%'))"
                + " LIMIT ?3 OFFSET ?4",
        resultSetMapping = "bacnetobjectmapping"
)


@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectById",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name,"
                + " bo.user_data_value, bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location,bo.bacnet_device_id,"
                + " l.id as location_id, bo.last_seen, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type, "
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.bacnet_device_id = ?1 AND bo.id = ?2",
        resultSetMapping = "bacnetobjectmapping"
)

@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectByAttributeId",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name,"
                + " bo.user_data_value, bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location,bo.bacnet_device_id,"
                + " l.id as location_id, bo.last_seen, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type, "
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN bacnet_attributes ba ON (bo.id = ba.bacnet_object_id AND bo.bacnet_device_id = ba.bacnet_object_bacnet_device_id)"
                + " WHERE ba.id =?1",
        resultSetMapping = "bacnetobjectmapping"
)

//get bacnet object for touchscreen popup socket event
@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectByIdSocket",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name,"
                + " bo.user_data_value, bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, bo.bacnet_device_id,"
                + " l.id as location_id, bo.last_seen, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type,"
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.bacnet_device_id = ?1 AND bo.id = ?2 AND d.monitor = 1 AND d.popup_notification = 1"
                + " AND bo.validity = 1 AND bo.device_id IS NOT NULL",
        resultSetMapping = "bacnetobjectmapping"
)

//get bacnet object info tagged to a device
@NamedNativeQuery(
        name = "Bacnet_Object.getDeviceBacnetObjects",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, l.id as location_id, bo.bacnet_device_id,"
                + " bo.last_seen, bo.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type,"
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.device_id = ?1",
        resultSetMapping = "bacnetobjectmapping"
)


//Touchscreen
//to be removed after pagination api works
@NamedNativeQuery(
        name = "Bacnet_Object.listBacnetSensorsTS",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name,"
                + " bo.user_data_value, bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location,"
                + " l.id as location_id, bo.last_seen, bo.bacnet_device_id, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type,"
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo "
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " WHERE (?1 = 'null' or bd.docker_name = ?1) AND (?2 = 'null' or b.id = ?2) AND (?3 = 'null' or f.id = ?3)"
                + " AND (?4 = 'null' or l.id = ?4) AND (?5 = 3 or bo.alert = ?5) AND bo.configuration = 1"
                + " AND bo.device_id IS NOT NULL AND d.monitor = 1",
        resultSetMapping = "bacnetobjectmapping"
)

//Added pagination for listBacnetSensorsTS
@NamedNativeQuery(
        name = "Bacnet_Object.listBacnetSensorsPaginationTS",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name,"
                + " bo.user_data_value, bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location,"
                + " l.id as location_id, bo.last_seen, bo.bacnet_device_id, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type,"
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo "
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " WHERE (?1 = 'null' or bd.docker_name = ?1) AND (?2 = 'null' or b.id = ?2) AND (?3 = 'null' or f.id = ?3)"
                + " AND (?4 = 'null' or l.id = ?4) AND (?5 = 3 or bo.alert = ?5) AND bo.configuration = 1"
                + " AND bo.device_id IS NOT NULL AND d.monitor = 1"
                + " LIMIT ?6  OFFSET ?7",
        resultSetMapping = "bacnetobjectmapping"
)

//to be removed after pagination api works
//get all bacnet objects of a network
@NamedNativeQuery(
        name = "Bacnet_Object.getNetworkBacnetObjects",
        query = "SELECT bo.id, bo.name, bo.bacnet_device_id,"
                + " bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, l.id as location_id,"
                + " bo.bacnet_device_id, bo.last_seen, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type,"
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bd.docker_name = ?1 AND bd.docker_vdms_id = ?2 AND bo.configuration = 1",
        resultSetMapping = "bacnetobjectmapping"
)

//Added pagination for getNetworkBacnetObjects
@NamedNativeQuery(
        name = "Bacnet_Object.getNetworkBacnetObjectsByPagination",
        query = "SELECT bo.id, bo.name, bo.bacnet_device_id,"
                + " bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, l.id as location_id,"
                + " bo.bacnet_device_id, bo.last_seen, bo.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type,"
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'all' or bd.docker_name = ?1) AND bd.docker_vdms_id = ?2 AND bo.configuration = 1"
                + " AND (?3 = 'null' or CONCAT_WS('', bo.name, bo.category, bo.user_data_name, d.display_name, d.user_data_name, l.name, bo.bacnet_device_id) LIKE CONCAT('%',?3,'%'))"
                + " LIMIT ?4 OFFSET ?5",
        resultSetMapping = "bacnetobjectmapping"
)


@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectByLastSeen",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, l.id as location_id, bo.bacnet_device_id,"
                + " bo.last_seen, bo.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type, "
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.last_seen > ?1",
        resultSetMapping = "bacnetobjectmapping"
)

@NamedNativeQuery(
        name = "Bacnet_Object.getAllBacnetObjectList",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, bo.alert, b.name as building, f.name as floor, l.name as location, l.id as location_id, bo.bacnet_device_id,"
                + " bo.last_seen, bo.device_id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " p.image_url_1 as device_image_url_1, bo.cov_subscription, bo.high_limit, bo.low_limit, bo.present_value_data_type, "
                + " bo.show_on_map, bd.name as bacnet_device_name, bo.show_on_scan,bo.off_normal,bo.normal,bo.fault"
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id",

        resultSetMapping = "bacnetobjectmapping"
)

//**********************New code changes*******************************************************
//get bacnet object info for bacnet alert
@SqlResultSetMapping(
        name = "bacnetobjectalertmapping",
        classes = {
                @ConstructorResult(
                        targetClass = AlertDTO.class,
                        columns = {
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "bacnet_device_name", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "device_monitor", type = Integer.class),
                                @ColumnResult(name = "email_alert", type = Integer.class),
                                @ColumnResult(name = "sms_alert", type = Integer.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetAlertInfoById",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name, bo.category, bo.unit,"
                + " bd.name as bacnet_device_name, d.docker_name,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " d.product_id, d.monitor as device_monitor, d.email_alert, d.sms_alert, do.vendor_org_id, do.vdms_id, do.system_type, v.customer_org_id,"
                + " b.name as building, f.name as floor, l.name as location"
                + " FROM bacnet_object bo"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN docker do ON d.docker_name = do.name and d.docker_vdms_id = do.vdms_id"
                + " LEFT JOIN vdms v ON do.vdms_id = v.id"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " WHERE bo.bacnet_device_id = ?1 AND bo.id = ?2",
        resultSetMapping = "bacnetobjectalertmapping"
)


//@SqlResultSetMapping(
//		name = "devicebacnetobjectmappingTS",
//		classes = {
//				@ConstructorResult(
//						targetClass = SensorDTO.class,
//						columns = {
//								@ColumnResult(name = "primary_id",type = String.class),
//								@ColumnResult(name = "secondary_id",type = String.class),
//								@ColumnResult(name = "name",type = String.class),
//								@ColumnResult(name = "alert",type = Boolean.class),
//								@ColumnResult(name = "category",type = String.class)
//						})
//		})
//
//
//
//@NamedNativeQuery(
//		name = "Bacnet_Object.getBacnetObjectsByDeviceIdTS",
//		query = "SELECT bo.id as primary_id, bo.bacnet_device_id as secondary_id, IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name, bo.alert, bo.category"
//				+ " FROM bacnet_object bo"
//				+ " LEFT JOIN device d ON bo.device_id = d.id"
//				+ " WHERE bo.device_id = ?1",
//				resultSetMapping = "devicebacnetobjectmappingTS"
//		)
//
//@SqlResultSetMapping(
//		name = "bacnetobjectvaluemappingTS",
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
//
//
//
//@NamedNativeQuery(
//		name = "Bacnet_Object.getBacnetObjectValuesByIdTS",
//		query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
//				+ " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),"
//				+ " bo.user_data_value) as value, bo.alert, bo.category"
//				+ " FROM bacnet_object bo"
//				+ " WHERE bo.id = ?1 AND bo.bacnet_device_id = ?2",
//				resultSetMapping = "bacnetobjectvaluemappingTS"
//		)
//


@SqlResultSetMapping(
        name = "devicebacnetobjectmapping",
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
        name = "Bacnet_Object.getBacnetObjectsByDeviceId",
        query = "SELECT bo.id as primary_id, bo.bacnet_device_id as secondary_id, IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " bo.alert, bo.category, 'bacnet' as protocol,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),bo.user_data_value) as value"
                + " FROM bacnet_object bo"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " WHERE bo.device_id = ?1 and bo.show_on_map = 1",
        resultSetMapping = "devicebacnetobjectmapping"
)


//get bancet object all details required for all platforms by bancet device id and bacnet object id
@SqlResultSetMapping(
        name = "bacnetobjectdetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = BacnetObjectDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "type", type = Integer.class),
                                @ColumnResult(name = "instance", type = Integer.class),
                                @ColumnResult(name = "present_value", type = String.class),
                                @ColumnResult(name = "validity", type = Boolean.class),
                                @ColumnResult(name = "unit", type = String.class),
                                @ColumnResult(name = "state_text", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "user_data_value", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "configuration", type = Boolean.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "alert", type = Boolean.class),
                                @ColumnResult(name = "last_seen", type = BigInteger.class),
                                @ColumnResult(name = "bacnet_device_id", type = String.class),
                                @ColumnResult(name = "bacnet_device_name", type = String.class),
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
                                @ColumnResult(name = "device_local_vendor_phone", type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectDetailsById",
        query = "SELECT bo.id, bo.name, bo.type, bo.instance, bo.present_value, bo.validity, bo.unit, bo.state_text, bo.user_data_name, bo.user_data_value,"
                + " bo.category, bo.configuration, b.name as building, f.name as floor, l.name as location, l.id as location_id, bo.alert, bo.last_seen,"
                + " bo.bacnet_device_id, bd.name as bacnet_device_name, bo.device_id,"
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
                + " ph.phone as device_local_vendor_phone"
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " LEFT JOIN device d ON bo.device_id = d.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " LEFT JOIN phonebook ph ON d.local_vendor_id = ph.id"
                + " LEFT JOIN docker do ON d.docker_name = do.name AND d.docker_vdms_id = do.vdms_id"
                + " LEFT JOIN vdms v On do.vdms_id = v.id"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE bo.bacnet_device_id = ?1 AND bo.id = ?2",
        resultSetMapping = "bacnetobjectdetailsmapping"
)


@SqlResultSetMapping(
        name = "bacnetcategorysensormapping",
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
        name = "Bacnet_Object.getSensorCategoryByFloor",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),"
                + " bo.user_data_value) as value, bo.alert, bo.category, bo.id as primary_id, bo.bacnet_device_id as secondary_id, 'bacnet' as protocol , l.id as location_id,bo.last_seen,bo.device_id "
                + " FROM bacnet_object bo "
                + "JOIN device d ON bo.device_id = d.id "
                + "JOIN location l ON d.location_id = l.id "
                + "JOIN floor f ON l.floor_id = f.id WHERE f.id = ?1 AND bo.category = ?2 AND bo.show_on_map = 1  AND d.monitor = 1",
        resultSetMapping = "bacnetcategorysensormapping"
)

@NamedNativeQuery(
        name = "Bacnet_Object.getSensorCategoryByFloorPagination",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),"
                + " bo.user_data_value) as value, bo.alert, bo.category, bo.id as primary_id, bo.bacnet_device_id as secondary_id, 'bacnet' as protocol , l.id as location_id,bo.last_seen,bo.device_id "
                + " FROM bacnet_object bo "
                + "JOIN device d ON bo.device_id = d.id "
                + "JOIN location l ON d.location_id = l.id "
                + "JOIN floor f ON l.floor_id = f.id WHERE f.id = ?1 AND bo.category = ?2 AND bo.show_on_map = 1  AND d.monitor = 1 "
                + " LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "bacnetcategorysensormapping"
)

@NamedNativeQuery(
        name = "Bacnet_Object.getSensorCategoryByLocationPagination",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),"
                + " bo.user_data_value) as value, bo.alert, bo.category, bo.id as primary_id, bo.bacnet_device_id as secondary_id, 'bacnet' as protocol , l.id as location_id,bo.last_seen,bo.device_id "
                + " FROM bacnet_object bo "
                + "JOIN device d ON bo.device_id = d.id "
                + "JOIN location l ON d.location_id = l.id "
                + "JOIN floor f ON l.floor_id = f.id WHERE l.id = ?1 AND bo.category = ?2 AND bo.show_on_map = 1  AND d.monitor = 1"
                + " LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "bacnetcategorysensormapping"
)

//@SqlResultSetMapping(
//		name = "analyticsbacnetobjectsmapping",
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
//@NamedNativeQuery(
//		name = "Bacnet_Object.getAnalyticsBacnetObjects",
//		query = "SELECT bo.id as primary_id, bo.bacnet_device_id as secondary_id, IF(bo.user_data_name IS NULL or bo.user_data_name = '', bd.name, bo.user_data_name) as name,"
//				+ " bo.alert, bo.category, bo.present_value as value, l.id as location_id, l.name as location_name, bd.name as sensor_name, bo.device_id,IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, bo.unit, bo.last_seen, 'bacnet' as protocol "
//				+ " FROM bacnet_object bo "
//				+ " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id "
//				+ " LEFT JOIN device d ON bo.device_id = d.id "
//				+ " LEFT JOIN location l ON d.location_id = l.id "
//				+ " WHERE (?1 = 'all' or bo.category = ?1) AND (?2 = 'null' or CONCAT_WS('',bo.id, bo.bacnet_device_id, bd.name, bo.category, l.id, l.name, d.id, d.display_name) LIKE CONCAT('%',?2,'%')) AND bo.device_id IS NOT NULL AND d.monitor = 1 "
//				+ " LIMIT ?3 OFFSET ?4 ",
//		resultSetMapping = "analyticsbacnetobjectsmapping"
//)

@SqlResultSetMapping(
        name = "analyticsbacnetobjectsmapping",
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
        name = "Bacnet_Object.getAnalyticsBacnetObjects",
        query = "SELECT bo.id as primary_id, bo.bacnet_device_id as secondary_id, IF(bo.user_data_name IS NULL or bo.user_data_name = '', bd.name, bo.user_data_name) as name,"
                + " bo.alert, bo.category, bo.present_value as value, l.id as location_id, l.name as location_name, bd.name as sensor_name, bo.device_id,IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, "
                + " bo.unit, bo.last_seen, 'bacnet' as protocol, IF(r.primary_id = bo.id and r.secondary_id = bo.bacnet_device_id,1,0) as is_added, r.id as report_attribute_id "
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id "
                + " LEFT JOIN device d ON bo.device_id = d.id "
                + " LEFT JOIN location l ON d.location_id = l.id "
                + " LEFT JOIN report_attributes r on r.primary_id = bo.id and r.secondary_id = bo.bacnet_device_id AND r.report_template_id = ?5 AND r.protocol = 'bacnet' "
                + " WHERE (?1 = 'all' or bo.category = ?1) AND "
                + " (?2 = 'null' or CONCAT_WS('',bo.id, bo.bacnet_device_id, bd.name, bo.category, l.id, l.name, d.id, d.display_name) LIKE CONCAT('%',?2,'%')) AND bo.device_id IS NOT NULL AND "
                + " d.monitor = 1  "
                + " LIMIT ?3 OFFSET ?4 ",
        resultSetMapping = "analyticsbacnetobjectsmapping"
)

@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectsByTemplateId",
        query = "SELECT bo.id as primary_id, bo.bacnet_device_id as secondary_id, IF(bo.user_data_name IS NULL or bo.user_data_name = '', bd.name, bo.user_data_name) as name,"
                + " bo.alert, bo.category, bo.present_value as value, l.id as location_id, l.name as location_name, bd.name as sensor_name, bo.device_id,IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " bo.unit, bo.last_seen, 'bacnet' as protocol, 0 as is_added , r.id as report_attribute_id"
                + " FROM bacnet_object bo "
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id "
                + " LEFT JOIN device d ON bo.device_id = d.id "
                + " LEFT JOIN location l ON d.location_id = l.id "
                + " LEFT JOIN report_attributes r on r.primary_id = bo.id and r.secondary_id = bo.bacnet_device_id AND r.id = ?4"
                + " WHERE bo.id = ?1 AND bo.bacnet_device_id = ?2 AND (?3 = 'null' or CONCAT_WS('', bd.name,bo.user_data_name) LIKE CONCAT('%',?3,'%'))",
        resultSetMapping = "analyticsbacnetobjectsmapping"
)

@SqlResultSetMapping(
        name = "bacnetobjectalertdetailsmapping",
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
                                @ColumnResult(name = "device_id", type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "Bacnet_Object.getBacnetObjectAlertDetails",
        query = "SELECT bo.id as primary_id , bo.bacnet_device_id as secondary_id, IF(bo.user_data_name IS NULL or bo.user_data_name = '', bd.name, bo.user_data_name) as primary_name, bd.name as secondary_name,"
                + " bo.category, bo.present_value as value, bo.unit, 'bacnet' as protocol, bo.device_id as device_id"
                + " FROM bacnet_object bo"
                + " LEFT JOIN bacnet_device bd ON bo.bacnet_device_id = bd.id"
                + " WHERE bo.bacnet_device_id = ?1 AND bo.id = ?2",
        resultSetMapping = "bacnetobjectalertdetailsmapping"
)

@SqlResultSetMapping(
        name = "bacnetlocationsensormapping",
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
        name = "Bacnet_Object.getSensorByDeviceId",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),"
                + " bo.user_data_value) as value, bo.alert, bo.category, bo.id as primary_id, bo.bacnet_device_id as secondary_id, 'bacnet' as protocol , d.location_id as location_id,bo.last_seen "
                + " FROM bacnet_object bo "
                + "JOIN device d ON bo.device_id = d.id AND d.monitor = 1 "
                + "WHERE (?1 = 'null' or d.id = ?1) AND bo.show_on_scan = 1",
        resultSetMapping = "bacnetlocationsensormapping"
)

@NamedNativeQuery(
        name = "Bacnet_Object.getSensorByLocationId",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),"
                + " bo.user_data_value) as value, bo.alert, bo.category, bo.id as primary_id, bo.bacnet_device_id as secondary_id, 'bacnet' as protocol , l.id as location_id,bo.last_seen "
                + " FROM bacnet_object bo "
                + "JOIN device d ON bo.device_id = d.id AND d.monitor = 1 "
                + "JOIN location l ON d.location_id = l.id "
                + "JOIN floor f ON l.floor_id = f.id WHERE (?1 = 'null' or l.id = ?1) AND bo.show_on_scan = 1",
        resultSetMapping = "bacnetlocationsensormapping"
)

@SqlResultSetMapping(
        name = "integrationbacnetlocationsensormapping",
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
        name = "Bacnet_Object.getIntegrationSensorByLocationId",
        query = "SELECT IF(bo.user_data_name IS NULL or bo.user_data_name = '', bo.name, bo.user_data_name) as name,"
                + " IF(bo.user_data_value IS NULL or bo.user_data_value = '', bo.present_value,"
                + " bo.user_data_value) as value,bo.unit, bo.alert, bo.category, bo.id as primary_id, bo.bacnet_device_id as secondary_id, 'bacnet' as protocol , l.id as location_id,bo.last_seen "
                + " FROM bacnet_object bo "
                + "JOIN device d ON bo.device_id = d.id AND d.monitor = 1 "
                + "JOIN location l ON d.location_id = l.id "
                + "JOIN floor f ON l.floor_id = f.id WHERE (?1 = 'null' or l.id = ?1) AND bo.show_on_scan = 1",
        resultSetMapping = "integrationbacnetlocationsensormapping"
)


public class Bacnet_Object {

    @Id
    private String id;

    @MapsId
    @ManyToOne
    private Bacnet_Device bacnet_device;

    @Column(length = 128)
    private String name;

    private Integer type;

    private Integer instance;

    @Column(length = 64)
    private String present_value;

    private Boolean validity;

    @Column(length = 64)
    private String unit;

    @Column(length = 256)
    private String state_text;

    @Column(length = 128)
    private String user_data_name;

    @Column(length = 64)
    private String user_data_value;

    @Column(length = 64)
    private String category;

    private String sub_category;

    @Column(columnDefinition = "boolean default false")
    private Boolean configuration;

    private String condition_type;

    private String possible_values;

    @Column(columnDefinition = "boolean default false")
    private Boolean alert;

    private BigInteger last_seen;

    @Column(length = 64)
    private Integer cov_subscription;

    @Column(length = 64)
    private String high_limit;

    @Column(length = 64)
    private String low_limit;

    @Column(length = 64)
    private Integer present_value_data_type;

    @Column(columnDefinition = "integer default 1")
    private Integer show_on_map;

    @Column(columnDefinition = "integer default 1")
    private Integer show_on_scan;

    //	@ManyToOne
    //	private Location location;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bacnet_object")
    private Set<Conditions> conditions;

    @OneToMany(mappedBy = "bacnet_object", cascade = CascadeType.ALL)
    private Set<History> history;

    @ManyToOne
    private Device device;

    @Column
    private Boolean off_normal;

    @Column
    private Boolean normal;

    @Column
    private Boolean fault;

    @OneToMany(mappedBy = "bacnet_object", cascade = CascadeType.ALL)
    private Set<Bacnet_Attributes> bacnet_attributes;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public Integer getInstance() {
        return instance;
    }

    public String getPresent_value() {
        return present_value;
    }

    public Boolean getValidity() {
        return validity;
    }

    public String getUnit() {
        return unit;
    }

    public String getState_text() {
        return state_text;
    }

    public String getUser_data_name() {
        return user_data_name;
    }

    public String getUser_data_value() {
        return user_data_value;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getConfiguration() {
        return configuration;
    }

    public Bacnet_Device getBacnet_device() {
        return bacnet_device;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setInstance(Integer instance) {
        this.instance = instance;
    }

    public void setPresent_value(String present_value) {
        this.present_value = present_value;
    }

    public void setValidity(Boolean validity) {
        this.validity = validity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setState_text(String state_text) {
        this.state_text = state_text;
    }

    public void setUser_data_name(String user_data_name) {
        this.user_data_name = user_data_name;
    }

    public void setUser_data_value(String user_data_value) {
        this.user_data_value = user_data_value;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setConfiguration(Boolean configuration) {
        this.configuration = configuration;
    }

    public String getCondition_type() {
        return condition_type;
    }

    public void setCondition_type(String condition_type) {
        this.condition_type = condition_type;
    }

    public String getPossible_values() {
        return possible_values;
    }

    public void setPossible_values(String possible_values) {
        this.possible_values = possible_values;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public BigInteger getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(BigInteger last_seen) {
        this.last_seen = last_seen;
    }

    public Integer getCov_subscription() {
        return cov_subscription;
    }

    public void setCov_subscription(Integer cov_subscription) {
        this.cov_subscription = cov_subscription;
    }

    public String getHigh_limit() {
        return high_limit;
    }

    public void setHigh_limit(String high_limit) {
        this.high_limit = high_limit;
    }

    public String getLow_limit() {
        return low_limit;
    }

    public void setLow_limit(String low_limit) {
        this.low_limit = low_limit;
    }

    public Integer getPresent_value_data_type() {
        return present_value_data_type;
    }

    public void setPresent_value_data_type(Integer present_value_data_type) {
        this.present_value_data_type = present_value_data_type;
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

    public void setBacnet_device(Bacnet_Device bacnet_device) {
        this.bacnet_device = bacnet_device;
    }

    public Set<Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(Set<Conditions> conditions) {
        this.conditions = conditions;
    }

    public Set<History> getHistory() {
        return history;
    }

    public void setHistory(Set<History> history) {
        this.history = history;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }

    public Boolean getOff_normal() {
        return off_normal;
    }

    public void setOff_normal(Boolean off_normal) {
        this.off_normal = off_normal;
    }

    public Boolean getNormal() {
        return normal;
    }

    public void setNormal(Boolean normal) {
        this.normal = normal;
    }

    public Boolean getFault() {
        return fault;
    }


    public void setFault(Boolean fault) {
        this.fault = fault;


    }
}
