-- Seed data for MeasuringInstrumentRepositoryIT
-- Minimal FK-respecting rows: device -> measuring_instrument -> measuring_instrument_attributes

-- vdms: required FK ancestor for device
INSERT INTO vdms (id, property_name) VALUES ('vdms-mi-1', 'MI Test VDMS');

-- device: FK target for measuring_instrument.device_id
INSERT INTO device (id, display_name, vdms_id, monitor)
VALUES ('dev-mi-1', 'TestDevice', 'vdms-mi-1', 1);

INSERT INTO device (id, display_name, vdms_id, monitor)
VALUES ('dev-mi-2', 'AnotherDevice', 'vdms-mi-1', 0);

-- measuring_instrument: main rows under test
INSERT INTO measuring_instrument
    (id, type, name, description, calculation_type, scale_type, attribute, parameter,
     category, sub_category, value, unit, tags, timestamp, sensor_type, alert,
     user_data_value, user_data_name, show_on_map, show_on_scan, measuring_entity, device_id)
VALUES
    ('mi-001', 'temperature', 'Temp Sensor A', 'desc-a', 'avg', 'linear',
     '{}', '{}', 'temperature', 'ambient', '22.5', 'C', null, 1000000,
     'temperature', false, null, null, 1, 1, 'device', 'dev-mi-1'),

    ('mi-002', 'humidity', 'Humidity Sensor B', 'desc-b', 'max', 'static',
     '{}', '{}', 'humidity', 'indoor', '60', '%', null, 2000000,
     'humidity', false, null, null, 1, 1, 'device', 'dev-mi-1'),

    ('mi-003', 'co2', 'CO2 Sensor C', 'desc-c', 'avg', 'static',
     '{}', '{}', 'air_quality', 'indoor', '400', 'ppm', null, 3000000,
     'co2', true, null, null, 1, 1, 'device', 'dev-mi-2'),

    ('mi-no-device', 'pressure', 'Pressure Orphan', 'desc-d', 'avg', 'static',
     '{}', '{}', 'pressure', 'ambient', '101', 'kPa', null, 4000000,
     'pressure', false, null, null, 0, 0, 'device', null);

-- measuring_instrument_attributes: for getMeasuringInstrumentIdsByProtocolAndPrimaryIds
--   and getTotalManualAttributesCountofMeasuringInstruments
INSERT INTO measuring_instrument_attributes
    (id, name, type, unit, value, protocol, category, primary_id, secondary_id, tertiary_id,
     attribute_index, measuring_instrument_id)
VALUES
    ('mia-001', 'Attr1', 'manual', 'C', '22', 'bacnet', 'temp', 'pid-1', 'sid-1', null, 1, 'mi-001'),
    ('mia-002', 'Attr2', 'manual', '%', '60', 'bacnet', 'humid', 'pid-2', 'sid-2', null, 1, 'mi-002'),
    ('mia-003', 'Attr3', 'auto',   'ppm','400','lorawan','air',  'pid-3', 'sid-3', null, 1, 'mi-003');
