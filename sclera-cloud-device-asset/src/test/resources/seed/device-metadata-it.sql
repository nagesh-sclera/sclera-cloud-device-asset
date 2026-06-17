-- Fixture for DeviceMetadataQueryBuilderIT (distinct device-column lookups).
-- dmx1 v1 Router/GA/Cat1 matched     dmx2 v1 Switch/GB/Cat1 unmatched
-- dmx3 v1 Camera archived(ams=3) -> excluded   dmx4 v1 Router, asset_group '' (empty), no category
-- dmx5 v2 Gateway -> only in unscoped results
INSERT INTO device (id, type, asset_group, category, asset_match_status, docker_vdms_id)
VALUES ('dmx1', 'Router', 'GA', 'Cat1', 1, 'v1');
INSERT INTO device (id, type, asset_group, category, asset_match_status, docker_vdms_id)
VALUES ('dmx2', 'Switch', 'GB', 'Cat1', 0, 'v1');
INSERT INTO device (id, type, asset_match_status, docker_vdms_id)
VALUES ('dmx3', 'Camera', 3, 'v1');
INSERT INTO device (id, type, asset_group, asset_match_status, docker_vdms_id)
VALUES ('dmx4', 'Router', '', 0, 'v1');
INSERT INTO device (id, type, asset_match_status, docker_vdms_id)
VALUES ('dmx5', 'Gateway', 0, 'v2');
