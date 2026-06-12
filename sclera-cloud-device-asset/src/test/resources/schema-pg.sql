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

-- ai_call_log_history: full column set required by AiCallLogHistoryRepository JPQL queries
CREATE TABLE IF NOT EXISTS ai_call_log_history (
    id            VARCHAR(255) PRIMARY KEY,
    created_at    BIGINT,
    description   TEXT,
    state         VARCHAR(255),
    ai_call_log_id VARCHAR(255) REFERENCES ai_call_log(id),
    technician_id VARCHAR(255) REFERENCES technician(id)
);

-- floor: FK target for location.floor_id (@ManyToOne Floor); also needed by JPQL
-- queries that navigate l.floor.id (e.g. getLocationIdsByFloorId, updateArea).
-- building_id is a plain VARCHAR (no FK constraint) to avoid ordering issues with
-- the building table which is declared later in this file.
CREATE TABLE IF NOT EXISTS floor (
    id                 VARCHAR(255) PRIMARY KEY,
    name               VARCHAR(128),
    initial_position   TEXT,
    image_url          VARCHAR(255),
    angle              INTEGER,
    path               TEXT,
    min_zoom           VARCHAR(128),
    max_zoom           VARCHAR(128),
    local_image_url    VARCHAR(255),
    updated_timestamp  BIGINT,
    source_type        VARCHAR(255),
    building_id        VARCHAR(255)
);

-- location: needed for LocationRepositoryIT
-- floor_id is a scalar FK column (Location.floor is @ManyToOne Floor, but in test schema
-- we keep it as a plain VARCHAR since there is no floor table in this minimal schema)
CREATE TABLE IF NOT EXISTS location (
    id                      VARCHAR(255) PRIMARY KEY,
    name                    VARCHAR(128),
    position                VARCHAR(128),
    area                    TEXT,
    type                    VARCHAR(128),
    code                    VARCHAR(128),
    z_index                 INTEGER      DEFAULT 0,
    status                  VARCHAR(255),
    record_checklist_status VARCHAR(32),
    record_checklist_count  INTEGER      DEFAULT 0,
    floor_id                VARCHAR(255),
    updated_timestamp       NUMERIC
);

-- device: FK target for asset_device_mapping.device_id (and AssetDeviceMapping.device @ManyToOne)
-- Only id is required for FK resolution; other columns added as nullable stubs.
-- location_id added as FK to location table (needed for getUnlinkedLocationIds subquery).
-- user_data_name added: referenced by AiCallLog native queries (getAllAiCallLog, getStatusInformation).
CREATE TABLE IF NOT EXISTS device (
    id              VARCHAR(255) PRIMARY KEY,
    display_name    VARCHAR(255),
    user_data_name  VARCHAR(255),
    mac_address     VARCHAR(64),
    type            VARCHAR(128),
    ip_address      VARCHAR(64),
    network_layer   VARCHAR(64),
    status          INTEGER,
    onboard_status  INTEGER,
    vdms_id         VARCHAR(64)  REFERENCES vdms(id),
    location_id     VARCHAR(255) REFERENCES location(id)
);

-- device_onboard_status: per-device onboarding progress (device is a @OneToOne -> device_id FK)
CREATE TABLE IF NOT EXISTS device_onboard_status (
    id                 VARCHAR(255) PRIMARY KEY,
    assignee_email     VARCHAR(255),
    image_status       INTEGER DEFAULT 0,
    geolocation_status INTEGER DEFAULT 0,
    tag_status         INTEGER DEFAULT 0,
    field_status       INTEGER DEFAULT 0,
    device_id          VARCHAR(255) REFERENCES device(id)
);

-- device_ip_address: IP addresses assigned to a device (FK -> device)
CREATE TABLE IF NOT EXISTS device_ip_address (
    id                  VARCHAR(255) PRIMARY KEY,
    ip_address          VARCHAR(64),
    ip_conflict_status  INTEGER DEFAULT 0,
    device_id           VARCHAR(255) REFERENCES device(id)
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

-- device_onboard_status_assignee: users assigned to an onboarding task (@ManyToOne -> device_onboard_status)
CREATE TABLE IF NOT EXISTS device_onboard_status_assignee (
    id                        VARCHAR(255) PRIMARY KEY,
    type                      VARCHAR(128),
    email                     VARCHAR(255),
    device_onboard_status_id  VARCHAR(255) REFERENCES device_onboard_status(id)
);

-- device_network_specification: per-device network metadata (@OneToOne Device via device_id FK)
CREATE TABLE IF NOT EXISTS device_network_specification (
    id                  VARCHAR(255) PRIMARY KEY,
    network_interfaces  TEXT,
    network_settings    TEXT,
    network_ports       TEXT,
    network_processes   TEXT,
    device_id           VARCHAR(255) REFERENCES device(id)
);

-- device_lifecycle_history: per-device lifecycle events (FK -> device)
-- Note: assigned_user_email is NOT a column of this table (it lives on device);
-- the getLatestAssignedUserEmailFromHistory query referencing it is kept native with a comment.
CREATE TABLE IF NOT EXISTS device_lifecycle_history (
    id                   VARCHAR(255) PRIMARY KEY,
    operational_status   VARCHAR(32),
    usage_status         VARCHAR(32),
    assigned_user_id     VARCHAR(128),
    assignment_count     INTEGER,
    created_timestamp    NUMERIC,
    assigned_timestamp   NUMERIC,
    description          TEXT,
    assigned_by_user_id  VARCHAR(255),
    device_id            VARCHAR(255) REFERENCES device(id)
);

-- device_specification: hardware/software metadata for a device (FK -> device via device_id)
-- @Id is the MAC address (serial number); device_id is a non-insertable/non-updatable FK column
-- managed via the @OneToOne Device relation.
CREATE TABLE IF NOT EXISTS device_specification (
    id               VARCHAR(255) PRIMARY KEY,
    created_at       BIGINT,
    updated_at       BIGINT,
    username         VARCHAR(255),
    email            VARCHAR(255),
    account_type     VARCHAR(255),
    user_uuid        VARCHAR(255),
    device_name      VARCHAR(255),
    model            VARCHAR(255),
    os_type          VARCHAR(255),
    location_info    VARCHAR(1024),
    os_info          VARCHAR(1024),
    cpu_info         VARCHAR(512),
    disk_drives      TEXT,
    physical_disks   TEXT,
    bios             VARCHAR(512),
    ram_info         VARCHAR(512),
    video_cards      VARCHAR(1024),
    sound_devices    VARCHAR(1024),
    battery_info     VARCHAR(255),
    processes        TEXT,
    system_updates   TEXT,
    child_devices    TEXT,
    device_id        VARCHAR(255) REFERENCES device(id)
);

-- notes: per-device notes; composite PK (id, device_id); device_id FK -> device
CREATE TABLE IF NOT EXISTS notes (
    id         VARCHAR(255) NOT NULL,
    title      VARCHAR(128),
    body       TEXT,
    type       VARCHAR(64),
    is_global  INTEGER      DEFAULT 0,
    device_id  VARCHAR(255) NOT NULL REFERENCES device(id),
    PRIMARY KEY (id, device_id)
);

-- managed_software: FK target for device_installed_apps.managed_software_id
CREATE TABLE IF NOT EXISTS managed_software (
    id                      VARCHAR(255) PRIMARY KEY,
    name                    VARCHAR(255),
    application_name        VARCHAR(255),
    application_type        VARCHAR(255),
    url                     VARCHAR(255),
    vendor                  VARCHAR(255),
    subscription_id         VARCHAR(255),
    subscription_type       VARCHAR(255),
    unit_price              DOUBLE PRECISION,
    currency                VARCHAR(32),
    subscription_start_date BIGINT,
    subscription_end_date   BIGINT,
    status                  VARCHAR(64),
    application_id          VARCHAR(255)
);

-- device_installed_apps: installed applications on a device/device-specification
CREATE TABLE IF NOT EXISTS device_installed_apps (
    id                      VARCHAR(255) PRIMARY KEY,
    created_at              BIGINT,
    name                    VARCHAR(255),
    publisher               VARCHAR(255),
    version                 VARCHAR(255),
    device_id               VARCHAR(255),
    device_specification_id VARCHAR(255) REFERENCES device_specification(id),
    managed_software_id     VARCHAR(255) REFERENCES managed_software(id),
    risk_status             INTEGER
);

-- conditions: alert conditions for sensors/instruments (ConditionsRepository)
CREATE TABLE IF NOT EXISTS conditions (
    id                                              VARCHAR(255) PRIMARY KEY,
    name                                            VARCHAR(128),
    value                                           VARCHAR(128),
    second_value                                    VARCHAR(128),
    alert_message                                   TEXT,
    start_time                                      VARCHAR(64),
    end_time                                        VARCHAR(64),
    alert_condition                                 VARCHAR(64),
    alert                                           BOOLEAN      DEFAULT false,
    show_alert                                      BOOLEAN      DEFAULT false,
    show_alert_message_as_value                     BOOLEAN      DEFAULT false,
    schedule                                        INTEGER      DEFAULT 0,
    schedule_conditions                             TEXT,
    max_alert_count                                 INTEGER      DEFAULT 0,
    alert_count                                     INTEGER      DEFAULT 0,
    alert_count_enabled                             INTEGER      DEFAULT 0,
    last_alerted_timestamp                          NUMERIC,
    alert_time                                      INTEGER,
    priority                                        VARCHAR(128),
    last_alerted                                    BOOLEAN      DEFAULT false,
    alert_count_time                                INTEGER,
    enable_threshold_line_onchart                   INTEGER,
    color_of_threshold_line_onchart                 VARCHAR(255),
    daintree_device_id                              VARCHAR(255),
    bacnet_object_bacnet_device_id                  VARCHAR(255),
    bacnet_object_id                                VARCHAR(255),
    lorawan_sensor_attributes_lorawan_sensor_id     VARCHAR(255),
    lorawan_sensor_attributes_name                  VARCHAR(255),
    snmp_device_id                                  VARCHAR(255),
    disruptive_sensor_id                            VARCHAR(255),
    my_devices_sensor_attributes_my_devices_sensor_id VARCHAR(255),
    my_devices_sensor_attributes_name              VARCHAR(255),
    monnit_sensor_id                                VARCHAR(255),
    pelican_sensor_attributes_pelican_sensor_id     VARCHAR(255),
    pelican_sensor_attributes_name                  VARCHAR(255),
    knx_group_address                               VARCHAR(255),
    knx_group_knx_device_address                    VARCHAR(255),
    snmp_object_snmp_device_configuration_id        VARCHAR(255),
    snmp_object_oid                                 VARCHAR(255),
    measuring_instrument_id                         VARCHAR(255),
    daintree_point_id                               VARCHAR(255),
    alert_profile_id                                VARCHAR(255),
    ecobee_sensor_attributes_ecobee_sensor_id       VARCHAR(255),
    ecobee_sensor_attributes_name                   VARCHAR(255),
    modbus_register_id                              VARCHAR(255)
);

-- device_conditions: device-level alert conditions (DeviceConditionsRepository)
-- device_id FK -> device (device table already declared above)
CREATE TABLE IF NOT EXISTS device_conditions (
    id                  VARCHAR(255) PRIMARY KEY,
    alert_condition     VARCHAR(64),
    device_id           VARCHAR(255) REFERENCES device(id),
    alert_profile_id    VARCHAR(128),
    last_alerted_time   NUMERIC,
    trigger_time        INTEGER,
    priority            VARCHAR(128),
    start_time          VARCHAR(64),
    end_time            VARCHAR(64),
    schedule            INTEGER      DEFAULT 0,
    schedule_conditions TEXT,
    max_alert_count     INTEGER      DEFAULT 0,
    alert_count         INTEGER      DEFAULT 0,
    alert_count_enabled INTEGER      DEFAULT 0,
    alert_count_time    INTEGER,
    last_alerted        BOOLEAN      DEFAULT false,
    alert_message       TEXT
);

-- location_history: audit trail for location status changes (LocationHistoryRepository)
-- location_id FK -> location (location table declared above)
CREATE TABLE IF NOT EXISTS location_history (
    id                 VARCHAR(255) PRIMARY KEY,
    status             VARCHAR(128),
    type               VARCHAR(255),
    description        TEXT,
    updated_timestamp  BIGINT,
    updated_email      VARCHAR(255),
    location_id        VARCHAR(255) REFERENCES location(id)
);

-- device_types: device category definitions (DeviceTypesRepository)
CREATE TABLE IF NOT EXISTS device_types (
    id                 VARCHAR(255) PRIMARY KEY,
    name               VARCHAR(255),
    updated_timestamp  BIGINT,
    old_name           VARCHAR(255)
);

-- asset_field: configurable custom/global asset fields (AssetFieldRepository)
CREATE TABLE IF NOT EXISTS asset_field (
    id               VARCHAR(255) PRIMARY KEY,
    name             VARCHAR(255),
    type             VARCHAR(128),
    tool_tip         TEXT,
    default_value    TEXT,
    is_active        BOOLEAN      DEFAULT true,
    options          TEXT,
    is_deleted       BOOLEAN      DEFAULT false,
    show_in_section  INTEGER      DEFAULT 0,
    created_at       BIGINT
);

-- system_interface: network interfaces tracked for discovery (SystemInterfaceRepository)
CREATE TABLE IF NOT EXISTS system_interface (
    interface_name  VARCHAR(255) PRIMARY KEY,
    status          VARCHAR(255),
    pid             VARCHAR(255),
    timestamp       BIGINT
);

-- device_technician_ai_suggestion: AI-generated technician suggestions per device type (DeviceTechnicianAISuggestionRepository)
CREATE TABLE IF NOT EXISTS device_technician_ai_suggestion (
    id           VARCHAR(255) PRIMARY KEY,
    device_type  VARCHAR(255) NOT NULL,
    technicians  JSONB,
    vdms_id      VARCHAR(64)  REFERENCES vdms(id)
);

-- vdms_details: per-VDMS presentation details (VdmsDetailsRepository)
CREATE TABLE IF NOT EXISTS vdms_details (
    id                   VARCHAR(255) PRIMARY KEY,
    weather_city         VARCHAR(64),
    weather_zip_code     VARCHAR(32),
    weather_country_code VARCHAR(32),
    weather_latitude     VARCHAR(32),
    weather_longitude    VARCHAR(32),
    weather_data         TEXT,
    weather_units        VARCHAR(32),
    layout_data          TEXT,
    device_custom_fields TEXT,
    corrigo_layout_data  TEXT,
    vdms_id              VARCHAR(64)  REFERENCES vdms(id)
);

-- technician_certificate: owned by technician (FK -> technician)
CREATE TABLE IF NOT EXISTS technician_certificate (
    id            VARCHAR(255) PRIMARY KEY,
    name          VARCHAR(255),
    type          VARCHAR(255),
    url           VARCHAR(255),
    technician_id VARCHAR(255) REFERENCES technician(id)
);

-- client_bar_code: barcode tags associated with devices and/or locations
-- device_id and location_id are nullable FKs (barcode can be unlinked, or linked to either)
CREATE TABLE IF NOT EXISTS client_bar_code (
    id                VARCHAR(255) PRIMARY KEY,
    added_at          VARCHAR(255),
    added_by          VARCHAR(255),
    client_bar_code_id VARCHAR(255),
    updated_at        NUMERIC,
    updated_by        VARCHAR(255),
    vdms_id           VARCHAR(255),
    batch_id          VARCHAR(255),
    device_id         VARCHAR(255) REFERENCES device(id),
    location_id       VARCHAR(255) REFERENCES location(id),
    is_deleted        BOOLEAN      DEFAULT false
);

-- media: media files (images, manuals, etc.) that can be tagged to devices
CREATE TABLE IF NOT EXISTS media (
    id                 VARCHAR(255) PRIMARY KEY,
    name               VARCHAR(255),
    description        TEXT,
    category           VARCHAR(128),
    link               VARCHAR(512),
    created_email      VARCHAR(255),
    created_timestamp  BIGINT,
    extension          VARCHAR(32),
    source_type        VARCHAR(64) DEFAULT 'vdms'
);

-- device_media: @ManyToMany join table between device and media
CREATE TABLE IF NOT EXISTS device_media (
    device_id  VARCHAR(255) NOT NULL REFERENCES device(id),
    media_id   VARCHAR(255) NOT NULL REFERENCES media(id),
    PRIMARY KEY (device_id, media_id)
);

-- document: documents (PDFs, etc.) that can be tagged to devices
CREATE TABLE IF NOT EXISTS document (
    id                 VARCHAR(255) PRIMARY KEY,
    name               VARCHAR(255),
    description        TEXT,
    category           VARCHAR(128),
    link               VARCHAR(512),
    created_email      VARCHAR(255),
    created_timestamp  BIGINT,
    encrypted_type     INTEGER,
    source_type        VARCHAR(64) DEFAULT 'vdms'
);

-- device_document: @ManyToMany join table between device and document
CREATE TABLE IF NOT EXISTS device_document (
    device_id    VARCHAR(255) NOT NULL REFERENCES device(id),
    document_id  VARCHAR(255) NOT NULL REFERENCES document(id),
    PRIMARY KEY (device_id, document_id)
);

-- application_user: application users associated with managed software (ApplicationUserRepository)
-- managed_software FK is nullable (users may be unassigned)
CREATE TABLE IF NOT EXISTS application_user (
    id                VARCHAR(255) PRIMARY KEY,
    technician_id     VARCHAR(255),
    email             VARCHAR(255),
    type              VARCHAR(128),
    managed_software  VARCHAR(255) REFERENCES managed_software(id)
);

-- device_technician: join table between device and technician (used by TechnicianRepository native tag/untag queries)
-- device_id and technician_id are both VARCHARs; no FK constraints to avoid ordering issues in minimal schema
CREATE TABLE IF NOT EXISTS device_technician (
    device_id     VARCHAR(255) NOT NULL,
    technician_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (device_id, technician_id)
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
