-- Seed data for DocumentRepositoryIT.
-- device row is required as FK target for device_document.
INSERT INTO device (id, display_name) VALUES ('dev-d1', 'Test Device Document');

-- Two document rows; doc2 has NULL description to exercise the COALESCE guard.
INSERT INTO document (id, name, category, description, link, created_email, created_timestamp, encrypted_type)
VALUES ('doc1', 'Policy A', 'policy', 'Main policy', 'http://example.com/doc1', 'admin@test.com', 1000000, 0);

INSERT INTO document (id, name, category, description, link, created_email, created_timestamp, encrypted_type)
VALUES ('doc2', 'Spec B', 'spec', NULL, 'http://example.com/doc2', 'admin@test.com', 2000000, 1);

-- Tag doc1 to dev-d1
INSERT INTO device_document (device_id, document_id) VALUES ('dev-d1', 'doc1');
