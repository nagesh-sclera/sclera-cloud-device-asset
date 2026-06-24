-- 30-qr-schema.sql
-- Idempotent, self-guarding QR schema additions for sclera-cloud-device-asset.
--
-- HOW THIS SCRIPT IS RUN
--   This file is mounted into /docker-entrypoint-initdb.d and executed ONCE at
--   first Postgres container startup, when the database is EMPTY. At that point
--   Hibernate has not yet connected, so the base tables (device, location,
--   qr_code, client_qr_code, global_qrcode, …) do NOT exist yet.
--
--   To avoid aborting container startup with "relation does not exist" errors,
--   every statement that touches a base table is wrapped in an existence guard.
--   When the guard fails (fresh init) the script emits a NOTICE and exits
--   cleanly — the Postgres container starts successfully.
--
-- WHEN THE REAL WORK HAPPENS
--   Run this script manually AFTER the application has started at least once
--   and Hibernate ddl-auto=update has created the base tables:
--     psql -U <user> -d device_asset_db -f 30-qr-schema.sql
--   Every statement is idempotent (IF NOT EXISTS / ADD COLUMN IF NOT EXISTS)
--   so repeated runs are safe.
--
-- WHAT THIS SCRIPT ADDS (not handled by ddl-auto)
--   • 5 new tables  : property_service, property_qrcode, property_service_request,
--                     property_service_response, qr_code_template
--   • 4 extra columns on Hibernate-managed tables:
--       qr_code(customer_org_id, adc_qr_code_check),
--       client_qr_code(adc_client_qr_code_check), device(qrcode_count)
--   • 3 UNIQUE indexes + 8 non-unique indexes (ddl-auto does not create these)
--
-- GUARD STRATEGY
--   Outer guard  : checks device + location; if absent → NOTICE + no-op.
--   Inner guards : each ALTER / CREATE INDEX on a Hibernate-managed table
--                  (qr_code, client_qr_code, global_qrcode, device) has its
--                  own per-table IF to_regclass('public.<t>') IS NOT NULL check.
--   All DDL inside the DO block is issued via EXECUTE so PL/pgSQL does not
--   try to resolve relation OIDs at parse time.

DO $$
BEGIN

  -- ── Outer guard: base tables created by Hibernate ──────────────────────────
  IF to_regclass('public.device') IS NULL OR to_regclass('public.location') IS NULL THEN
    RAISE NOTICE '30-qr-schema.sql: base tables (device / location) do not exist yet — '
                 'Hibernate has not run. Script is a no-op at this init. '
                 'Re-run manually after the application starts.';
  ELSE

    -- ── 1. New tables (5) ──────────────────────────────────────────────────

    -- property_service
    EXECUTE $sql$
      CREATE TABLE IF NOT EXISTS property_service (
          id       VARCHAR(255) PRIMARY KEY,
          name     VARCHAR(255),
          vdms_id  VARCHAR(64)
      )
    $sql$;

    -- property_qrcode: FK → property_service + location (both exist here)
    EXECUTE $sql$
      CREATE TABLE IF NOT EXISTS property_qrcode (
          id                  VARCHAR(255) PRIMARY KEY,
          image_url           VARCHAR(512),
          property_service_id VARCHAR(255) REFERENCES property_service(id),
          location_id         VARCHAR(255) REFERENCES location(id)
      )
    $sql$;

    -- property_service_request: FK → property_service
    EXECUTE $sql$
      CREATE TABLE IF NOT EXISTS property_service_request (
          id                  VARCHAR(255) PRIMARY KEY,
          label               VARCHAR(512),
          type                VARCHAR(128),
          options             TEXT,
          property_service_id VARCHAR(255) REFERENCES property_service(id)
      )
    $sql$;

    -- property_service_response: FK → property_qrcode + property_service_request
    EXECUTE $sql$
      CREATE TABLE IF NOT EXISTS property_service_response (
          id                          VARCHAR(255) PRIMARY KEY,
          value                       TEXT,
          alert                       BOOLEAN  DEFAULT false,
          timestamp                   NUMERIC,
          property_qrcode_id          VARCHAR(255) REFERENCES property_qrcode(id),
          property_service_request_id VARCHAR(255) REFERENCES property_service_request(id)
      )
    $sql$;

    -- qr_code_template: no FKs to Hibernate-managed tables
    EXECUTE $sql$
      CREATE TABLE IF NOT EXISTS qr_code_template (
          id                   VARCHAR(255) PRIMARY KEY,
          name                 VARCHAR(255),
          qr_template_json     TEXT,
          qr_code_template_url VARCHAR(512),
          qr_code_logo_url     VARCHAR(512),
          customer_org_id      VARCHAR(64),
          creation_timestamp   NUMERIC,
          added_by             VARCHAR(255),
          updated_timestamp    NUMERIC,
          updated_by           VARCHAR(255),
          in_use               INTEGER  DEFAULT 0,
          is_default           INTEGER  DEFAULT 0
      )
    $sql$;

    -- ── 2. Indexes on new (script-created) tables ──────────────────────────
    -- property_qrcode and qr_code_template are created above, so they exist here.

    EXECUTE 'CREATE INDEX IF NOT EXISTS ix_property_qrcode_location_id         ON property_qrcode(location_id)';
    EXECUTE 'CREATE INDEX IF NOT EXISTS ix_property_qrcode_property_service_id ON property_qrcode(property_service_id)';
    EXECUTE 'CREATE INDEX IF NOT EXISTS ix_qr_code_template_customer_org_id    ON qr_code_template(customer_org_id)';

    -- ── 3. Extra columns on Hibernate-managed tables ───────────────────────
    -- Each block has its own per-table guard because these tables are created
    -- by ddl-auto and might not exist even when device+location do.

    -- qr_code
    IF to_regclass('public.qr_code') IS NOT NULL THEN
      EXECUTE 'ALTER TABLE qr_code ADD COLUMN IF NOT EXISTS customer_org_id   VARCHAR(64)';
      EXECUTE 'ALTER TABLE qr_code ADD COLUMN IF NOT EXISTS adc_qr_code_check INTEGER';
    ELSE
      RAISE NOTICE '30-qr-schema.sql: qr_code table not found — skipping ALTER TABLE qr_code.';
    END IF;

    -- client_qr_code
    IF to_regclass('public.client_qr_code') IS NOT NULL THEN
      EXECUTE 'ALTER TABLE client_qr_code ADD COLUMN IF NOT EXISTS adc_client_qr_code_check INTEGER';
    ELSE
      RAISE NOTICE '30-qr-schema.sql: client_qr_code table not found — skipping ALTER TABLE client_qr_code.';
    END IF;

    -- device (already in outer guard scope, but double-checking is harmless)
    IF to_regclass('public.device') IS NOT NULL THEN
      EXECUTE 'ALTER TABLE device ADD COLUMN IF NOT EXISTS qrcode_count INTEGER';
    END IF;

    -- ── 4. Indexes on Hibernate-managed tables ─────────────────────────────

    -- qr_code indexes (non-unique)
    IF to_regclass('public.qr_code') IS NOT NULL THEN
      EXECUTE 'CREATE INDEX IF NOT EXISTS ix_qr_code_device_id   ON qr_code(device_id)';
      EXECUTE 'CREATE INDEX IF NOT EXISTS ix_qr_code_location_id ON qr_code(location_id)';
      EXECUTE 'CREATE INDEX IF NOT EXISTS ix_qr_code_vdms_id     ON qr_code(vdms_id)';
    ELSE
      RAISE NOTICE '30-qr-schema.sql: qr_code table not found — skipping qr_code indexes.';
    END IF;

    -- client_qr_code indexes (1 unique + 2 non-unique)
    IF to_regclass('public.client_qr_code') IS NOT NULL THEN
      EXECUTE 'CREATE UNIQUE INDEX IF NOT EXISTS uix_client_qr_code_client_qr_code_id ON client_qr_code(client_qr_code_id)';
      EXECUTE 'CREATE INDEX IF NOT EXISTS ix_client_qr_code_device_id   ON client_qr_code(device_id)';
      EXECUTE 'CREATE INDEX IF NOT EXISTS ix_client_qr_code_location_id ON client_qr_code(location_id)';
    ELSE
      RAISE NOTICE '30-qr-schema.sql: client_qr_code table not found — skipping client_qr_code indexes.';
    END IF;

    -- global_qrcode indexes (2 unique)
    IF to_regclass('public.global_qrcode') IS NOT NULL THEN
      EXECUTE 'CREATE UNIQUE INDEX IF NOT EXISTS uix_global_qrcode_device_id   ON global_qrcode(device_id)';
      EXECUTE 'CREATE UNIQUE INDEX IF NOT EXISTS uix_global_qrcode_location_id ON global_qrcode(location_id)';
    ELSE
      RAISE NOTICE '30-qr-schema.sql: global_qrcode table not found — skipping global_qrcode indexes.';
    END IF;

  END IF; -- end outer guard

END $$;
