-- Seed data for SystemInterfaceRepositoryIT
INSERT INTO system_interface (interface_name, status, pid, timestamp)
VALUES ('eth0', 'UP',   'pid-111', 1700000001000);

INSERT INTO system_interface (interface_name, status, pid, timestamp)
VALUES ('eth1', 'DOWN', null,      null);
