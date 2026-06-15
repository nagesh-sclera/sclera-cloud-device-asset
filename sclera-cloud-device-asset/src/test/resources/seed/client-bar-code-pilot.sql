-- Seed data for ClientBarCodeRepositoryIT
-- Pre-existing parent rows (device and location tables already seeded by schema-pg.sql;
-- we insert minimal rows here because @Sql(BEFORE_TEST_METHOD) runs after schema creation)

INSERT INTO device (id, display_name) VALUES ('dev-cbc-1', 'TestDevice1');
INSERT INTO device (id, display_name) VALUES ('dev-cbc-2', 'TestDevice2');

INSERT INTO location (id, name) VALUES ('loc-cbc-1', 'TestLocation1');
INSERT INTO location (id, name) VALUES ('loc-cbc-2', 'TestLocation2');

-- bar codes: some linked to device, some to location, some flagged deleted
INSERT INTO client_bar_code (id, device_id, location_id, is_deleted)
    VALUES ('cbc-1', 'dev-cbc-1', NULL,       false);
INSERT INTO client_bar_code (id, device_id, location_id, is_deleted)
    VALUES ('cbc-2', 'dev-cbc-2', NULL,       false);
INSERT INTO client_bar_code (id, device_id, location_id, is_deleted)
    VALUES ('cbc-3', NULL,        'loc-cbc-1', false);
INSERT INTO client_bar_code (id, device_id, location_id, is_deleted)
    VALUES ('cbc-4', NULL,        'loc-cbc-2', false);
INSERT INTO client_bar_code (id, device_id, location_id, is_deleted)
    VALUES ('cbc-5', 'dev-cbc-1', NULL,       true);
