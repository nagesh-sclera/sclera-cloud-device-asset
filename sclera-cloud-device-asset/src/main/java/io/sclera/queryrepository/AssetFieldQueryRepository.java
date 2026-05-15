package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class AssetFieldQueryRepository {

    public String getQueryForUpsertAssetField() {
        return "INSERT INTO asset_field(" +
                "id, name, type, tool_tip, default_value, is_active, options, is_deleted, show_in_section, created_at " +
                ") VALUES (?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), type = VALUES(type), tool_tip = VALUES(tool_tip), default_value = VALUES(default_value), " +
                "is_active = VALUES(is_active), options = VALUES(options), is_deleted = VALUES(is_deleted), show_in_section = VALUES(show_in_section), created_at = VALUES(created_at) ";

    }
}
