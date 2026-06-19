-- Cleanup for DeviceByIdIT (child rows first to satisfy FK constraints).
DELETE FROM inventory_device WHERE tracking_id = 'trk-7';
DELETE FROM device_onboard_status WHERE id = 'obs-1';
DELETE FROM device WHERE id IN ('dev-1', 'dev-2');
DELETE FROM location WHERE id = 'loc-1';
DELETE FROM floor WHERE id = 'flr-1';
DELETE FROM building WHERE id = 'bld-1';
