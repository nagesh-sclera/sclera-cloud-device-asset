-- Seed data for AssetRepositoryIT baseline smoke test.
-- device row first: asset_device_mapping.device_id has a FK to device.
INSERT INTO device (id) VALUES ('d1');

INSERT INTO asset (id, display_name, description, type, network_layer, original_keys, import_type, is_matched, subsystem_count)
VALUES ('a1', 'Alpha Pump', 'desc-a', 'pump', 7, '{}', 'corrigo', false, 0);

INSERT INTO asset (id, display_name, description, type, network_layer, original_keys, import_type, is_matched, subsystem_parent_id, subsystem_count)
VALUES ('a2', 'Beta Valve', 'desc-b', 'valve', 7, '{}', 'corrigo', true, 'a1', 0);

INSERT INTO asset (id, display_name, description, type, network_layer, original_keys, import_type, is_matched, subsystem_count)
VALUES ('a3', 'Gamma Meter', 'desc-g', 'meter', 7, '{}', 'bacnet', false, 0);

INSERT INTO asset_device_mapping (id, asset_id, device_id, match_score)
VALUES ('m1', 'a1', 'd1', 88);
