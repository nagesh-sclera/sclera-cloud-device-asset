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
	 * Returns the IP-address records for the given device.
	 *
	 * @param id the device identifier
	 * @return the matching IP-address projections
	 */
	@Query("SELECT new io.sclera.dto.touchscreen.DeviceIPAddressDTO(d.ip_address, d.ip_conflict_status) " +
	       "FROM Device_IP_Address d WHERE d.device.id = ?1")
	List<DeviceIPAddressDTO> getIPAddressByDeviceId(String id);

	/**
	 * Deletes all IP-address records for the given device.
	 *
	 * @param id the device identifier
	 */
	@Modifying
	@Transactional
	@Query("DELETE FROM Device_IP_Address d WHERE d.device.id = ?1")
	void deleteIPAddressByDeviceId(String id);

}
