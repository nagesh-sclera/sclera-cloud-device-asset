-- Seed for DeviceByIdIT.
-- dev-1: fully related device (building -> floor -> location, onboard status, inventory).
-- dev-2: bare device with no location/onboard/inventory/vendor rows (proves every join is LEFT).
INSERT INTO building(id, name) VALUES ('bld-1', 'Tower A');
INSERT INTO floor(id, name, building_id) VALUES ('flr-1', 'Floor 1', 'bld-1');
INSERT INTO location(id, name, floor_id) VALUES ('loc-1', 'Room 101', 'flr-1');

INSERT INTO device(id, display_name, type, snmp_count, location_id)
VALUES ('dev-1', 'Boiler-1', 'hvac', 3, 'loc-1');

INSERT INTO device(id, display_name)
VALUES ('dev-2', 'Bare2');

INSERT INTO device_onboard_status(id, device_id, assignee_email, image_status, geolocation_status, tag_status, field_status)
VALUES ('obs-1', 'dev-1', 'a@x.com', 1, 0, 1, 0);

INSERT INTO inventory_device(tracking_id, device_id)
VALUES ('trk-7', 'dev-1');
