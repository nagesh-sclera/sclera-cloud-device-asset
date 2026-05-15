package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class FloorQueryRepository {

    public String getQueryForUpsertFloor() {
        return "INSERT INTO floor(" +
                "id, name, building_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), building_id = VALUES(building_id), updated_timestamp = VALUES(updated_timestamp), source_type = VALUES(source_type)";

    }
}
