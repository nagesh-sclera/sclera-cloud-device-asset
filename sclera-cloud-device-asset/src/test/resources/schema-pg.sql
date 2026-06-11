-- Minimal schema for @DataJpaTest / Testcontainers integration tests.
-- Only the tables and columns needed by the named queries under test are created.
-- We use ddl-auto=none so Hibernate does NOT try to generate DDL (which would
-- carry MySQL backtick quoting for the `condition` column and fail on PG).

-- vdms: needed as FK target for technician.vdms_id (nullable FK; table must exist)
CREATE TABLE IF NOT EXISTS vdms (
    id                        VARCHAR(64)  PRIMARY KEY,
    property_name             VARCHAR(128),
    activation_status         VARCHAR(16),
    is_block                  BOOLEAN,
    creation_timestamp        BIGINT,
    last_seen                 BIGINT,
    public_ip                 VARCHAR(64),
    public_port               VARCHAR(16),
    status                    INTEGER,
    mac_address               VARCHAR(32),
    location                  TEXT,
    timezone                  VARCHAR(128),
    start_port                VARCHAR(8),
    end_port                  VARCHAR(8),
    block_timestamp           BIGINT,
    address                   VARCHAR(255),
    city                      VARCHAR(64),
    country                   VARCHAR(64),
    state                     VARCHAR(64),
    zip                       INTEGER,
    is_master                 INTEGER,
    has_secondary_device      INTEGER,
    secondary_device_id       VARCHAR(255),
    slave_ip                  TEXT,
    master_ip                 TEXT,
    latitude                  TEXT,
    longitude                 TEXT,
    password                  TEXT,
    image_url                 VARCHAR(128),
    activation_timestamp      BIGINT,
    deployment_type           VARCHAR(16),
    region                    VARCHAR(32),
    customer_org_id           VARCHAR(64),
    adc_configuration_id      VARCHAR(64),
    inspection_activity_timeout INTEGER DEFAULT 0,
    updated_timestamp         BIGINT
);

-- technician: the primary table for the availability query
CREATE TABLE IF NOT EXISTS technician (
    id            VARCHAR(255) PRIMARY KEY,
    email         VARCHAR(255),
    phone         VARCHAR(255),
    country_code  VARCHAR(255),
    name          VARCHAR(255),
    department    VARCHAR(32)  DEFAULT 'generic',
    designation   VARCHAR(32)  DEFAULT 'generic',
    time_zone     VARCHAR(50)  DEFAULT 'UTC',
    created_by    VARCHAR(255),
    created_at    BIGINT,
    vdms_id       VARCHAR(64)  REFERENCES vdms(id),
    cost          INTEGER,
    unit          VARCHAR(255),
    type          VARCHAR(255)
);

-- technician_skill: joined for primarySkill column
CREATE TABLE IF NOT EXISTS technician_skill (
    id            VARCHAR(255) PRIMARY KEY,
    name          VARCHAR(255),
    type          VARCHAR(255),
    rating        NUMERIC(2,1) DEFAULT 0,
    ranking       INTEGER,
    created_by    VARCHAR(255),
    created_at    BIGINT,
    technician_id VARCHAR(255) REFERENCES technician(id)
);

-- technician_availability: joined for availability logic
-- NOTE: "condition" is NOT a reserved word in PostgreSQL (unlike MySQL),
-- so no quoting is needed here.
CREATE TABLE IF NOT EXISTS technician_availability (
    id            VARCHAR(255) PRIMARY KEY,
    start_date    BIGINT,
    end_date      BIGINT,
    start_time    VARCHAR(16)  DEFAULT '09:00',
    end_time      VARCHAR(16)  DEFAULT '17:00',
    is_all_day    BOOLEAN      DEFAULT false,
    frequency     VARCHAR(255),
    condition     TEXT,
    technician_id VARCHAR(255) REFERENCES technician(id)
);

-- ai_call_log: needed because Technician entity has @OneToMany to AiCallLog
-- (Hibernate may try to validate the mapping even with ddl-auto=none on some versions)
CREATE TABLE IF NOT EXISTS ai_call_log (
    id            VARCHAR(255) PRIMARY KEY,
    created_at    BIGINT,
    assigned_at   BIGINT,
    description   TEXT,
    issue_type    VARCHAR(255),
    priority      VARCHAR(255),
    status        VARCHAR(255),
    is_completed  BOOLEAN,
    device_id     VARCHAR(255),
    technician_id VARCHAR(255) REFERENCES technician(id)
);

-- ai_call_log_history: same reason as above
CREATE TABLE IF NOT EXISTS ai_call_log_history (
    id            VARCHAR(255) PRIMARY KEY,
    technician_id VARCHAR(255) REFERENCES technician(id)
);

-- device: FK target for asset_device_mapping.device_id (and AssetDeviceMapping.device @ManyToOne)
-- Only id is required for FK resolution; other columns added as nullable stubs.
CREATE TABLE IF NOT EXISTS device (
    id            VARCHAR(255) PRIMARY KEY,
    display_name  VARCHAR(255),
    mac_address   VARCHAR(64),
    type          VARCHAR(128),
    ip_address    VARCHAR(64),
    network_layer VARCHAR(64),
    status        INTEGER,
    vdms_id       VARCHAR(64)  REFERENCES vdms(id)
);

-- asset: the primary table under conversion
CREATE TABLE IF NOT EXISTS asset (
    id                   VARCHAR(255)  PRIMARY KEY,
    display_name         VARCHAR(128),
    description          TEXT,
    mac_address          VARCHAR(32),
    model                VARCHAR(255),
    vendor               VARCHAR(255),
    type                 VARCHAR(128),
    ip_address           VARCHAR(64),
    network_layer        INTEGER,
    serial_number        VARCHAR(255),
    warranty             VARCHAR(32),
    import_type          VARCHAR(255),
    is_matched           BOOLEAN       DEFAULT false,
    subsystem_parent_id  VARCHAR(255),
    subsystem_count      INTEGER       DEFAULT 0,
    original_keys        TEXT          NOT NULL,
    custom_fields        TEXT,
    matched_products     TEXT,
    vdms_id              VARCHAR(64)   REFERENCES vdms(id)
);

-- asset_device_mapping: join table between asset and device
CREATE TABLE IF NOT EXISTS asset_device_mapping (
    id            VARCHAR(255)  PRIMARY KEY,
    asset_id      VARCHAR(255)  REFERENCES asset(id),
    device_id     VARCHAR(255)  REFERENCES device(id),
    match_score   INTEGER
);

-- building: eagerly loaded via Asset -> vdms -> building when a full Asset entity is fetched
CREATE TABLE IF NOT EXISTS building (
    id                 VARCHAR(255)  PRIMARY KEY,
    name               VARCHAR(128),
    code               VARCHAR(128),
    updated_timestamp  NUMERIC,
    source_type        VARCHAR(255),
    vdms_id            VARCHAR(64)   REFERENCES vdms(id)
);
