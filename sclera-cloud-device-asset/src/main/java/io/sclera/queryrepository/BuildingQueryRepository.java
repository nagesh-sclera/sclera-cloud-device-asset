package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Provides native upsert SQL for persisting building records.
 */
@Component
public class BuildingQueryRepository {

    /**
     * Returns the native SQL statement that inserts or updates a building row.
     *
     * @return the building upsert SQL
     */
    public String getQueryForUpsertBuilding(){
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO building(" +
                "id, name, vdms_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, vdms_id = EXCLUDED.vdms_id, updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";

    }
}
