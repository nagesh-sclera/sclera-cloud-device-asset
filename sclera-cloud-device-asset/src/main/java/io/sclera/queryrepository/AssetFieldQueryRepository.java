package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Provides native upsert SQL for persisting asset field records.
 */
@Component
public class AssetFieldQueryRepository {

    /**
     * Returns the native SQL statement that inserts or updates an asset field row.
     *
     * @return the asset field upsert SQL
     */
    public String getQueryForUpsertAssetField() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO asset_field(" +
                "id, name, type, tool_tip, default_value, is_active, options, is_deleted, show_in_section, created_at " +
                ") VALUES (?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, type = EXCLUDED.type, tool_tip = EXCLUDED.tool_tip, default_value = EXCLUDED.default_value, " +
                "is_active = EXCLUDED.is_active, options = EXCLUDED.options, is_deleted = EXCLUDED.is_deleted, show_in_section = EXCLUDED.show_in_section, created_at = EXCLUDED.created_at ";

    }
}
