-- Explicit DDL — kept in sync with the JPA entity.
-- Hibernate ddl-auto=update will also create/update this table on first boot;
-- this file documents the production-target shape and the UNIQUE constraint
-- on vdms_id that the named queries assume.

CREATE TABLE IF NOT EXISTS maximo_configuration (
    id            VARCHAR(64)  PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    server_url    VARCHAR(512) NOT NULL,
    auth_url      VARCHAR(512) NOT NULL,
    client_id     VARCHAR(255),
    client_secret VARCHAR(512),
    sites         TEXT,
    vdms_id       VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_maximo_configuration_vdms_id UNIQUE (vdms_id)
);

CREATE INDEX IF NOT EXISTS idx_maximo_configuration_vdms_id
    ON maximo_configuration (vdms_id);
