-- Cleanup for VdmsDetailsRepositoryIT
DELETE FROM vdms_details WHERE id IN ('vdd-001', 'vdd-002');
DELETE FROM vdms WHERE id = 'vdms-det-01';
