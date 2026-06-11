-- Seed data for DeviceConditionsRepositoryIT
-- device FK target (vdms not required since vdms_id is nullable)
INSERT INTO device (id) VALUES ('dev-dc-001');

-- Row 1: normal (non-AI-call) condition
INSERT INTO device_conditions (id, alert_condition, device_id, alert_profile_id, trigger_time,
    priority, start_time, end_time, schedule, schedule_conditions,
    max_alert_count, alert_count, alert_count_enabled, alert_count_time,
    last_alerted, alert_message, last_alerted_time)
VALUES (
    'dc-001', 'device_offline', 'dev-dc-001', null, 10,
    'high', '09:00', '17:00', 0, null,
    3, 0, 0, 60,
    false, 'Device went offline', null
);

-- Row 2: AI-call condition for the same device
INSERT INTO device_conditions (id, alert_condition, device_id, alert_profile_id, trigger_time,
    priority, start_time, end_time, schedule, schedule_conditions,
    max_alert_count, alert_count, alert_count_enabled, alert_count_time,
    last_alerted, alert_message, last_alerted_time)
VALUES (
    'dc-002', 'device_offline_ai_call_alert', 'dev-dc-001', null, 5,
    'medium', null, null, 0, null,
    2, 1, 1, 30,
    false, 'AI call alert', null
);
