-- Cleanup after TechnicianCertificateRepositoryIT test methods
DELETE FROM technician_certificate WHERE technician_id = 'tech-cert-1';
DELETE FROM technician WHERE id = 'tech-cert-1';
