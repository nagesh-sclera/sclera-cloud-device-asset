DELETE FROM device_installed_apps;
DELETE FROM managed_software WHERE id = 'ms-1';
DELETE FROM device_specification WHERE id IN ('spec-1', 'spec-2');
DELETE FROM device WHERE id = 'dev-dia1';
