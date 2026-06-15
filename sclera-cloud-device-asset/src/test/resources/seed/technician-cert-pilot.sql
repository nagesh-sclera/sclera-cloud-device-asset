-- Seed data for TechnicianCertificateRepositoryIT
INSERT INTO technician (id, name, department) VALUES ('tech-cert-1', 'Alice', 'engineering');

INSERT INTO technician_certificate (id, name, type, url, technician_id)
    VALUES ('cert-1', 'AWS Cert',   'cloud',    'https://example.com/cert1', 'tech-cert-1');
INSERT INTO technician_certificate (id, name, type, url, technician_id)
    VALUES ('cert-2', 'K8s Cert',   'infra',    'https://example.com/cert2', 'tech-cert-1');
INSERT INTO technician_certificate (id, name, type, url, technician_id)
    VALUES ('cert-3', 'Safety Cert','safety',   'https://example.com/cert3', 'tech-cert-1');
