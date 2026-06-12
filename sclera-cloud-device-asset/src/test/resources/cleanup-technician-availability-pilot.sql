-- Cleanup for TechnicianAvailabilityRepositoryIT
DELETE FROM technician_availability WHERE id IN ('avail-1','avail-2','avail-3','avail-upsert');
DELETE FROM technician WHERE id IN ('tech-avail-1','tech-avail-2');
