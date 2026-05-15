package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class DeviceQueryRepository {

    public String getQueryForUpdateDeviceRecordChecklistStatus() {
        return "UPDATE device SET record_checklist_status = ?, record_checklist_count =? WHERE id = ?";
    }


    public String getQueryForUpsertCollection() {
        return "INSERT INTO device (" +
                "id, system_type_name, asset_type_name, asset_sub_type_name, adc_json, created_email, assigned_user_email, system_type_id, asset_type_id, asset_sub_type_id, " +
                "location_id, docker_name, type, monitor, docker_vdms_id, virtual_device_type, asset_match_status, created_timestamp, asset_group, onboard_status, category, sub_category, " +
                "location_status, source_type, display_name, model, vendor, serial_number, warranty, description, user_data_name, user_data_model, user_data_vendor " +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "system_type_name = VALUES(system_type_name), " +
                "asset_type_name = VALUES(asset_type_name), " +
                "asset_sub_type_name = VALUES(asset_sub_type_name), " +
                "adc_json = VALUES(adc_json), " +
                "created_email = VALUES(created_email), " +
                "assigned_user_email = VALUES(assigned_user_email), " +
                "system_type_id = VALUES(system_type_id), " +
                "asset_type_id = VALUES(asset_type_id), " +
                "asset_sub_type_id = VALUES(asset_sub_type_id), " +
                "location_id = VALUES(location_id), " +
                "docker_name = VALUES(docker_name), " +
                "type = VALUES(type), " +
                "monitor = VALUES(monitor), " +
                "docker_vdms_id = VALUES(docker_vdms_id), " +
                "virtual_device_type = VALUES(virtual_device_type), " +
                "asset_match_status = VALUES(asset_match_status), " +
                "created_timestamp = VALUES(created_timestamp), " +
                "asset_group = VALUES(asset_group), " +
                "onboard_status = VALUES(onboard_status), " +
                "category = VALUES(category), " +
                "sub_category = VALUES(sub_category), " +
                "location_status = VALUES(location_status), " +
                "source_type = VALUES(source_type), " +
                "display_name = VALUES(display_name), " +
                "model = VALUES(model), " +
                "vendor = VALUES(vendor), " +
                "serial_number = VALUES(serial_number), " +
                "warranty = VALUES(warranty), " +
                "description = VALUES(description), " +
                "user_data_name = VALUES(user_data_name), " +
                "user_data_model = VALUES(user_data_model), " +
                "user_data_vendor = VALUES(user_data_vendor)";
    }

    public String getQueryForUpdateImage(){
        return " UPDATE device SET asset_image_url = ?, asset_ocr_image_url = ?, asset_tag_images_url = ? WHERE id = ? ";
    }
}
