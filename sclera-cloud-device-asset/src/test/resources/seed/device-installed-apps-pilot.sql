-- Seed data for DeviceInstalledAppsRepositoryIT.
-- FK: device_installed_apps.device_specification_id -> device_specification.id (requires device)
-- FK: device_installed_apps.managed_software_id    -> managed_software.id

-- Parent device row (device_specification has a FK to device)
INSERT INTO device (id) VALUES ('dev-dia1') ON CONFLICT DO NOTHING;

-- device_specification rows
INSERT INTO device_specification (id, device_id)
VALUES ('spec-1', 'dev-dia1') ON CONFLICT DO NOTHING;

INSERT INTO device_specification (id, device_id)
VALUES ('spec-2', 'dev-dia1') ON CONFLICT DO NOTHING;

-- managed_software row
INSERT INTO managed_software (id, name) VALUES ('ms-1', 'TestSoftware') ON CONFLICT DO NOTHING;

-- installed apps
-- app-1: spec-1, ms-1, risk_status=0 (compliant)
INSERT INTO device_installed_apps (id, name, device_id, device_specification_id, managed_software_id, risk_status)
VALUES ('app-1', 'AppOne', 'dev-dia1', 'spec-1', 'ms-1', 0);

-- app-2: spec-2, ms-1, risk_status=1 (risky)
INSERT INTO device_installed_apps (id, name, device_id, device_specification_id, managed_software_id, risk_status)
VALUES ('app-2', 'AppTwo', 'dev-dia1', 'spec-2', 'ms-1', 1);

-- app-3: spec-1, ms-1, risk_status=2 (compliant)
INSERT INTO device_installed_apps (id, name, device_id, device_specification_id, managed_software_id, risk_status)
VALUES ('app-3', 'AppThree', 'dev-dia1', 'spec-1', 'ms-1', 2);

-- app-4: no managed software, just device
INSERT INTO device_installed_apps (id, name, device_id, device_specification_id)
VALUES ('app-4', 'AppFour', 'dev-dia1', 'spec-1');
