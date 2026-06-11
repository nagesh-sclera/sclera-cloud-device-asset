-- Cleanup for FloorRepositoryIT seed data.
-- Delete in child-first order to respect FK constraints.
DELETE FROM location   WHERE id IN ('loc-floor-1');
DELETE FROM floor      WHERE id IN ('f-floor-1', 'f-floor-2', 'f-floor-3');
DELETE FROM building   WHERE id = 'b-floor-test';
DELETE FROM vdms       WHERE id = 'v-floor-test';
