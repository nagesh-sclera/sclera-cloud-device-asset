-- Seed data for TechnicianAvailabilityTest
--
-- Technician timezone: UTC
-- Availability: Mon-Fri, 09:00-17:00, 2024-01-01 to 2024-12-31
-- No exception days
--
-- "Available" probe:  2024-01-17T12:00:00Z (Wednesday 12:00 UTC)  => epoch ms 1705492800000
-- "Not Available" probe: 2024-01-14T12:00:00Z (Sunday   12:00 UTC)  => epoch ms 1705233600000
--   Sunday is not in days=[MON,TUE,WED,THU,FRI] => NOT AVAILABLE
--
-- start_date epoch: 2024-01-01T00:00:00Z = 1704067200000
-- end_date   epoch: 2024-12-31T00:00:00Z = 1735603200000

INSERT INTO technician (id, name, department, time_zone)
VALUES ('tech-001', 'Alice Engineer', 'IT', 'UTC');

INSERT INTO technician_skill (id, name, type, technician_id)
VALUES ('skill-001', 'Network Diagnostics', 'primary', 'tech-001');

INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id)
VALUES (
    'avail-001',
    1704067200000,
    1735603200000,
    '09:00',
    '17:00',
    false,
    'weekly',
    '{"days":["MON","TUE","WED","THU","FRI"],"exceptions":[]}',
    'tech-001'
);
