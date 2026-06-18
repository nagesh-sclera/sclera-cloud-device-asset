-- Seed for AssetDeviceMappingRepositoryIT.
-- asset.original_keys is NOT NULL; device only needs id. FK targets for asset_device_mapping.
INSERT INTO asset (id, original_keys) VALUES ('a1', '{}'), ('a2', '{}');
INSERT INTO device (id) VALUES ('d1'), ('d2');

INSERT INTO asset_device_mapping (id, asset_id, device_id, match_score) VALUES
  ('m1', 'a1', 'd1', 80),
  ('m2', 'a2', 'd2', 90);
