package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.projection.DeviceOnboardRow;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigInteger;

/**
 * Maps {@link DeviceOnboardRow} to {@link DeviceDTO} for the single-device onboard read
 * ({@code getDeviceByDeviceIdNew}). With {@code ignoreByDefault = true} only the explicitly listed
 * targets are populated, so the output matches the old {@code deviceonboardmapping} @ConstructorResult
 * exactly (unselected fields stay null). Compared to {@code DeviceDtoMapper}, this query does NOT
 * select assigned_user_email, inventory_tracking_id, cost_value, ai_call, cost_unit, is_dnd_enabled,
 * operational_status, digital_twin_image_url, asset_tag_images_url — so those mappings are omitted.
 */
@Mapper(componentModel = "spring")
public interface DeviceOnboardDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    // --- device scalar columns (entity fields are snake_case) ---
    @Mapping(target = "id",                          source = "device.id")
    @Mapping(target = "status",                       source = "device.status")
    @Mapping(target = "display_name",                 source = "device.display_name")
    @Mapping(target = "last_seen_on",                 source = "device.last_seen_on", qualifiedByName = "bigIntToStr")
    @Mapping(target = "mac_address",                  source = "device.mac_address")
    @Mapping(target = "vendor",                       source = "device.vendor")
    @Mapping(target = "model",                        source = "device.model")
    @Mapping(target = "type",                         source = "device.type")
    @Mapping(target = "ip_address",                   source = "device.ip_address")
    @Mapping(target = "monitor",                      source = "device.monitor")
    @Mapping(target = "network_layer",                source = "device.network_layer")
    @Mapping(target = "user_data_model",              source = "device.user_data_model")
    @Mapping(target = "user_data_vendor",             source = "device.user_data_vendor")
    @Mapping(target = "user_data_name",               source = "device.user_data_name")
    @Mapping(target = "parent",                       source = "device.parent")
    @Mapping(target = "snmp_parent",                  source = "device.snmp_parent")
    @Mapping(target = "system_type",                  source = "device.type")
    @Mapping(target = "remote_access",                source = "device.remote_access")
    @Mapping(target = "product_id",                   source = "device.product_id")
    @Mapping(target = "alarm",                        source = "device.alarm", qualifiedByName = "intToStr")
    @Mapping(target = "virtual_device_type",          source = "device.virtual_device_type")
    @Mapping(target = "warranty",                     source = "device.warranty")
    @Mapping(target = "quick_link_name",              source = "device.quick_link_name")
    @Mapping(target = "quick_link_url",               source = "device.quick_link_url")
    @Mapping(target = "email_alert",                  source = "device.email_alert")
    @Mapping(target = "sms_alert",                    source = "device.sms_alert")
    @Mapping(target = "popup_notification",           source = "device.popup_notification")
    @Mapping(target = "snmp_count",                   source = "device.snmp_count")
    @Mapping(target = "snmp_status",                  source = "device.snmp_status")
    @Mapping(target = "interface_count",              source = "device.interface_count")
    @Mapping(target = "notes_count",                  source = "device.notes_count")
    @Mapping(target = "ticket_count",                 source = "device.ticket_count")
    @Mapping(target = "ticket_status",                source = "device.ticket_status")
    @Mapping(target = "serial_number",                source = "device.serial_number")
    @Mapping(target = "bacnet_count",                 source = "device.bacnet_count")
    @Mapping(target = "bacnet_status",                source = "device.bacnet_status")
    @Mapping(target = "lorawan_count",                source = "device.lorawan_count")
    @Mapping(target = "lorawan_status",               source = "device.lorawan_status")
    @Mapping(target = "disruptive_count",             source = "device.disruptive_count")
    @Mapping(target = "disruptive_status",            source = "device.disruptive_status")
    @Mapping(target = "my_devices_count",             source = "device.my_devices_count")
    @Mapping(target = "my_devices_status",            source = "device.my_devices_status")
    @Mapping(target = "local_vendor_email_alert",     source = "device.local_vendor_email_alert")
    @Mapping(target = "local_vendor_sms_alert",       source = "device.local_vendor_sms_alert")
    @Mapping(target = "monnit_count",                 source = "device.monnit_count")
    @Mapping(target = "monnit_status",                source = "device.monnit_status")
    @Mapping(target = "pelican_count",                source = "device.pelican_count")
    @Mapping(target = "pelican_status",               source = "device.pelican_status")
    @Mapping(target = "knx_count",                     source = "device.knx_count")
    @Mapping(target = "knx_status",                    source = "device.knx_status")
    @Mapping(target = "subsystem_parent_id",          source = "device.subsystem_parent_id")
    @Mapping(target = "subsystem_count",              source = "device.subsystem_count")
    @Mapping(target = "custom_fields",                source = "device.custom_fields")
    @Mapping(target = "description",                   source = "device.description")
    @Mapping(target = "asset_match_status",           source = "device.asset_match_status")
    @Mapping(target = "matched_product_ids",          source = "device.matched_product_ids")
    @Mapping(target = "latitude",                      source = "device.latitude")
    @Mapping(target = "longitude",                     source = "device.longitude")
    @Mapping(target = "measuring_instrument_count",   source = "device.measuring_instrument_count")
    @Mapping(target = "document_count",               source = "device.document_count")
    @Mapping(target = "media_count",                   source = "device.media_count")
    @Mapping(target = "checklist_template_count",     source = "device.checklist_template_count")
    @Mapping(target = "snmp_object_count",            source = "device.snmp_object_count")
    @Mapping(target = "snmp_object_status",           source = "device.snmp_object_status")
    @Mapping(target = "position",                      source = "device.position")
    @Mapping(target = "measuring_instrument_status",  source = "device.measuring_instrument_status")
    @Mapping(target = "record_checklist_count",       source = "device.record_checklist_count")
    @Mapping(target = "record_checklist_status",      source = "device.record_checklist_status")
    @Mapping(target = "daintree_count",               source = "device.daintree_count")
    @Mapping(target = "daintree_status",              source = "device.daintree_status")
    @Mapping(target = "asset_image_url",              source = "device.asset_image_url")
    @Mapping(target = "created_timestamp",            source = "device.created_timestamp")
    @Mapping(target = "ecobee_count",                 source = "device.ecobee_count")
    @Mapping(target = "ecobee_status",                source = "device.ecobee_status")
    @Mapping(target = "modbus_count",                 source = "device.modbus_count")
    @Mapping(target = "modbus_status",                source = "device.modbus_status")
    @Mapping(target = "created_email",                source = "device.created_email")
    @Mapping(target = "asset_group",                   source = "device.asset_group")
    @Mapping(target = "updated_email",                source = "device.updated_email")
    @Mapping(target = "updated_timestamp",            source = "device.updated_timestamp")
    @Mapping(target = "onboard_status",               source = "device.onboard_status")
    @Mapping(target = "asset_ocr_image_url",          source = "device.asset_ocr_image_url")
    @Mapping(target = "category",                      source = "device.category")
    @Mapping(target = "sub_category",                 source = "device.sub_category")
    @Mapping(target = "location_status",              source = "device.location_status")
    @Mapping(target = "poly_lens_count",              source = "device.poly_lens_count")
    @Mapping(target = "adc_json",                      source = "device.adc_json")
    @Mapping(target = "system_type_id",               source = "device.system_type_id")
    @Mapping(target = "system_type_name",             source = "device.system_type_name")
    @Mapping(target = "asset_type_id",                source = "device.asset_type_id")
    @Mapping(target = "asset_type_name",              source = "device.asset_type_name")
    @Mapping(target = "asset_sub_type_id",            source = "device.asset_sub_type_id")
    @Mapping(target = "asset_sub_type_name",          source = "device.asset_sub_type_name")
    @Mapping(target = "source_type",                  source = "device.source_type")
    // --- association-FK ids + join scalars (from the projection record) ---
    @Mapping(target = "docker_name",                  source = "dockerName")
    @Mapping(target = "vdms_id",                       source = "vdmsId")
    @Mapping(target = "location",                      source = "location")
    @Mapping(target = "location_id",                   source = "locationId")
    @Mapping(target = "floor",                         source = "floor")
    @Mapping(target = "floor_id",                      source = "floorId")
    @Mapping(target = "building",                      source = "building")
    @Mapping(target = "building_id",                   source = "buildingId")
    @Mapping(target = "local_vendor_id",              source = "localVendorId")
    @Mapping(target = "global_vendor_id",             source = "globalVendorId")
    @Mapping(target = "other_vendor_1_id",            source = "otherVendor1Id")
    @Mapping(target = "other_vendor_2_id",            source = "otherVendor2Id")
    @Mapping(target = "other_vendor_3_id",            source = "otherVendor3Id")
    @Mapping(target = "device_onboard_status_id",     source = "onboardStatusId")
    @Mapping(target = "assignee_email",               source = "assigneeEmail")
    @Mapping(target = "image_status",                 source = "imageStatus")
    @Mapping(target = "geolocation_status",           source = "geolocationStatus")
    @Mapping(target = "tag_status",                    source = "tagStatus")
    @Mapping(target = "field_status",                 source = "fieldStatus")
    DeviceDTO toDto(DeviceOnboardRow row);

    /** bigint column rendered as String, null-safe — matches the native @ColumnResult(String) cast. */
    @Named("bigIntToStr")
    default String bigIntToStr(BigInteger v) { return v == null ? null : v.toString(); }

    /** integer column rendered as String, null-safe — matches the native @ColumnResult(String) cast. */
    @Named("intToStr")
    default String intToStr(Integer v) { return v == null ? null : v.toString(); }
}
