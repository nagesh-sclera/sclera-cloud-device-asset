-- Cleanup for LocationRepositoryIT — delete in FK-safe order.
DELETE FROM device WHERE id = 'dev1';
DELETE FROM location WHERE id IN ('loc1', 'loc2', 'loc3');
DELETE FROM floor WHERE id IN ('f1', 'f2');
