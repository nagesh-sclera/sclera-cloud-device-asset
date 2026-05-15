package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class LocationQueryRepository {
    public String getQueryForUpdateLocationRecordChecklistStatus() {
        return "UPDATE location SET record_checklist_status = ?, record_checklist_count =? WHERE id = ?";
    }

    public String getQueryForUpsertLocation(){
        return "INSERT INTO location(" +
                "id, name, code, floor_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), code = VALUES(code), floor_id = VALUES(floor_id), updated_timestamp = VALUES(updated_timestamp) , source_type = VALUES(source_type)";
    }
}
