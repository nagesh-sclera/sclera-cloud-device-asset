-- Seed data for LocationHistoryRepositoryIT
-- Location row (FK target for location_history.location_id)
INSERT INTO location (id, name, status, floor_id)
VALUES ('loc-001', 'Room A', 'active', null);

-- Two history entries for loc-001
INSERT INTO location_history (id, status, type, description, updated_timestamp, updated_email, location_id)
VALUES ('lh-001', 'active', 'status_change', 'Activated room', 1700000001000, 'admin@example.com', 'loc-001');

INSERT INTO location_history (id, status, type, description, updated_timestamp, updated_email, location_id)
VALUES ('lh-002', 'inactive', 'status_change', 'Deactivated room', 1700000002000, 'user@example.com', 'loc-001');

-- History for a different location (should not appear in loc-001 queries)
INSERT INTO location (id, name, status, floor_id)
VALUES ('loc-002', 'Room B', 'active', null);

INSERT INTO location_history (id, status, type, description, updated_timestamp, updated_email, location_id)
VALUES ('lh-003', 'active', 'status_change', 'Activated room B', 1700000003000, 'admin@example.com', 'loc-002');
