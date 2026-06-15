-- Seed data for FloorRepositoryIT.
-- building row is required because Floor.building is @ManyToOne Building (FK building_id).
-- vdms row is required because building.vdms_id has a FK to vdms.
INSERT INTO vdms (id) VALUES ('v-floor-test');

INSERT INTO building (id, name, code, updated_timestamp, source_type, vdms_id)
VALUES ('b-floor-test', 'Test Building', 'TB-001', 1000, 'vdms', 'v-floor-test');

-- f1 and f2 are floors on the test building (with full detail columns for projection tests)
INSERT INTO floor (id, name, initial_position, image_url, angle, path, min_zoom, max_zoom, local_image_url, updated_timestamp, source_type, building_id)
VALUES ('f-floor-1', 'Ground Floor', '{"lat":1.0,"lng":2.0}', 'http://example.com/f1.png', 0, '/path/to/f1', '5', '20', '/local/f1.png', 1717000000, 'vdms', 'b-floor-test');

INSERT INTO floor (id, name, initial_position, image_url, angle, path, min_zoom, max_zoom, local_image_url, updated_timestamp, source_type, building_id)
VALUES ('f-floor-2', 'First Floor', '{"lat":1.1,"lng":2.1}', 'http://example.com/f2.png', 45, '/path/to/f2', '5', '18', '/local/f2.png', 1717000001, 'vdms', 'b-floor-test');

-- f3 has NO associated location → must appear in getUnlinkedFloorIds
INSERT INTO floor (id, name, initial_position, image_url, angle, path, min_zoom, max_zoom, local_image_url, updated_timestamp, source_type, building_id)
VALUES ('f-floor-3', 'Second Floor', '{"lat":1.2,"lng":2.2}', 'http://example.com/f3.png', 90, '/path/to/f3', '5', '16', '/local/f3.png', 1717000002, 'vdms', 'b-floor-test');

-- loc1 references f-floor-1 → f-floor-1 is linked; f-floor-2 and f-floor-3 are unlinked
-- (f-floor-2 also has no location → also unlinked, but f-floor-3 is the explicitly unlinked one per test intent)
INSERT INTO location (id, name, floor_id, updated_timestamp)
VALUES ('loc-floor-1', 'Room A', 'f-floor-1', 1717000010);
