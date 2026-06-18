-- Seed data for DeviceTypesRepositoryIT
-- dt-001: current active type (no old_name)
INSERT INTO device_types (id, name, updated_timestamp, old_name)
VALUES ('dt-001', 'Camera', 1700000001000, null);

-- dt-002: recently renamed (old_name set); higher timestamp
INSERT INTO device_types (id, name, updated_timestamp, old_name)
VALUES ('dt-002', 'SmartSwitch', 1700000002000, 'Switch');

-- dt-003: another active type without rename; highest timestamp
INSERT INTO device_types (id, name, updated_timestamp, old_name)
VALUES ('dt-003', 'Sensor', 1700000003000, null);
