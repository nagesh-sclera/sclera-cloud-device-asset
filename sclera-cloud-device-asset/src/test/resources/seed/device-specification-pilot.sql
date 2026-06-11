-- Seed for DeviceSpecificationRepositoryIT
-- device rows: device_specification.device_id is a FK to device
INSERT INTO device (id, display_name) VALUES ('spec-dev1', 'Spec Test Device 1');
INSERT INTO device (id, display_name) VALUES ('spec-dev2', 'Spec Test Device 2');

-- Two device_specification rows with distinct email and os_type values
INSERT INTO device_specification
    (id, email, os_type, child_devices, device_id)
VALUES
    ('spec-mac1', 'alice@example.com', 'Windows', 'child-a', 'spec-dev1'),
    ('spec-mac2', 'bob@example.com',   'Linux',   'child-b', 'spec-dev2');
