-- Cleanup for SystemInterfaceRepositoryIT
DELETE FROM system_interface WHERE interface_name IN ('eth0', 'eth1', 'eth2');
