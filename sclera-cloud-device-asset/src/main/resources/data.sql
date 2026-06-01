-- Seed a default VDMS row so test-UI auto-fill (VDMS760) and /vdms/* endpoints
-- have something to return on a fresh PG volume. Idempotent: ON CONFLICT DO NOTHING.
-- Requires: spring.sql.init.mode=always and spring.jpa.defer-datasource-initialization=true
-- (so this runs AFTER Hibernate ddl-auto creates the vdms table).
INSERT INTO vdms (
    id, property_name, activation_status, status, timezone, address, city, country, state,
    region, customer_org_id, adc_configuration_id, is_master, has_secondary_device,
    deployment_type, latitude, longitude, activation_timestamp
) VALUES (
    'VDMS760', 'Sclera HQ — Demo Building', 'ACTIVE', 1, 'America/Los_Angeles',
    '101 Innovation Drive, Suite 500', 'San Francisco', 'USA', 'CA',
    'West Coast', 'org-demo-sclera', 'adc-config-001', 1, 0, 'cloud',
    '37.7749', '-122.4194', 1748563200000
) ON CONFLICT (id) DO NOTHING;

-- Seed a default Docker (network) so the test-UI's default dockername 'right_wing' resolves.
-- Device has a composite FK (docker_vdms_id, docker_name) -> docker(vdms_id, name) via the
-- @ManyToOne Docker association. Without this row, adddevice fails 23503.
INSERT INTO docker (
    name, vdms_id, gateway, host, mac_address, system_type, internet_required, internet_status,
    cidr, approval_status, configuration_status, network_origin
) VALUES (
    'right_wing', 'VDMS760', '192.168.1.1', true, '00:00:00:00:00:00', 'generic', false, 'unknown',
    24, 'approved', 'configured', 1
) ON CONFLICT (name, vdms_id) DO NOTHING;

