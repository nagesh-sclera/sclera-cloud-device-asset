package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting and querying {@code floor} records.
 */
@Component
public class FloorQueryRepository {

    /**
     * Returns the SQL for upserting a floor, updating its mutable columns on id conflict.
     *
     * @return the parameterized floor upsert SQL statement
     */
    public String getQueryForUpsertFloor() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO floor(" +
                "id, name, building_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, building_id = EXCLUDED.building_id, updated_timestamp = EXCLUDED.updated_timestamp, source_type = EXCLUDED.source_type";

    }
}
