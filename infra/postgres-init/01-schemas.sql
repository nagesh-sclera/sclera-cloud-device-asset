-- PostgreSQL init script: runs once on first data-volume initialisation.
-- Creates schemas used by sister microservices that share the `vdms` DB.
CREATE SCHEMA IF NOT EXISTS vdms_svc;
CREATE SCHEMA IF NOT EXISTS integrations_svc;
CREATE SCHEMA IF NOT EXISTS inspection_svc;
