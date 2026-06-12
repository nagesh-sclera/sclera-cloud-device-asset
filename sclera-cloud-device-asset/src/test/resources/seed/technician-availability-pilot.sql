-- Seed data for TechnicianAvailabilityRepositoryIT
INSERT INTO technician (id, name, department, time_zone) VALUES ('tech-avail-1', 'Bob', 'field', 'UTC');
INSERT INTO technician (id, name, department, time_zone) VALUES ('tech-avail-2', 'Carol', 'field', 'UTC');

INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id)
    VALUES ('avail-1', 1700000000000, 1700086400000, '09:00', '17:00', false, 'weekly', '{"days":["MON"],"exceptions":[]}', 'tech-avail-1');
INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id)
    VALUES ('avail-2', 1700000000000, 1700086400000, '08:00', '16:00', false, 'daily',  '{"days":["TUE"],"exceptions":[]}', 'tech-avail-1');
INSERT INTO technician_availability (id, start_date, end_date, start_time, end_time, is_all_day, frequency, condition, technician_id)
    VALUES ('avail-3', 1700000000000, 1700086400000, '10:00', '18:00', true,  'once',   '{"days":["WED"],"exceptions":[]}', 'tech-avail-2');
