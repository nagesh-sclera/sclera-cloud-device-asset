-- Seed for DeviceRepositoryIT
-- d1 has user_data_name set; d2 does not (tests CASE WHEN fallback)
-- d2 has subsystem_parent_id = d1 (tests subsystem queries)
INSERT INTO device(id, display_name, user_data_name, status, monitor, asset_match_status, subsystem_parent_id)
VALUES ('d1', 'Generic1', 'MyDevice', 1, 1, 1, NULL),
       ('d2', 'Device2',  '',         0, 0, 0, 'd1');
