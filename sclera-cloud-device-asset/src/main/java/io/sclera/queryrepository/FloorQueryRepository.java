package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class FloorQueryRepository {

    public String getQueryForUpsertFloor() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO floor(" +
                "id, name, building_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, building_id = EXCLUDED.building_id, updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";

    }
}
