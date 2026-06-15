-- Seed data for ApplicationUserRepositoryIT.

-- One managed_software row (FK target for application_user.managed_software)
INSERT INTO managed_software (id, name, status) VALUES ('ms-au1', 'TestSW', 'active') ON CONFLICT DO NOTHING;
INSERT INTO managed_software (id, name, status) VALUES ('ms-au2', 'OtherSW', 'active') ON CONFLICT DO NOTHING;

-- Three application_user rows
INSERT INTO application_user (id, technician_id, email, type, managed_software)
VALUES
  ('au-1', 'tech-1', 'user1@example.com', 'admin',    'ms-au1'),
  ('au-2', 'tech-2', 'user2@example.com', 'standard', 'ms-au1'),
  ('au-3', 'tech-3', 'user3@example.com', 'standard', NULL);
