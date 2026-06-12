-- Cleanup for LocationHistoryRepositoryIT
DELETE FROM location_history WHERE id IN ('lh-001', 'lh-002', 'lh-003', 'lh-new');
DELETE FROM location WHERE id IN ('loc-001', 'loc-002');
