package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.*;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.dto.touchscreen.assetmapper.AssetDeviceDTO;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Device.class)


//*************************New Code Changes************************************
@SqlResultSetMapping(
        name = "devicedtomapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "monitor", type = Integer.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "network_layer", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "parent", type = String.class),
                                @ColumnResult(name = "snmp_parent", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "remote_access", type = Integer.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "local_vendor_id", type = String.class),
                                @ColumnResult(name = "global_vendor_id", type = String.class),
                                @ColumnResult(name = "other_vendor_1_id", type = String.class),
                                @ColumnResult(name = "other_vendor_2_id", type = String.class),
                                @ColumnResult(name = "other_vendor_3_id", type = String.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "alarm", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "quick_link_name", type = String.class),
                                @ColumnResult(name = "quick_link_url", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "email_alert", type = Integer.class),
                                @ColumnResult(name = "sms_alert", type = Integer.class),
                                @ColumnResult(name = "popup_notification", type = Integer.class),
                                @ColumnResult(name = "snmp_count", type = Integer.class),
                                @ColumnResult(name = "snmp_status", type = String.class),
                                @ColumnResult(name = "interface_count", type = Integer.class),
                                @ColumnResult(name = "notes_count", type = Integer.class),
                                @ColumnResult(name = "ticket_count", type = Integer.class),
                                @ColumnResult(name = "ticket_status", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "bacnet_count", type = Integer.class),
                                @ColumnResult(name = "bacnet_status", type = String.class),
                                @ColumnResult(name = "lorawan_count", type = Integer.class),
                                @ColumnResult(name = "lorawan_status", type = String.class),
                                @ColumnResult(name = "disruptive_count", type = Integer.class),
                                @ColumnResult(name = "disruptive_status", type = String.class),
                                @ColumnResult(name = "my_devices_count", type = Integer.class),
                                @ColumnResult(name = "my_devices_status", type = String.class),
                                @ColumnResult(name = "local_vendor_email_alert", type = Integer.class),
                                @ColumnResult(name = "local_vendor_sms_alert", type = Integer.class),
                                @ColumnResult(name = "monnit_count", type = Integer.class),
                                @ColumnResult(name = "monnit_status", type = String.class),
                                @ColumnResult(name = "pelican_count", type = Integer.class),
                                @ColumnResult(name = "pelican_status", type = String.class),
                                @ColumnResult(name = "knx_count", type = Integer.class),
                                @ColumnResult(name = "knx_status", type = String.class),
                                @ColumnResult(name = "subsystem_parent_id", type = String.class),
                                @ColumnResult(name = "subsystem_count", type = Integer.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "asset_match_status", type = Integer.class),
                                @ColumnResult(name = "matched_product_ids", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "measuring_instrument_count", type = Integer.class),
                                @ColumnResult(name = "document_count", type = Integer.class),
                                @ColumnResult(name = "media_count", type = Integer.class),
                                @ColumnResult(name = "checklist_template_count", type = Integer.class),
                                @ColumnResult(name = "snmp_object_count", type = Integer.class),
                                @ColumnResult(name = "snmp_object_status", type = String.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "measuring_instrument_status", type = String.class),
                                @ColumnResult(name = "record_checklist_count", type = Integer.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "daintree_count", type = Integer.class),
                                @ColumnResult(name = "daintree_status", type = String.class),
                                @ColumnResult(name = "asset_image_url", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "ecobee_count", type = Integer.class),
                                @ColumnResult(name = "ecobee_status", type = String.class),
                                @ColumnResult(name = "modbus_count", type = Integer.class),
                                @ColumnResult(name = "modbus_status", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "created_email", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "updated_email", type = String.class),
                                @ColumnResult(name = "updated_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "onboard_status", type = Integer.class),
                                @ColumnResult(name = "device_onboard_status_id", type = String.class),
                                @ColumnResult(name = "assignee_email", type = String.class),
                                @ColumnResult(name = "image_status", type = Integer.class),
                                @ColumnResult(name = "geolocation_status", type = Integer.class),
                                @ColumnResult(name = "tag_status", type = Integer.class),
                                @ColumnResult(name = "field_status", type = Integer.class),
                                @ColumnResult(name = "asset_ocr_image_url", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "location_status", type = String.class),
                                @ColumnResult(name = "digital_twin_image_url", type = String.class),
                                @ColumnResult(name = "poly_lens_count", type = Integer.class),
                                @ColumnResult(name = "cost_value", type = BigDecimal.class),
                                @ColumnResult(name = "assigned_user_email", type = String.class),
                                @ColumnResult(name = "ai_call", type = Boolean.class),
                                @ColumnResult(name = "cost_unit", type = String.class),
                                @ColumnResult(name = "is_dnd_enabled", type = Boolean.class),
                                @ColumnResult(name = "operational_status", type = String.class),
                                @ColumnResult(name = "inventory_tracking_id", type = String.class),
                                @ColumnResult(name = "adc_json", type = String.class),
                                @ColumnResult(name = "system_type_id", type = String.class),
                                @ColumnResult(name = "system_type_name", type = String.class),
                                @ColumnResult(name = "asset_type_id", type = String.class),
                                @ColumnResult(name = "asset_type_name", type = String.class),
                                @ColumnResult(name = "asset_sub_type_id", type = String.class),
                                @ColumnResult(name = "asset_sub_type_name", type = String.class),
                                @ColumnResult(name = "source_type", type = String.class),
                                @ColumnResult(name = "asset_tag_images_url", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getSubsystemParentDevicesByPagination",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields,"
                + " d.description, d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,"
                + " d.document_count,d.media_count,d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status, d.qrcode_count, d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email,"
                + " d.updated_timestamp, d.onboard_status , dos.id as device_onboard_status_id, dos.assignee_email, dos.image_status, dos.geolocation_status, "
                + " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status , d.digital_twin_image_url, d.poly_lens_count,"
                + " d.cost_value, d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status, ind.tracking_id AS inventory_tracking_id,"
                + " d.adc_json, d.system_type_id, d.system_type_name, d.asset_type_id, d.asset_type_name, d.asset_sub_type_id, d.asset_sub_type_name, d.source_type, d.asset_tag_images_url "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.subsystem_parent_id IS NULL  AND ((?9 = 123) OR IF(?9 = 210 ,(d.onboard_status IS NULL OR d.onboard_status = 0 OR d.onboard_status = 1 OR d.onboard_status = 2) , ?9 = d.onboard_status))  AND (?1 = 'null' or d.docker_vdms_id = ?1) AND (?2 = 'all' or d.docker_name = ?2) "
                + " AND (?3 IS NULL or IF(?3 = 123 , (d.virtual_device_type IS NOT NULL AND (d.virtual_device_type!= 0 AND d.virtual_device_type!= 1)), NULL))"
                + " AND (?4 IS NULL or ?4 = d.status) AND ( ?5 = 123 or IF(?5=1 , ?5 = d.monitor , d.monitor IS NULL  or ?5 = d.monitor))"
                + " AND ((?6 IS NULL and d.asset_match_status != 3) or IF(?6 = 3 , d.asset_match_status = ?6 , (d.asset_match_status = ?6 and d.asset_match_status != 3)))"
                + " AND (?10 IS NULL or IF(?10 = 0, (d.assigned_user_email IS NULL or d.assigned_user_email = 'null'), ( d.assigned_user_email IS NOT NULL or d.assigned_user_email != 'null'))) "
                + " AND IF('all' = ?11, true , assigned_user_email = ?11)"
//				+ " ORDER BY d.created_timestamp DESC, d.id "
                + " ORDER BY (CASE ?6 WHEN 3 THEN d.updated_timestamp ELSE d.created_timestamp END) DESC, d.id "
                + " LIMIT ?7  OFFSET ?8",
        resultSetMapping = "devicedtomapping"
)

@NamedNativeQuery(
        name = "Device.getDevicesByIdList",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url,  l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields, d.description, "
                + " d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,d.document_count,d.media_count,"
                + " d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status,  d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email,"
                + " d.updated_timestamp, d.onboard_status , dos.id as device_onboard_status_id , dos.assignee_email, dos.image_status, dos.geolocation_status, "
                + " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, d.digital_twin_image_url, "
                + " d.poly_lens_count, d.cost_value, d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status, ind.tracking_id AS inventory_tracking_id, "
                + " d.adc_json, d.system_type_id, d.system_type_name, d.asset_type_id, d.asset_type_name, d.asset_sub_type_id, d.asset_sub_type_name, d.source_type, d.asset_tag_images_url "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.id IN ?1"
                + " ORDER BY FIELD(d.id,?1)",
        resultSetMapping = "devicedtomapping"
)


@NamedNativeQuery(
        name = "Device.getDeviceByDeviceId",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url,  l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields, d.description, "
                + " d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,d.document_count,d.media_count,"
                + " d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status,  d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email,"
                + " d.updated_timestamp, d.onboard_status  , dos.id as device_onboard_status_id, dos.assignee_email, dos.image_status, dos.geolocation_status,"
                + " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, d.digital_twin_image_url, d.poly_lens_count, d.cost_value,"
                + " d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status , ind.tracking_id AS inventory_tracking_id,   "
                + " d.adc_json, d.system_type_id, d.system_type_name, d.asset_type_id, d.asset_type_name, d.asset_sub_type_id, d.asset_sub_type_name, d.source_type, d.asset_tag_images_url "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.id = ?1",
        resultSetMapping = "devicedtomapping"
)

@SqlResultSetMapping(
        name = "devicemapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "monitor", type = Integer.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "network_layer", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "parent", type = String.class),
                                @ColumnResult(name = "snmp_parent", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "remote_access", type = Integer.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "local_vendor_id", type = String.class),
                                @ColumnResult(name = "global_vendor_id", type = String.class),
                                @ColumnResult(name = "other_vendor_1_id", type = String.class),
                                @ColumnResult(name = "other_vendor_2_id", type = String.class),
                                @ColumnResult(name = "other_vendor_3_id", type = String.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "alarm", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "quick_link_name", type = String.class),
                                @ColumnResult(name = "quick_link_url", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "email_alert", type = Integer.class),
                                @ColumnResult(name = "sms_alert", type = Integer.class),
                                @ColumnResult(name = "popup_notification", type = Integer.class),
                                @ColumnResult(name = "snmp_count", type = Integer.class),
                                @ColumnResult(name = "snmp_status", type = String.class),
                                @ColumnResult(name = "interface_count", type = Integer.class),
                                @ColumnResult(name = "notes_count", type = Integer.class),
                                @ColumnResult(name = "ticket_count", type = Integer.class),
                                @ColumnResult(name = "ticket_status", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "bacnet_count", type = Integer.class),
                                @ColumnResult(name = "bacnet_status", type = String.class),
                                @ColumnResult(name = "lorawan_count", type = Integer.class),
                                @ColumnResult(name = "lorawan_status", type = String.class),
                                @ColumnResult(name = "disruptive_count", type = Integer.class),
                                @ColumnResult(name = "disruptive_status", type = String.class),
                                @ColumnResult(name = "my_devices_count", type = Integer.class),
                                @ColumnResult(name = "my_devices_status", type = String.class),
                                @ColumnResult(name = "local_vendor_email_alert", type = Integer.class),
                                @ColumnResult(name = "local_vendor_sms_alert", type = Integer.class),
                                @ColumnResult(name = "monnit_count", type = Integer.class),
                                @ColumnResult(name = "monnit_status", type = String.class),
                                @ColumnResult(name = "pelican_count", type = Integer.class),
                                @ColumnResult(name = "pelican_status", type = String.class),
                                @ColumnResult(name = "knx_count", type = Integer.class),
                                @ColumnResult(name = "knx_status", type = String.class),
                                @ColumnResult(name = "subsystem_parent_id", type = String.class),
                                @ColumnResult(name = "subsystem_count", type = Integer.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "asset_match_status", type = Integer.class),
                                @ColumnResult(name = "matched_product_ids", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "measuring_instrument_count", type = Integer.class),
                                @ColumnResult(name = "document_count", type = Integer.class),
                                @ColumnResult(name = "media_count", type = Integer.class),
                                @ColumnResult(name = "checklist_template_count", type = Integer.class),
                                @ColumnResult(name = "snmp_object_count", type = Integer.class),
                                @ColumnResult(name = "snmp_object_status", type = String.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "measuring_instrument_status", type = String.class),
                                @ColumnResult(name = "record_checklist_count", type = Integer.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "daintree_count", type = Integer.class),
                                @ColumnResult(name = "daintree_status", type = String.class),
                                @ColumnResult(name = "asset_image_url", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "ecobee_count", type = Integer.class),
                                @ColumnResult(name = "ecobee_status", type = String.class),
                                @ColumnResult(name = "modbus_count", type = Integer.class),
                                @ColumnResult(name = "modbus_status", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "created_email", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "updated_email", type = String.class),
                                @ColumnResult(name = "updated_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "onboard_status", type = Integer.class),
                                @ColumnResult(name = "device_onboard_status_id", type = String.class),
                                @ColumnResult(name = "assignee_email", type = String.class),
                                @ColumnResult(name = "image_status", type = Integer.class),
                                @ColumnResult(name = "geolocation_status", type = Integer.class),
                                @ColumnResult(name = "tag_status", type = Integer.class),
                                @ColumnResult(name = "field_status", type = Integer.class),
                                @ColumnResult(name = "asset_ocr_image_url", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "location_status", type = String.class),
                                @ColumnResult(name = "digital_twin_image_url", type = String.class),
                                @ColumnResult(name = "poly_lens_count", type = Integer.class),
                                @ColumnResult(name = "cost_value", type = BigDecimal.class),
                                @ColumnResult(name = "assigned_user_email", type = String.class),
                                @ColumnResult(name = "ai_call", type = Boolean.class),
                                @ColumnResult(name = "cost_unit", type = String.class),
                                @ColumnResult(name = "is_dnd_enabled", type = Boolean.class),
                                @ColumnResult(name = "operational_status", type = String.class),
                                @ColumnResult(name = "inventory_tracking_id", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Device.listAllDevicebyVdmsidAndDockerName",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields, d.description,"
                + " d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude, d.measuring_instrument_count,d.document_count,d.media_count,"
                + " d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status,d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, "
                + " d.updated_email, d.updated_timestamp, d.onboard_status , dos.id as device_onboard_status_id, dos.assignee_email, dos.image_status, " +
                " dos.geolocation_status, dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, d.digital_twin_image_url, " +
                " d.poly_lens_count, d.cost_value, d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status, ind.tracking_id AS inventory_tracking_id  "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or d.docker_vdms_id = ?1) AND (?2 = 'null' or d.docker_name = ?2)",
        resultSetMapping = "devicemapping"
)


@NamedNativeQuery(
        name = "Device.getfilterdevices",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields,"
                + " d.description, d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,"
                + " d.document_count,d.media_count,d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status, d.qrcode_count, d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email,"
                + " d.updated_timestamp, d.onboard_status, dos.id as device_onboard_status_id , dos.assignee_email, dos.image_status, dos.geolocation_status, " +
                " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, d.digital_twin_image_url, d.poly_lens_count, d.cost_value, " +
                " d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status, ind.tracking_id AS inventory_tracking_id    "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or d.docker_vdms_id = ?1) AND (?2 = 'all' or d.docker_name = ?2) "
                + " AND (?3 = 'null' or CONCAT_WS('',d.ip_address,d.display_name ,d.user_data_name, d.vendor, d.user_data_vendor, d.mac_address, l.name) LIKE CONCAT('%',?3,'%'))"
                + " AND (?4 IS NULL or IF(?4 = 123 , (d.virtual_device_type IS NOT NULL AND (d.virtual_device_type!= 0 AND d.virtual_device_type!= 1)), NULL))"
                + " AND (?5 IS NULL or ?5 = d.status) AND ( ?6 = 123 or IF(?6=1 , ?6 = d.monitor , d.monitor IS NULL  or ?6 = d.monitor))"
                + " AND (?10 IS NULL or IF(?10 = 0, (d.assigned_user_email IS NULL or d.assigned_user_email = 'null'), ( d.assigned_user_email IS NOT NULL or d.assigned_user_email != 'null'))) "
                + " AND ((?7 IS NULL and d.asset_match_status != 3) or IF(?7 = 3 , d.asset_match_status = ?7 , (d.asset_match_status = ?7 and d.asset_match_status != 3)))"
                + " LIMIT ?8  OFFSET ?9",
        resultSetMapping = "devicemapping"
)

@NamedNativeQuery(
        name = "Device.getSubsystemParentDevices",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields,"
                + " d.description, d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,"
                + " d.document_count,d.media_count,d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status, d.qrcode_count, d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email,"
                + " d.updated_timestamp, d.onboard_status , dos.id as device_onboard_status_id, dos.assignee_email, dos.image_status, dos.geolocation_status, " +
                " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, d.digital_twin_image_url, " +
                " d.poly_lens_count, d.cost_value, d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status, ind.tracking_id AS inventory_tracking_id    "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.subsystem_parent_id IS NULL  AND ((?7 = 123) OR IF(?7 = 0 ,(d.onboard_status IS NULL OR d.onboard_status = 0) , ?7 = d.onboard_status))  AND (?1 = 'null' or d.docker_vdms_id = ?1) AND (?2 = 'all' or d.docker_name = ?2) "
                + " AND (?3 IS NULL or IF(?3 = 123 , (d.virtual_device_type IS NOT NULL AND (d.virtual_device_type!= 0 AND d.virtual_device_type!= 1)), NULL))"
                + " AND (?4 IS NULL or ?4 = d.status) AND ( ?5 = 123 or IF(?5=1 , ?5 = d.monitor , d.monitor IS NULL  or ?5 = d.monitor))"
                + " AND (?8 IS NULL or IF(?8 = 0, (d.assigned_user_email IS NULL or d.assigned_user_email = 'null'), ( d.assigned_user_email IS NOT NULL or d.assigned_user_email != 'null'))) "
                + " AND ((?6 IS NULL and d.asset_match_status != 3) or IF(?6 = 3 , d.asset_match_status = ?6 , (d.asset_match_status = ?6 and d.asset_match_status != 3)))"
//				+ " ORDER BY d.created_timestamp DESC, d.id "
                + " ORDER BY (CASE ?6 WHEN 3 THEN d.updated_timestamp ELSE d.created_timestamp END) DESC, d.id ",
        resultSetMapping = "devicemapping"
)


@NamedNativeQuery(
        name = "Device.getSubsystemDevicesByPagination",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields, d.description, "
                + " d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,d.document_count,d.media_count,"
                + " d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status,  d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, "
                + " d.updated_email, d.updated_timestamp, d.onboard_status , dos.id as device_onboard_status_id , dos.assignee_email, dos.image_status, "
                + " dos.geolocation_status, dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, " +
                " d.digital_twin_image_url, d.poly_lens_count, d.cost_value, d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status , ind.tracking_id AS inventory_tracking_id   "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.subsystem_parent_id = ?1"
                + " AND IF('all' = ?4, true , assigned_user_email = ?4)"
                + " ORDER BY d.created_timestamp DESC, d.id "
                + " LIMIT ?2  OFFSET ?3",
        resultSetMapping = "devicemapping"
)

//( ?6 = 123 or ?6 = d.monitor or (d.monitor IS NULL or d.monitor = 0))
//IF(?6=1 , ?6 = d.monitor , ?6 = d.monitor or ?6 IS NULL)



@NamedNativeQuery(
        name = "Device.listAllDeviceByVdmsId",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields, d.description,"
                + " d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude, d.measuring_instrument_count,d.document_count,d.media_count,"
                + " d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status,  d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email, "
                + " d.updated_timestamp, d.onboard_status, dos.id as device_onboard_status_id  , dos.assignee_email, dos.image_status, dos.geolocation_status, " +
                " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, d.digital_twin_image_url, d.poly_lens_count, d.cost_value, " +
                " d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status , ind.tracking_id AS inventory_tracking_id   "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or d.docker_vdms_id = ?1)",
        resultSetMapping = "devicemapping"
)

@NamedNativeQuery(
        name = "Device.getAssetsByLocationId",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url, l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields,"
                + " d.description, d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,"
                + " d.document_count,d.media_count,d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status, d.qrcode_count, d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email, "
                + " d.updated_timestamp, d.onboard_status, dos.id as device_onboard_status_id  , dos.assignee_email, dos.image_status, dos.geolocation_status, " +
                " dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status , d.digital_twin_image_url, " +
                " d.poly_lens_count, d.cost_value, d.assigned_user_email, d.ai_call, d.cost_unit, d.is_dnd_enabled, d.operational_status , ind.tracking_id AS inventory_tracking_id  "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN inventory_device ind ON d.id = ind.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 "
                + " AND l.id = ?1 AND d.monitor = 1 "
                + " ORDER BY d.created_timestamp DESC, d.id "
                + " LIMIT ?2  OFFSET ?3",
        resultSetMapping = "devicemapping"
)


@SqlResultSetMapping(
        name = "devicesyncvirtual",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.listAllVirtualdevices",
        query = "SELECT id, status, ip_address, docker_name, docker_vdms_id as vdms_id FROM device WHERE virtual_device_type = 1",
        resultSetMapping = "devicesyncvirtual"
)


@SqlResultSetMapping(
        name = "devicedatamapping",
        classes = {
                @ConstructorResult(
                        targetClass = DevicedataDTO.class,
                        columns = {
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = BigInteger.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "network_name", type = String.class),
                                @ColumnResult(name = "vdms", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "Device.getAllDevices",
        query = "SELECT  d.id AS device_id ,d.display_name ,d.last_seen_on ,d.model ,d.vendor ,d.user_data_name ,"
                + "d.user_data_model ,d.user_data_vendor ,d.docker_name AS network_name ,d.docker_vdms_id AS vdms ,"
                + "d.virtual_device_type ,d.warranty ,d.type,d.asset_group FROM device d ",
        resultSetMapping = "devicedatamapping"
)




//************************************************************Touchscreen query**************************************************************************************

@SqlResultSetMapping(
        name = "devicelistmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceListDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "image_url_1", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "snmp_status", type = String.class),
                                @ColumnResult(name = "bacnet_status", type = String.class),
                                @ColumnResult(name = "lorawan_status", type = String.class),
                                @ColumnResult(name = "disruptive_status", type = String.class),
                                @ColumnResult(name = "my_devices_status", type = String.class),
                                @ColumnResult(name = "monnit_status", type = String.class),
                                @ColumnResult(name = "pelican_status", type = String.class),
                                @ColumnResult(name = "knx_status", type = String.class),
                                @ColumnResult(name = "snmp_object_status", type = String.class),
                                @ColumnResult(name = "measuring_instrument_status", type = String.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "daintree_status", type = String.class),
                                @ColumnResult(name = "ecobee_status", type = String.class),
                                @ColumnResult(name = "modbus_status", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "category", type = String.class)

                        })
        })


//To be removed after new pagination api works
@NamedNativeQuery(
        name = "Device.listDevicesTs",
        query = "SELECT d.id, d.display_name, d.user_data_name, l.name as location, f.name as floor, b.name as building, d.status, d.model,"
                + " d.last_seen_on , p.image_url_1, d.virtual_device_type, d.snmp_status, d.bacnet_status, d.lorawan_status, d.disruptive_status,"
                + " d.my_devices_status, d.monnit_status, d.pelican_status,d.knx_status, d.snmp_object_status, d.measuring_instrument_status,d.record_checklist_status, d.daintree_status, d.ecobee_status, d.modbus_status, d.type, d.sub_category, d.category "
                + " From device d"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " Left JOIN product_details p ON d.product_id = p.product_id"
                + " WHERE (?1 = 'null' or d.docker_name = ?1) AND (?2 = 'null' or b.id = ?2) AND (?3 = 'null' or f.id = ?3) AND (?4 = 'null' or l.id = ?4) AND (?5 = 3 or d.status = ?5) AND d.monitor = 1",
        resultSetMapping = "devicelistmapping")


//Added Pagination for listDevices
@NamedNativeQuery(
        name = "Device.listDevicesByPaginationTs",
        query = "SELECT d.id, d.display_name, d.user_data_name, l.name as location, f.name as floor, b.name as building, d.status, d.model,"
                + " d.last_seen_on , p.image_url_1, d.virtual_device_type, d.snmp_status, d.bacnet_status, d.lorawan_status, d.disruptive_status,"
                + " d.my_devices_status, d.monnit_status, d.pelican_status,d.knx_status, d.snmp_object_status, d.measuring_instrument_status,d.record_checklist_status, d.daintree_status, d.ecobee_status, d.modbus_status, d.type, d.sub_category, d.category "
                + " From device d"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " Left JOIN product_details p ON d.product_id = p.product_id"
                + " WHERE (?1 = 'null' or d.docker_name = ?1) AND (?2 = 'null' or b.id = ?2) AND (?3 = 'null' or f.id = ?3) AND (?4 = 'null' or l.id = ?4) AND "
                + "  (?5 = 3 or d.status = ?5)  AND (?8 IS NULL or IF(?8 = 123 , (d.virtual_device_type IS NOT NULL AND (d.virtual_device_type!= 0 AND d.virtual_device_type!= 1)), NULL)) AND d.monitor = 1"
                + " LIMIT ?6  OFFSET ?7",
        resultSetMapping = "devicelistmapping")

@SqlResultSetMapping(
        name = "deviceinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDetailsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "alarm", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "local_vendor_id", type = String.class),
                                @ColumnResult(name = "other_vendor_1_id", type = String.class),
                                @ColumnResult(name = "other_vendor_2_id", type = String.class),
                                @ColumnResult(name = "other_vendor_3_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "image_url_1", type = String.class),
                                @ColumnResult(name = "image_url_2", type = String.class),
                                @ColumnResult(name = "image_url_3", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "snmp_status", type = String.class),
                                @ColumnResult(name = "bacnet_status", type = String.class),
                                @ColumnResult(name = "lorawan_status", type = String.class),
                                @ColumnResult(name = "disruptive_status", type = String.class),
                                @ColumnResult(name = "my_devices_status", type = String.class),
                                @ColumnResult(name = "monnit_status", type = String.class),
                                @ColumnResult(name = "pelican_status", type = String.class),
                                @ColumnResult(name = "knx_status", type = String.class),
                                @ColumnResult(name = "snmp_object_status", type = String.class),
                                @ColumnResult(name = "measuring_instrument_status", type = String.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "daintree_status", type = String.class),
                                @ColumnResult(name = "ecobee_status", type = String.class),
                                @ColumnResult(name = "modbus_status", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class)


                        })
        }
)


@NamedNativeQuery(
        name = "Device.getDeviceInfoById",
        query = "SELECT d.id, d.alarm, d.display_name, d.product_id, d.ip_address, d.mac_address, d.last_seen_on, d.model, d.status, d.type, d.user_data_model, d.user_data_name, d.user_data_vendor,"
                + "d.vendor, d.local_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id, d.docker_name, b.name as building,"
                + " f.name as floor, l.name as location,  p.image_url_1, p.image_url_2, p.image_url_3, d.virtual_device_type,"
                + " d.snmp_status, d.bacnet_status, d.lorawan_status, d.disruptive_status, d.my_devices_status, d.monnit_status,"
                + " d.pelican_status,d.knx_status, d.snmp_object_status, d.measuring_instrument_status,d.record_checklist_status, d.daintree_status, d.ecobee_status, d.modbus_status,d.asset_group"
                + " FROM device d"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " Left JOIN product_details p ON d.product_id = p.product_id"
                + " WHERE d.id = ?1",
        resultSetMapping = "deviceinfomapping")


@SqlResultSetMapping(
        name = "devicelistparentTS",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceListDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "alarm", type = String.class),
                                @ColumnResult(name = "image_url_1", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "account_number", type = String.class),
                                @ColumnResult(name = "company_name", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "phone", type = String.class),
                                @ColumnResult(name = "phone_type", type = String.class),
                                @ColumnResult(name = "value", type = String.class),
                                @ColumnResult(name = "vendor_name", type = String.class)
                        })
        }
)


//to be removed after pagination api works
@NamedNativeQuery(
        name = "Device.listofflinedeviceByParentTs",
        query = "SELECT d.id, d.display_name, d.user_data_name, d.ip_address, d.mac_address, d.last_seen_on, d.model, d.status, d.user_data_model , d.alarm,"
                + " d.vendor, d.user_data_vendor, d.docker_name, b.name as building, f.name as floor, l.name as location,  p.image_url_1, ph.account_number, ph.company_name, ph.email,"
                + " ph.phone, ph.phone_type, ph.value, ph.vendor_name"
                + " FROM device d"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " Left JOIN product_details p ON d.product_id = p.product_id"
                + " Left JOIN phonebook ph ON d.local_vendor_id = ph.id "
                + "WHERE d.status = 0 AND d.monitor = 1",
        resultSetMapping = "devicelistparentTS")

//Added pagination for listofflinedeviceByParentTs
@NamedNativeQuery(
        name = "Device.listofflinedeviceByParentByPaginationTs",
        query = "SELECT d.id, d.display_name, d.user_data_name, d.ip_address, d.mac_address, d.last_seen_on, d.model, d.status, d.user_data_model , d.alarm,"
                + " d.vendor, d.user_data_vendor, d.docker_name, b.name as building, f.name as floor, l.name as location,  p.image_url_1, ph.account_number, ph.company_name, ph.email,"
                + " ph.phone, ph.phone_type, ph.value, ph.vendor_name"
                + " FROM device d"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " Left JOIN product_details p ON d.product_id = p.product_id"
                + " Left JOIN phonebook ph ON d.local_vendor_id = ph.id "
                + "WHERE d.status = 0 AND d.monitor = 1"
                + " LIMIT ?1  OFFSET ?2",
        resultSetMapping = "devicelistparentTS")


@NamedNativeQuery(
        name = "Device.DeviceInfoById",
        query = "SELECT d.id, d.display_name, d.user_data_name, d.ip_address, d.mac_address, d.last_seen_on, d.model, d.status, d.user_data_model , d.alarm,"
                + " d.vendor, d.user_data_vendor, d.docker_name, b.name as building, f.name as floor, l.name as location,  p.image_url_1, ph.account_number, ph.company_name, ph.email,"
                + " ph.phone, ph.phone_type, ph.value, ph.vendor_name"
                + " FROM device d"
                + " Left JOIN location l ON d.location_id = l.id"
                + " Left JOIN floor f ON l.floor_id = f.id"
                + " Left JOIN building b ON f.building_id = b.id"
                + " Left JOIN product_details p ON d.product_id = p.product_id"
                + " Left JOIN phonebook ph ON d.local_vendor_id = ph.id "
                + "WHERE d.monitor = 1 AND d.id = ?1 AND d.popup_notification = 1",
        resultSetMapping = "devicelistparentTS")


@SqlResultSetMapping(
        name = "devicenamemapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Device.getDeviceNamesByVdmsIdAndDockerName",
        query = "SELECT id ,display_name ,user_data_name FROM device WHERE docker_vdms_id = ?1 AND docker_name = ?2",
        resultSetMapping = "devicenamemapping"
)


//*****************************************************8API FROM MONITOR****************************************************
@SqlResultSetMapping(
        name = "devicemonitornamemapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceMonitorDTO.class,
                        columns = {
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Device.getDeviceListMonitor",
        query = "SELECT ip_address, mac_address, status FROM device WHERE docker_name = ?1 AND (virtual_device_type = 0 OR virtual_device_type IS NULL)  ",
        resultSetMapping = "devicemonitornamemapping"
)

// with multiple IP
@NamedNativeQuery(
        name = "Device.getDeviceListMonitorIp",
        query = "SELECT dip.ip_address, d.mac_address, d.status "
                + "FROM device_ip_address dip "
                + "Left Join device d on d.id = dip.device_id "
                + "WHERE d.docker_name = ?1 AND (d.virtual_device_type = 0 OR d.virtual_device_type IS NULL)  ",
        resultSetMapping = "devicemonitornamemapping"
)


//@SqlResultSetMapping(
//		name = "devicesnmpmapping",
//		classes = {
//				@ConstructorResult(
//						targetClass = SnmpValuesDTO.class,
//						columns = {
//								@ColumnResult(name = "id",type = String.class),
//								@ColumnResult(name = "community_string",type = String.class),
//								@ColumnResult(name = "version",type = String.class),
//								@ColumnResult(name = "ip_address",type = String.class),
//								@ColumnResult(name = "snmp_parent",type = String.class),
//								@ColumnResult(name = "mac_address",type = String.class),
//								@ColumnResult(name = "port",type = Integer.class),
//								@ColumnResult(name = "security_level",type = Integer.class),
//								@ColumnResult(name = "username",type = String.class),
//								@ColumnResult(name = "auth_passphrase",type = String.class),
//								@ColumnResult(name = "priv_passphrase",type = String.class)
//						}
//						)
//		}
//		)
//
//
//@NamedNativeQuery(
//		name = "Device.getDeviceListSnmp",
//		query = "SELECT d.id, sc.community_string, sc.snmp_version as version, d.ip_address, d.snmp_parent, d.mac_address, sc.port, sc.security_level, "
//				+ "sc.username, sc.auth_passphrase, sc.priv_passphrase "
//				+ "FROM device d LEFT JOIN snmp_configuration sc ON d.id = sc.device_id "
//				+ "WHERE d.docker_name = ?1 AND (d.virtual_device_type = 0 OR d.virtual_device_type IS NULL) AND d.status = 1",
//				resultSetMapping = "devicesnmpmapping"
//		)
//
////Get Single Device Info By Device Id for Snmp Sync
//@NamedNativeQuery(
//		name = "Device.getDeviceSnmpByDeviceId",
//		query = "SELECT d.id, sc.community_string, sc.snmp_version as version, d.ip_address, d.snmp_parent, d.mac_address, sc.port, sc.security_level, "
//				+ "sc.username, sc.auth_passphrase, sc.priv_passphrase "
//				+ "FROM device d LEFT JOIN snmp_configuration sc ON d.id = sc.device_id "
//				+ "WHERE d.docker_name = ?1 AND (d.virtual_device_type = 0 OR d.virtual_device_type IS NULL) AND d.id = ?2 AND d.status = 1",
//				resultSetMapping = "devicesnmpmapping"
//		)

@SqlResultSetMapping(
        name = "devicesnmpmapping",
        classes = {
                @ConstructorResult(
                        targetClass = SnmpValuesDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "community_string", type = String.class),
                                @ColumnResult(name = "version", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "snmp_parent", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "port", type = Integer.class),
                                @ColumnResult(name = "security_level", type = Integer.class),
                                @ColumnResult(name = "username", type = String.class),
                                @ColumnResult(name = "auth_passphrase", type = String.class),
                                @ColumnResult(name = "priv_passphrase", type = String.class),
                                @ColumnResult(name = "auth_type", type = String.class),
                                @ColumnResult(name = "priv_type", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Device.getDeviceListSnmp",
        query = "SELECT d.id, sc.community_string, sc.snmp_version as version, d.ip_address, d.snmp_parent, d.mac_address, sc.port, sc.security_level, "
                + "sc.username, sc.auth_passphrase, sc.priv_passphrase, sc.auth_type, sc.priv_type "
                + "FROM device d LEFT JOIN snmp_configuration sc ON d.id = sc.device_id "
                + "WHERE d.docker_name = ?1 AND (d.virtual_device_type = 0 OR d.virtual_device_type IS NULL) AND d.status = 1",
        resultSetMapping = "devicesnmpmapping"
)

//Get Single Device Info By Device Id for Snmp Sync
@NamedNativeQuery(
        name = "Device.getDeviceSnmpByDeviceId",
        query = "SELECT d.id, sc.community_string, sc.snmp_version as version, d.ip_address, d.snmp_parent, d.mac_address, sc.port, sc.security_level, "
                + "sc.username, sc.auth_passphrase, sc.priv_passphrase, sc.auth_type, sc.priv_type "
                + "FROM device d LEFT JOIN snmp_configuration sc ON d.id = sc.device_id "
                + "WHERE d.docker_name = ?1 AND (d.virtual_device_type = 0 OR d.virtual_device_type IS NULL) AND d.id = ?2 AND d.status = 1",
        resultSetMapping = "devicesnmpmapping"
)


//to get information when device alert
@SqlResultSetMapping(
        name = "devicealertmapping",
        classes = {
                @ConstructorResult(
                        targetClass = AlertDTO.class,
                        columns = {
                                @ColumnResult(name = "email_alert", type = Integer.class),
                                @ColumnResult(name = "sms_alert", type = Integer.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "device_name", type = String.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "monitor", type = Integer.class),
                                @ColumnResult(name = "local_vendor_email_alert", type = Integer.class),
                                @ColumnResult(name = "local_vendor_sms_alert", type = Integer.class),
                                @ColumnResult(name = "local_vendor_name", type = String.class),
                                @ColumnResult(name = "local_vendor_email", type = String.class),
                                @ColumnResult(name = "local_vendor_extension", type = String.class),
                                @ColumnResult(name = "local_vendor_phone", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceAlertInfoByDeviceId",
        query = "SELECT d.email_alert, d.sms_alert, d.docker_vdms_id AS vdms_id, v.customer_org_id, do.vendor_org_id, d.docker_name, do.system_type, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, d.product_id, "
                + "b.name as building, f.name as floor, l.name as location, d.monitor, "
                + "d.local_vendor_email_alert as local_vendor_email_alert, d.local_vendor_sms_alert as local_vendor_sms_alert, "
                + "ph.vendor_name as local_vendor_name, ph.email as local_vendor_email, ph.value as local_vendor_extension, "
                + "ph.phone as local_vendor_phone "
                + "FROM device d "
                + "LEFT JOIN phonebook ph ON d.local_vendor_id = ph.id "
                + "LEFT JOIN docker do ON d.docker_name = do.name AND d.docker_vdms_id = do.vdms_id "
                + "LEFT JOIN vdms v ON do.vdms_id = v.id "
                + "LEFT JOIN location l ON d.location_id = l.id "
                + "LEFT JOIN floor f ON l.floor_id = f.id "
                + "LEFT JOIN building b ON f.building_id = b.id "
                + "WHERE d.id = ?1",
        resultSetMapping = "devicealertmapping"
)

@SqlResultSetMapping(
        name = "devicelisttopology",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "parent", type = String.class),
                                @ColumnResult(name = "snmp_parent", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "type", type = String.class)

                        })
        }
)


@NamedNativeQuery(
        name = "Device.listTopologyDevicesByDockerName",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.last_seen_on, d.mac_address, d.vendor, d.model, d.ip_address, l.name location, d.user_data_vendor, d.user_data_name, "
                + "IF(d.parent = '', 'no_parent', d.parent) as parent, d.snmp_parent, d.docker_name, b.name as building, f.name as floor, d.status, d.virtual_device_type, d.type from device d "
                + "Left JOIN location l ON d.location_id = l.id "
                + "Left JOIN floor f ON l.floor_id = f.id "
                + "Left JOIN building b ON f.building_id = b.id "
                + "WHERE (?1 IS NOT NULL AND d.docker_name = ?1) OR (?2 IS NOT NULL AND d.id = ?2) OR ?1 = 'all' ",
        resultSetMapping = "devicelisttopology")


@SqlResultSetMapping(
        name = "alllisttopologymapper",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceTopologyDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "parent", type = String.class),
                                @ColumnResult(name = "snmp_parent", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "connection_type", type = String.class),
                                @ColumnResult(name = "user_connection_type", type = String.class)

                        })
        }
)

@NamedNativeQuery(
        name = "Device.listTopologyDevices",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.last_seen_on, d.mac_address, d.vendor, d.model, d.ip_address, l.name location, d.user_data_vendor, d.user_data_name, "
                + "IF(d.parent = '', 'no_parent', d.parent) as parent, d.snmp_parent, d.docker_name, b.name as building, f.name as floor, d.status, d.virtual_device_type, d.type, d.connection_type, d.user_connection_type from device d "
                + "Left JOIN location l ON d.location_id = l.id "
                + "Left JOIN floor f ON l.floor_id = f.id "
                + "Left JOIN building b ON f.building_id = b.id "
                + "WHERE (?1 IS NOT NULL AND d.docker_name = ?1) OR (?2 IS NOT NULL AND d.id = ?2) OR ?1 = 'all' ",
        resultSetMapping = "alllisttopologymapper")


//get devices by type
@SqlResultSetMapping(
        name = "devicelistbytypes",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceSensorsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "sensor_alert", type = Boolean.class)
                        })
        }
)

//IF(d.position IS NULL, l.position, d.position) as position
@NamedNativeQuery(
        name = "Device.getDevicesByType",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, l.name as location, l.id as location_id,"
                + " d.type, d.virtual_device_type, d.status, IF(d.position IS NULL, l.position, d.position) as position,"
                + " IF(d.bacnet_status = 'alert' OR d.lorawan_status = 'alert' OR d.disruptive_status = 'alert' OR d.my_devices_status = 'alert' OR"
                + " d.monnit_status = 'alert' OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.snmp_object_status = 'alert' OR d.measuring_instrument_status = 'alert' OR d.ecobee_status = 'alert' OR d.modbus_status = 'alert', 1, 0) as sensor_alert "
                + " FROM device d"
                + " LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id)"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " WHERE (?1 = 'all' or d.docker_name = ?1) AND (?2 = 'null' or f.id = ?2) AND d.type IN ?3 AND d.monitor = 1",
        resultSetMapping = "devicelistbytypes"
)

@NamedNativeQuery(
        name = "Device.getDevicesByTypePagination",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, l.name as location, l.id as location_id,"
                + " d.type, d.virtual_device_type, d.status, IF(d.position IS NULL, l.position, d.position) as position,"
                + " IF(d.bacnet_status = 'alert' OR d.lorawan_status = 'alert' OR d.disruptive_status = 'alert' OR d.my_devices_status = 'alert' OR"
                + " d.monnit_status = 'alert' OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.snmp_object_status = 'alert' OR d.measuring_instrument_status = 'alert' OR d.ecobee_status = 'alert' OR d.modbus_status = 'alert', 1, 0) as sensor_alert "
                + " FROM device d"
                + " LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id)"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " WHERE (?1 = 'all' or d.docker_name = ?1) AND (?2 = 'null' or f.id = ?2) AND d.type IN ?3 AND d.monitor = 1 AND d.asset_match_status != 3 "
                + " LIMIT ?4 OFFSET ?5 ",
        resultSetMapping = "devicelistbytypes"
)


// device list for integrations
@SqlResultSetMapping(
        name = "devicelistbytypesintegration",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class)
                        })
        }
)

//IF(d.position IS NULL, l.position, d.position) as position
@NamedNativeQuery(
        name = "Device.listDevicebyDockerIntegration",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, l.name as location, d.docker_name"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " WHERE (?1 = 'null' or d.docker_name = ?1) AND d.monitor = 1 AND d.asset_match_status != 3",
        resultSetMapping = "devicelistbytypesintegration")


//@SqlResultSetMapping(
//		name = "parentdevicemapping",
//		classes = {
//				@ConstructorResult(
//						targetClass = DeviceDTO.class,
//						columns = {
//								@ColumnResult(name = "id",type = String.class),
//								@ColumnResult(name = "name",type = String.class),
//								@ColumnResult(name = "ip_address",type = String.class),
//								@ColumnResult(name = "status",type = Integer.class),
//								@ColumnResult(name = "type",type = String.class),
//								@ColumnResult(name = "vendor",type = String.class),
//								@ColumnResult(name = "mac_address",type = String.class),
//								@ColumnResult(name = "location",type = String.class),
//								@ColumnResult(name = "docker_name",type = String.class)
//						}
//				)
//		}
//)
//
//@NamedNativeQuery(
//		name = "Device.getNetworkParentDeviceByPagination",
//		query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.ip_address,"
//				+ " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
//				+ " d.mac_address, l.name as location, d.docker_name"
//				+ " FROM device d"
//				+ " LEFT JOIN location l ON l.id = d.location_id"
//				+ " WHERE d.docker_name IN ?1"
//				+ " AND (?2 = 'null'  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor) LIKE CONCAT('%',?2,'%'))"
//				+ " LIMIT ?3 OFFSET ?4",
//		resultSetMapping = "parentdevicemapping")
//
//
//@NamedNativeQuery(
//		name = "Device.getAllParentDeviceByPagination",
//		query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.ip_address,"
//				+ " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
//				+ " d.mac_address, l.name as location, d.docker_name"
//				+ " FROM device d"
//				+ " LEFT JOIN location l ON l.id = d.location_id"
//				+ " WHERE (?1 = 'null'  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor) LIKE CONCAT('%',?1,'%'))"
//				+ " LIMIT ?2 OFFSET ?3",
//		resultSetMapping = "parentdevicemapping")


@SqlResultSetMapping(
        name = "parentdevicemapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getNetworkParentDeviceByPagination",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name,d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ('all' IN ?1 or d.docker_name IN ?1) AND ('all' IN ?2 or d.type IN ?2) AND ('all' IN ?6 or (IF ('other' IN ?6, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?6, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?6, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?3 = 'null'  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?3,'%'))"
                + " LIMIT ?4 OFFSET ?5",
        resultSetMapping = "parentdevicemapping")


@NamedNativeQuery(
        name = "Device.getAllParentDeviceByPagination",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null'  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?1,'%'))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "parentdevicemapping")

@NamedNativeQuery(
        name = "Device.listAlldevices",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, d.vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE virtual_device_type = 0 or virtual_device_type = 1 or virtual_device_type IS NULL",
        resultSetMapping = "parentdevicemapping"
)

@NamedNativeQuery(
        name = "Device.getAllNetworkParentDeviceByPagination",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name,d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?1 or d.docker_name IN ?1) AND ('all' IN ?2 or d.type IN ?2) AND ('all' IN ?6 or (IF ('other' IN ?6, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?6, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?6, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?3 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?3,'%'))"
                + " AND (?7 IS NULL OR IF(?7, d.id IN ?8 , d.id NOT IN ?8))"
                + " AND (?9 IS NULL OR IF(?9, d.id IN ?10 , d.id NOT IN ?10))"
                + " AND (?11 IS NULL OR IF(?11, d.id IN ?12 , d.id NOT IN ?12))"
                + " LIMIT ?4 OFFSET ?5",
        resultSetMapping = "parentdevicemapping")


/*****************************************Asset Mapper Queries*********************************************************/

@SqlResultSetMapping(
        name = "assetDeviceMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AssetDeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "network_layer", type = Integer.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "customFields", type = String.class),
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.findByDisplayName",
        query = "SELECT id,"
                + "display_name,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "custom_fields AS customFields FROM device WHERE display_name LIKE CONCAT('%',?1,'%')",
        resultSetMapping = "assetDeviceMapping"
)

@NamedNativeQuery(
        name = "Device.findByModel",
        query = "SELECT id,"
                + "display_name,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "custom_fields AS customFields FROM device WHERE model LIKE CONCAT('%',?1,'%')",
        resultSetMapping = "assetDeviceMapping"
)

@NamedNativeQuery(
        name = "Device.findByVendor",
        query = "SELECT id,"
                + "display_name,"
                + "mac_address,"
                + "model,"
                + "vendor,"
                + "type,"
                + "ip_address,"
                + "network_layer,"
                + "serial_number,"
                + "warranty,"
                + "custom_fields AS customFields FROM device WHERE vendor LIKE CONCAT('%',?1,'%')",
        resultSetMapping = "assetDeviceMapping"
)

@NamedNativeQuery(
        name = "Device.getPaginatedDevices",
        query = "SELECT d.id,"
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name,"
                + "d.mac_address,"
                + "IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model,"
                + "IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + "d.type,"
                + "d.ip_address,"
                + "d.network_layer,"
                + "d.serial_number,"
                + "d.warranty,"
                + "d.custom_fields AS customFields FROM device d LIMIT ?1 OFFSET ?2",
        resultSetMapping = "assetDeviceMapping"
)

//to get unmatched devices by pagination
@SqlResultSetMapping(
        name = "assetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AssetDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "matched_product_ids", type = String.class),
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getUntaggedProductDevicesByPagination",
        query = "SELECT d.id,"
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, "
                + "IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, "
                + "IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor, "
                + "d.type, "
                + "d.matched_product_ids "
                + "FROM device d "
                + "WHERE d.product_id IS NULL "
                + "LIMIT ?1 OFFSET ?2",
        resultSetMapping = "assetMapping"
)

@NamedNativeQuery(
        name = "Device.getAssetMapperDevicesByIdList",
        query = "SELECT d.id,"
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, "
                + "IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, "
                + "IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor, "
                + "d.type, "
                + "d.matched_product_ids "
                + "FROM device d "
                + "WHERE d.id IN ?1",
        resultSetMapping = "assetMapping"
)

@NamedNativeQuery(
        name = "Device.getAssetMapperDeviceById",
        query = "SELECT d.id,"
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, "
                + "IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, "
                + "IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor, "
                + "d.type, "
                + "d.matched_product_ids "
                + "FROM device d "
                + "WHERE d.id = ?1",
        resultSetMapping = "assetMapping"
)

@NamedNativeQuery(
        name = "Device.getAssetMapperSubSystemDevicesById",
        query = "SELECT d.id,"
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, "
                + "IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, "
                + "IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor, "
                + "d.type, "
                + "d.matched_product_ids "
                + "FROM device d "
                + "WHERE d.subsystem_parent_id = ?1",
        resultSetMapping = "assetMapping"
)

/*****************************************Asset Mapper Queries*********************************************************/


@SqlResultSetMapping(
        name = "devicedetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class)
                        }
                )
        }
)

//list devices for snmp discovery
@NamedNativeQuery(
        name = "Device.getAllDeviceByVdmsIdAndDockerName",
        query = "SELECT  d.id, d.status, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address,l.name as location,"
                + " d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " b.name as building, f.name as floor,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or d.docker_vdms_id = ?1) AND (?2 = 'null' or d.docker_name = ?2)"
                + " LIMIT ?3 OFFSET ?4",
        resultSetMapping = "devicedetailsmapping"
)

@NamedNativeQuery(
        name = "Device.getDeviceDetails",
        query = "SELECT  d.id, d.status, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.mac_address, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor, IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model,"
                + " d.type , d.ip_address,l.name as location,"
                + " d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " b.name as building, f.name as floor,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.id = ?1",
        resultSetMapping = "devicedetailsmapping"
)

@NamedNativeQuery(
        name = "Device.getAllDeviceByVdmsIdAndDockerNameWithoutPagination",
        query = "SELECT  d.id, d.status, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address,l.name as location,"
                + " d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " b.name as building, f.name as floor,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 = 'null' or d.docker_vdms_id = ?1) AND (?2 = 'null' or d.docker_name = ?2)",
        resultSetMapping = "devicedetailsmapping"
)

@SqlResultSetMapping(
        name = "devicealertdetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceAlertDTO.class,
                        columns = {

                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "docker_system_type", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "device_monitor", type = Integer.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "type", type = String.class)


                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceAlertInfoById",
        query = "SELECT d.id, d.docker_name, do.system_type as docker_system_type, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, "
                + "b.name as building, f.name as floor, l.name as location, d.monitor as device_monitor, d.product_id, p.global_image_url_1 as image_url, d.type "
                + "FROM device d "
                + "LEFT JOIN docker do ON d.docker_name = do.name AND d.docker_vdms_id = do.vdms_id "
                + "LEFT JOIN vdms v ON do.vdms_id = v.id "
                + "LEFT JOIN produc" +
                "t_details p ON d.product_id = p.product_id "
                + "LEFT JOIN location l ON d.location_id = l.id "
                + "LEFT JOIN floor f ON l.floor_id = f.id "
                + "LEFT JOIN building b ON f.building_id = b.id "
                + "WHERE d.id = ?1",
        resultSetMapping = "devicealertdetailsmapping"
)

//Room Status
@SqlResultSetMapping(
        name = "roomstatusdevicemapping",
        classes = {
                @ConstructorResult(
                        targetClass = RoomStatusDTO.class,
                        columns = {
                                @ColumnResult(name = "device_status", type = Integer.class),
                                @ColumnResult(name = "sensor_alert_status", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getRoomStatusByDeviceId",
        query = "SELECT d.status as device_status,"
                + " IF(d.bacnet_status = 'alert' OR d.lorawan_status = 'alert' OR d.disruptive_status = 'alert' OR d.my_devices_status = 'alert' OR"
                + " d.monnit_status = 'alert' OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.snmp_object_status = 'alert' OR d.measuring_instrument_status = 'alert' OR d.daintree_status = 'alert', 1, IF(d.bacnet_count > 0 OR d.lorawan_count > 0  OR d.disruptive_count > 0  OR"
                + " d.my_devices_count > 0  OR d.monnit_count > 0  OR d.pelican_count > 0 OR d.knx_count > 0 OR d.snmp_object_count > 0 OR d.measuring_instrument_count > 0 OR d.daintree_count > 0 OR d.ecobee_count > 0 OR d.modbus_count > 0, 0, NULL)) as sensor_alert_status"
                + " FROM device d"
                + " WHERE ('null' = ?1 or d.id = ?1) AND d.monitor = 1",
        resultSetMapping = "roomstatusdevicemapping"
)

@SqlResultSetMapping(
        name = "roomstatuslocationmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceMonitorSpaceDTO.class,
                        columns = {

                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "sensorstatus", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getRoomStatusByLocationId",
        query = "SELECT d.location_id as id, d.status, "
                + " IF(d.bacnet_status = 'alert' OR d.lorawan_status = 'alert' OR d.disruptive_status = 'alert' OR d.my_devices_status = 'alert' OR"
                + " d.monnit_status = 'alert' OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.snmp_object_status = 'alert' OR d.measuring_instrument_status = 'alert' OR d.daintree_status = 'alert', 1, IF(d.bacnet_count > 0 OR d.lorawan_count > 0  OR d.disruptive_count > 0  OR"
                + " d.my_devices_count > 0  OR d.monnit_count > 0  OR d.pelican_count > 0 OR d.knx_count > 0 OR d.snmp_object_count > 0 OR d.measuring_instrument_count > 0  OR d.daintree_count > 0 OR d.ecobee_count > 0 OR d.modbus_count > 0, 0, NULL)) as sensorstatus"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE l.id = ?1 AND d.monitor = 1",
        resultSetMapping = "roomstatuslocationmapping"
)

@SqlResultSetMapping(
        name = "isaddeddevicemapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "is_added", type = Integer.class),
                                @ColumnResult(name = "asset_group", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getAllRecordChecklistDevicesPagination",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(rc.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN record_checklist rc ON rc.device_id = d.id  AND rc.global_checklist_id IN ?6 AND rc.inspection_record_id = ?7 AND rc.is_removed = 0"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?4 or d.docker_name IN ?4) AND ('all' IN ?5 or d.type IN ?5) AND ('all' IN ?8 or (IF ('other' IN ?8, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?8, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?8, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?1,'%'))"
                + " AND (?9 IS NULL OR IF(?9, d.id IN ?10 , d.id NOT IN ?10))"
                + " AND (?11 IS NULL OR IF(?11, d.id IN ?12 , d.id NOT IN ?12))"
                + " AND (?13 IS NULL OR IF(?13, d.id IN ?14 , d.id NOT IN ?14))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddeddevicemapping")

@NamedNativeQuery(
        name = "Device.getAllQrcodeDevicesPagination",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(gr.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN global_qrcode gr ON gr.device_id = d.id"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?4 or d.docker_name IN ?4) AND ('all' IN ?5 or d.type IN ?5) AND ('all' IN ?6 or (IF ('other' IN ?6, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?6, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?6, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 = 'null'  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description) LIKE CONCAT('%',?1,'%'))"
                + " AND (?7 IS NULL OR IF(?7, d.id IN ?8 , d.id NOT IN ?8))"
                + " AND (?9 IS NULL OR IF(?9, d.id IN ?10 , d.id NOT IN ?10))"
                + " AND (?11 IS NULL OR IF(?11, d.id IN ?12 , d.id NOT IN ?12))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddeddevicemapping")

@NamedNativeQuery(
        name = "Device.getAllBarCodeDevicesPagination",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(gr.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN client_bar_code cbc ON cbc.device_id = d.id"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?4 or d.docker_name IN ?4) AND ('all' IN ?5 or d.type IN ?5) AND ('all' IN ?6 or (IF ('other' IN ?6, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?6, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?6, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 = 'null'  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description) LIKE CONCAT('%',?1,'%'))"
                + " AND (?7 IS NULL OR IF(?7, d.id IN ?8 , d.id NOT IN ?8))"
                + " AND (?9 IS NULL OR IF(?9, d.id IN ?10 , d.id NOT IN ?10))"
                + " AND (?11 IS NULL OR IF(?11, d.id IN ?12 , d.id NOT IN ?12))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddeddevicemapping")


@NamedNativeQuery(
        name = "Device.getAllChecklistDevicesPagination",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(dgc.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN device_global_checklist dgc ON dgc.device_id = d.id AND dgc.global_checklist_id IN ?6 AND dgc.is_removed = 0"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?4 or d.docker_name IN ?4) AND ('all' IN ?5 or d.type IN ?5) AND ('all' IN ?7 or (IF ('other' IN ?7, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?7, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?7, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?1,'%'))"
                + " AND (?8 IS NULL OR IF(?8, d.id IN ?9 , d.id NOT IN ?9))"
                + " AND (?10 IS NULL OR IF(?10, d.id IN ?11 , d.id NOT IN ?11))"
                + " AND (?12 IS NULL OR IF(?12, d.id IN ?13 , d.id NOT IN ?13))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddeddevicemapping")


@NamedNativeQuery(
        name = "Device.getAllInspectionDevicesPagination",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(gir.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN global_inspection_relation gir ON gir.device_id = d.id  AND gir.global_checklist_id IN ?6 AND gir.global_inspection_record_id = ?7 AND gir.is_removed = 0"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?4 or d.docker_name IN ?4) AND ('all' IN ?5 or d.type IN ?5) AND ('all' IN ?8 or (IF ('other' IN ?8, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?8, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?8, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?1,'%'))"
                + " AND (?9 IS NULL OR IF(?9, d.id IN ?10 , d.id NOT IN ?10))"
                + " AND (?11 IS NULL OR IF(?11, d.id IN ?12 , d.id NOT IN ?12))"
                + " AND (?13 IS NULL OR IF(?13, d.id IN ?14 , d.id NOT IN ?14))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddeddevicemapping")

@NamedNativeQuery(
        name = "Device.getAllChecklistDevices",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(dgc.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN device_global_checklist dgc ON dgc.device_id = d.id AND dgc.global_checklist_id IN ?4 AND dgc.is_removed = 0 "
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?2 or d.docker_name IN ?2) AND ('all' IN ?3 or d.type IN ?3) AND ('all' IN ?5 or (IF ('other' IN ?5, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?5, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?5, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 IS NULL or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description) LIKE CONCAT('%',?1,'%'))"
                + " AND (?6 IS NULL OR IF(?6, d.id IN ?7 , d.id NOT IN ?7))"
                + " AND (?8 IS NULL OR IF(?8, d.id IN ?9 , d.id NOT IN ?9))",
        resultSetMapping = "isaddeddevicemapping")


@NamedNativeQuery(
        name = "Device.getAllInspectionDevices",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(gir.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN global_inspection_relation gir ON gir.device_id = d.id  AND gir.global_checklist_id = ?4 AND gir.global_inspection_record_id = ?5 AND gir.is_removed = 0 "
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?2 or d.docker_name IN ?2) AND ('all' IN ?3 or d.type IN ?3) AND ('all' IN ?6 or (IF ('other' IN ?6, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?6, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?6, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 IS NULL or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description) LIKE CONCAT('%',?1,'%'))"
                + " AND (?7 IS NULL OR IF(?7, d.id IN ?8 , d.id NOT IN ?8))"
                + " AND (?9 IS NULL OR IF(?9, d.id IN ?10 , d.id NOT IN ?10))",
        resultSetMapping = "isaddeddevicemapping")


@NamedNativeQuery(
        name = "Device.getAllQrcodeDevices",
        query = "SELECT DISTINCT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,"
                + " IF(gr.device_id = d.id, 1, 0) as is_added,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN global_qrcode gr ON gr.device_id = d.id"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?2 or d.docker_name IN ?2) AND ('all' IN ?3 or d.type IN ?3) AND ('all' IN ?4 or (IF ('other' IN ?4, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?4, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?4, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?1 IS NULL  or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description) LIKE CONCAT('%',?1,'%'))"
                + " AND (?5 IS NULL OR IF(?5, d.id IN ?6 , d.id NOT IN ?6))"
                + " AND (?7 IS NULL OR IF(?7, d.id IN ?8 , d.id NOT IN ?8))",

        resultSetMapping = "isaddeddevicemapping")

@NamedNativeQuery(
        name = "Device.getAllNetworkParentDevices",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name,d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?1 or d.docker_name IN ?1) AND ('all' IN ?2 or d.type IN ?2) AND ('all' IN ?4 or (IF ('other' IN ?4, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?4, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?4, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?3 IS NULL or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?3,'%'))"
                + " AND (?5 IS NULL OR IF(?5, d.id IN ?6 , d.id NOT IN ?6))"
                + " AND (?7 IS NULL OR IF(?8, d.id IN ?8 , d.id NOT IN ?8))",
        resultSetMapping = "parentdevicemapping")

@SqlResultSetMapping(
        name = "deviceconditionalertdetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceAlertDTO.class,
                        columns = {

                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "docker_system_type", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "device_monitor", type = Integer.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "local_vendor_id", type = String.class),
                                @ColumnResult(name = "type", type = String.class)



                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceConditionAlertInfoById",
        query = "SELECT d.id, d.docker_name, do.system_type as docker_system_type, "
                + "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, "
                + "b.name as building, f.name as floor, l.name as location, d.monitor as device_monitor, d.product_id, p.global_image_url_1 as image_url, d.local_vendor_id, d.type "
                + "FROM device d "
                + "LEFT JOIN docker do ON d.docker_name = do.name AND d.docker_vdms_id = do.vdms_id "
                + "LEFT JOIN vdms v ON do.vdms_id = v.id "
                + "LEFT JOIN product_details p ON d.product_id = p.product_id "
                + "LEFT JOIN location l ON d.location_id = l.id "
                + "LEFT JOIN floor f ON l.floor_id = f.id "
                + "LEFT JOIN building b ON f.building_id = b.id "
                + "WHERE d.id = ?1",
        resultSetMapping = "deviceconditionalertdetailsmapping"
)


@SqlResultSetMapping(
        name = "getfilterdevicesmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getFilterVirtualDevicesByPagination",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.ip_address,"
                + " d.status, d.type, IF(d.user_data_vendor IS NULL OR d.user_data_vendor = '', d.vendor, d.user_data_vendor) as vendor,"
                + " d.mac_address, l.name as location, d.docker_name, d.latitude, d.longitude, d.warranty, b.name as building, f.name as floor,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model, d.serial_number, d.custom_fields, d.virtual_device_type,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ('all' IN ?4 or d.docker_name IN ?4) AND ('all' IN ?6 or (IF ('other' IN ?6, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?6, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?6, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL))) AND ('all' IN ?5 or d.type IN ?5)"
                + " AND (?1 = 'null' or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude,d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?1,'%'))"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "getfilterdevicesmapping"
)

@SqlResultSetMapping(
        name = "devicedetailsfortopologymapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "asset_group", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceDetailsByDeviceIdList",
        query = "SELECT  d.id, d.status, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.mac_address, d.vendor, d.model, d.virtual_device_type,"
                + " d.type , d.ip_address,l.name as location,"
                + " d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " b.name as building, f.name as floor,d.asset_group"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.id IN ?1",
        resultSetMapping = "devicedetailsfortopologymapping"
)

@NamedNativeQuery(
        name = "Device.getDevicesByFilter",
        query = "SELECT  d.id, d.status, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.mac_address, d.vendor, d.model, d.virtual_device_type,"
                + " d.type , d.ip_address,l.name as location,"
                + " d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " b.name as building, f.name as floor,d.asset_group"
                + " FROM device d LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.asset_match_status != 3 AND ('all' IN ?1 or d.docker_name IN ?1) "
                + " AND ('all' IN ?2 or d.type IN ?2)"
                + " AND ('all' IN ?4 or (IF ('other' IN ?4, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?4, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?4, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))"
                + " AND (?3 IS NULL or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?3,'%')) "
                + " AND (?5 IS NULL OR IF(?5, d.id IN ?6 , d.id NOT IN ?6))"
                + " AND (?7 IS NULL OR IF(?8, d.id IN ?8 , d.id NOT IN ?8))"
                + " AND ('all' IN ?9 OR l.id IN ?9)"
                + " AND ('all' IN ?10 OR d.id IN ?10)",
        resultSetMapping = "devicedetailsfortopologymapping"
)


@SqlResultSetMapping(
        name = "devicesbylocationid",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "onboard_status", type = Integer.class),
                                @ColumnResult(name = "image_status", type = Integer.class),
                                @ColumnResult(name = "geolocation_status", type = Integer.class),
                                @ColumnResult(name = "tag_status", type = Integer.class),
                                @ColumnResult(name = "field_status", type = Integer.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "Device.getDevicesByLocationId",
        query = "SELECT  d.id, d.onboard_status, dos.image_status, dos.geolocation_status, dos.tag_status, dos.field_status "
                + " FROM device d "
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " WHERE d.location_id = ?1",
        resultSetMapping = "devicesbylocationid"
)

@SqlResultSetMapping(
        name = "deviceonboardmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "monitor", type = Integer.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "network_layer", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "parent", type = String.class),
                                @ColumnResult(name = "snmp_parent", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "system_type", type = String.class),
                                @ColumnResult(name = "remote_access", type = Integer.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "local_vendor_id", type = String.class),
                                @ColumnResult(name = "global_vendor_id", type = String.class),
                                @ColumnResult(name = "other_vendor_1_id", type = String.class),
                                @ColumnResult(name = "other_vendor_2_id", type = String.class),
                                @ColumnResult(name = "other_vendor_3_id", type = String.class),
                                @ColumnResult(name = "product_id", type = String.class),
                                @ColumnResult(name = "alarm", type = String.class),
                                @ColumnResult(name = "virtual_device_type", type = Integer.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "quick_link_name", type = String.class),
                                @ColumnResult(name = "quick_link_url", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "email_alert", type = Integer.class),
                                @ColumnResult(name = "sms_alert", type = Integer.class),
                                @ColumnResult(name = "popup_notification", type = Integer.class),
                                @ColumnResult(name = "snmp_count", type = Integer.class),
                                @ColumnResult(name = "snmp_status", type = String.class),
                                @ColumnResult(name = "interface_count", type = Integer.class),
                                @ColumnResult(name = "notes_count", type = Integer.class),
                                @ColumnResult(name = "ticket_count", type = Integer.class),
                                @ColumnResult(name = "ticket_status", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "bacnet_count", type = Integer.class),
                                @ColumnResult(name = "bacnet_status", type = String.class),
                                @ColumnResult(name = "lorawan_count", type = Integer.class),
                                @ColumnResult(name = "lorawan_status", type = String.class),
                                @ColumnResult(name = "disruptive_count", type = Integer.class),
                                @ColumnResult(name = "disruptive_status", type = String.class),
                                @ColumnResult(name = "my_devices_count", type = Integer.class),
                                @ColumnResult(name = "my_devices_status", type = String.class),
                                @ColumnResult(name = "local_vendor_email_alert", type = Integer.class),
                                @ColumnResult(name = "local_vendor_sms_alert", type = Integer.class),
                                @ColumnResult(name = "monnit_count", type = Integer.class),
                                @ColumnResult(name = "monnit_status", type = String.class),
                                @ColumnResult(name = "pelican_count", type = Integer.class),
                                @ColumnResult(name = "pelican_status", type = String.class),
                                @ColumnResult(name = "knx_count", type = Integer.class),
                                @ColumnResult(name = "knx_status", type = String.class),
                                @ColumnResult(name = "subsystem_parent_id", type = String.class),
                                @ColumnResult(name = "subsystem_count", type = Integer.class),
                                @ColumnResult(name = "custom_fields", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "asset_match_status", type = Integer.class),
                                @ColumnResult(name = "matched_product_ids", type = String.class),
                                @ColumnResult(name = "latitude", type = String.class),
                                @ColumnResult(name = "longitude", type = String.class),
                                @ColumnResult(name = "measuring_instrument_count", type = Integer.class),
                                @ColumnResult(name = "document_count", type = Integer.class),
                                @ColumnResult(name = "media_count", type = Integer.class),
                                @ColumnResult(name = "checklist_template_count", type = Integer.class),
                                @ColumnResult(name = "snmp_object_count", type = Integer.class),
                                @ColumnResult(name = "snmp_object_status", type = String.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "measuring_instrument_status", type = String.class),
                                @ColumnResult(name = "record_checklist_count", type = Integer.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "daintree_count", type = Integer.class),
                                @ColumnResult(name = "daintree_status", type = String.class),
                                @ColumnResult(name = "asset_image_url", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "ecobee_count", type = Integer.class),
                                @ColumnResult(name = "ecobee_status", type = String.class),
                                @ColumnResult(name = "modbus_count", type = Integer.class),
                                @ColumnResult(name = "modbus_status", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "created_email", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "updated_email", type = String.class),
                                @ColumnResult(name = "updated_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "onboard_status", type = Integer.class),
                                @ColumnResult(name = "assignee_email", type = String.class),
                                @ColumnResult(name = "image_status", type = Integer.class),
                                @ColumnResult(name = "geolocation_status", type = Integer.class),
                                @ColumnResult(name = "tag_status", type = Integer.class),
                                @ColumnResult(name = "field_status", type = Integer.class),
                                @ColumnResult(name = "asset_ocr_image_url", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "location_status", type = String.class),
                                @ColumnResult(name = "device_onboard_status_id", type = String.class),
                                @ColumnResult(name = "poly_lens_count", type = Integer.class),
                                @ColumnResult(name = "adc_json", type = String.class),
                                @ColumnResult(name = "system_type_id", type = String.class),
                                @ColumnResult(name = "system_type_name", type = String.class),
                                @ColumnResult(name = "asset_type_id", type = String.class),
                                @ColumnResult(name = "asset_type_name", type = String.class),
                                @ColumnResult(name = "asset_sub_type_id", type = String.class),
                                @ColumnResult(name = "asset_sub_type_name", type = String.class),
                                @ColumnResult(name = "source_type", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceByDeviceIdNew",
        query = "SELECT  d.id, d.status, d.display_name, d.last_seen_on, d.mac_address, d.vendor, d.model,"
                + " d.type , d.ip_address, d.monitor, l.name as location, d.network_layer, d.user_data_model ,"
                + " d.user_data_vendor, d.user_data_name, d.parent, d.snmp_parent,  d.docker_vdms_id AS vdms_id, d.docker_name,"
                + " d.type AS system_type, d.remote_access, b.name as building, f.name as floor, d.local_vendor_id, "
                + " d.global_vendor_id, d.other_vendor_1_id, d.other_vendor_2_id, d.other_vendor_3_id,"
                + " d.product_id, d.alarm, d.virtual_device_type, d.warranty, d.quick_link_name, d.quick_link_url,  l.id AS location_id, d.email_alert, d.sms_alert,"
                + " d.popup_notification, d.snmp_count, d.snmp_status, d.interface_count, d.notes_count, d.ticket_count, d.ticket_status, d.serial_number,"
                + " d.bacnet_count, d.bacnet_status, d.lorawan_count, d.lorawan_status, d.disruptive_count, d.disruptive_status, d.my_devices_count,"
                + " d.my_devices_status, d.local_vendor_email_alert, d.local_vendor_sms_alert, d.monnit_count, d.monnit_status,"
                + " d.pelican_count, d.pelican_status,d.knx_count,d.knx_status, d.subsystem_parent_id, d.subsystem_count, d.custom_fields, d.description, "
                + " d.asset_match_status, d.matched_product_ids, d.latitude, d.longitude,d.measuring_instrument_count,d.document_count,d.media_count,"
                + " d.checklist_template_count, d.snmp_object_count,d.snmp_object_status, d.position, d.measuring_instrument_status,"
                + " d.record_checklist_count, d.record_checklist_status, d.daintree_count, d.daintree_status,  d.asset_image_url, d.created_timestamp,"
                + " d.ecobee_count, d.ecobee_status, d.modbus_count, d.modbus_status, f.id as floor_id, d.created_email,d.asset_group, b.id as building_id, d.updated_email,"
                + " d.updated_timestamp, d.onboard_status  , dos.assignee_email, dos.image_status, dos.geolocation_status, dos.tag_status, dos.field_status, d.asset_ocr_image_url, d.category, d.sub_category, d.location_status, dos.id AS device_onboard_status_id, d.poly_lens_count,  "
                + " d.adc_json, d.system_type_id, d.system_type_name, d.asset_type_id, d.asset_type_name, d.asset_sub_type_id, d.asset_sub_type_name, d.source_type "
                + " FROM device d"
                + " LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE d.id = ?1",
        resultSetMapping = "deviceonboardmapping"
)

@SqlResultSetMapping(
        name = "deviceDetailsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "model", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceDetailsByIdList",
        query = "SELECT d.id, d.status, "
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model,"
                + " IF(d.user_data_vendor  IS NULL OR d.user_data_vendor  = '', d.vendor, d.user_data_vendor ) as vendor "
                + " FROM device d"
                + " WHERE d.id IN (?1)",
        resultSetMapping = "deviceDetailsMapping"
)

@SqlResultSetMapping(
        name = "devicedetailsfornativeticketmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDeviceById",
        query = "SELECT d.id, d.status, "
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name,"
                + " IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model,"
                + " IF(d.user_data_vendor  IS NULL OR d.user_data_vendor  = '', d.vendor, d.user_data_vendor ) as vendor" +
                ", d.type, d.ip_address, d.mac_address, d.serial_number, b.name as building, f.name as floor, l.name as location, d.docker_name, d.category"
                + " FROM device d"
                + " LEFT JOIN location l ON d.location_id = l.id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " LEFT JOIN product_details p ON d.product_id = p.product_id"
                + " WHERE d.id = ?1",
        resultSetMapping = "devicedetailsfornativeticketmapping"
)


@SqlResultSetMapping(
        name = "deviceAiCallFlowMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name",type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "ai_call", type = Boolean.class),
                                @ColumnResult(name = "is_added", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.browseAiCallFlowDevicesWithSearch",
        query = "SELECT d.id, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name, d.docker_name, d.ai_call, IF(cfr.device_id = d.id, 1, 0) as is_added " +
                "FROM device d " +
                "LEFT JOIN call_flow_rule cfr ON d.id = cfr.device_id " +
                "LEFT JOIN location l ON l.id = d.location_id " +
                "LEFT JOIN floor f ON l.floor_id = f.id " +
                "LEFT JOIN building b ON f.building_id = b.id " +
                "WHERE (?1 = 'null' OR d.docker_vdms_id = ?1) " +
                "AND (?2 = 'all' OR d.docker_name = ?2) " +
                "AND d.ai_call = true " +
                "AND (?5 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type, d.description), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?5,'%')) " +
                "LIMIT ?4 OFFSET ?3 ",
        resultSetMapping = "deviceAiCallFlowMapping"
)

@SqlResultSetMapping(
        name = "devicedetailesforcallstatusmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "alarm", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "last_seen_on", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "user_data_model", type = String.class),
                                @ColumnResult(name = "user_data_vendor", type = String.class),
                                @ColumnResult(name = "vendor", type = String.class),
                                @ColumnResult(name = "warranty", type = String.class),
                                @ColumnResult(name = "serial_number", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "parent", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "sub_category", type = String.class),
                                @ColumnResult(name = "docker_vdms_id", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "building", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "floor", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "location", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Device.getDeviceInfoFromDb",
        query = "SELECT d.id, d.alarm, d.user_data_name, d.display_name, d.ip_address, d.mac_address, d.last_seen_on, d.model, d.user_data_model, d.user_data_vendor, d.vendor, d.warranty, d.serial_number, d.docker_name, d.parent, d.description, d.category, d.sub_category, d.docker_vdms_id, f.building_id, b.name AS building, l.floor_id, f.name AS floor, d.location_id, l.name AS location " +
                "FROM device d " +
                "LEFT JOIN location l ON d.location_id = l.id " +
                "LEFT JOIN floor f ON l.floor_id = f.id " +
                "LEFT JOIN building b ON f.building_id = b.id " +
                "WHERE d.id = ?1",
        resultSetMapping = "devicedetailesforcallstatusmapping"
)

@SqlResultSetMapping(
        name = "deviceAiCallJobSchedulerMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "ai_call", type = Boolean.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getAiCallAndDeviceStatus",
        query = "SELECT d.status,d.ai_call FROM device d WHERE d.id=?1",
        resultSetMapping = "deviceAiCallJobSchedulerMapping"
)

@SqlResultSetMapping(
        name = "dndDevicesMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "dnd_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "is_dnd_enabled", type = Boolean.class),
                                @ColumnResult(name = "system_dnd_enabled", type = Boolean.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getDndDevices",
        query = "SELECT d.id, d.dnd_timestamp, d.is_dnd_enabled, d.system_dnd_enabled " +
                "FROM device d " +
                "WHERE d.is_dnd_enabled = ?1 " +
                "AND d.dnd_timestamp IS NOT NULL " +
                "AND d.system_dnd_enabled = ?1 " +
                "AND d.dnd_timestamp < ((UNIX_TIMESTAMP() * 1000) - 86400000)",  // 24 hrs = 86400000 ms
        resultSetMapping = "dndDevicesMapping"
)

@NamedNativeQuery(
        name = "Device.getDeviceDndAndSystemDndStatus",
        query = "SELECT d.id, d.dnd_timestamp, d.is_dnd_enabled, d.system_dnd_enabled " +
                "FROM device d " +
                "WHERE d.id = ?1 " +
                "AND d.is_dnd_enabled = ?2 AND d.system_dnd_enabled = ?2 ",
        resultSetMapping = "dndDevicesMapping"
)


@SqlResultSetMapping(
        name = "deviceCustomDetailsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "ip_address", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Device.getAllDeviceCustomDetailsPaginated",
        query = "SELECT d.id, d.ip_address, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.status " +
                "FROM device d " +
                "WHERE ( 'all' IN ?4 OR d.id NOT IN ?4 ) " +
                "AND ( ?5 = 'all' OR d.docker_name = ?5 ) " +
                "AND (?1 = 'null' OR CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%')) " +
                "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
                "LIMIT ?2 OFFSET ?3 ",
        resultSetMapping = "deviceCustomDetailsMapping"
)

@NamedNativeQuery(
        name = "Device.getAllDeviceCustomDetails",
        query = "SELECT d.id, d.ip_address, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.status " +
                "FROM device d " +
                "WHERE ( 'all' IN ?2 OR d.id NOT IN ?2 ) " +
                "AND ( ?3 = 'all' OR d.docker_name = ?3 ) " +
                "AND (?1 = 'null' OR CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%')) " +
                "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) ",
        resultSetMapping = "deviceCustomDetailsMapping"
)

@NamedNativeQuery(
        name = "Device.getDeviceCustomDetailsPaginated",
        query = "SELECT d.id, d.ip_address, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.status " +
                "FROM device d " +
                "WHERE ( d.id IN ?4 ) " +
                "AND (?1 = 'null' OR CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%')) " +
                "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
                "LIMIT ?2 OFFSET ?3 ",
        resultSetMapping = "deviceCustomDetailsMapping"
)

@NamedNativeQuery(
        name = "Device.getDeviceCustomDetails",
        query = "SELECT d.id, d.ip_address, IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as display_name, d.status " +
                "FROM device d " +
                "WHERE ( d.id IN ?2 ) " +
                "AND (?1 = 'null' OR CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%')) " +
                "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) ",
        resultSetMapping = "deviceCustomDetailsMapping"
)

@NamedNativeQuery(
        name = "Device.getDeviceCustomDetailsByIds",
        query =
                "SELECT d.id, d.ip_address, " +
                        "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS display_name, " +
                        "d.status " +
                        "FROM device d " +
                        "WHERE ( 'null' in ?2 OR d.id IN ?2 ) " +
                        "AND ( 'null' in ?3  OR d.model IN ?3 ) " +
                        "AND ( 'null' in ?4 OR d.category IN ?4 ) " +
                        "AND ( ?1 = 'null' OR CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%') ) " +
                        "AND ( d.virtual_device_type IS NULL OR d.virtual_device_type = 0 )",
        resultSetMapping = "deviceCustomDetailsMapping"
)


@NamedNativeQuery(
        name = "Device.getDeviceCustomDetailsByIdsPaginated",
        query =
                "SELECT d.id, d.ip_address, " +
                        "IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) AS display_name, " +
                        "d.status " +
                        "FROM device d " +
                        "WHERE ( 'null' in ?4 OR d.id IN ?4 ) " +
                        "AND ( 'null' in ?5 OR d.model IN ?5 ) " +
                        "AND ( 'null' in ?6 OR d.category IN ?6 ) " +
                        "AND ( ?1 = 'null' OR CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%') ) " +
                        "AND ( d.virtual_device_type IS NULL OR d.virtual_device_type = 0 ) " +
                        "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "deviceCustomDetailsMapping"
)

@SqlResultSetMapping(
        name = "deviceforiaqMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "asset_group", type = String.class),
                                @ColumnResult(name = "docker_name", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "user_data_name", type = String.class),
                                @ColumnResult(name = "display_name", type = String.class),
                                @ColumnResult(name = "onboard_status", type = Integer.class)
                        }
                )
        }
)

//for IAQ
@NamedNativeQuery(
        name = "Device.getDeviceByMeasuringInstrumentId",
        query = "SELECT  d.id, d.asset_group, d.docker_name, d.location_id , d.user_data_name , d.display_name , d.onboard_status "
                + " FROM device d"
                + " LEFT JOIN measuring_instrument mi ON d.id = mi.device_id"
                + " WHERE mi.id = ?1",
        resultSetMapping = "deviceforiaqMapping"
)


@SqlResultSetMapping(
        name = "deviceImageMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDTO.class,
                        columns = {

                                @ColumnResult(name = "ai_call", type = Boolean.class),
                                @ColumnResult(name = "asset_image_url", type = String.class),
                                @ColumnResult(name = "asset_ocr_image_url", type = String.class),
                                @ColumnResult(name = "asset_tag_images_url", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Device.getAllDeviceImages",
        query = "SELECT  ai_call, asset_image_url, asset_ocr_image_url, asset_tag_images_url FROM device WHERE id = ?1 ",
        resultSetMapping = "deviceImageMapping"
)




public class Device {

    @Id
    private String id;

    @Column(length = 8)
    private Integer alarm;

    @Column(length = 128, columnDefinition = "varchar(128) default 'Generic'")
    private String display_name;

    @Column(length = 64)
    private String ip_address;

    @Column(length = 32)
    private String mac_address;

//	@Column(length = 8)
//	private Integer important;

    private BigInteger last_seen_on;

    private String model;

    @Column(length = 8)
    private Integer monitor;

    @Column(length = 64)
    private String network_layer;


    @Column(columnDefinition = "integer default 1", length = 1)
    private Integer remote_access;

    @Column(length = 8)
    private Integer status;

    @Column(length = 128, columnDefinition = "varchar(128) default 'generic'")
    private String type;

    @Column(length = 128)
    private String user_data_type;

    @Column
    private String user_data_model;

    @Column
    private String user_data_name;

    @Column
    private String user_data_vendor;

    @Column
    private String vendor;

//	@Column(length = 1)
//	private Boolean is_virtual;

    @Column(length = 32)
    private String warranty;

    @Column(length = 128)
    private String quick_link_name;

    private String quick_link_url;

//	@Column(length = 20)
//	private String dashboard_status;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer email_alert;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer sms_alert;

    @Column(length = 8, columnDefinition = "integer default 1")
    private Integer popup_notification;

    @Column(length = 32)
    private Integer snmp_count;

    @Column(length = 128)
    private String snmp_status;

    @Column(length = 32)
    private Integer interface_count;

    @Column(length = 32)
    private Integer notes_count;

    @Column(length = 64)
    private Integer ticket_count;

    @Column(length = 128)
    private String ticket_status;

    @Column(length = 8)
    private Integer virtual_device_type;

    private String serial_number;

    @Column(length = 32)
    private Integer bacnet_count;

    @Column(length = 128)
    private String bacnet_status;

    @Column(length = 32)
    private Integer lorawan_count;

    @Column(length = 128)
    private String lorawan_status;

    @Column(length = 32)
    private Integer disruptive_count;

    @Column(length = 128)
    private String disruptive_status;

    @Column(length = 20)
    private String snmp_parent_index;

    @Column(length = 32)
    private Integer my_devices_count;

    @Column(length = 128)
    private String my_devices_status;

    @Column(length = 32)
    private Integer monnit_count;

    @Column(length = 128)
    private String monnit_status;

    @Column(length = 32)
    private Integer pelican_count;

    @Column(length = 128)
    private String pelican_status;

    @Column(length = 32)
    private Integer knx_count;

    @Column(length = 128)
    private String knx_status;

    @Column(length = 32)
    private Integer snmp_object_count;

    @Column(length = 128)
    private String snmp_object_status;

    private String position;

    private String latitude;

    private String longitude;

    @Column(length = 32)
    private Integer measuring_instrument_count;

    @Column(length = 32)
    private String measuring_instrument_status;

    @Column(length = 32)
    private Integer document_count;

    @Column(length = 32)
    private Integer media_count;

    @Column(length = 32)
    private Integer checklist_template_count;

    // removed: relation to Docker (edge-only)
    @javax.persistence.Transient
    // (Docker relation skipped — Bucket-D)

    //	@OneToOne(cascade = CascadeType.ALL)
    private String parent;

    //	@OneToOne(cascade = CascadeType.ALL)
    private String snmp_parent;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer local_vendor_email_alert;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer local_vendor_sms_alert;

    private String subsystem_parent_id;

    @Column(columnDefinition = "integer default 0")
    private Integer subsystem_count;

    @Column(columnDefinition = "LONGTEXT")
    public String custom_fields;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 8, columnDefinition = "integer default 0")
    private Integer asset_match_status;

    @Column(columnDefinition = "TEXT")
    private String matched_product_ids;

    @Column(length = 64, columnDefinition = "varchar(64) default 'wired'")
    private String connection_type;

    @Column(length = 64)
    private String user_connection_type;


    @Column(length = 32)
    private String record_checklist_status;

    @Column(length = 32)
    private Integer record_checklist_count;


    @Column(length = 32)
    private Integer daintree_count;

    @Column(length = 128)
    private String daintree_status;

    @Column
    private Integer qrcode_count;

    @ManyToOne
    private Phonebook global_vendor;

    @Column(columnDefinition = "TEXT")
    private String asset_image_url;

    private BigInteger created_timestamp;

    @Column(length = 32)
    private Integer ecobee_count;

    @Column(length = 128)
    private String ecobee_status;

    @Column
    private String reboot_status;

    @Column(length = 10, columnDefinition = "integer default 0")
    private Integer onboard_status;

    @Column(columnDefinition = "TEXT")
    private String asset_ocr_image_url;

    @ManyToOne
    private Phonebook local_vendor;

    @Column(length = 32)
    private Integer modbus_count;

    @Column(length = 128)
    private String modbus_status;

    private String created_email;

    @Column(length = 128, columnDefinition = "varchar(128) default 'generic'")
    private String asset_group;

    private String updated_email;

    private BigInteger updated_timestamp;


    @Column(length = 128)
    private String location_status;

    @Column(columnDefinition = "varchar(255) default 'generic'")
    private String category;

    @Column(columnDefinition = "varchar(255) default 'generic'")
    private String sub_category;

    private String digital_twin_image_url;

    @ManyToOne
    private Phonebook other_vendor_1;


    @ManyToOne
    private Phonebook other_vendor_2;


    @ManyToOne
    private Phonebook other_vendor_3;

    // removed: relation to Snmp_Configuration (AP-C2)
    @javax.persistence.Transient
    private Snmp_Configuration snmp_configuration;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<Interface> interfaces;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<Service> service;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<Notes> notes;

    // removed: relation to Product_Details (AP-C8)
    @javax.persistence.Transient
    private Product_Details product_details;

    // removed: relation to RemoteAccessSession (edge-only)
    @javax.persistence.Transient
    private RemoteAccessSession remote_access_session;

    @ManyToOne
    private Location location;

    // removed: relation to History (AP-C6)
    @javax.persistence.Transient
    private java.util.Set<History> history;

    // removed: relation to Ticket (AP-C3)
    @javax.persistence.Transient
    private java.util.Set<Ticket> ticket;

    // removed: relation to Lorawan_Sensor (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<Lorawan_Sensor> lorawan_sensor;

    // removed: relation to Bacnet_Object (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<Bacnet_Object> bacnet_object;

    // removed: relation to DisruptiveSensor (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<DisruptiveSensor> disruptive_sensor;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private Set<Device_IP_Address> device_ip_address;

    // removed: relation to Datahoist (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<Datahoist> datahoist;

    // removed: relation to MyDevicesSensor (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<MyDevicesSensor> my_devices_sensor;

    // removed: relation to Monnit_Sensor (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<Monnit_Sensor> monnit_sensor;

    // removed: relation to PelicanSensor (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<PelicanSensor> pelican_sensor;

    // removed: relation to Snmp_Dump (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<Snmp_Dump> snmp_dump;

    // removed: relation to KNXGroup (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<KNXGroup> knx_group;

    // removed: relation to SnmpObject (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<SnmpObject> snmp_object;

    @ManyToMany()
    @JoinTable(name = "device_document", joinColumns = @JoinColumn(name = "device_id"), inverseJoinColumns = @JoinColumn(name = "document_id"))
    private Set<Document> document;


    @ManyToMany()
    @JoinTable(name = "device_media", joinColumns = @JoinColumn(name = "device_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    private Set<Media> media;

    // removed: relation to CheckListTemplate (AP-C4)
    @javax.persistence.Transient
    private java.util.Set<CheckListTemplate> check_list_template;

    // removed: relation to CheckListRecord (AP-C4)
    @javax.persistence.Transient
    private java.util.Set<CheckListRecord> check_list_record;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private Set<AssetDeviceMapping> asset_device_mapping;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MeasuringInstrument> measuring_instrument;

    // removed: relation to Inventory (AP-C8)
    @javax.persistence.Transient
    private Inventory inventory;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL)
    private GlobalQrcode global_qrcode;

    // removed: relation to RecordChecklist (AP-C4)
    @javax.persistence.Transient
    private java.util.Set<RecordChecklist> record_checklist;

    // removed: relation to GlobalChecklist (AP-C4)
    @javax.persistence.Transient
    private java.util.Set<GlobalChecklist> global_checklist;

    // removed: relation to GlobalInspectionRelation (AP-C4)
    @javax.persistence.Transient
    private java.util.Set<GlobalInspectionRelation> global_inspection_relation;

    // removed: relation to GlobalChecklistConditions (AP-C4)
    @javax.persistence.Transient
    private java.util.Set<GlobalChecklistConditions> global_checklist_conditions;

    // removed: relation to DaintreeDevice (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<DaintreeDevice> daintree_device;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<DeviceConditions> device_conditions;

    // removed: relation to EcobeeSensor (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<EcobeeSensor> ecobee_sensor;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<Specifications> specifications;

    // removed: relation to ModbusRegister (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<ModbusRegister> modbus_register;

    // removed: relation to SiemensAsset (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<SiemensAsset> siemens_asset;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "device")
    private DeviceOnboardStatus device_onboard_status;

    @Column(length = 32)
    private Integer poly_lens_count;


    // removed: relation to Bucket-C entity PolyLensDevice (AP-C2)

    // removed: relation to Bucket-C entity MqttDevice (AP-C2)

    @Column(length = 32)
    private Integer mqtt_count;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<Nfc> nfc;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<QrCode> qrcode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<ClientNfc> client_nfc;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<ClientQrCode> client_qrcode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device")
    private Set<ClientBarCode> client_barcode;

    // removed: relation to Bucket-C entity GlobalChecklistConditions (AP-C4)

    @Column(name = "cost_value", precision = 16, scale = 2)
    private BigDecimal cost_value;
    @Column(name = "ai_call", columnDefinition = "boolean default false", length = 8)
    private Boolean ai_call;

    @Column(name = "cost_unit", length = 25)
    private String cost_unit;

    @Column(name = "is_dnd_enabled", columnDefinition = "boolean default false", length = 8)
    private Boolean is_dnd_enabled;

    @Column(length = 32)
    private String operational_status;

    @Column(name = "dnd_timestamp")
    private BigInteger dnd_timestamp;

    @Column(name = "system_dnd_enabled",columnDefinition = "boolean default false", length = 8)
    private Boolean system_dnd_enabled;

    @Column(name = "adc_json", columnDefinition = "JSON")
    private String adc_json;

    @Column
    private String system_type_id;

    @Column
    private String system_type_name;

    @Column
    private String asset_type_id;

    @Column
    private String asset_type_name;

    @Column
    private String asset_sub_type_id;

    @Column
    private String asset_sub_type_name;

    @Column
    @ColumnDefault("'vdms'")
    private String source_type;

    @Column(columnDefinition = "TEXT")
    private String asset_tag_images_url;

    @ManyToOne
    @JoinColumn(name = "assigned_user_email", referencedColumnName = "email")
    User user;

    // removed: relation to Bucket-C entity Technician (AP-C3)

    // removed: relation to Bucket-C entity InventoryDevice (AP-C8)

    public String getOperational_status() {
        return operational_status;
    }

    public void setOperational_status(String operational_status) {
        this.operational_status = operational_status;
    }

    public String getCost_unit() {
        return cost_unit;
    }

    public void setCost_unit(String cost_unit) {
        this.cost_unit = cost_unit;
    }

    public Boolean getIs_dnd_enabled() {
        return is_dnd_enabled;
    }

    public void setIs_dnd_enabled(Boolean is_dnd_enabled) {
        this.is_dnd_enabled = is_dnd_enabled;
    }

    public BigDecimal getCost_value() {
        return cost_value;
    }

    public void setCost_value(BigDecimal cost_value) {
        this.cost_value = cost_value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getAi_call() {
        return ai_call;
    }

    public void setAi_call(Boolean ai_call) {
        this.ai_call = ai_call;
    }

    // removed: relation to GaiameshController (AP-C2)
    @javax.persistence.Transient
    private java.util.Set<GaiameshController> gaiamesh_controller;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // removed: getter/setter for Bucket-D Docker (edge-only)
    // stub: added back as @Transient for compile compatibility
    @javax.persistence.Transient
    private Docker docker;

    public Docker getDocker() { return docker; }
    public void setDocker(Docker docker) { this.docker = docker; }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getSnmp_parent() {
        return snmp_parent;
    }

    public void setSnmp_parent(String snmp_parent) {
        this.snmp_parent = snmp_parent;
    }

    public Phonebook getGlobal_vendor() {
        return global_vendor;
    }

    public void setGlobal_vendor(Phonebook global_vendor) {
        this.global_vendor = global_vendor;

    }

    public Phonebook getLocal_vendor() {
        return local_vendor;
    }

    public void setLocal_vendor(Phonebook local_vendor) {
        this.local_vendor = local_vendor;
    }

    public Phonebook getOther_vendor_1() {
        return other_vendor_1;
    }

    public void setOther_dealer_1(Phonebook other_vendor_1) {
        this.other_vendor_1 = other_vendor_1;
    }

    public Phonebook getOther_vendor_2() {
        return other_vendor_2;
    }

    public void setOther_dealer_2(Phonebook other_vendor_2) {
        this.other_vendor_2 = other_vendor_2;
    }

    public Phonebook getOther_vendor_3() {
        return other_vendor_3;
    }

    public void setOther_dealer_3(Phonebook other_vendor_3) {
        this.other_vendor_3 = other_vendor_3;
    }

    // removed: getter/setter for Bucket-C Snmp_Configuration (AP-C2)

    public Set<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Set<Interface> interfaces) {
        this.interfaces = interfaces;
        interfaces.forEach((temp) -> {
            temp.setDevice(this);
        });
    }

    public Set<Service> getService() {
        return service;
    }

    public void setService(Set<Service> service) {
        this.service = service;
        service.forEach((temp) -> {
            temp.setDevice(this);
        });
    }

    public Set<Notes> getNotes() {
        return notes;
    }

    public void setNotes(Set<Notes> notes) {
        this.notes = notes;
        notes.forEach((temp) -> {
            temp.setDevice(this);
        });
    }

    @ManyToOne
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getAlarm() {
        return alarm;
    }

    public void setAlarm(Integer alarm) {
        this.alarm = alarm;
    }

    public String getDisplay_nameproduct_details() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

//	public Integer getImportant() {
//		return important;
//	}
//
//	public void setImportant(Integer important) {
//		this.important = important;
//	}

    public BigInteger getLast_seen_on() {
        return last_seen_on;
    }

    public void setLast_seen_on(BigInteger last_seen_on) {
        this.last_seen_on = last_seen_on;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getMonitor() {
        return monitor;
    }

    public void setMonitor(Integer monitor) {
        this.monitor = monitor;
    }

    public String getNetwork_layer() {
        return network_layer;
    }

    public void setNetwork_layer(String network_layer) {
        this.network_layer = network_layer;
    }


    public Integer getRemote_access() {
        return remote_access;
    }

    public void setRemote_access(Integer remote_access) {
        this.remote_access = remote_access;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_data_type() {
        return user_data_type;
    }

    public void setUser_data_type(String user_data_type) {
        this.user_data_type = user_data_type;
    }

    public String getUser_data_model() {
        return user_data_model;
    }

    public void setUser_data_model(String user_data_model) {
        this.user_data_model = user_data_model;
    }

    public String getUser_data_name() {
        return user_data_name;
    }

    public void setUser_data_name(String user_data_name) {
        this.user_data_name = user_data_name;
    }

    public String getUser_data_vendor() {
        return user_data_vendor;
    }

    public void setUser_data_vendor(String user_data_vendor) {
        this.user_data_vendor = user_data_vendor;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getQuick_link_name() {
        return quick_link_name;
    }

    public void setQuick_link_name(String quick_link_name) {
        this.quick_link_name = quick_link_name;
    }

    public String getQuick_link_url() {
        return quick_link_url;
    }

    public void setQuick_link_url(String quick_link_url) {
        this.quick_link_url = quick_link_url;
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

    public Integer getPopup_notification() {
        return popup_notification;
    }

    public void setPopup_notification(Integer popup_notification) {
        this.popup_notification = popup_notification;
    }

    public Integer getSnmp_count() {
        return snmp_count;
    }

    public void setSnmp_count(Integer snmp_count) {
        this.snmp_count = snmp_count;
    }

    public String getSnmp_status() {
        return snmp_status;
    }

    public void setSnmp_status(String snmp_status) {
        this.snmp_status = snmp_status;
    }

    public Integer getInterface_count() {
        return interface_count;
    }

    public void setInterface_count(Integer interface_count) {
        this.interface_count = interface_count;
    }

    public Integer getNotes_count() {
        return notes_count;
    }

    public void setNotes_count(Integer notes_count) {
        this.notes_count = notes_count;
    }

    public Integer getTicket_count() {
        return ticket_count;
    }

    public void setTicket_count(Integer ticket_count) {
        this.ticket_count = ticket_count;
    }

    public String getTicket_status() {
        return ticket_status;
    }

    public void setTicket_status(String ticket_status) {
        this.ticket_status = ticket_status;
    }

    public Integer getVirtual_device_type() {
        return virtual_device_type;
    }

    public void setVirtual_device_type(Integer virtual_device_type) {
        this.virtual_device_type = virtual_device_type;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public Integer getBacnet_count() {
        return bacnet_count;
    }

    public void setBacnet_count(Integer bacnet_count) {
        this.bacnet_count = bacnet_count;
    }

    public String getBacnet_status() {
        return bacnet_status;
    }

    public void setBacnet_status(String bacnet_status) {
        this.bacnet_status = bacnet_status;
    }

    public Integer getLorawan_count() {
        return lorawan_count;
    }

    public void setLorawan_count(Integer lorawan_count) {
        this.lorawan_count = lorawan_count;
    }

    public String getLorawan_status() {
        return lorawan_status;
    }

    public void setLorawan_status(String lorawan_status) {
        this.lorawan_status = lorawan_status;
    }

    public Integer getDisruptive_count() {
        return disruptive_count;
    }

    public void setDisruptive_count(Integer disruptive_count) {
        this.disruptive_count = disruptive_count;
    }

    public String getDisruptive_status() {
        return disruptive_status;
    }

    public void setDisruptive_status(String disruptive_status) {
        this.disruptive_status = disruptive_status;
    }

    // removed: getter/setter for Bucket-C Product_Details (AP-C8)

    public String getDisplay_name() {
        return display_name;
    }

    public void setOther_vendor_1(Phonebook other_vendor_1) {
        this.other_vendor_1 = other_vendor_1;
    }

    public void setOther_vendor_2(Phonebook other_vendor_2) {
        this.other_vendor_2 = other_vendor_2;
    }

    public void setOther_vendor_3(Phonebook other_vendor_3) {
        this.other_vendor_3 = other_vendor_3;
    }

    public String getSnmp_parent_index() {
        return snmp_parent_index;
    }

    public void setSnmp_parent_index(String snmp_parent_index) {
        this.snmp_parent_index = snmp_parent_index;
    }

    // removed: getter/setter for Bucket-D RemoteAccessSession (edge-only)

    // removed: getter/setter for Bucket-C History (AP-C6)

    // removed: getter/setter for Bucket-C Ticket (AP-C3)

    // removed: getter/setter for Bucket-C Lorawan_Sensor (AP-C2)

    // removed: getter/setter for Bucket-C Bacnet_Object (AP-C2)

    // removed: getter/setter for Bucket-C DisruptiveSensor (AP-C2)

    public Set<Device_IP_Address> getDevice_ip_address() {
        return device_ip_address;
    }

    public void setDevice_ip_address(Set<Device_IP_Address> device_ip_address) {
        this.device_ip_address = device_ip_address;
    }

    public Device() {
        super();
    }

    public Device(String id) {
        super();
        this.id = id;
    }

    // removed: getter/setter for Bucket-C Datahoist (AP-C2)

    // removed: getter/setter for Bucket-C MyDevicesSensor (AP-C2)

    public Integer getMy_devices_count() {
        return my_devices_count;
    }

    public void setMy_devices_count(Integer my_devices_count) {
        this.my_devices_count = my_devices_count;
    }

    public String getMy_devices_status() {
        return my_devices_status;
    }

    public void setMy_devices_status(String my_devices_status) {
        this.my_devices_status = my_devices_status;
    }

    public Integer getMonnit_count() {
        return monnit_count;
    }

    public void setMonnit_count(Integer monnit_count) {
        this.monnit_count = monnit_count;
    }

    public String getMonnit_status() {
        return monnit_status;
    }

    public void setMonnit_status(String monnit_status) {
        this.monnit_status = monnit_status;
    }

    public Integer getPelican_count() {
        return pelican_count;
    }

    public void setPelican_count(Integer pelican_count) {
        this.pelican_count = pelican_count;
    }

    public String getPelican_status() {
        return pelican_status;
    }

    public void setPelican_status(String pelican_status) {
        this.pelican_status = pelican_status;
    }

    public Integer getKnx_count() {
        return knx_count;
    }

    public void setKnx_count(Integer knx_count) {
        this.knx_count = knx_count;
    }

    public String getKnx_status() {
        return knx_status;
    }

    public void setKnx_status(String knx_status) {
        this.knx_status = knx_status;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getMeasuring_instrument_count() {
        return measuring_instrument_count;
    }

    public void setMeasuring_instrument_count(Integer measuring_instrument_count) {
        this.measuring_instrument_count = measuring_instrument_count;
    }

    public String getMeasuring_instrument_status() {
        return measuring_instrument_status;
    }

    public void setMeasuring_instrument_status(String measuring_instrument_status) {
        this.measuring_instrument_status = measuring_instrument_status;
    }

    public Integer getDocument_count() {
        return document_count;
    }

    public void setDocument_count(Integer document_count) {
        this.document_count = document_count;
    }

    public Integer getMedia_count() {
        return media_count;
    }

    public void setMedia_count(Integer media_count) {
        this.media_count = media_count;
    }

    public Integer getChecklist_template_count() {
        return checklist_template_count;
    }

    public void setChecklist_template_count(Integer checklist_template_count) {
        this.checklist_template_count = checklist_template_count;
    }


    public Set<Snmp_Dump> getSnmp_dump() {
        return snmp_dump;
    }

    public void setSnmp_dump(Set<Snmp_Dump> snmp_dump) {
        this.snmp_dump = snmp_dump;
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

    // removed: getter/setter for Bucket-C Monnit_Sensor (AP-C2)

    // removed: getter/setter for Bucket-C PelicanSensor (AP-C2)

    // removed: getter/setter for Bucket-C KNXGroup (AP-C2)

    public Set<Document> getDocument() {
        return document;
    }

    public void setDocument(Set<Document> document) {
        this.document = document;
    }

    public Set<Media> getMedia() {
        return media;
    }

    public void setMedia(Set<Media> media) {
        this.media = media;
    }

    // removed: getter/setter for Bucket-C CheckListTemplate (AP-C4)

    public String getSubsystem_parent_id() {
        return subsystem_parent_id;
    }

    public void setSubsystem_parent_id(String subsystem_parent_id) {
        this.subsystem_parent_id = subsystem_parent_id;
    }

    public Integer getSubsystem_count() {
        return subsystem_count;
    }

    public void setSubsystem_count(Integer subsystem_count) {
        this.subsystem_count = subsystem_count;
    }

    // removed: getter/setter for Bucket-C CheckListRecord (AP-C4)

    public String getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(String custom_fields) {
        this.custom_fields = custom_fields;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAsset_match_status() {
        return asset_match_status;
    }

    public void setAsset_match_status(Integer asset_match_status) {
        this.asset_match_status = asset_match_status;
    }

    public String getMatched_product_ids() {
        return matched_product_ids;
    }

    public void setMatched_product_ids(String matched_product_ids) {
        this.matched_product_ids = matched_product_ids;
    }

    public Set<AssetDeviceMapping> getAsset_device_mapping() {
        return asset_device_mapping;
    }

    public void setAsset_device_mapping(Set<AssetDeviceMapping> asset_device_mapping) {
        this.asset_device_mapping = asset_device_mapping;
    }

    public Set<MeasuringInstrument> getMeasuring_instrument() {
        return measuring_instrument;
    }

    public void setMeasuring_instrument(Set<MeasuringInstrument> measuring_instrument) {
        this.measuring_instrument = measuring_instrument;
    }

    public String getConnection_type() {
        return connection_type;
    }

    public void setConnection_type(String connection_type) {
        this.connection_type = connection_type;
    }

    public String getUser_connection_type() {
        return user_connection_type;
    }

    public void setUser_connection_type(String user_connection_type) {
        this.user_connection_type = user_connection_type;
    }

    public Integer getSnmp_object_count() {
        return snmp_object_count;
    }

    public void setSnmp_object_count(Integer snmp_object_count) {
        this.snmp_object_count = snmp_object_count;
    }

    public String getSnmp_object_status() {
        return snmp_object_status;
    }

    public void setSnmp_object_status(String snmp_object_status) {
        this.snmp_object_status = snmp_object_status;
    }

    // removed: getter/setter for Bucket-C SnmpObject (AP-C2)

    // removed: getter/setter for Bucket-C Inventory (AP-C8)

    public GlobalQrcode getGlobal_qrcode() {
        return global_qrcode;
    }

    public void setGlobal_qrcode(GlobalQrcode global_qrcode) {
        this.global_qrcode = global_qrcode;
    }

    // removed: getter/setter for Bucket-C RecordChecklist (AP-C4)

    public Set<GlobalChecklist> getGlobal_checklist() {
        return global_checklist;
    }

    public void setGlobal_checklist(Set<GlobalChecklist> global_checklist) {
        this.global_checklist = global_checklist;
    }

    public String getRecord_checklist_status() {
        return record_checklist_status;
    }

    public void setRecord_checklist_status(String record_checklist_status) {
        this.record_checklist_status = record_checklist_status;
    }

    public Integer getRecord_checklist_count() {
        return record_checklist_count;
    }

    public void setRecord_checklist_count(Integer record_checklist_count) {
        this.record_checklist_count = record_checklist_count;
    }

    // removed: getter/setter for Bucket-C GlobalInspectionRelation (AP-C4)

    public Integer getDaintree_count() {
        return daintree_count;
    }

    public void setDaintree_count(Integer daintree_count) {
        this.daintree_count = daintree_count;
    }

    public String getDaintree_status() {
        return daintree_status;
    }

    public void setDaintree_status(String daintree_status) {
        this.daintree_status = daintree_status;
    }

    public Integer getQrcode_count() {
        return qrcode_count;
    }

    public void setQrcode_count(Integer qrcode_count) {
        this.qrcode_count = qrcode_count;
    }

    // removed: getter/setter for Bucket-C DaintreeDevice (AP-C2)

    public String getAsset_image_url() {
        return asset_image_url;
    }

    public void setAsset_image_url(String asset_image_url) {
        this.asset_image_url = asset_image_url;
    }

    public BigInteger getCreated_timestamp() {
        return created_timestamp;
    }

    public void setCreated_timestamp(BigInteger created_timestamp) {
        this.created_timestamp = created_timestamp;
    }

    public Set<DeviceConditions> getDevice_conditions() {
        return device_conditions;
    }

    public void setDevice_conditions(Set<DeviceConditions> device_conditions) {
        this.device_conditions = device_conditions;
    }

    public Integer getEcobee_count() {
        return ecobee_count;
    }

    public void setEcobee_count(Integer ecobee_count) {
        this.ecobee_count = ecobee_count;
    }

    public String getEcobee_status() {
        return ecobee_status;
    }

    public void setEcobee_status(String ecobee_status) {
        this.ecobee_status = ecobee_status;
    }

    // removed: getter/setter for Bucket-C EcobeeSensor (AP-C2)

    public Set<Specifications> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Set<Specifications> specifications) {
        this.specifications = specifications;
    }

    public Integer getModbus_count() {
        return modbus_count;
    }

    public void setModbus_count(Integer modbus_count) {
        this.modbus_count = modbus_count;
    }

    public String getModbus_status() {
        return modbus_status;
    }

    public void setModbus_status(String modbus_status) {
        this.modbus_status = modbus_status;
    }

    // removed: getter/setter for Bucket-C ModbusRegister (AP-C2)

    // removed: getter/setter for Bucket-C SiemensAsset (AP-C2)

    public String getCreated_email() {
        return created_email;
    }

    public void setCreated_email(String created_email) {
        this.created_email = created_email;
    }

    public String getAsset_group() {
        return asset_group;
    }

    public void setAsset_group(String asset_group) {
        this.asset_group = asset_group;
    }

    public String getReboot_status() {
        return reboot_status;
    }

    public void setReboot_status(String reboot_status) {
        this.reboot_status = reboot_status;
    }


    public String getUpdated_email() {
        return updated_email;
    }

    public void setUpdated_email(String updated_email) {
        this.updated_email = updated_email;
    }

    public BigInteger getUpdated_timestamp() {
        return updated_timestamp;
    }

    public void setUpdated_timestamp(BigInteger updated_timestamp) {
        this.updated_timestamp = updated_timestamp;
    }

    public Integer getOnboard_status() {
        return onboard_status;
    }

    public void setOnboard_status(Integer onboard_status) {
        this.onboard_status = onboard_status;
    }

    public String getAsset_ocr_image_url() {
        return asset_ocr_image_url;
    }

    public void setAsset_ocr_image_url(String asset_ocr_image_url) {
        this.asset_ocr_image_url = asset_ocr_image_url;
    }

    public DeviceOnboardStatus getDevice_onboard_status() {
        return device_onboard_status;
    }

    public void setDevice_onboard_status(DeviceOnboardStatus device_onboard_status) {
        this.device_onboard_status = device_onboard_status;
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

    public String getLocation_status() {
        return location_status;
    }

    public void setLocation_status(String location_status) {
        this.location_status = location_status;
    }

    public String getDigital_twin_image_url() {
        return digital_twin_image_url;
    }

    public void setDigital_twin_image_url(String digital_twin_image_url) {
        this.digital_twin_image_url = digital_twin_image_url;
    }

    // removed: getter/setter for Bucket-C PolyLensDevice (AP-C2)

    public Integer getPoly_lens_count() {
        return poly_lens_count;
    }

    public void setPoly_lens_count(Integer poly_lens_count) {
        this.poly_lens_count = poly_lens_count;
    }


    // removed: getter/setter for Bucket-C MqttDevice (AP-C2)

    public Integer getMqtt_count() {
        return mqtt_count;
    }

    public void setMqtt_count(Integer mqtt_count) {
        this.mqtt_count = mqtt_count;
    }

    public Set<Nfc> getNfc() {
        return nfc;
    }

    public void setNfc(Set<Nfc> nfc) {
        this.nfc = nfc;
    }

    public Set<QrCode> getQrcode() {
        return qrcode;
    }

    public void setQrcode(Set<QrCode> qrcode) {
        this.qrcode = qrcode;
    }

    public Set<ClientNfc> getClient_nfc() {
        return client_nfc;
    }

    public void setClient_nfc(Set<ClientNfc> client_nfc) {
        this.client_nfc = client_nfc;
    }

    public Set<ClientQrCode> getClient_qrcode() {
        return client_qrcode;
    }

    public void setClient_qrcode(Set<ClientQrCode> client_qrcode) {
        this.client_qrcode = client_qrcode;
    }

    public Set<ClientBarCode> getClient_barcode() {
        return client_barcode;
    }

    public void setClient_barcode(Set<ClientBarCode> client_barcode) {
        this.client_barcode = client_barcode;
    }

    // removed: getter/setter for Bucket-C GlobalChecklistConditions (AP-C4)

    // removed: getter/setter for Bucket-C Technician (AP-C3)

    // removed: getter/setter for Bucket-C InventoryDevice (AP-C8)

    public BigInteger getDnd_timestamp() {
        return dnd_timestamp;
    }

    public void setDnd_timestamp(BigInteger dnd_timestamp) {
        this.dnd_timestamp = dnd_timestamp;
    }

    public Boolean getSystem_dnd_enabled() {
        return system_dnd_enabled;
    }

    public void setSystem_dnd_enabled(Boolean system_dnd_enabled) {
        this.system_dnd_enabled = system_dnd_enabled;
    }


    // removed: getter/setter for Bucket-C GaiameshController (AP-C2)

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", alarm=" + alarm +
                ", display_name='" + display_name + '\'' +
                ", ip_address='" + ip_address + '\'' +
                ", mac_address='" + mac_address + '\'' +
                ", last_seen_on=" + last_seen_on +
                ", model='" + model + '\'' +
                ", monitor=" + monitor +
                ", network_layer='" + network_layer + '\'' +
                ", remote_access=" + remote_access +
                ", status=" + status +
                ", type='" + type + '\'' +
                ", user_data_type='" + user_data_type + '\'' +
                ", user_data_model='" + user_data_model + '\'' +
                ", user_data_name='" + user_data_name + '\'' +
                ", user_data_vendor='" + user_data_vendor + '\'' +
                ", vendor='" + vendor + '\'' +
                ", warranty='" + warranty + '\'' +
                ", quick_link_name='" + quick_link_name + '\'' +
                ", quick_link_url='" + quick_link_url + '\'' +
                ", email_alert=" + email_alert +
                ", sms_alert=" + sms_alert +
                ", popup_notification=" + popup_notification +
                ", snmp_count=" + snmp_count +
                ", snmp_status='" + snmp_status + '\'' +
                ", interface_count=" + interface_count +
                ", notes_count=" + notes_count +
                ", ticket_count=" + ticket_count +
                ", ticket_status='" + ticket_status + '\'' +
                ", virtual_device_type=" + virtual_device_type +
                ", serial_number='" + serial_number + '\'' +
                ", bacnet_count=" + bacnet_count +
                ", bacnet_status='" + bacnet_status + '\'' +
                ", lorawan_count=" + lorawan_count +
                ", lorawan_status='" + lorawan_status + '\'' +
                ", disruptive_count=" + disruptive_count +
                ", disruptive_status='" + disruptive_status + '\'' +
                ", snmp_parent_index='" + snmp_parent_index + '\'' +
                ", my_devices_count=" + my_devices_count +
                ", my_devices_status='" + my_devices_status + '\'' +
                ", monnit_count=" + monnit_count +
                ", monnit_status='" + monnit_status + '\'' +
                ", pelican_count=" + pelican_count +
                ", pelican_status='" + pelican_status + '\'' +
                ", knx_count=" + knx_count +
                ", knx_status='" + knx_status + '\'' +
                ", snmp_object_count=" + snmp_object_count +
                ", snmp_object_status='" + snmp_object_status + '\'' +
                ", position='" + position + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", measuring_instrument_count=" + measuring_instrument_count +
                ", measuring_instrument_status='" + measuring_instrument_status + '\'' +
                ", document_count=" + document_count +
                ", media_count=" + media_count +
                ", checklist_template_count=" + checklist_template_count +
                ", parent='" + parent + '\'' +
                ", snmp_parent='" + snmp_parent + '\'' +
                ", local_vendor_email_alert=" + local_vendor_email_alert +
                ", local_vendor_sms_alert=" + local_vendor_sms_alert +
                ", subsystem_parent_id='" + subsystem_parent_id + '\'' +
                ", subsystem_count=" + subsystem_count +
                ", custom_fields='" + custom_fields + '\'' +
                ", description='" + description + '\'' +
                ", asset_match_status=" + asset_match_status +
                ", matched_product_ids='" + matched_product_ids + '\'' +
                ", connection_type='" + connection_type + '\'' +
                ", user_connection_type='" + user_connection_type + '\'' +
                ", record_checklist_status='" + record_checklist_status + '\'' +
                ", record_checklist_count=" + record_checklist_count +
                ", daintree_count=" + daintree_count +
                ", daintree_status='" + daintree_status + '\'' +
                ", qrcode_count=" + qrcode_count +
                ", global_vendor=" + global_vendor +
                ", asset_image_url='" + asset_image_url + '\'' +
                ", created_timestamp=" + created_timestamp +
                ", ecobee_count=" + ecobee_count +
                ", ecobee_status='" + ecobee_status + '\'' +
                ", reboot_status='" + reboot_status + '\'' +
                ", onboard_status=" + onboard_status +
                ", asset_ocr_image_url='" + asset_ocr_image_url + '\'' +
                ", local_vendor=" + local_vendor +
                ", modbus_count=" + modbus_count +
                ", modbus_status='" + modbus_status + '\'' +
                ", created_email='" + created_email + '\'' +
                ", asset_group='" + asset_group + '\'' +
                ", updated_email='" + updated_email + '\'' +
                ", updated_timestamp=" + updated_timestamp +
                ", location_status='" + location_status + '\'' +
                ", category='" + category + '\'' +
                ", sub_category='" + sub_category + '\'' +
                ", digital_twin_image_url='" + digital_twin_image_url + '\'' +
                ", other_vendor_1=" + other_vendor_1 +
                ", other_vendor_2=" + other_vendor_2 +
                ", other_vendor_3=" + other_vendor_3 +
                ", snmp_configuration=" + snmp_configuration +
                ", interfaces=" + interfaces +
                ", service=" + service +
                ", notes=" + notes +
                ", product_details=" + product_details +
                ", remote_access_session=" + remote_access_session +
                ", location=" + location +
                ", history=" + history +
                ", ticket=" + ticket +
                ", lorawan_sensor=" + lorawan_sensor +
                ", bacnet_object=" + bacnet_object +
                ", disruptive_sensor=" + disruptive_sensor +
                ", device_ip_address=" + device_ip_address +
                ", datahoist=" + datahoist +
                ", my_devices_sensor=" + my_devices_sensor +
                ", monnit_sensor=" + monnit_sensor +
                ", pelican_sensor=" + pelican_sensor +
                ", snmp_dump=" + snmp_dump +
                ", knx_group=" + knx_group +
                ", snmp_object=" + snmp_object +
                ", document=" + document +
                ", media=" + media +
                ", check_list_template=" + check_list_template +
                ", check_list_record=" + check_list_record +
                ", asset_device_mapping=" + asset_device_mapping +
                ", measuring_instrument=" + measuring_instrument +
                ", inventory=" + inventory +
                ", global_qrcode=" + global_qrcode +
                ", record_checklist=" + record_checklist +
                ", global_checklist=" + global_checklist +
                ", global_inspection_relation=" + global_inspection_relation +
                ", daintree_device=" + daintree_device +
                ", device_conditions=" + device_conditions +
                ", ecobee_sensor=" + ecobee_sensor +
                ", specifications=" + specifications +
                ", modbus_register=" + modbus_register +
                ", siemens_asset=" + siemens_asset +
                ", device_onboard_status=" + device_onboard_status +
                ", poly_lens_count=" + poly_lens_count +
                ", cost_value=" + cost_value +
                ", assigned_user_email='" + user + '\'' +
                ", ai_call=" + ai_call +
                ", cost_unit='" + cost_unit + '\'' +
                ", is_dnd_enabled=" + is_dnd_enabled +
                ", operational_status='" + operational_status + '\'' +
                ", gaiameshController=" + gaiamesh_controller +
                '}';
    }


    public String getAsset_tag_images_url() {
        return asset_tag_images_url;
    }

    public void setAsset_tag_images_url(String asset_tag_images_url) {
        this.asset_tag_images_url = asset_tag_images_url;
    }

    public Snmp_Configuration getSnmp_configuration() { return snmp_configuration; }
    public void setSnmp_configuration(Snmp_Configuration snmp_configuration) { this.snmp_configuration = snmp_configuration; }
    public Product_Details getProduct_details() { return product_details; }
    public void setProduct_details(Product_Details product_details) { this.product_details = product_details; }
    public RemoteAccessSession getRemote_access_session() { return remote_access_session; }
    public void setRemote_access_session(RemoteAccessSession remote_access_session) { this.remote_access_session = remote_access_session; }
    public java.util.Set<History> getHistory() { return history; }
    public void setHistory(java.util.Set<History> history) { this.history = history; }
    public java.util.Set<Ticket> getTicket() { return ticket; }
    public void setTicket(java.util.Set<Ticket> ticket) { this.ticket = ticket; }
    public java.util.Set<Lorawan_Sensor> getLorawan_sensor() { return lorawan_sensor; }
    public void setLorawan_sensor(java.util.Set<Lorawan_Sensor> lorawan_sensor) { this.lorawan_sensor = lorawan_sensor; }
    public java.util.Set<Bacnet_Object> getBacnet_object() { return bacnet_object; }
    public void setBacnet_object(java.util.Set<Bacnet_Object> bacnet_object) { this.bacnet_object = bacnet_object; }
    public java.util.Set<DisruptiveSensor> getDisruptive_sensor() { return disruptive_sensor; }
    public void setDisruptive_sensor(java.util.Set<DisruptiveSensor> disruptive_sensor) { this.disruptive_sensor = disruptive_sensor; }
    public java.util.Set<Datahoist> getDatahoist() { return datahoist; }
    public void setDatahoist(java.util.Set<Datahoist> datahoist) { this.datahoist = datahoist; }
    public java.util.Set<MyDevicesSensor> getMy_devices_sensor() { return my_devices_sensor; }
    public void setMy_devices_sensor(java.util.Set<MyDevicesSensor> my_devices_sensor) { this.my_devices_sensor = my_devices_sensor; }
    public java.util.Set<Monnit_Sensor> getMonnit_sensor() { return monnit_sensor; }
    public void setMonnit_sensor(java.util.Set<Monnit_Sensor> monnit_sensor) { this.monnit_sensor = monnit_sensor; }
    public java.util.Set<PelicanSensor> getPelican_sensor() { return pelican_sensor; }
    public void setPelican_sensor(java.util.Set<PelicanSensor> pelican_sensor) { this.pelican_sensor = pelican_sensor; }
    
    
    public java.util.Set<KNXGroup> getKnx_group() { return knx_group; }
    public void setKnx_group(java.util.Set<KNXGroup> knx_group) { this.knx_group = knx_group; }
    public java.util.Set<SnmpObject> getSnmp_object() { return snmp_object; }
    public void setSnmp_object(java.util.Set<SnmpObject> snmp_object) { this.snmp_object = snmp_object; }
    public java.util.Set<CheckListTemplate> getCheck_list_template() { return check_list_template; }
    public void setCheck_list_template(java.util.Set<CheckListTemplate> check_list_template) { this.check_list_template = check_list_template; }
    public java.util.Set<CheckListRecord> getCheck_list_record() { return check_list_record; }
    public void setCheck_list_record(java.util.Set<CheckListRecord> check_list_record) { this.check_list_record = check_list_record; }
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public java.util.Set<RecordChecklist> getRecord_checklist() { return record_checklist; }
    public void setRecord_checklist(java.util.Set<RecordChecklist> record_checklist) { this.record_checklist = record_checklist; }
    
    
    public java.util.Set<GlobalInspectionRelation> getGlobal_inspection_relation() { return global_inspection_relation; }
    public void setGlobal_inspection_relation(java.util.Set<GlobalInspectionRelation> global_inspection_relation) { this.global_inspection_relation = global_inspection_relation; }
    public java.util.Set<GlobalChecklistConditions> getGlobal_checklist_conditions() { return global_checklist_conditions; }
    public void setGlobal_checklist_conditions(java.util.Set<GlobalChecklistConditions> global_checklist_conditions) { this.global_checklist_conditions = global_checklist_conditions; }
    public java.util.Set<DaintreeDevice> getDaintree_device() { return daintree_device; }
    public void setDaintree_device(java.util.Set<DaintreeDevice> daintree_device) { this.daintree_device = daintree_device; }
    public java.util.Set<EcobeeSensor> getEcobee_sensor() { return ecobee_sensor; }
    public void setEcobee_sensor(java.util.Set<EcobeeSensor> ecobee_sensor) { this.ecobee_sensor = ecobee_sensor; }
    public java.util.Set<ModbusRegister> getModbus_register() { return modbus_register; }
    public void setModbus_register(java.util.Set<ModbusRegister> modbus_register) { this.modbus_register = modbus_register; }
    public java.util.Set<SiemensAsset> getSiemens_asset() { return siemens_asset; }
    public void setSiemens_asset(java.util.Set<SiemensAsset> siemens_asset) { this.siemens_asset = siemens_asset; }
    public java.util.Set<GaiameshController> getGaiamesh_controller() { return gaiamesh_controller; }
    public void setGaiamesh_controller(java.util.Set<GaiameshController> gaiamesh_controller) { this.gaiamesh_controller = gaiamesh_controller; }

    public DaintreeDevice getInventory_device() {
        return new DaintreeDevice();
    }

}
