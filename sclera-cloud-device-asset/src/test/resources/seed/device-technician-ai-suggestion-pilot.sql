-- Seed data for DeviceTechnicianAISuggestionRepositoryIT
-- vdms row needed as FK target for vdms_id
INSERT INTO vdms (id, property_name, activation_status)
VALUES ('vdms-ai-01', 'AI Test VDMS', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO device_technician_ai_suggestion (id, device_type, technicians, vdms_id)
VALUES ('dtas-001', 'Camera',  '{"ids":["tech-1"]}'::jsonb, 'vdms-ai-01');

INSERT INTO device_technician_ai_suggestion (id, device_type, technicians, vdms_id)
VALUES ('dtas-002', 'Sensor',  '{"ids":["tech-2","tech-3"]}'::jsonb, 'vdms-ai-01');
