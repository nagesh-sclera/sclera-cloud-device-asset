-- Cleanup for DeviceTypesRepositoryIT
DELETE FROM device_types WHERE id IN ('dt-001', 'dt-002', 'dt-003', 'dt-upsert');
