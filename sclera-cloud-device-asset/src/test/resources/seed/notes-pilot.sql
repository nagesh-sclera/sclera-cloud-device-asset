-- Seed data for NotesRepositoryIT.
-- FK: notes.device_id -> device.id
INSERT INTO device (id) VALUES ('dev-n1') ON CONFLICT DO NOTHING;

-- two non-global notes for dev-n1
INSERT INTO notes (id, title, body, is_global, device_id)
VALUES ('note-1', 'Title One', 'Body One', 0, 'dev-n1');

INSERT INTO notes (id, title, body, is_global, device_id)
VALUES ('note-2', 'Title Two', 'Body Two', 0, 'dev-n1');

-- one global note for dev-n1
INSERT INTO notes (id, title, body, is_global, device_id)
VALUES ('note-g1', 'Global Title', 'Global Body', 1, 'dev-n1');

-- second device for isolation tests
INSERT INTO device (id) VALUES ('dev-n2') ON CONFLICT DO NOTHING;

INSERT INTO notes (id, title, body, is_global, device_id)
VALUES ('note-3', 'Other Device Note', 'Other Body', 0, 'dev-n2');
