-- Seed data for ManagedSoftwareRepositoryIT.

-- Three managed_software rows with distinct statuses and subscription types.
INSERT INTO managed_software (id, name, application_name, application_type, url, vendor,
    subscription_id, subscription_type, unit_price, currency,
    subscription_start_date, subscription_end_date, status, application_id)
VALUES
  ('ms-a1', 'Antivirus Pro',  'AntivirusPro',  'security', 'https://av.example.com',
   'VendorA', 'sub-001', 'monthly_fees', 9.99, 'USD',
   1000000, 9999999999, 'active', 'app-001'),
  ('ms-b2', 'Office Suite',   'OfficeSuite',   'productivity', 'https://office.example.com',
   'VendorB', 'sub-002', 'annually',     99.00, 'USD',
   1000000, 9999999999, 'expired', 'app-002'),
  ('ms-c3', 'Dev Tools',      'DevTools',      'development', 'https://dev.example.com',
   'VendorC', 'sub-003', 'trial',        0.00,  'USD',
   1000000, 9999999999, 'trial',  NULL);

-- device_specification rows for getManagedSoftwareUsers test
INSERT INTO device (id) VALUES ('dev-msit1') ON CONFLICT DO NOTHING;
INSERT INTO device_specification (id, username, user_uuid, account_type, email, device_name, model, os_type, device_id)
VALUES ('spec-msit1', 'alice', 'uuid-alice', 'admin', 'alice@example.com', 'AliceLaptop', 'ThinkPad', 'Linux', 'dev-msit1');

-- device_installed_apps linking spec-msit1 -> ms-a1
INSERT INTO device_installed_apps (id, name, device_id, device_specification_id, managed_software_id, risk_status)
VALUES ('dia-msit1', 'AntivirusPro', 'dev-msit1', 'spec-msit1', 'ms-a1', 2);
