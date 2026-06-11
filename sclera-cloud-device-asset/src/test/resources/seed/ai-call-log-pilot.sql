-- Seed data for AiCallLogRepositoryIT.
-- Dependency order: vdms → technician → device → ai_call_log

INSERT INTO vdms (id, property_name) VALUES ('v1', 'Test Property');

INSERT INTO technician (id, name, email) VALUES ('t1', 'Alice', 'alice@example.com');
INSERT INTO technician (id, name, email) VALUES ('t2', 'Bob',   'bob@example.com');

INSERT INTO device (id, display_name, user_data_name) VALUES ('dev1', 'Device Alpha', 'Alpha');
INSERT INTO device (id, display_name, user_data_name) VALUES ('dev2', 'Device Beta',  'Beta');

-- dev1: two logs — one accepted (older), one non-accepted (newer)
INSERT INTO ai_call_log (id, created_at, issue_type, description, priority, status, is_completed, device_id, technician_id)
VALUES ('acl1', 1000, 'hardware', 'Old accepted call', 'low', 'accepted', true, 'dev1', 't1');

INSERT INTO ai_call_log (id, created_at, issue_type, description, priority, status, is_completed, device_id, technician_id)
VALUES ('acl2', 2000, 'network',  'Latest non-accepted', 'high', 'ongoing', false, 'dev1', 't1');

-- dev2: single non-accepted log with different technician
INSERT INTO ai_call_log (id, created_at, issue_type, description, priority, status, is_completed, device_id, technician_id)
VALUES ('acl3', 3000, 'software', 'Software crash', 'medium', 'ongoing', false, 'dev2', 't2');
