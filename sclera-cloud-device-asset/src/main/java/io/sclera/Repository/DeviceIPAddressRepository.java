package io.sclera.Repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.touchscreen.DeviceIPAddressDTO;
import io.sclera.models.Device_IP_Address;


@Repository
public interface DeviceIPAddressRepository extends JpaRepository<Device_IP_Address, String> {

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM device_ip_address WHERE device_id = ?1", nativeQuery = true)
	void deleteIPAddressByDeviceId(String id);

	@Query(nativeQuery = true)
	List<DeviceIPAddressDTO> getIPAddressByDeviceId(String id);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO device_ip_address (id, ip_address, ip_conflict_status, device_id)"
			+ " VALUES (?1, ?2, ?3, ?4)", nativeQuery = true)
	void insertIPAddressByDeviceId(String id, String ip_address, Integer ip_conflict_status, String device_id);

}
