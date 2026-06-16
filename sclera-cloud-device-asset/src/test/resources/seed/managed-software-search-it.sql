-- Fixture for ManagedSoftwareSearchQueryBuilderIT.
-- Five managed_software rows across the status buckets, plus device_specification +
-- device_installed_apps links so the email/os_type join (filter/search/sort) is exercised.
--
-- msx1 active   linked to alice(windows) AND bob(linux)  -> tests DISTINCT (two dia rows)
-- msx2 expired  linked to bob(linux)
-- msx3 'trial'  -> 'others' bucket (status NOT IN active/expired, non-null)
-- msx4 active   no installed apps (email/os_type NULL via LEFT JOIN)
-- msx5 status NULL -> only in 'all' (NULL is not 'others')

INSERT INTO device_specification (id, email, os_type) VALUES ('dspecA', 'alice@x.com', 'windows');
INSERT INTO device_specification (id, email, os_type) VALUES ('dspecB', 'bob@x.com',   'linux');

INSERT INTO managed_software (id, name, application_name, application_type, vendor, subscription_id,
                              subscription_type, unit_price, currency, subscription_start_date,
                              subscription_end_date, status)
VALUES ('msx1', 'Acme', 'Suite', 'security', 'AcmeCorp', 'SUB-1', 'annual', 100, 'USD', 100, 900, 'active');
INSERT INTO managed_software (id, name, application_name, application_type, vendor, subscription_id,
                              subscription_type, unit_price, currency, subscription_start_date,
                              subscription_end_date, status)
VALUES ('msx2', 'Beta', 'Tool', 'backup', 'BetaInc', 'SUB-2', 'monthly', 50, 'EUR', 200, 800, 'expired');
INSERT INTO managed_software (id, name, application_name, vendor, currency,
                              subscription_start_date, subscription_end_date, status)
VALUES ('msx3', 'Gamma', 'App', 'GammaLtd', 'GBP', 300, 700, 'trial');
INSERT INTO managed_software (id, name, application_name, vendor,
                              subscription_start_date, subscription_end_date, status)
VALUES ('msx4', 'Delta', 'Pack', 'DeltaCo', 400, 600, 'active');
-- msx5: status NULL, application_name NULL, no end_date
INSERT INTO managed_software (id, name, vendor, subscription_start_date)
VALUES ('msx5', 'Epsilon', 'EpsilonAB', 500);

INSERT INTO device_installed_apps (id, managed_software_id, device_specification_id)
VALUES ('diax1', 'msx1', 'dspecA');
INSERT INTO device_installed_apps (id, managed_software_id, device_specification_id)
VALUES ('diax2', 'msx2', 'dspecB');
INSERT INTO device_installed_apps (id, managed_software_id, device_specification_id)
VALUES ('diax3', 'msx1', 'dspecB');
