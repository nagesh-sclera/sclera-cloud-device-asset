-- Cleanup for AiCallLogRepositoryIT seed data.
DELETE FROM ai_call_log WHERE id IN ('acl1', 'acl2', 'acl3');
DELETE FROM device      WHERE id IN ('dev1', 'dev2');
DELETE FROM technician  WHERE id IN ('t1', 't2');
DELETE FROM vdms        WHERE id IN ('v1');
