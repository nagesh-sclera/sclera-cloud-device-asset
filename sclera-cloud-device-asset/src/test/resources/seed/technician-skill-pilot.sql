-- Seed data for TechnicianSkillRepositoryIT
INSERT INTO technician (id, name, department) VALUES ('tech-skill-1', 'Bob', 'field');

INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id)
    VALUES ('skill-1', 'Electrical', 'primary',   4.5, 1, 'admin', 1700000000000, 'tech-skill-1');
INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id)
    VALUES ('skill-2', 'Plumbing',   'secondary', 3.0, 2, 'admin', 1700000001000, 'tech-skill-1');
INSERT INTO technician_skill (id, name, type, rating, ranking, created_by, created_at, technician_id)
    VALUES ('skill-3', 'HVAC',       'secondary', 3.5, 3, 'admin', 1700000002000, 'tech-skill-1');
