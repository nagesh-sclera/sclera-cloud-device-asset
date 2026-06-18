-- Seed for DeviceOnboardStatusAssigneeRepositoryIT.
-- FK chain: device -> device_onboard_status -> device_onboard_status_assignee.
INSERT INTO device (id) VALUES ('deva1');

INSERT INTO device_onboard_status (id, device_id) VALUES
  ('dos-a1', 'deva1'),
  ('dos-a2', null);

INSERT INTO device_onboard_status_assignee (id, type, email, device_onboard_status_id) VALUES
  ('asn1', 'ADMIN',   'admin@x.com',  'dos-a1'),
  ('asn2', 'VIEWER',  'viewer@x.com', 'dos-a1'),
  ('asn3', 'ADMIN',   'admin@x.com',  'dos-a2');
