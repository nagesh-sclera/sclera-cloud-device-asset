-- Seed data for LocationRepositoryIT
-- Two floor rows are required because getLocationIdsByFloorId uses JPQL l.floor.id = ?1
-- which Hibernate translates to an INNER JOIN on the floor table.

INSERT INTO floor (id, name)
VALUES ('f1', 'Floor 1'), ('f2', 'Floor 2');

-- Three locations on two floors; one device linked to loc1 to exercise getUnlinkedLocationIds.
INSERT INTO location (id, name, position, area, type, code, z_index, status, record_checklist_status, record_checklist_count, floor_id, updated_timestamp)
VALUES
  ('loc1', 'Room A',  '{"x":10,"y":20}', '{"w":100,"h":80}', 'office',   'LOC-001', 1, 'active',   'completed', 3, 'f1', 1717000000),
  ('loc2', 'Room B',  '{"x":30,"y":40}', '{"w":120,"h":90}', 'lab',      'LOC-002', 2, 'inactive', 'pending',   0, 'f1', 1717000001),
  ('loc3', 'Hall C',  '{"x":50,"y":60}', '{"w":200,"h":50}', 'corridor', 'LOC-003', 3, 'active',   null,        0, 'f2', 1717000002);

-- Insert a device linked to loc1; loc2 and loc3 remain unlinked.
INSERT INTO device (id, location_id)
VALUES ('dev1', 'loc1');
