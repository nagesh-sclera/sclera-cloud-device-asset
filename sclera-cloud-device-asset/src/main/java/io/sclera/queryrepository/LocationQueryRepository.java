package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting and querying {@code location} records.
 */
@Component
public class LocationQueryRepository {
    /**
     * Returns the SQL for updating a location's record checklist status and count by id.
     *
     * @return the parameterized checklist-status update SQL statement
     */
    public String getQueryForUpdateLocationRecordChecklistStatus() {
        return "UPDATE location SET record_checklist_status = ?, record_checklist_count =? WHERE id = ?";
    }

    /**
     * Returns the SQL for upserting a location, updating its mutable columns on id conflict.
     *
     * @return the parameterized location upsert SQL statement
     */
    public String getQueryForUpsertLocation(){
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO location(" +
                "id, name, code, floor_id, updated_timestamp, source_type " +
                ") VALUES (?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, code = EXCLUDED.code, floor_id = EXCLUDED.floor_id, updated_timestamp = EXCLUDED.updated_timestamp , source_type = EXCLUDED.source_type";
    }
}
