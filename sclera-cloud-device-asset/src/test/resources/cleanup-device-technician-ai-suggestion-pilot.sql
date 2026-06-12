-- Cleanup for DeviceTechnicianAISuggestionRepositoryIT
DELETE FROM device_technician_ai_suggestion WHERE id IN ('dtas-001', 'dtas-002', 'dtas-003');
DELETE FROM vdms WHERE id = 'vdms-ai-01';
