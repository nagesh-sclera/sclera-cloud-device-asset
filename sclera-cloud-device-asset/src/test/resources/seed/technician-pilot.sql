-- Seed data for TechnicianRepositoryIT
INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at)
    VALUES ('tech-r-1', 'alice@example.com', '1111111111', '+1', 'Alice', 'engineering', 'engineer', 'UTC', 'admin', 1700000000000);
INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at)
    VALUES ('tech-r-2', 'bob@example.com',   '2222222222', '+1', 'Bob',   'operations',  'operator', 'UTC', 'admin', 1700000000001);
INSERT INTO technician (id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at)
    VALUES ('tech-r-3', 'carol@example.com', '3333333333', '+44','Carol', 'engineering', 'lead',     'UTC', 'admin', 1700000000002);
