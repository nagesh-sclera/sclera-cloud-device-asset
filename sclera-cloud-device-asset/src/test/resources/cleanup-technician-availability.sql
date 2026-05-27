-- Cleanup after each TechnicianAvailabilityTest method.
-- Removes seed rows in FK-safe order so subsequent test methods start clean.
DELETE FROM technician_availability WHERE id = 'avail-001';
DELETE FROM technician_skill       WHERE id = 'skill-001';
DELETE FROM technician             WHERE id = 'tech-001';
