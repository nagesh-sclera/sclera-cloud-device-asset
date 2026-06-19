-- Database for sclera-workorders (sclera-cloud-workorder service).
-- The shared postgres instance already owns role "root"; create the DB if absent.
SELECT 'CREATE DATABASE workorder_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'workorder_db')\gexec
