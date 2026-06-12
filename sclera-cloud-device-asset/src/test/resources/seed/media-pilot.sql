-- Seed data for MediaRepositoryIT.
-- device row is required as FK target for device_media.
INSERT INTO device (id, display_name) VALUES ('dev-m1', 'Test Device Media');

-- Two media items; m2 has a NULL description to exercise the COALESCE guard.
INSERT INTO media (id, name, category, description, link, created_email, created_timestamp, extension)
VALUES ('med1', 'Manual A', 'manual', 'First manual', 'http://example.com/a', 'user@test.com', 1000000, 'pdf');

INSERT INTO media (id, name, category, description, link, created_email, created_timestamp, extension)
VALUES ('med2', 'Image B', 'image', NULL, 'http://example.com/b', 'user@test.com', 2000000, 'png');

-- Tag med1 to dev-m1
INSERT INTO device_media (device_id, media_id) VALUES ('dev-m1', 'med1');
