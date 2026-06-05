package io.sclera.Repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.touchscreen.DeviceIPAddressDTO;
import io.sclera.models.Device_IP_Address;


/**
 * Manages persistence and querying of {@link Device_IP_Address} entities.
 */
@Repository
public interface DeviceIPAddressRepository extends JpaRepository<Device_IP_Address, String> {

	/**
	 * Deletes all IP-address records for the given device.
	 *
	 * @param id the device identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM device_ip_address WHERE device_id = ?1", nativeQuery = true)
	void deleteIPAddressByDeviceId(String id);

	/**
	 * Returns the IP-address records for the given device.
	 *
	 * @param id the device identifier
	 * @return the matching IP-address projections
	 */
	@Query(nativeQuery = true)
	List<DeviceIPAddressDTO> getIPAddressByDeviceId(String id);

	/**
	 * Inserts an IP-address record for a device.
	 *
	 * @param id                 the IP-address record identifier
	 * @param ip_address         the IP address
	 * @param ip_conflict_status the IP conflict status flag
	 * @param device_id          the device identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO device_ip_address (id, ip_address, ip_conflict_status, device_id)"
			+ " VALUES (?1, ?2, ?3, ?4)", nativeQuery = true)
	void insertIPAddressByDeviceId(String id, String ip_address, Integer ip_conflict_status, String device_id);

}
