-- Cleanup for DeviceConditionsRepositoryIT
DELETE FROM device_conditions WHERE id IN ('dc-001', 'dc-002');
DELETE FROM device WHERE id = 'dev-dc-001';
