-- Cleanup for AiCallLogHistoryRepositoryIT seed data.
DELETE FROM ai_call_log_history WHERE id IN ('h1', 'h2', 'h3');
DELETE FROM ai_call_log         WHERE id IN ('acl1', 'acl2');
DELETE FROM device              WHERE id IN ('dev1');
DELETE FROM technician          WHERE id IN ('t1', 't2');
DELETE FROM vdms                WHERE id IN ('v1');
