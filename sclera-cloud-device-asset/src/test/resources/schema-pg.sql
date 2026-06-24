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
    monitor         INTEGER      DEFAULT 1,
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

-- measuring_instrument: sensors/instruments tied to devices (MeasuringInstrumentRepository)
-- device_id is the FK column backing the @ManyToOne Device relation (LAZY); no FK constraint
-- to device here because the minimal test schema has a sparse device table and we seed devices separately.
CREATE TABLE IF NOT EXISTS measuring_instrument (
    id                    VARCHAR(255) PRIMARY KEY,
    type                  VARCHAR(255),
    name                  VARCHAR(255),
    description           TEXT,
    calculation_type      VARCHAR(255),
    scale_type            VARCHAR(64)  DEFAULT 'static',
    attribute             TEXT,
    parameter             TEXT,
    category              VARCHAR(255) DEFAULT 'generic',
    sub_category          VARCHAR(255) DEFAULT 'generic',
    value                 VARCHAR(255),
    unit                  VARCHAR(255),
    tags                  TEXT,
    timestamp             NUMERIC,
    sensor_type           VARCHAR(255) DEFAULT 'generic',
    alert                 BOOLEAN      DEFAULT false,
    user_data_value       VARCHAR(64),
    user_data_name        VARCHAR(128),
    show_on_map           INTEGER      DEFAULT 1,
    show_on_scan          INTEGER      DEFAULT 1,
    measuring_entity      VARCHAR(128) DEFAULT 'device',
    digital_twin_position TEXT,
    device_id             VARCHAR(255) REFERENCES device(id)
);

-- measuring_instrument_location: @ManyToMany join table (instrument <-> location)
CREATE TABLE IF NOT EXISTS measuring_instrument_location (
    measuring_instrument_id VARCHAR(255) NOT NULL REFERENCES measuring_instrument(id),
    location_id             VARCHAR(255) NOT NULL REFERENCES location(id),
    PRIMARY KEY (measuring_instrument_id, location_id)
);

-- measuring_instrument_attributes: per-instrument protocol attributes (MeasuringInstrument_Attributes)
CREATE TABLE IF NOT EXISTS measuring_instrument_attributes (
    id                      VARCHAR(255) PRIMARY KEY,
    name                    VARCHAR(255),
    type                    VARCHAR(255),
    unit                    VARCHAR(255),
    value                   VARCHAR(255),
    protocol                VARCHAR(255),
    category                VARCHAR(255),
    primary_id              VARCHAR(255),
    secondary_id            VARCHAR(255),
    tertiary_id             VARCHAR(255),
    attribute_index         INTEGER,
    measuring_instrument_id VARCHAR(255) REFERENCES measuring_instrument(id)
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

-- docker: FK target for device.docker (@ManyToOne; columns docker_name + docker_vdms_id reference docker.name + docker.vdms_id)
-- vdms_id references vdms(id) but we declare without FK constraint to avoid ordering issues.
CREATE TABLE IF NOT EXISTS docker (
    name     VARCHAR(255) NOT NULL,
    vdms_id  VARCHAR(64)  NOT NULL,
    PRIMARY KEY (name, vdms_id)
);

-- users: FK target for device.user (@ManyToOne; @JoinColumn assigned_user_email references users.email)
CREATE TABLE IF NOT EXISTS users (
    email  VARCHAR(255) PRIMARY KEY,
    name   VARCHAR(255)
);

-- Extend device table with all columns needed by converted JPQL queries.
-- All columns added as nullable with no constraints to avoid seeding complexity.
ALTER TABLE device ADD COLUMN IF NOT EXISTS snmp_count                    INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS snmp_status                   VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS interface_count               INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS notes_count                   INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS ticket_count                  INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS ticket_status                 VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS bacnet_count                  INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS bacnet_status                 VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS lorawan_count                 INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS lorawan_status                VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS disruptive_count              INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS disruptive_status             VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS my_devices_count              INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS my_devices_status             VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS monnit_count                  INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS monnit_status                 VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS pelican_count                 INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS pelican_status                VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS knx_count                     INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS knx_status                    VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS measuring_instrument_count    INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS measuring_instrument_status   VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS document_count                INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS media_count                   INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS checklist_template_count      INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS snmp_object_count             INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS snmp_object_status            VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS subsystem_count               INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS subsystem_parent_id           VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS asset_match_status            INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS virtual_device_type           INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS parent                        VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS snmp_parent                   VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS user_connection_type          VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS record_checklist_count        INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS record_checklist_status       VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS daintree_count                INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS daintree_status               VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS asset_image_url               VARCHAR(512);
ALTER TABLE device ADD COLUMN IF NOT EXISTS created_timestamp             BIGINT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS ecobee_count                  INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS ecobee_status                 VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS modbus_count                  INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS modbus_status                 VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS created_email                 VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS asset_group                   VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS updated_email                 VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS updated_timestamp             BIGINT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS category                      VARCHAR(128);
ALTER TABLE device ADD COLUMN IF NOT EXISTS sub_category                  VARCHAR(128);
ALTER TABLE device ADD COLUMN IF NOT EXISTS location_status               VARCHAR(128);
ALTER TABLE device ADD COLUMN IF NOT EXISTS digital_twin_image_url        VARCHAR(512);
ALTER TABLE device ADD COLUMN IF NOT EXISTS poly_lens_count               INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS mqtt_count                    INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS ai_call                       BOOLEAN;
ALTER TABLE device ADD COLUMN IF NOT EXISTS cost_value                    NUMERIC(19,2);
ALTER TABLE device ADD COLUMN IF NOT EXISTS cost_unit                     VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS is_dnd_enabled                BOOLEAN;
ALTER TABLE device ADD COLUMN IF NOT EXISTS operational_status            VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS dnd_timestamp                 NUMERIC;
ALTER TABLE device ADD COLUMN IF NOT EXISTS system_dnd_enabled            BOOLEAN;
ALTER TABLE device ADD COLUMN IF NOT EXISTS adc_json                      JSONB;
ALTER TABLE device ADD COLUMN IF NOT EXISTS matched_product_ids           TEXT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS asset_ocr_image_url           VARCHAR(512);
ALTER TABLE device ADD COLUMN IF NOT EXISTS asset_tag_images_url          TEXT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS reboot_status                 VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS qrcode_count                  INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS position                      VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS latitude                      VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS longitude                     VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS vendor                        VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS docker_name                   VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS docker_vdms_id                VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS assigned_user_email           VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS serial_number                 VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS last_seen_on                  BIGINT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS user_data_name                VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS user_data_model               VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS user_data_vendor              VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS model                         VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS warranty                      VARCHAR(64);
ALTER TABLE device ADD COLUMN IF NOT EXISTS description                   TEXT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS custom_fields                 TEXT;
ALTER TABLE device ADD COLUMN IF NOT EXISTS remote_access                 INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS email_alert                   INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS sms_alert                     INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS popup_notification            INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS local_vendor_email_alert      INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS local_vendor_sms_alert        INTEGER;
ALTER TABLE device ADD COLUMN IF NOT EXISTS network_layer                 VARCHAR(64);

-- inventory_device: associates an inventory tracking id with a device (@OneToOne Device via device_id FK)
-- tracking_id is the @Id; device_id is the default join column for the owning @OneToOne relation.
CREATE TABLE IF NOT EXISTS inventory_device (
    tracking_id  VARCHAR(255) PRIMARY KEY,
    device_id    VARCHAR(255) REFERENCES device(id)
);

-- phonebook: FK target for device.global_vendor / local_vendor / other_vendor_1/2/3 (@ManyToOne Phonebook)
-- Minimal stub — only the columns referenced by the eager join from Device entity load.
CREATE TABLE IF NOT EXISTS phonebook (
    id            VARCHAR(255) PRIMARY KEY,
    account_number VARCHAR(255),
    vendor_name   VARCHAR(128),
    email         VARCHAR(255),
    phone         VARCHAR(32),
    phone_type    VARCHAR(32),
    value         VARCHAR(16),
    company_name  VARCHAR(128),
    website       VARCHAR(255),
    address       VARCHAR(255),
    city          VARCHAR(64),
    country       VARCHAR(64),
    state         VARCHAR(64),
    street        VARCHAR(255),
    zip           INTEGER
);

-- FK columns on device for Phonebook @ManyToOne relations
ALTER TABLE device ADD COLUMN IF NOT EXISTS global_vendor_id   VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS local_vendor_id    VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS other_vendor_1_id  VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS other_vendor_2_id  VARCHAR(255);
ALTER TABLE device ADD COLUMN IF NOT EXISTS other_vendor_3_id  VARCHAR(255);

-- ── QR-code tables ──────────────────────────────────────────────────────────
-- Placed after device/location/floor/building so FK references are valid.

-- qr_code: Sclera-generated QR codes linked to a device and/or location.
-- customerOrgId (String) → VARCHAR; adcQrCodeCheck (Integer) → INTEGER.
-- creationTime (BigInteger) → NUMERIC (mirrors BigInteger Hibernate mapping).
CREATE TABLE IF NOT EXISTS qr_code (
    id               VARCHAR(255) PRIMARY KEY,
    image_url        VARCHAR(512),
    vdms_id          VARCHAR(64),
    created_by       VARCHAR(255),
    creation_time    NUMERIC,
    batch_id         VARCHAR(255),
    qr_code_link     VARCHAR(512),
    updated_time     VARCHAR(255),
    updated_by       VARCHAR(255),
    is_deleted       BOOLEAN      DEFAULT false,
    customer_org_id  VARCHAR(64),
    adc_qr_code_check INTEGER,
    device_id        VARCHAR(255) REFERENCES device(id),
    location_id      VARCHAR(255) REFERENCES location(id)
);

-- client_qr_code: Client-supplied QR codes linked to a device and/or location.
-- addedAt (String) → VARCHAR; updatedAt (BigInteger) → NUMERIC.
-- adcClientQrCodeCheck (Integer) → INTEGER.
CREATE TABLE IF NOT EXISTS client_qr_code (
    id                      VARCHAR(255) PRIMARY KEY,
    added_at                VARCHAR(255),
    added_by                VARCHAR(255),
    client_qr_code_id       VARCHAR(255),
    updated_at              NUMERIC,
    updated_by              VARCHAR(255),
    vdms_id                 VARCHAR(64),
    batch_id                VARCHAR(255),
    is_deleted              BOOLEAN      DEFAULT false,
    adc_client_qr_code_check INTEGER,
    device_id               VARCHAR(255) REFERENCES device(id),
    location_id             VARCHAR(255) REFERENCES location(id)
);

-- global_qrcode: Globally managed QR codes; optionally bound to one device OR one location.
-- Field image_url is already snake_case in the entity (private String image_url).
-- @OneToOne Device → device_id; @OneToOne Location → location_id.
CREATE TABLE IF NOT EXISTS global_qrcode (
    id           VARCHAR(255) PRIMARY KEY,
    image_url    VARCHAR(512),
    device_id    VARCHAR(255) REFERENCES device(id),
    location_id  VARCHAR(255) REFERENCES location(id)
);

-- property_service: Property-service definition (e.g. cleaning, maintenance) scoped to a tenant.
-- vdms_id is a plain scalar (no FK — no cross-service Vdms join in device-asset).
CREATE TABLE IF NOT EXISTS property_service (
    id       VARCHAR(255) PRIMARY KEY,
    name     VARCHAR(255),
    vdms_id  VARCHAR(64)
);

-- property_qrcode: QR code bound to a property_service and a location.
-- image_url is already snake_case in the entity.
-- @ManyToOne PropertyService property_service → property_service_id FK.
-- @ManyToOne Location location → location_id FK.
CREATE TABLE IF NOT EXISTS property_qrcode (
    id                  VARCHAR(255) PRIMARY KEY,
    image_url           VARCHAR(512),
    property_service_id VARCHAR(255) REFERENCES property_service(id),
    location_id         VARCHAR(255) REFERENCES location(id)
);

-- property_service_request: A single input field (question) in a property-service form.
-- @ManyToOne PropertyService property_service → property_service_id FK.
CREATE TABLE IF NOT EXISTS property_service_request (
    id                  VARCHAR(255) PRIMARY KEY,
    label               VARCHAR(512),
    type                VARCHAR(128),
    options             TEXT,
    property_service_id VARCHAR(255) REFERENCES property_service(id)
);

-- property_service_response: A submitted answer during a QR-scan inspection.
-- timestamp (BigInteger) → NUMERIC; alert BOOLEAN mirrors @Column(columnDefinition).
-- @ManyToOne PropertyQrcode property_qrcode → property_qrcode_id FK.
-- @ManyToOne PropertyServiceRequest property_service_request → property_service_request_id FK.
CREATE TABLE IF NOT EXISTS property_service_response (
    id                          VARCHAR(255) PRIMARY KEY,
    value                       TEXT,
    alert                       BOOLEAN      DEFAULT false,
    timestamp                   NUMERIC,
    property_qrcode_id          VARCHAR(255) REFERENCES property_qrcode(id),
    property_service_request_id VARCHAR(255) REFERENCES property_service_request(id)
);

-- qr_code_template: QR code visual/layout templates scoped to a customer org.
-- qrTemplateJson → TEXT (mirrors @Column(columnDefinition = "TEXT")).
-- inUse / isDefault → INTEGER DEFAULT 0 (mirrors @Column(columnDefinition = "integer default 0")).
-- creationTimestamp / updatedTimestamp (BigInteger) → NUMERIC.
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
    in_use               INTEGER      DEFAULT 0,
    is_default           INTEGER      DEFAULT 0
);
