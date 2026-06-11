-- Seed for DeviceLifeCycleHistoryRepositoryIT
-- device row first: device_lifecycle_history.device_id is a FK to device
INSERT INTO device (id, display_name) VALUES ('dlc-dev1', 'Lifecycle Test Device');

-- Two history rows for dlc-dev1, oldest first (lower created_timestamp)
INSERT INTO device_lifecycle_history
    (id, operational_status, usage_status, assigned_user_id, assignment_count,
     created_timestamp, assigned_timestamp, device_id, description, assigned_by_user_id)
VALUES
    ('dlc-h1', 'active',   'used', 'user-a', 1, 1000, 900, 'dlc-dev1', 'First assignment',  'admin1'),
    ('dlc-h2', 'inactive', 'used', 'user-b', 2, 2000, 1900, 'dlc-dev1', 'Second assignment', 'admin2');
