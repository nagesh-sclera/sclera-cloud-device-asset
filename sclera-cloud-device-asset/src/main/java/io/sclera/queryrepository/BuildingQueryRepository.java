package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class BuildingQueryRepository {

    public String getQueryForUpsertBuilding(){
        return "INSERT INTO building(" +
                "id, name, vdms_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), vdms_id = VALUES(vdms_id), updated_timestamp = VALUES(updated_timestamp), source_type = VALUES(source_type)";

    }
}
