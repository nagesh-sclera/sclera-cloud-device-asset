-- Seed data for VdmsDetailsRepositoryIT
-- vdms row needed as FK target
INSERT INTO vdms (id, property_name, activation_status)
VALUES ('vdms-det-01', 'Details Test VDMS', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO vdms_details (id, weather_city, weather_zip_code, weather_country_code,
                          weather_latitude, weather_longitude, weather_data, weather_units,
                          layout_data, device_custom_fields, corrigo_layout_data, vdms_id)
VALUES ('vdd-001', 'London', 'EC1A', 'GB',
        '51.5074', '-0.1278', '{"temp":15}', 'metric',
        '{"cols":2}', '{"fields":["sn"]}', '{"corrigo":true}', 'vdms-det-01');
