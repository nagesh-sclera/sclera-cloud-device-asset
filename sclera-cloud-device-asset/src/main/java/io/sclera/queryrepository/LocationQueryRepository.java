package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class LocationQueryRepository {
    public String getQueryForUpdateLocationRecordChecklistStatus() {
        return "UPDATE location SET record_checklist_status = ?, record_checklist_count =? WHERE id = ?";
    }

    public String getQueryForUpsertLocation(){
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO location(" +
                "id, name, code, floor_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, code = EXCLUDED.code, floor_id = EXCLUDED.floor_id, updated_timestamp = EXCLUDED.updated_timestamp , source_type = EXCLUDED.source_type";
    }
}
