-- Phase 2 Round 2: dedicated database for the extracted measuring-instrument service (DB-per-service).
SELECT 'CREATE DATABASE sclera_cloud_measuring_instrument'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sclera_cloud_measuring_instrument')\gexec
