-- Seed for DeviceNetworkSpecificationRepositoryIT.
-- Insert a device so the FK device_id can be set.
INSERT INTO device (id) VALUES ('dev1');

-- ns1: linked to dev1; ns2: unlinked (device_id = null).
-- @Lob TEXT columns left NULL to avoid ClobJdbcType extraction issues on PostgreSQL
-- (the @Lob annotation on the entity causes Hibernate to use Clob extraction; the
-- UPDATE/DELETE tests do not need the Lob values).
INSERT INTO device_network_specification (id, device_id)
VALUES
  ('ns1', 'dev1'),
  ('ns2', null);
