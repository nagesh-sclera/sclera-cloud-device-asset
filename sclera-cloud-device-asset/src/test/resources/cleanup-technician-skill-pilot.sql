-- Cleanup after TechnicianSkillRepositoryIT test methods
DELETE FROM technician_skill WHERE technician_id = 'tech-skill-1';
DELETE FROM technician WHERE id = 'tech-skill-1';
