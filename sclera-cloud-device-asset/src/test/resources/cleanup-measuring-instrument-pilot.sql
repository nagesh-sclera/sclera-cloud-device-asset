-- Cleanup for MeasuringInstrumentRepositoryIT
DELETE FROM measuring_instrument_attributes WHERE id IN ('mia-001','mia-002','mia-003');
DELETE FROM measuring_instrument WHERE id IN ('mi-001','mi-002','mi-003','mi-no-device');
DELETE FROM device WHERE id IN ('dev-mi-1','dev-mi-2');
DELETE FROM vdms WHERE id = 'vdms-mi-1';
