-- Scope: all devices live in docker_vdms_id='v1', docker_name='dock1' except dsx9 (other vdms)
INSERT INTO building (id, name) VALUES ('bldg1', 'HeadQuarters');
INSERT INTO floor (id, name, building_id) VALUES ('flr1', 'FirstFloor', 'bldg1');
INSERT INTO location (id, name, floor_id) VALUES ('loc1', 'MainLab', 'flr1');

-- dsx1: online, matched(1), onboarded(3), assigned, custom dept=Engineering, cat Hardware/Server
INSERT INTO device (id, display_name, user_data_name, vendor, user_data_vendor, model, type,
                    ip_address, mac_address, serial_number, monitor, status, onboard_status,
                    asset_match_status, assigned_user_email, docker_vdms_id, docker_name,
                    custom_fields, category, sub_category, asset_group, source_type,
                    record_checklist_count, document_count, asset_image_url, monnit_status,
                    created_timestamp, updated_timestamp, location_id)
VALUES ('dsx1', 'Alpha Device', 'My-Alpha', 'Cisco', NULL, 'CX100', 'router',
        '10.0.0.2', 'AA:BB:01', 'SN-001', 1, 1, 3,
        1, 'alice@sclera.com', 'v1', 'dock1',
        '[{"department":"Engineering","owner":"alice"}]', 'Hardware', 'Server', 'GroupA', 'vdms',
        2, 1, '["img1.png"]', 'alert',
        100, 500, 'loc1');

-- dsx2: offline, unmatched(0), notonboarded(1), unassigned(NULL), custom dept=Finance
INSERT INTO device (id, display_name, vendor, model, type, ip_address, monitor, status,
                    onboard_status, asset_match_status, assigned_user_email, docker_vdms_id,
                    docker_name, custom_fields, category, sub_category, asset_group, source_type,
                    created_timestamp, updated_timestamp)
VALUES ('dsx2', 'Beta Device', 'HP', 'HP-22', 'switch', '10.0.0.10', 1, 0,
        1, 0, NULL, 'v1',
        'dock1', '[{"department":"Finance"}]', 'Hardware', 'Laptop', 'GroupB', 'adc',
        200, 400);

-- dsx3: unmonitored(0), verified(2), assigned_user_email literal 'null' (counts as unassigned)
INSERT INTO device (id, display_name, type, monitor, status, onboard_status, asset_match_status,
                    assigned_user_email, docker_vdms_id, docker_name, created_timestamp, updated_timestamp)
VALUES ('dsx3', 'Gamma!! Device', 'sensor', 0, 0, 2, 2,
        'null', 'v1', 'dock1', 300, 300);

-- dsx4: archived(3) — excluded from 'all' (asset_match_status != 3 default filter)
INSERT INTO device (id, display_name, type, monitor, asset_match_status, docker_vdms_id,
                    docker_name, created_timestamp, updated_timestamp)
VALUES ('dsx4', 'Delta Device', 'router', 1, 3, 'v1', 'dock1', 400, 200);

-- dsx5: virtual 'other' (virtual_device_type=5), NULL monitor, ip for inet sort
INSERT INTO device (id, display_name, type, monitor, virtual_device_type, asset_match_status,
                    docker_vdms_id, docker_name, ip_address, created_timestamp, updated_timestamp)
VALUES ('dsx5', 'Epsilon Device', 'camera', NULL, 5, 0, 'v1', 'dock1', '9.1.1.1', 500, 100);

-- dsx9: different vdms — must NEVER appear in v1-scoped results
INSERT INTO device (id, display_name, type, monitor, asset_match_status, docker_vdms_id,
                    docker_name, created_timestamp, updated_timestamp)
VALUES ('dsx9', 'Foreign Device', 'router', 1, 0, 'v2', 'dock2', 900, 900);

INSERT INTO device_onboard_status (id, assignee_email, geolocation_status, image_status,
                                   field_status, tag_status, device_id)
VALUES ('dosx1', 'tech1@sclera.com', 1, 0, 2, 3, 'dsx1');
INSERT INTO device_onboard_status (id, assignee_email, geolocation_status, device_id)
VALUES ('dosx2', NULL, 0, 'dsx2');
INSERT INTO device_onboard_status_assignee (id, email, device_onboard_status_id)
VALUES ('dosax1', 'tech2@sclera.com', 'dosx1');

INSERT INTO device_specification (id, username, email, os_type, device_id)
VALUES ('dspec1', 'winuser01', 'winuser01@sclera.com', 'windows', 'dsx1');

INSERT INTO qr_code (id, device_id) VALUES ('qrx1', 'dsx1');
INSERT INTO client_bar_code (id, device_id) VALUES ('cbcx1', 'dsx2');
INSERT INTO nfc (id, device_id) VALUES ('nfcx1', 'dsx2');
