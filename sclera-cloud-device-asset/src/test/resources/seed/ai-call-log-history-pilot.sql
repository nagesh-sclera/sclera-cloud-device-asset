-- Seed data for AiCallLogHistoryRepositoryIT.
-- Dependency order: vdms → technician → device → ai_call_log → ai_call_log_history

INSERT INTO vdms (id, property_name) VALUES ('v1', 'Test Property');

INSERT INTO technician (id, name, email) VALUES ('t1', 'Alice', 'alice@example.com');
INSERT INTO technician (id, name, email) VALUES ('t2', 'Bob',   'bob@example.com');

-- device required so ai_call_log JPQL navigation (a.device.id) resolves correctly
INSERT INTO device (id, display_name) VALUES ('dev1', 'Test Device');

-- two ai_call_log parent rows
INSERT INTO ai_call_log (id, created_at, issue_type, description, priority, status, is_completed, device_id, technician_id)
VALUES ('acl1', 1000, 'hardware', 'Disk failure', 'high', 'ongoing', false, 'dev1', 't1');

INSERT INTO ai_call_log (id, created_at, issue_type, description, priority, status, is_completed, device_id, technician_id)
VALUES ('acl2', 2000, 'network', 'Link down',  'low',  'ongoing', false, 'dev1', 't2');

-- history entries for acl1 (two states, ordered by created_at)
INSERT INTO ai_call_log_history (id, created_at, description, state, ai_call_log_id, technician_id)
VALUES ('h1', 100, 'Initial call received', 'created',  'acl1', 't1');

INSERT INTO ai_call_log_history (id, created_at, description, state, ai_call_log_id, technician_id)
VALUES ('h2', 200, 'Technician en-route',   'in-progress', 'acl1', 't1');

-- single history entry for acl2 (different technician)
INSERT INTO ai_call_log_history (id, created_at, description, state, ai_call_log_id, technician_id)
VALUES ('h3', 300, 'Network checked',       'resolved',    'acl2', 't2');
