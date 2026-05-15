package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.touchscreen.DeviceIPAddressDTO;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDTO {

    private String id;
    private String name;
    private Integer status;
    private String display_name;

    private String last_seen_on;
    private String mac_address;
    private String vendor;
    private String model;
    private String type;
    private String ip_address;
    // private List<NotesDTO> notes;

    private Integer monitor;
    private String location;
    private String network_layer;
    private String user_data_model;

    private String user_data_vendor;
    private String user_data_name;
    private String parent;
    private String snmp_parent;
    private String vdms_id;
    private String docker_name;

    private String system_type;
    private Integer remote_access;
    private String building;
    private String floor;
    private String local_vendor_id;
    private String global_vendor_id;
    private String other_vendor_1_id;
    private String other_vendor_2_id;
    private String other_vendor_3_id;

    private String product_id;
    private String alarm;
    //	private Boolean is_virtual;
    private String warranty;
    private String quick_link_name;
    private String quick_link_url;
    private String location_id;
    private BuildingDTO building_object;

    private Integer email_alert;
    private Integer sms_alert;
    private Integer popup_notification;

    //New field
    private String latitude;
    private String longitude;

    //	private Boolean snmp_alert;
    private Integer snmp_count;
    private String snmp_status;
    private Integer notes_count;
    private Integer interface_count;
    private Integer ticket_count;
    private String ticket_status;
    private Integer bacnet_count;
    private String bacnet_status;
    private Integer lorawan_count;
    private String lorawan_status;
    private Integer disruptive_count;
    private String disruptive_status;
    private Integer my_devices_count;
    private String my_devices_status;
    private Integer monnit_count;
    private String monnit_status;
    private Integer pelican_count;
    private String pelican_status;
    private Integer knx_count;
    private String knx_status;
    private Integer snmp_object_count;
    private String snmp_object_status;
    private Integer ecobee_count;
    private String ecobee_status;
    private Integer datahoist_count;


    private Integer virtual_device_type;
    private String serial_number;

    // SNMP
    private String snmp_parent_index;

    private String position;

    private Integer local_vendor_email_alert;

    private Integer local_vendor_sms_alert;

    private List<InterfaceDTO> ports;

    // Device IP addresses
    private List<DeviceIPAddressDTO> ip_addresses;


    private String parent_name;

    private String subsystem_parent_id;

    private String previous_subsystem_parent_id;

    private Integer subsystem_count;

    private Set<DeviceDTO> subsystems;

    private String custom_fields;

    private String description;

    private Integer asset_match_status;

    private String matched_product_ids;

    private Integer measuring_instrument_count;

    private Integer document_count;

    private Integer media_count;

    private Integer checklist_template_count;

    private Integer matched_score;

    private String matched_column;

    private String matched_info;

    private String subsystem_parent_name;

    private String measuring_instrument_status;

    private String record_checklist_status;
    private Integer record_checklist_count;

    private Integer daintree_count;
    private String daintree_status;

    private Integer qrcode_count;

    private String asset_image_url;
    private BigInteger created_timestamp;
    private String snmp_parent_name;
    private Integer is_added;
    private Integer nfc_count;
    private Integer modbus_count;
    private String modbus_status;
    private String floor_id;
    private String created_email;
    private String asset_group;
    private String building_id;
    private String updated_email;
    private BigInteger updated_timestamp;
    private Integer onboard_status;
    private List<SpecificationsDTO> specifications;
    private List<LoadCalculationDTO> load_calculation;
    private DeviceOnboardStatusDTO onboard_data;
    private String asset_ocr_image_url;

    private String location_status;

    @JsonIgnore
    private Integer image_status;

    @JsonIgnore
    private Integer geolocation_status;

    @JsonIgnore
    private String assignee_email;

    @JsonIgnore
    private Integer tag_status;

    @JsonIgnore
    private Integer field_status;

    @JsonIgnore
    private String device_onboard_status_id;

    private String category;

    private String sub_category;

    private String device_id;

    private String digital_twin_image_url;


    private Integer poly_lens_count;

    private BigDecimal cost_value;

    private String assigned_user_email;

    private Boolean ai_call;

    private String cost_unit;

    private Boolean is_dnd_enabled;

    private String operational_status;
    private Integer barcode_count;

    private Set<MeasuringInstrumentDTO> measuringInstruments;


    private String inventory_tracking_id;

    private String docker_vdms_id;

    private BigInteger dnd_timestamp;

    private Boolean system_dnd_enabled;

    private String pc_location;

    private String isp_location;

    private String username;

    private Boolean is_agent_device;

    private String adc_json;
    private String system_type_id;
    private String system_type_name;
    private String asset_type_id;
    private String asset_type_name;
    private String asset_sub_type_id;
    private String asset_sub_type_name;
    private String source_type;
    private String asset_tag_images_url;

    public DeviceDTO() {
    }

    public DeviceDTO(Boolean ai_call, String asset_image_url, String asset_ocr_image_url, String asset_tag_images_url) {
        this.ai_call = ai_call;
        this.asset_image_url = asset_image_url;
        this.asset_ocr_image_url = asset_ocr_image_url;
        this.asset_tag_images_url = asset_tag_images_url;
    }

    //devicemapping
    public DeviceDTO(String id, Integer status, String display_name, String last_seen_on, String mac_address,
                     String vendor, String model, String type, String ip_address, Integer monitor, String location,
                     String network_layer, String user_data_model, String user_data_vendor,
                     String user_data_name, String parent, String snmp_parent, String vdms_id, String docker_name,
                     String system_type, Integer remote_access, String building, String floor,
                     String local_vendor_id, String global_vendor_id, String other_vendor_1_id, String other_vendor_2_id,
                     String other_vendor_3_id, String product_id, String alarm, Integer virtual_device_type,
                     String warranty, String quick_link_name, String quick_link_url, String location_id, Integer email_alert, Integer sms_alert, Integer popup_notification,
                     Integer snmp_count, String snmp_status, Integer interface_count, Integer notes_count, Integer ticket_count, String ticket_status,
                     String serial_number, Integer bacnet_count, String bacnet_status, Integer lorawan_count, String lorawan_status,
                     Integer disruptive_count, String disruptive_status, Integer my_devices_count, String my_devices_status,
                     Integer local_vendor_email_alert, Integer local_vendor_sms_alert, Integer monnit_count, String monnit_status,
                     Integer pelican_count, String pelican_status, Integer knx_count, String knx_status, String subsystem_parent_id, Integer subsystem_count,
                     String custom_fields, String description, Integer asset_match_status, String matched_product_ids, String latitude, String longitude,
                     Integer measuring_instrument_count, Integer document_count, Integer media_count, Integer checklist_template_count,
                     Integer snmp_object_count, String snmp_object_status, String position, String measuring_instrument_status, Integer record_checklist_count,
                     String record_checklist_status, Integer daintree_count, String daintree_status,
                     String asset_image_url, BigInteger created_timestamp, Integer ecobee_count, String ecobee_status,
                     Integer modbus_count, String modbus_status, String floor_id, String created_email, String asset_group, String building_id,
                     String updated_email, BigInteger updated_timestamp, Integer onboard_status, String device_onboard_status_id, String assignee_email, Integer image_status,
                     Integer geolocation_status, Integer tag_status, Integer field_status, String asset_ocr_image_url, String category,
                     String sub_category, String location_status, String digital_twin_image_url, Integer poly_lens_count, BigDecimal cost_value,
                     String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status, String inventory_tracking_id) {
        super();
        this.id = id;
        this.status = status;
        this.display_name = display_name;
        this.last_seen_on = last_seen_on;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.monitor = monitor;
        this.location = location;
        this.network_layer = network_layer;
        this.user_data_model = user_data_model;
        this.user_data_vendor = user_data_vendor;
        this.user_data_name = user_data_name;
        this.parent = parent;
        this.snmp_parent = snmp_parent;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.system_type = system_type;
        this.remote_access = remote_access;
        this.building = building;
        this.floor = floor;
        this.local_vendor_id = local_vendor_id;
        this.global_vendor_id = global_vendor_id;
        this.other_vendor_1_id = other_vendor_1_id;
        this.other_vendor_2_id = other_vendor_2_id;
        this.other_vendor_3_id = other_vendor_3_id;
        this.product_id = product_id;
        this.alarm = alarm;
        this.virtual_device_type = virtual_device_type;
        this.warranty = warranty;
        this.quick_link_name = quick_link_name;
        this.quick_link_url = quick_link_url;
        this.location_id = location_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.popup_notification = popup_notification;
        this.snmp_count = snmp_count;
        this.snmp_status = snmp_status;
        this.interface_count = interface_count;
        this.notes_count = notes_count;
        this.ticket_count = ticket_count;
        this.ticket_status = ticket_status;
        this.serial_number = serial_number;
        this.bacnet_count = bacnet_count;
        this.bacnet_status = bacnet_status;
        this.lorawan_count = lorawan_count;
        this.lorawan_status = lorawan_status;
        this.disruptive_count = disruptive_count;
        this.disruptive_status = disruptive_status;
        this.my_devices_count = my_devices_count;
        this.my_devices_status = my_devices_status;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.monnit_count = monnit_count;
        this.monnit_status = monnit_status;
        this.pelican_count = pelican_count;
        this.pelican_status = pelican_status;
        this.knx_count = knx_count;
        this.knx_status = knx_status;
        this.subsystem_parent_id = subsystem_parent_id;
        this.subsystem_count = subsystem_count;
        this.custom_fields = custom_fields;
        this.description = description;
        this.asset_match_status = asset_match_status;
        this.matched_product_ids = matched_product_ids;
        this.latitude = latitude;
        this.longitude = longitude;
        this.measuring_instrument_count = measuring_instrument_count;
        this.document_count = document_count;
        this.media_count = media_count;
        this.checklist_template_count = checklist_template_count;
        this.snmp_object_count = snmp_object_count;
        this.snmp_object_status = snmp_object_status;
        this.position = position;
        this.measuring_instrument_status = measuring_instrument_status;
        this.record_checklist_count = record_checklist_count;
        this.record_checklist_status = record_checklist_status;
        this.daintree_count = daintree_count;
        this.daintree_status = daintree_status;
        this.asset_image_url = asset_image_url;
        this.created_timestamp = created_timestamp;
        this.ecobee_count = ecobee_count;
        this.ecobee_status = ecobee_status;
        this.modbus_count = modbus_count;
        this.modbus_status = modbus_status;
        this.floor_id = floor_id;
        this.created_email = created_email;
        this.asset_group = asset_group;
        this.building_id = building_id;
        this.updated_email = updated_email;
        this.updated_timestamp = updated_timestamp;
        this.onboard_status = onboard_status;
        this.device_onboard_status_id = device_onboard_status_id;
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.asset_ocr_image_url = asset_ocr_image_url;
        this.category = category;
        this.sub_category = sub_category;
        this.location_status = location_status;
        this.digital_twin_image_url = digital_twin_image_url;
        this.poly_lens_count = poly_lens_count;
        this.cost_value = cost_value;
        this.assigned_user_email = assigned_user_email;
        this.ai_call = ai_call;
        this.cost_unit = cost_unit;
        this.is_dnd_enabled = is_dnd_enabled;
        this.operational_status = operational_status;
        this.inventory_tracking_id = inventory_tracking_id;
    }

    //devicedtomapping
    public DeviceDTO(String id, Integer status, String display_name, String last_seen_on, String mac_address,
                     String vendor, String model, String type, String ip_address, Integer monitor, String location,
                     String network_layer, String user_data_model, String user_data_vendor,
                     String user_data_name, String parent, String snmp_parent, String vdms_id, String docker_name,
                     String system_type, Integer remote_access, String building, String floor,
                     String local_vendor_id, String global_vendor_id, String other_vendor_1_id, String other_vendor_2_id,
                     String other_vendor_3_id, String product_id, String alarm, Integer virtual_device_type,
                     String warranty, String quick_link_name, String quick_link_url, String location_id, Integer email_alert, Integer sms_alert, Integer popup_notification,
                     Integer snmp_count, String snmp_status, Integer interface_count, Integer notes_count, Integer ticket_count, String ticket_status,
                     String serial_number, Integer bacnet_count, String bacnet_status, Integer lorawan_count, String lorawan_status,
                     Integer disruptive_count, String disruptive_status, Integer my_devices_count, String my_devices_status,
                     Integer local_vendor_email_alert, Integer local_vendor_sms_alert, Integer monnit_count, String monnit_status,
                     Integer pelican_count, String pelican_status, Integer knx_count, String knx_status, String subsystem_parent_id, Integer subsystem_count,
                     String custom_fields, String description, Integer asset_match_status, String matched_product_ids, String latitude, String longitude,
                     Integer measuring_instrument_count, Integer document_count, Integer media_count, Integer checklist_template_count,
                     Integer snmp_object_count, String snmp_object_status, String position, String measuring_instrument_status, Integer record_checklist_count,
                     String record_checklist_status, Integer daintree_count, String daintree_status,
                     String asset_image_url, BigInteger created_timestamp, Integer ecobee_count, String ecobee_status,
                     Integer modbus_count, String modbus_status, String floor_id, String created_email, String asset_group, String building_id,
                     String updated_email, BigInteger updated_timestamp, Integer onboard_status, String device_onboard_status_id, String assignee_email, Integer image_status,
                     Integer geolocation_status, Integer tag_status, Integer field_status, String asset_ocr_image_url, String category,
                     String sub_category, String location_status, String digital_twin_image_url, Integer poly_lens_count, BigDecimal cost_value,
                     String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status, String inventory_tracking_id,
                     String adc_json, String system_type_id, String system_type_name, String asset_type_id, String asset_type_name, String asset_sub_type_id, String asset_sub_type_name, String source_type, String asset_tag_images_url) {
        super();
        this.id = id;
        this.status = status;
        this.display_name = display_name;
        this.last_seen_on = last_seen_on;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.monitor = monitor;
        this.location = location;
        this.network_layer = network_layer;
        this.user_data_model = user_data_model;
        this.user_data_vendor = user_data_vendor;
        this.user_data_name = user_data_name;
        this.parent = parent;
        this.snmp_parent = snmp_parent;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.system_type = system_type;
        this.remote_access = remote_access;
        this.building = building;
        this.floor = floor;
        this.local_vendor_id = local_vendor_id;
        this.global_vendor_id = global_vendor_id;
        this.other_vendor_1_id = other_vendor_1_id;
        this.other_vendor_2_id = other_vendor_2_id;
        this.other_vendor_3_id = other_vendor_3_id;
        this.product_id = product_id;
        this.alarm = alarm;
        this.virtual_device_type = virtual_device_type;
        this.warranty = warranty;
        this.quick_link_name = quick_link_name;
        this.quick_link_url = quick_link_url;
        this.location_id = location_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.popup_notification = popup_notification;
        this.snmp_count = snmp_count;
        this.snmp_status = snmp_status;
        this.interface_count = interface_count;
        this.notes_count = notes_count;
        this.ticket_count = ticket_count;
        this.ticket_status = ticket_status;
        this.serial_number = serial_number;
        this.bacnet_count = bacnet_count;
        this.bacnet_status = bacnet_status;
        this.lorawan_count = lorawan_count;
        this.lorawan_status = lorawan_status;
        this.disruptive_count = disruptive_count;
        this.disruptive_status = disruptive_status;
        this.my_devices_count = my_devices_count;
        this.my_devices_status = my_devices_status;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.monnit_count = monnit_count;
        this.monnit_status = monnit_status;
        this.pelican_count = pelican_count;
        this.pelican_status = pelican_status;
        this.knx_count = knx_count;
        this.knx_status = knx_status;
        this.subsystem_parent_id = subsystem_parent_id;
        this.subsystem_count = subsystem_count;
        this.custom_fields = custom_fields;
        this.description = description;
        this.asset_match_status = asset_match_status;
        this.matched_product_ids = matched_product_ids;
        this.latitude = latitude;
        this.longitude = longitude;
        this.measuring_instrument_count = measuring_instrument_count;
        this.document_count = document_count;
        this.media_count = media_count;
        this.checklist_template_count = checklist_template_count;
        this.snmp_object_count = snmp_object_count;
        this.snmp_object_status = snmp_object_status;
        this.position = position;
        this.measuring_instrument_status = measuring_instrument_status;
        this.record_checklist_count = record_checklist_count;
        this.record_checklist_status = record_checklist_status;
        this.daintree_count = daintree_count;
        this.daintree_status = daintree_status;
        this.asset_image_url = asset_image_url;
        this.created_timestamp = created_timestamp;
        this.ecobee_count = ecobee_count;
        this.ecobee_status = ecobee_status;
        this.modbus_count = modbus_count;
        this.modbus_status = modbus_status;
        this.floor_id = floor_id;
        this.created_email = created_email;
        this.asset_group = asset_group;
        this.building_id = building_id;
        this.updated_email = updated_email;
        this.updated_timestamp = updated_timestamp;
        this.onboard_status = onboard_status;
        this.device_onboard_status_id = device_onboard_status_id;
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.asset_ocr_image_url = asset_ocr_image_url;
        this.category = category;
        this.sub_category = sub_category;
        this.location_status = location_status;
        this.digital_twin_image_url = digital_twin_image_url;
        this.poly_lens_count = poly_lens_count;
        this.cost_value = cost_value;
        this.assigned_user_email = assigned_user_email;
        this.ai_call = ai_call;
        this.cost_unit = cost_unit;
        this.is_dnd_enabled = is_dnd_enabled;
        this.operational_status = operational_status;
        this.inventory_tracking_id = inventory_tracking_id;
        this.adc_json = adc_json;
        this.system_type_id = system_type_id;
        this.system_type_name = system_type_name;
        this.asset_type_id = asset_type_id;
        this.asset_type_name = asset_type_name;
        this.asset_sub_type_id = asset_sub_type_id;
        this.asset_sub_type_name = asset_sub_type_name;
        this.source_type = source_type;
        this.asset_tag_images_url = asset_tag_images_url;
    }

    //devicenamemapping
    public DeviceDTO(String id, String display_name, String user_data_name) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.user_data_name = user_data_name;
    }

    /**************************Constructor used for Asset Mapper***********************************************/

    public DeviceDTO(String display_name, String mac_address, String model, String vendor, String type, String ip_address, String network_layer, String serial_number, String warranty,
                     String custom_fields, String device_id, String subsystem_parent_id, Integer subsystem_count, String docker_name, String vdms_id) {
        this.user_data_name = display_name;
        this.mac_address = mac_address;
        this.user_data_model = model;
        this.user_data_vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.network_layer = network_layer;
        this.serial_number = serial_number;
        this.warranty = warranty;
        this.custom_fields = custom_fields;
        this.id = device_id;
        this.subsystem_parent_id = subsystem_parent_id;
        this.subsystem_count = subsystem_count;
        this.docker_name = docker_name;
        this.vdms_id = vdms_id;
    }

    ////updated dto for saving asset
    public DeviceDTO(String display_name, String mac_address, String model, String vendor, String type, String ip_address, String network_layer, String serial_number, String warranty,
                     String custom_fields, String device_id, String subsystem_parent_id, String docker_name, String vdms_id) {
        this.user_data_name = display_name;
        this.mac_address = mac_address;
        this.user_data_model = model;
        this.user_data_vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.network_layer = network_layer;
        this.serial_number = serial_number;
        this.warranty = warranty;
        this.custom_fields = custom_fields;
        this.id = device_id;
        this.subsystem_parent_id = subsystem_parent_id;
        this.docker_name = docker_name;
        this.vdms_id = vdms_id;
    }

    /**************************Constructor used for Asset Mapper***********************************************/


    // For vendor name Sync
    public DeviceDTO(String id, String display_name, String mac_address, String vendor, String ip_address,
                     String docker_name) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.ip_address = ip_address;
        this.docker_name = docker_name;
    }

    //devicesyncvirtual
    public DeviceDTO(String id, Integer status, String ip_address, String docker_name, String vdms_id) {
        super();
        this.id = id;
        this.status = status;
        this.ip_address = ip_address;
        this.docker_name = docker_name;
        this.vdms_id = vdms_id;
    }

    //devicelisttopology
    public DeviceDTO(String id, String display_name, String last_seen_on, String mac_address, String vendor,
                     String model, String ip_address, String location, String user_data_vendor, String user_data_name,
                     String parent, String snmp_parent, String docker_name, String building, String floor, Integer status,
                     Integer virtual_device_type, String type) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.last_seen_on = last_seen_on;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.ip_address = ip_address;
        this.location = location;
        this.user_data_vendor = user_data_vendor;
        this.user_data_name = user_data_name;
        this.parent = parent;
        this.snmp_parent = snmp_parent;
        this.docker_name = docker_name;
        this.building = building;
        this.floor = floor;
        this.status = status;
        this.virtual_device_type = virtual_device_type;
        this.type = type;
    }

    public DeviceDTO(String id, String position) {
        super();
        this.id = id;
        this.position = position;
    }

    //devicelistbytypesintegration
    public DeviceDTO(String id, String display_name, String location, String docker_name) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.location = location;
        this.docker_name = docker_name;
    }

    //parentdevicemapping
    public DeviceDTO(String id, String display_name, String ip_address, Integer status, String type, String vendor, String mac_address,
                     String location, String docker_name, String latitude, String longitude, String warranty, String building, String floor, String model, String serial_number, String custom_fields, String asset_group) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.ip_address = ip_address;
        this.status = status;
        this.type = type;
        this.vendor = vendor;
        this.mac_address = mac_address;
        this.location = location;
        this.docker_name = docker_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.warranty = warranty;
        this.building = building;
        this.floor = floor;
        this.model = model;
        this.serial_number = serial_number;
        this.custom_fields = custom_fields;
        this.asset_group = asset_group;
    }

    /*****************************Snmp Devices DTO - used for Snmp Device Discovery**********************************/
    //devicedetailsmapping
    public DeviceDTO(String id, Integer status, String name, String mac_address,
                     String vendor, String model, String type, String ip_address, String location,
                     String vdms_id, String docker_name, String building, String floor, String asset_group) {
        super();
        this.id = id;
        this.status = status;
        this.name = name;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.location = location;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.building = building;
        this.floor = floor;
        this.asset_group = asset_group;
    }

    public DeviceDTO(String id, Integer status, String name, String mac_address,
                     String vendor, String model, String type, String ip_address, String location,
                     String vdms_id, String docker_name, String building, String floor, String asset_group,
                     Integer is_added, String device_id) {
        super();
        this.id = id;
        this.status = status;
        this.name = name;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.location = location;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.building = building;
        this.floor = floor;
        this.asset_group = asset_group;
        this.is_added = is_added;
        this.device_id = device_id;
    }

    //isaddeddevicemapping - Get all devices with is_added
    public DeviceDTO(String id, String display_name, String ip_address, Integer status, String type, String vendor, String mac_address,
                     String location, String docker_name, String latitude, String longitude, String warranty, String building,
                     String floor, String model, String serial_number, String custom_fields, Integer is_added, String asset_group) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.ip_address = ip_address;
        this.status = status;
        this.type = type;
        this.vendor = vendor;
        this.mac_address = mac_address;
        this.location = location;
        this.docker_name = docker_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.warranty = warranty;
        this.building = building;
        this.floor = floor;
        this.model = model;
        this.serial_number = serial_number;
        this.custom_fields = custom_fields;
        this.is_added = is_added;
        this.asset_group = asset_group;
    }

    //getfilterdevicesmapping - get filter virtual devices
    public DeviceDTO(String id, String display_name, String ip_address, Integer status, String type, String vendor, String mac_address,
                     String location, String docker_name, String latitude, String longitude, String warranty, String building,
                     String floor, String model, String serial_number, Integer virtual_device_type, String custom_fields, String asset_group) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.ip_address = ip_address;
        this.status = status;
        this.type = type;
        this.vendor = vendor;
        this.mac_address = mac_address;
        this.location = location;
        this.docker_name = docker_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.warranty = warranty;
        this.building = building;
        this.floor = floor;
        this.model = model;
        this.serial_number = serial_number;
        this.virtual_device_type = virtual_device_type;
        this.custom_fields = custom_fields;
        this.asset_group = asset_group;


    }

    //devicedetailsfortopologymapping - get devices for topology
    public DeviceDTO(String id, Integer status, String name, String mac_address,
                     String vendor, String model, String type, String ip_address, String location,
                     String vdms_id, String docker_name, String building, String floor, Integer virtual_device_type, String asset_group) {
        super();
        this.id = id;
        this.status = status;
        this.name = name;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.location = location;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.building = building;
        this.floor = floor;
        this.virtual_device_type = virtual_device_type;
        this.asset_group = asset_group;
    }

    //devicesbylocationid
    public DeviceDTO(String id, Integer onboard_status, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status) {
        this.id = id;
        this.onboard_status = onboard_status;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
    }

    //deviceonboardmapping
    public DeviceDTO(String id, Integer status, String display_name, String last_seen_on, String mac_address,
                     String vendor, String model, String type, String ip_address, Integer monitor, String location,
                     String network_layer, String user_data_model, String user_data_vendor,
                     String user_data_name, String parent, String snmp_parent, String vdms_id, String docker_name,
                     String system_type, Integer remote_access, String building, String floor,
                     String local_vendor_id, String global_vendor_id, String other_vendor_1_id, String other_vendor_2_id,
                     String other_vendor_3_id, String product_id, String alarm, Integer virtual_device_type,
                     String warranty, String quick_link_name, String quick_link_url, String location_id, Integer email_alert, Integer sms_alert, Integer popup_notification,
                     Integer snmp_count, String snmp_status, Integer interface_count, Integer notes_count, Integer ticket_count, String ticket_status,
                     String serial_number, Integer bacnet_count, String bacnet_status, Integer lorawan_count, String lorawan_status,
                     Integer disruptive_count, String disruptive_status, Integer my_devices_count, String my_devices_status,
                     Integer local_vendor_email_alert, Integer local_vendor_sms_alert, Integer monnit_count, String monnit_status,
                     Integer pelican_count, String pelican_status, Integer knx_count, String knx_status, String subsystem_parent_id, Integer subsystem_count,
                     String custom_fields, String description, Integer asset_match_status, String matched_product_ids, String latitude, String longitude,
                     Integer measuring_instrument_count, Integer document_count, Integer media_count, Integer checklist_template_count,
                     Integer snmp_object_count, String snmp_object_status, String position, String measuring_instrument_status, Integer record_checklist_count,
                     String record_checklist_status, Integer daintree_count, String daintree_status,
                     String asset_image_url, BigInteger created_timestamp, Integer ecobee_count, String ecobee_status,
                     Integer modbus_count, String modbus_status, String floor_id, String created_email, String asset_group, String building_id,
                     String updated_email, BigInteger updated_timestamp, Integer onboard_status, String assignee_email, Integer image_status,
                     Integer geolocation_status, Integer tag_status, Integer field_status, String asset_ocr_image_url, String category, String sub_category, String location_status,
                     String device_onboard_status_id, Integer poly_lens_count) {
        super();
        this.id = id;
        this.status = status;
        this.display_name = display_name;
        this.last_seen_on = last_seen_on;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.monitor = monitor;
        this.location = location;
        this.network_layer = network_layer;
        this.user_data_model = user_data_model;
        this.user_data_vendor = user_data_vendor;
        this.user_data_name = user_data_name;
        this.parent = parent;
        this.snmp_parent = snmp_parent;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.system_type = system_type;
        this.remote_access = remote_access;
        this.building = building;
        this.floor = floor;
        this.local_vendor_id = local_vendor_id;
        this.global_vendor_id = global_vendor_id;
        this.other_vendor_1_id = other_vendor_1_id;
        this.other_vendor_2_id = other_vendor_2_id;
        this.other_vendor_3_id = other_vendor_3_id;
        this.product_id = product_id;
        this.alarm = alarm;
        this.virtual_device_type = virtual_device_type;
        this.warranty = warranty;
        this.quick_link_name = quick_link_name;
        this.quick_link_url = quick_link_url;
        this.location_id = location_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.popup_notification = popup_notification;
        this.snmp_count = snmp_count;
        this.snmp_status = snmp_status;
        this.interface_count = interface_count;
        this.notes_count = notes_count;
        this.ticket_count = ticket_count;
        this.ticket_status = ticket_status;
        this.serial_number = serial_number;
        this.bacnet_count = bacnet_count;
        this.bacnet_status = bacnet_status;
        this.lorawan_count = lorawan_count;
        this.lorawan_status = lorawan_status;
        this.disruptive_count = disruptive_count;
        this.disruptive_status = disruptive_status;
        this.my_devices_count = my_devices_count;
        this.my_devices_status = my_devices_status;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.monnit_count = monnit_count;
        this.monnit_status = monnit_status;
        this.pelican_count = pelican_count;
        this.pelican_status = pelican_status;
        this.knx_count = knx_count;
        this.knx_status = knx_status;
        this.subsystem_parent_id = subsystem_parent_id;
        this.subsystem_count = subsystem_count;
        this.custom_fields = custom_fields;
        this.description = description;
        this.asset_match_status = asset_match_status;
        this.matched_product_ids = matched_product_ids;
        this.latitude = latitude;
        this.longitude = longitude;
        this.measuring_instrument_count = measuring_instrument_count;
        this.document_count = document_count;
        this.media_count = media_count;
        this.checklist_template_count = checklist_template_count;
        this.snmp_object_count = snmp_object_count;
        this.snmp_object_status = snmp_object_status;
        this.position = position;
        this.measuring_instrument_status = measuring_instrument_status;
        this.record_checklist_count = record_checklist_count;
        this.record_checklist_status = record_checklist_status;
        this.daintree_count = daintree_count;
        this.daintree_status = daintree_status;
        this.asset_image_url = asset_image_url;
        this.created_timestamp = created_timestamp;
        this.ecobee_count = ecobee_count;
        this.ecobee_status = ecobee_status;
        this.modbus_count = modbus_count;
        this.modbus_status = modbus_status;
        this.floor_id = floor_id;
        this.created_email = created_email;
        this.asset_group = asset_group;
        this.building_id = building_id;
        this.updated_email = updated_email;
        this.updated_timestamp = updated_timestamp;
        this.onboard_status = onboard_status;
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.asset_ocr_image_url = asset_ocr_image_url;
        this.category = category;
        this.sub_category = sub_category;
        this.location_status = location_status;
        this.device_onboard_status_id = device_onboard_status_id;
        this.poly_lens_count = poly_lens_count;
    }

    public DeviceDTO(String id, Integer status, String display_name, String last_seen_on, String mac_address,
                     String vendor, String model, String type, String ip_address, Integer monitor, String location,
                     String network_layer, String user_data_model, String user_data_vendor,
                     String user_data_name, String parent, String snmp_parent, String vdms_id, String docker_name,
                     String system_type, Integer remote_access, String building, String floor,
                     String local_vendor_id, String global_vendor_id, String other_vendor_1_id, String other_vendor_2_id,
                     String other_vendor_3_id, String product_id, String alarm, Integer virtual_device_type,
                     String warranty, String quick_link_name, String quick_link_url, String location_id, Integer email_alert, Integer sms_alert, Integer popup_notification,
                     Integer snmp_count, String snmp_status, Integer interface_count, Integer notes_count, Integer ticket_count, String ticket_status,
                     String serial_number, Integer bacnet_count, String bacnet_status, Integer lorawan_count, String lorawan_status,
                     Integer disruptive_count, String disruptive_status, Integer my_devices_count, String my_devices_status,
                     Integer local_vendor_email_alert, Integer local_vendor_sms_alert, Integer monnit_count, String monnit_status,
                     Integer pelican_count, String pelican_status, Integer knx_count, String knx_status, String subsystem_parent_id, Integer subsystem_count,
                     String custom_fields, String description, Integer asset_match_status, String matched_product_ids, String latitude, String longitude,
                     Integer measuring_instrument_count, Integer document_count, Integer media_count, Integer checklist_template_count,
                     Integer snmp_object_count, String snmp_object_status, String position, String measuring_instrument_status, Integer record_checklist_count,
                     String record_checklist_status, Integer daintree_count, String daintree_status,
                     String asset_image_url, BigInteger created_timestamp, Integer ecobee_count, String ecobee_status,
                     Integer modbus_count, String modbus_status, String floor_id, String created_email, String asset_group, String building_id,
                     String updated_email, BigInteger updated_timestamp, Integer onboard_status, String assignee_email, Integer image_status,
                     Integer geolocation_status, Integer tag_status, Integer field_status, String asset_ocr_image_url, String category, String sub_category, String location_status,
                     String device_onboard_status_id, Integer poly_lens_count,String adc_json,String system_type_id, String system_type_name, String asset_type_id, String asset_type_name, String asset_sub_type_id, String asset_sub_type_name, String source_type) {
        super();
        this.id = id;
        this.status = status;
        this.display_name = display_name;
        this.last_seen_on = last_seen_on;
        this.mac_address = mac_address;
        this.vendor = vendor;
        this.model = model;
        this.type = type;
        this.ip_address = ip_address;
        this.monitor = monitor;
        this.location = location;
        this.network_layer = network_layer;
        this.user_data_model = user_data_model;
        this.user_data_vendor = user_data_vendor;
        this.user_data_name = user_data_name;
        this.parent = parent;
        this.snmp_parent = snmp_parent;
        this.vdms_id = vdms_id;
        this.docker_name = docker_name;
        this.system_type = system_type;
        this.remote_access = remote_access;
        this.building = building;
        this.floor = floor;
        this.local_vendor_id = local_vendor_id;
        this.global_vendor_id = global_vendor_id;
        this.other_vendor_1_id = other_vendor_1_id;
        this.other_vendor_2_id = other_vendor_2_id;
        this.other_vendor_3_id = other_vendor_3_id;
        this.product_id = product_id;
        this.alarm = alarm;
        this.virtual_device_type = virtual_device_type;
        this.warranty = warranty;
        this.quick_link_name = quick_link_name;
        this.quick_link_url = quick_link_url;
        this.location_id = location_id;
        this.email_alert = email_alert;
        this.sms_alert = sms_alert;
        this.popup_notification = popup_notification;
        this.snmp_count = snmp_count;
        this.snmp_status = snmp_status;
        this.interface_count = interface_count;
        this.notes_count = notes_count;
        this.ticket_count = ticket_count;
        this.ticket_status = ticket_status;
        this.serial_number = serial_number;
        this.bacnet_count = bacnet_count;
        this.bacnet_status = bacnet_status;
        this.lorawan_count = lorawan_count;
        this.lorawan_status = lorawan_status;
        this.disruptive_count = disruptive_count;
        this.disruptive_status = disruptive_status;
        this.my_devices_count = my_devices_count;
        this.my_devices_status = my_devices_status;
        this.local_vendor_email_alert = local_vendor_email_alert;
        this.local_vendor_sms_alert = local_vendor_sms_alert;
        this.monnit_count = monnit_count;
        this.monnit_status = monnit_status;
        this.pelican_count = pelican_count;
        this.pelican_status = pelican_status;
        this.knx_count = knx_count;
        this.knx_status = knx_status;
        this.subsystem_parent_id = subsystem_parent_id;
        this.subsystem_count = subsystem_count;
        this.custom_fields = custom_fields;
        this.description = description;
        this.asset_match_status = asset_match_status;
        this.matched_product_ids = matched_product_ids;
        this.latitude = latitude;
        this.longitude = longitude;
        this.measuring_instrument_count = measuring_instrument_count;
        this.document_count = document_count;
        this.media_count = media_count;
        this.checklist_template_count = checklist_template_count;
        this.snmp_object_count = snmp_object_count;
        this.snmp_object_status = snmp_object_status;
        this.position = position;
        this.measuring_instrument_status = measuring_instrument_status;
        this.record_checklist_count = record_checklist_count;
        this.record_checklist_status = record_checklist_status;
        this.daintree_count = daintree_count;
        this.daintree_status = daintree_status;
        this.asset_image_url = asset_image_url;
        this.created_timestamp = created_timestamp;
        this.ecobee_count = ecobee_count;
        this.ecobee_status = ecobee_status;
        this.modbus_count = modbus_count;
        this.modbus_status = modbus_status;
        this.floor_id = floor_id;
        this.created_email = created_email;
        this.asset_group = asset_group;
        this.building_id = building_id;
        this.updated_email = updated_email;
        this.updated_timestamp = updated_timestamp;
        this.onboard_status = onboard_status;
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.asset_ocr_image_url = asset_ocr_image_url;
        this.category = category;
        this.sub_category = sub_category;
        this.location_status = location_status;
        this.device_onboard_status_id = device_onboard_status_id;
        this.poly_lens_count = poly_lens_count;
        this.adc_json = adc_json;
        this.system_type_id = system_type_id;
        this.system_type_name = system_type_name;
        this.asset_type_id = asset_type_id;
        this.asset_type_name = asset_type_name;
        this.asset_sub_type_id = asset_sub_type_id;
        this.asset_sub_type_name = asset_sub_type_name;
        this.source_type = source_type;
    }

    //recordcheckliststatuscountdevicemapping
    public DeviceDTO(String record_checklist_status, Integer record_checklist_count, String device_id) {
        this.record_checklist_status = record_checklist_status;
        this.record_checklist_count = record_checklist_count;
        this.device_id = device_id;
    }

    //deviceDetailsMapping
    public DeviceDTO(String id, Integer status, String vendor, String model) {
        this.id = id;
        this.status = status;
        this.vendor = vendor;
        this.model = model;
    }

    //devicedetailsfornativeticketmapping
    public DeviceDTO(String id, String name, String model, String vendor, String type,
                     String ip_address, String mac_address, String serial_number,
                     String building, String floor, String location, String docker_name,
                     String category) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.vendor = vendor;
        this.type = type;
        this.ip_address = ip_address;
        this.mac_address = mac_address;
        this.serial_number = serial_number;
        this.building = building;
        this.floor = floor;
        this.location = location;
        this.docker_name = docker_name;
        this.category = category;
    }

    //deviceAiCallFlowMapping
    public DeviceDTO(String id, String name, String docker_name, Boolean ai_call, Integer is_added) {
        super();
        this.id = id;
        this.name = name;
        this.docker_name = docker_name;
        this.ai_call = ai_call;
        this.is_added = is_added;
    }

    //deviceforiaqMapping - IAQ value
    public DeviceDTO(String id,String asset_group,String docker_name,String location_id,String user_data_name,String display_name,Integer onboard_status){
        this.id = id;
        this.asset_group=asset_group;
        this.docker_name=docker_name;
        this.location_id=location_id;
        this.user_data_name=user_data_name;
        this.display_name=display_name;
        this.onboard_status=onboard_status;

    }

    //devicedetailesforcallstatusmapping
    public DeviceDTO(
            String id,
            String alarm,
            String user_data_name,
            String display_name,
            String ip_address,
            String mac_address,
            String last_seen_on,
            String model,
            String user_data_model,
            String user_data_vendor,
            String vendor,
            String warranty,
            String serial_number,
            String docker_name,
            String parent,
            String description,
            String category,
            String sub_category,
            String docker_vdms_id,
            String building_id,
            String building,
            String floor_id,
            String floor,
            String location_id,
            String location
    ) {
        this.id = id;
        this.alarm = alarm;
        this.user_data_name = user_data_name;
        this.display_name = display_name;
        this.ip_address = ip_address;
        this.mac_address = mac_address;
        this.last_seen_on = last_seen_on;
        this.model = model;
        this.user_data_model = user_data_model;
        this.user_data_vendor = user_data_vendor;
        this.vendor = vendor;
        this.warranty = warranty;
        this.serial_number = serial_number;
        this.docker_name = docker_name;
        this.parent = parent;
        this.description = description;
        this.category = category;
        this.sub_category = sub_category;
        this.docker_vdms_id = docker_vdms_id;
        this.building_id = building_id;
        this.building = building;
        this.floor_id = floor_id;
        this.floor = floor;
        this.location_id = location_id;
        this.location = location;
    }

    //deviceAiCallJobSchedulerMapping
    public DeviceDTO(Integer status, Boolean ai_call) {
        this.status = status;
        this.ai_call = ai_call;
    }

    //dndDevicesMapping
    public DeviceDTO(String id, BigInteger dnd_timestamp, Boolean is_dnd_enabled,Boolean system_dnd_enabled) {
        this.id = id;
        this.dnd_timestamp = dnd_timestamp;
        this.is_dnd_enabled = is_dnd_enabled;
        this.system_dnd_enabled = system_dnd_enabled;
    }

    //deviceCustomDetailsMapping
    public DeviceDTO(String id, String ip_address, String display_name, int status) {
        this.id = id;
        this.ip_address = ip_address;
        this.display_name = display_name;
        this.status = status;
    }

    public Boolean getIs_agent_device() {
        return is_agent_device;
    }

    public void setIs_agent_device(Boolean is_agent_device) {
        this.is_agent_device = is_agent_device;
    }

    public String getPc_location() {
        return pc_location;
    }

    public void setPc_location(String pc_location) {
        this.pc_location = pc_location;
    }

    public String getIsp_location() {
        return isp_location;
    }

    public void setIsp_location(String isp_location) {
        this.isp_location = isp_location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAssigned_user_email() {
        return assigned_user_email;
    }

    public void setAssigned_user_email(String assigned_user_email) {
        this.assigned_user_email = assigned_user_email;
    }

    public Boolean getAi_call() {
        return ai_call;
    }

    public void setAi_call(Boolean ai_call) {
        this.ai_call = ai_call;
    }

    public Integer getBarcode_count() {
        return barcode_count;
    }

    public void setBarcode_count(Integer barcode_count) {
        this.barcode_count = barcode_count;
    }

    public BuildingDTO getBuilding_object() {
        return building_object;
    }

    public void setBuilding_object(BuildingDTO building_object) {
        this.building_object = building_object;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getLast_seen_on() {
        return last_seen_on;
    }

    public void setLast_seen_on(String last_seen_on) {
        this.last_seen_on = last_seen_on;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public Integer getMonitor() {
        return monitor;
    }

    public void setMonitor(Integer monitor) {
        this.monitor = monitor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNetwork_layer() {
        return network_layer;
    }

    public void setNetwork_layer(String network_layer) {
        this.network_layer = network_layer;
    }

    public String getUser_data_model() {
        return user_data_model;
    }

    public void setUser_data_model(String user_data_model) {
        this.user_data_model = user_data_model;
    }

    public String getUser_data_vendor() {
        return user_data_vendor;
    }

    public void setUser_data_vendor(String user_data_vendor) {
        this.user_data_vendor = user_data_vendor;
    }

    public String getUser_data_name() {
        return user_data_name;
    }

    public void setUser_data_name(String user_data_name) {
        this.user_data_name = user_data_name;
    }

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

    public String getSystem_type() {
        return system_type;
    }

    public void setSystem_type(String system_type) {
        this.system_type = system_type;
    }
//	public Boolean getIs_virtual() {
//		return is_virtual;
//	}
//	public void setIs_virtual(Boolean is_virtual) {
//		this.is_virtual = is_virtual;
//	}

    public Integer getRemote_access() {
        return remote_access;
    }

    public void setRemote_access(Integer remote_access) {
        this.remote_access = remote_access;
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

    public String getLocal_vendor_id() {
        return local_vendor_id;
    }

    public void setLocal_vendor_id(String local_vendor_id) {
        this.local_vendor_id = local_vendor_id;
    }

    public String getGlobal_vendor_id() {
        return global_vendor_id;
    }

    public void setGlobal_vendor_id(String global_vendor_id) {
        this.global_vendor_id = global_vendor_id;
    }

    public String getOther_vendor_1_id() {
        return other_vendor_1_id;
    }

    public void setOther_vendor_1_id(String other_vendor_1_id) {
        this.other_vendor_1_id = other_vendor_1_id;
    }

    public String getOther_vendor_2_id() {
        return other_vendor_2_id;
    }

    public void setOther_vendor_2_id(String other_vendor_2_id) {
        this.other_vendor_2_id = other_vendor_2_id;
    }

    public String getOther_vendor_3_id() {
        return other_vendor_3_id;
    }

    public void setOther_vendor_3_id(String other_vendor_3_id) {
        this.other_vendor_3_id = other_vendor_3_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
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

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
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

    public String getSnmp_status() {
        return snmp_status;
    }

    public void setSnmp_status(String snmp_status) {
        this.snmp_status = snmp_status;
    }

    public Integer getNotes_count() {
        return notes_count;
    }

    public void setNotes_count(Integer notes_count) {
        this.notes_count = notes_count;
    }

    public Integer getInterface_count() {
        return interface_count;
    }

    public void setInterface_count(Integer interface_count) {
        this.interface_count = interface_count;
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

    public String getSnmp_parent_index() {
        return snmp_parent_index;
    }

    public void setSnmp_parent_index(String snmp_parent_index) {
        this.snmp_parent_index = snmp_parent_index;
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

    public List<InterfaceDTO> getPorts() {
        return ports;
    }

    public void setPorts(List<InterfaceDTO> ports) {
        this.ports = ports;
    }

    public List<DeviceIPAddressDTO> getIp_addresses() {
        return ip_addresses;
    }

    public void setIp_addresses(List<DeviceIPAddressDTO> ip_addresses) {
        this.ip_addresses = ip_addresses;
    }

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

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    public String getSubsystem_parent_id() {
        return subsystem_parent_id;
    }

    public void setSubsystem_parent_id(String subsystem_parent_id) {
        this.subsystem_parent_id = subsystem_parent_id;
    }

    public String getPrevious_subsystem_parent_id() {
        return previous_subsystem_parent_id;
    }

    public void setPrevious_subsystem_parent_id(String previous_subsystem_parent_id) {
        this.previous_subsystem_parent_id = previous_subsystem_parent_id;
    }

    public Integer getSubsystem_count() {
        return subsystem_count;
    }

    public void setSubsystem_count(Integer subsystem_count) {
        this.subsystem_count = subsystem_count;
    }

    public Set<DeviceDTO> getSubsystems() {
        return subsystems;
    }

    public void setSubsystems(Set<DeviceDTO> subsystems) {
        this.subsystems = subsystems;
    }

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

    public Integer getMeasuring_instrument_count() {
        return measuring_instrument_count;
    }

    public void setMeasuring_instrument_count(Integer measuring_instrument_count) {
        this.measuring_instrument_count = measuring_instrument_count;
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

    public Integer getMatched_score() {
        return matched_score;
    }

    public void setMatched_score(Integer matched_score) {
        this.matched_score = matched_score;
    }

    public String getMatched_column() {
        return matched_column;
    }

    public void setMatched_column(String matched_column) {
        this.matched_column = matched_column;
    }

    public String getMatched_info() {
        return matched_info;
    }

    public void setMatched_info(String matched_info) {
        this.matched_info = matched_info;
    }

    public String getSubsystem_parent_name() {
        return subsystem_parent_name;
    }

    public void setSubsystem_parent_name(String subsystem_parent_name) {
        this.subsystem_parent_name = subsystem_parent_name;
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

    public String getMeasuring_instrument_status() {
        return measuring_instrument_status;
    }

    public void setMeasuring_instrument_status(String measuring_instrument_status) {
        this.measuring_instrument_status = measuring_instrument_status;
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

    public String getSnmp_parent_name() {
        return snmp_parent_name;
    }

    public void setSnmp_parent_name(String snmp_parent_name) {
        this.snmp_parent_name = snmp_parent_name;
    }

    public Integer getIs_added() {
        return is_added;
    }

    public void setIs_added(Integer is_added) {
        this.is_added = is_added;
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

    public List<SpecificationsDTO> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<SpecificationsDTO> specifications) {
        this.specifications = specifications;
    }

    public Integer getNfc_count() {
        return nfc_count;
    }

    public void setNfc_count(Integer nfc_count) {
        this.nfc_count = nfc_count;
    }

    public List<LoadCalculationDTO> getLoad_calculation() {
        return load_calculation;
    }

    public void setLoad_calculation(List<LoadCalculationDTO> load_calculation) {
        this.load_calculation = load_calculation;
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

    public String getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(String floor_id) {
        this.floor_id = floor_id;
    }

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

    public String getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(String building_id) {
        this.building_id = building_id;
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

    public DeviceOnboardStatusDTO getOnboard_data() {
        return onboard_data;
    }

    public void setOnboard_data(DeviceOnboardStatusDTO onboard_data) {
        this.onboard_data = onboard_data;
    }

    public String getAsset_ocr_image_url() {
        return asset_ocr_image_url;
    }

    public void setAsset_ocr_image_url(String asset_ocr_image_url) {
        this.asset_ocr_image_url = asset_ocr_image_url;
    }

    public Integer getImage_status() {
        return image_status;
    }

    public void setImage_status(Integer image_status) {
        this.image_status = image_status;
    }

    public Integer getGeolocation_status() {
        return geolocation_status;
    }

    public void setGeolocation_status(Integer geolocation_status) {
        this.geolocation_status = geolocation_status;
    }

    public String getAssignee_email() {
        return assignee_email;
    }

    public void setAssignee_email(String assignee_email) {
        this.assignee_email = assignee_email;
    }

    public Integer getTag_status() {
        return tag_status;
    }

    public void setTag_status(Integer tag_status) {
        this.tag_status = tag_status;
    }

    public Integer getField_status() {
        return field_status;
    }

    public void setField_status(Integer field_status) {
        this.field_status = field_status;
    }

    public Integer getOnboard_status() {
        return onboard_status;
    }

    public void setOnboard_status(Integer onboard_status) {
        this.onboard_status = onboard_status;
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

    public String getDevice_onboard_status_id() {
        return device_onboard_status_id;
    }

    public void setDevice_onboard_status_id(String device_onboard_status_id) {
        this.device_onboard_status_id = device_onboard_status_id;
    }

    //Get Parent Device by Pagination
//	public DeviceDTO(String id, String name,String ip_address, Integer status, String type, String vendor, String mac_address, 
//			String location, String docker_name) {
//		super();
//		this.id = id;
//		this.name = name;
//		this.ip_address = ip_address;
//		this.status = status;
//		this.type = type;
//		this.vendor = vendor;
//		this.mac_address = mac_address;
//		this.location = location;
//		this.docker_name = docker_name;
//
//	}

    public Integer getDatahoist_count() {
        return datahoist_count;
    }

    public void setDatahoist_count(Integer datahoist_count) {
        this.datahoist_count = datahoist_count;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDigital_twin_image_url() {
        return digital_twin_image_url;
    }

    public void setDigital_twin_image_url(String digital_twin_image_url) {
        this.digital_twin_image_url = digital_twin_image_url;
    }

    public Integer getPoly_lens_count() {
        return poly_lens_count;
    }

    public void setPoly_lens_count(Integer poly_lens_count) {
        this.poly_lens_count = poly_lens_count;
    }

    public Set<MeasuringInstrumentDTO> getMeasuringInstruments() {
        return measuringInstruments;
    }

    public void setMeasuringInstruments(Set<MeasuringInstrumentDTO> measuringInstruments) {
        this.measuringInstruments = measuringInstruments;
    }


    public String getOperational_status() {
        return operational_status;
    }

    public void setOperational_status(String operational_status) {
        this.operational_status = operational_status;
    }

    public String getInventory_tracking_id() {
        return inventory_tracking_id;
    }

    public void setInventory_tracking_id(String inventory_tracking_id) {
        this.inventory_tracking_id = inventory_tracking_id;
    }


    public String getAdc_json() {
        return adc_json;
    }

    public void setAdc_json(String adc_json) {
        this.adc_json = adc_json;
    }

    public String getSystem_type_id() {
        return system_type_id;
    }

    public void setSystem_type_id(String system_type_id) {
        this.system_type_id = system_type_id;
    }

    public String getSystem_type_name() {
        return system_type_name;
    }

    public void setSystem_type_name(String system_type_name) {
        this.system_type_name = system_type_name;
    }

    public String getAsset_type_id() {
        return asset_type_id;
    }

    public void setAsset_type_id(String asset_type_id) {
        this.asset_type_id = asset_type_id;
    }

    public String getAsset_type_name() {
        return asset_type_name;
    }

    public void setAsset_type_name(String asset_type_name) {
        this.asset_type_name = asset_type_name;
    }

    public String getAsset_sub_type_id() {
        return asset_sub_type_id;
    }

    public void setAsset_sub_type_id(String asset_sub_type_id) {
        this.asset_sub_type_id = asset_sub_type_id;
    }

    public String getAsset_sub_type_name() {
        return asset_sub_type_name;
    }

    public void setAsset_sub_type_name(String asset_sub_type_name) {
        this.asset_sub_type_name = asset_sub_type_name;
    }

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }


    @Override
    public String toString() {
        return "DeviceDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", display_name='" + display_name + '\'' +
                ", last_seen_on='" + last_seen_on + '\'' +
                ", mac_address='" + mac_address + '\'' +
                ", vendor='" + vendor + '\'' +
                ", model='" + model + '\'' +
                ", type='" + type + '\'' +
                ", ip_address='" + ip_address + '\'' +
                ", monitor=" + monitor +
                ", location='" + location + '\'' +
                ", network_layer='" + network_layer + '\'' +
                ", user_data_model='" + user_data_model + '\'' +
                ", user_data_vendor='" + user_data_vendor + '\'' +
                ", user_data_name='" + user_data_name + '\'' +
                ", parent='" + parent + '\'' +
                ", snmp_parent='" + snmp_parent + '\'' +
                ", vdms_id='" + vdms_id + '\'' +
                ", docker_name='" + docker_name + '\'' +
                ", system_type='" + system_type + '\'' +
                ", remote_access=" + remote_access +
                ", building='" + building + '\'' +
                ", floor='" + floor + '\'' +
                ", local_vendor_id='" + local_vendor_id + '\'' +
                ", global_vendor_id='" + global_vendor_id + '\'' +
                ", other_vendor_1_id='" + other_vendor_1_id + '\'' +
                ", other_vendor_2_id='" + other_vendor_2_id + '\'' +
                ", other_vendor_3_id='" + other_vendor_3_id + '\'' +
                ", product_id='" + product_id + '\'' +
                ", alarm='" + alarm + '\'' +
                ", warranty='" + warranty + '\'' +
                ", quick_link_name='" + quick_link_name + '\'' +
                ", quick_link_url='" + quick_link_url + '\'' +
                ", location_id='" + location_id + '\'' +
                ", building_object=" + building_object +
                ", email_alert=" + email_alert +
                ", sms_alert=" + sms_alert +
                ", popup_notification=" + popup_notification +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", snmp_count=" + snmp_count +
                ", snmp_status='" + snmp_status + '\'' +
                ", notes_count=" + notes_count +
                ", interface_count=" + interface_count +
                ", ticket_count=" + ticket_count +
                ", ticket_status='" + ticket_status + '\'' +
                ", bacnet_count=" + bacnet_count +
                ", bacnet_status='" + bacnet_status + '\'' +
                ", lorawan_count=" + lorawan_count +
                ", lorawan_status='" + lorawan_status + '\'' +
                ", disruptive_count=" + disruptive_count +
                ", disruptive_status='" + disruptive_status + '\'' +
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
                ", ecobee_count=" + ecobee_count +
                ", ecobee_status='" + ecobee_status + '\'' +
                ", virtual_device_type=" + virtual_device_type +
                ", serial_number='" + serial_number + '\'' +
                ", snmp_parent_index='" + snmp_parent_index + '\'' +
                ", position='" + position + '\'' +
                ", local_vendor_email_alert=" + local_vendor_email_alert +
                ", local_vendor_sms_alert=" + local_vendor_sms_alert +
                ", ports=" + ports +
                ", ip_addresses=" + ip_addresses +
                ", parent_name='" + parent_name + '\'' +
                ", subsystem_parent_id='" + subsystem_parent_id + '\'' +
                ", previous_subsystem_parent_id='" + previous_subsystem_parent_id + '\'' +
                ", subsystem_count=" + subsystem_count +
                ", subsystems=" + subsystems +
                ", custom_fields='" + custom_fields + '\'' +
                ", description='" + description + '\'' +
                ", asset_match_status=" + asset_match_status +
                ", matched_product_ids='" + matched_product_ids + '\'' +
                ", measuring_instrument_count=" + measuring_instrument_count +
                ", document_count=" + document_count +
                ", media_count=" + media_count +
                ", checklist_template_count=" + checklist_template_count +
                ", matched_score=" + matched_score +
                ", matched_column='" + matched_column + '\'' +
                ", matched_info='" + matched_info + '\'' +
                ", subsystem_parent_name='" + subsystem_parent_name + '\'' +
                ", measuring_instrument_status='" + measuring_instrument_status + '\'' +
                ", record_checklist_status='" + record_checklist_status + '\'' +
                ", record_checklist_count=" + record_checklist_count +
                ", daintree_count=" + daintree_count +
                ", daintree_status='" + daintree_status + '\'' +
                ", qrcode_count=" + qrcode_count +
                ", asset_image_url='" + asset_image_url + '\'' +
                ", created_timestamp=" + created_timestamp +
                ", snmp_parent_name='" + snmp_parent_name + '\'' +
                ", is_added=" + is_added +
                ", nfc_count=" + nfc_count +
                ", modbus_count=" + modbus_count +
                ", modbus_status='" + modbus_status + '\'' +
                ", floor_id='" + floor_id + '\'' +
                ", created_email='" + created_email + '\'' +
                ", asset_group='" + asset_group + '\'' +
                ", building_id='" + building_id + '\'' +
                ", updated_email='" + updated_email + '\'' +
                ", updated_timestamp=" + updated_timestamp +
                ", onboard_status=" + onboard_status +
                ", specifications=" + specifications +
                ", load_calculation=" + load_calculation +
                ", onboard_data=" + onboard_data +
                ", asset_ocr_image_url='" + asset_ocr_image_url + '\'' +
                ", location_status='" + location_status + '\'' +
                ", image_status=" + image_status +
                ", geolocation_status=" + geolocation_status +
                ", assignee_email='" + assignee_email + '\'' +
                ", tag_status=" + tag_status +
                ", field_status=" + field_status +
                ", category='" + category + '\'' +
                ", sub_category='" + sub_category + '\'' +
                ", poly_lens_count='" + poly_lens_count + '\'' +
                ", cost_value=" + cost_value +
                ", assigned_user_email='" + assigned_user_email + '\'' +
                ", ai_call=" + ai_call +
                ", cost_unit='" + cost_unit + '\'' +
                ", is_dnd_enabled=" + is_dnd_enabled +
                ", operational_status='" + operational_status + '\'' +
                '}';
    }

    public String getDocker_vdms_id() {
        return docker_vdms_id;
    }

    public void setDocker_vdms_id(String docker_vdms_id) {
        this.docker_vdms_id = docker_vdms_id;
    }

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


    public String getAsset_tag_images_url() {
        return asset_tag_images_url;
    }

    public void setAsset_tag_images_url(String asset_tag_images_url) {
        this.asset_tag_images_url = asset_tag_images_url;
    }
}
