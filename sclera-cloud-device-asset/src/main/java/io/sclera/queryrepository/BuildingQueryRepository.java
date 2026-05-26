package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class BuildingQueryRepository {

    public String getQueryForUpsertBuilding(){
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO building(" +
                "id, name, vdms_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, vdms_id = EXCLUDED.vdms_id, updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";

    }
}
