-- Cleanup for ConditionsRepositoryIT
DELETE FROM conditions WHERE id IN ('cond-001', 'cond-002', 'cond-upd-001');
