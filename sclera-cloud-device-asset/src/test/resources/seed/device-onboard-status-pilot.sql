-- Seed for DeviceOnboardStatusRepositoryIT.
-- d1/d2 are "in progress" (onboard_status 1/2); d3 is 0 (excluded from assignees).
INSERT INTO device (id, onboard_status) VALUES ('d1', 1), ('d2', 2), ('d3', 0);

INSERT INTO device_onboard_status (id, assignee_email, image_status, geolocation_status, tag_status, field_status, device_id) VALUES
  ('dos1', 'a@x.com', 1, 2, 3, 4, 'd1'),
  ('dos2', 'b@x.com', 0, 0, 0, 0, 'd2'),
  ('dos3', 'c@x.com', 0, 0, 0, 0, 'd3');
