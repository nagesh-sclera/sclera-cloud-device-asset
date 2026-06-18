-- Seed data for AssetFieldRepositoryIT
-- af-001: active, not deleted — returned by getAllAssetFields and getGlobalAssetFields
INSERT INTO asset_field (id, name, type, tool_tip, default_value, is_active, options, is_deleted, show_in_section, created_at)
VALUES ('af-001', 'Location', 'text', 'Physical location', 'Unknown', true, null, false, 1, 1700000001000);

-- af-002: active, not deleted — also returned by getAllAssetFields; name used for getGlobalAssetFields
INSERT INTO asset_field (id, name, type, tool_tip, default_value, is_active, options, is_deleted, show_in_section, created_at)
VALUES ('af-002', 'Vendor', 'text', 'Device vendor', null, true, '["VendorA","VendorB"]', false, 2, 1700000002000);

-- af-003: deleted — NOT returned by getAllAssetFields (is_deleted = true)
INSERT INTO asset_field (id, name, type, tool_tip, default_value, is_active, options, is_deleted, show_in_section, created_at)
VALUES ('af-003', 'OldField', 'text', null, null, false, null, true, 0, 1700000003000);
