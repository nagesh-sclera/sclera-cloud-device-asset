package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class DeviceQueryRepository {

    public String getQueryForUpdateDeviceRecordChecklistStatus() {
        return "UPDATE device SET record_checklist_status = ?, record_checklist_count =? WHERE id = ?";
    }


    public String getQueryForUpsertCollection() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        // PG-port: adc_json is a jsonb column; the bound String must be cast (pgjdbc binds setString as
        //   varchar -> "column is of type jsonb but expression is of type character varying"). Matches the
        //   CAST(? AS jsonb) the AISuggestion native queries already use. (Method currently has no callers.)
        return "INSERT INTO device (" +
                "id, system_type_name, asset_type_name, asset_sub_type_name, adc_json, created_email, assigned_user_email, system_type_id, asset_type_id, asset_sub_type_id, " +
                "location_id, docker_name, type, monitor, docker_vdms_id, virtual_device_type, asset_match_status, created_timestamp, asset_group, onboard_status, category, sub_category, " +
                "location_status, source_type, display_name, model, vendor, serial_number, warranty, description, user_data_name, user_data_model, user_data_vendor " +
                ") VALUES (?,?,?,?,CAST(? AS jsonb),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "system_type_name = EXCLUDED.system_type_name, " +
                "asset_type_name = EXCLUDED.asset_type_name, " +
                "asset_sub_type_name = EXCLUDED.asset_sub_type_name, " +
                "adc_json = EXCLUDED.adc_json, " +
                "created_email = EXCLUDED.created_email, " +
                "assigned_user_email = EXCLUDED.assigned_user_email, " +
                "system_type_id = EXCLUDED.system_type_id, " +
                "asset_type_id = EXCLUDED.asset_type_id, " +
                "asset_sub_type_id = EXCLUDED.asset_sub_type_id, " +
                "location_id = EXCLUDED.location_id, " +
                "docker_name = EXCLUDED.docker_name, " +
                "type = EXCLUDED.type, " +
                "monitor = EXCLUDED.monitor, " +
                "docker_vdms_id = EXCLUDED.docker_vdms_id, " +
                "virtual_device_type = EXCLUDED.virtual_device_type, " +
                "asset_match_status = EXCLUDED.asset_match_status, " +
                "created_timestamp = EXCLUDED.created_timestamp, " +
                "asset_group = EXCLUDED.asset_group, " +
                "onboard_status = EXCLUDED.onboard_status, " +
                "category = EXCLUDED.category, " +
                "sub_category = EXCLUDED.sub_category, " +
                "location_status = EXCLUDED.location_status, " +
                "source_type = EXCLUDED.source_type, " +
                "display_name = EXCLUDED.display_name, " +
                "model = EXCLUDED.model, " +
                "vendor = EXCLUDED.vendor, " +
                "serial_number = EXCLUDED.serial_number, " +
                "warranty = EXCLUDED.warranty, " +
                "description = EXCLUDED.description, " +
                "user_data_name = EXCLUDED.user_data_name, " +
                "user_data_model = EXCLUDED.user_data_model, " +
                "user_data_vendor = EXCLUDED.user_data_vendor";
    }

    public String getQueryForUpdateImage(){
        return " UPDATE device SET asset_image_url = ?, asset_ocr_image_url = ?, asset_tag_images_url = ? WHERE id = ? ";
    }
}
