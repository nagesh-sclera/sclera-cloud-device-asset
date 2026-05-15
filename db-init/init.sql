-- Sclara 2.0 Demo Seed Data
-- Runs automatically on first MySQL container start

CREATE TABLE IF NOT EXISTS vdms (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    property_name VARCHAR(128),
    activation_status VARCHAR(16),
    status INT,
    location TEXT,
    timezone VARCHAR(128),
    address TEXT,
    city VARCHAR(64),
    country VARCHAR(64),
    state VARCHAR(64),
    zip INT,
    image_url VARCHAR(128),
    latitude TEXT,
    longitude TEXT,
    region VARCHAR(32),
    customer_org_id VARCHAR(64),
    adc_configuration_id VARCHAR(64),
    is_master INT,
    has_secondary_device INT,
    secondary_device_id TEXT,
    master_ip TEXT,
    slave_ip TEXT,
    activation_timestamp BIGINT,
    deployment_type VARCHAR(16)
);

INSERT IGNORE INTO vdms (
    id, property_name, activation_status, status,
    address, city, country, state, zip,
    timezone, region, customer_org_id, adc_configuration_id,
    is_master, has_secondary_device, deployment_type,
    latitude, longitude, activation_timestamp
) VALUES (
    'demo-vdms-001',
    'Sclara HQ — Demo Building',
    'ACTIVE', 1,
    '101 Innovation Drive, Suite 500',
    'San Francisco', 'USA', 'CA', 94105,
    'America/Los_Angeles',
    'West Coast',
    'org-demo-sclara',
    'adc-config-001',
    1, 0, 'cloud',
    '37.7749', '-122.4194',
    UNIX_TIMESTAMP() * 1000
);
