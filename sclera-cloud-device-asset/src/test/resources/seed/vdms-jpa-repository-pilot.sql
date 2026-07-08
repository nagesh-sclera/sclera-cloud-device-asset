-- Seed data for VdmsJpaRepositoryIT.
INSERT INTO vdms (id, property_name) VALUES ('v-jpa-1', 'Site One');
INSERT INTO vdms (id, property_name) VALUES ('v-jpa-2', 'Site Two');

-- v-jpa-1 has two buildings and one asset; v-jpa-2 has neither (edge case for zero counts)
INSERT INTO building (id, name, code, vdms_id) VALUES ('b-jpa-1', 'Block A', 'BLK-A', 'v-jpa-1');
INSERT INTO building (id, name, code, vdms_id) VALUES ('b-jpa-2', 'Block B', 'BLK-B', 'v-jpa-1');

INSERT INTO asset (id, display_name, original_keys, vdms_id) VALUES ('a-jpa-1', 'Alpha Pump', '{}', 'v-jpa-1');
