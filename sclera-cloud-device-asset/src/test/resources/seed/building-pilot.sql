-- Seed data for BuildingRepositoryIT.
-- vdms row first: building.vdms_id has a FK to vdms.
INSERT INTO vdms (id) VALUES ('v1');

-- b1 and b2 belong to vdms v1; b3 belongs to no vdms (edge case for getUnlinkedBuildingIds)
INSERT INTO building (id, name, code, updated_timestamp, source_type, vdms_id)
VALUES ('b1', 'Block A', 'BLK-A', 1000, 'vdms', 'v1');

INSERT INTO building (id, name, code, updated_timestamp, source_type, vdms_id)
VALUES ('b2', 'Block B', 'BLK-B', 2000, 'vdms', 'v1');

-- b3 has no vdms and no floor → always unlinked
INSERT INTO building (id, name, code, updated_timestamp, source_type, vdms_id)
VALUES ('b3', 'Block C', 'BLK-C', 3000, 'vdms', 'v1');

-- floor f1 references b1 → b1 is linked; b2 and b3 are unlinked
INSERT INTO floor (id, name, building_id)
VALUES ('f1', 'Ground Floor', 'b1');
