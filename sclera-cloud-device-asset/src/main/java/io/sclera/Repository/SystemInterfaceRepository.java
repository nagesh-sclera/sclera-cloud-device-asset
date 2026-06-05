package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;

import jakarta.transaction.Transactional;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VlanDTO;
import io.sclera.models.Address;
import io.sclera.models.System_interface;


/**
 * Manages persistence and querying of {@link System_interface} entities.
 */
@Repository
public interface SystemInterfaceRepository  extends JpaRepository<System_interface, String> {



	/**
	 * Inserts a system interface status, or updates the status if the interface name already exists.
	 *
	 * @param interface_name interface name
	 * @param interface_status interface status
	 */
	@Modifying
	@Transactional
	// PG-port: ON DUPLICATE KEY -> ON CONFLICT (interface_name) DO UPDATE SET (VALUES->EXCLUDED); VALUE->VALUES
	@Query(value = "INSERT INTO system_interface(interface_name, status) VALUES (?1 , ?2) "
			+ "ON CONFLICT (interface_name) DO UPDATE SET status = EXCLUDED.status", nativeQuery = true)
	void upsertInterfaceStatus(String interface_name, String interface_status);



	/**
	 * Returns the status of the interface with the given name.
	 *
	 * @param interface_name interface name
	 * @return the interface status
	 */
	@Transactional
	@Query(value = "SELECT status FROM system_interface WHERE interface_name = ?1", nativeQuery = true)
	String getInterfaceStatus(String interface_name);



	/**
	 * Returns the status of all system interfaces.
	 *
	 * @return list of interface status details
	 */
	@Modifying
	@Transactional
	@Query(nativeQuery = true)
	List<DockerInfoDto> getInterfaceStatusList();


	/**
	 * Returns the VLAN discovery process details for the interface with the given name.
	 *
	 * @param interface_name interface name
	 * @return the VLAN discovery details for the interface
	 */
	@Transactional
	@Query(nativeQuery = true)
	VlanDTO getVlanDiscoverPidByInterfaceName(String interface_name);



	/**
	 * Updates the VLAN discovery process id and timestamp for the interface with the given name.
	 *
	 * @param pid VLAN discovery process id
	 * @param timestamp update timestamp
	 * @param interface_name interface name
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE system_interface SET pid = ?1, timestamp = ?2 WHERE interface_name = ?3", nativeQuery = true)
	void updateVlanDiscoverPidByInterfaceName(String pid, BigInteger timestamp, String interface_name);



	/**
	 * Deletes all system interface records.
	 */
	@Modifying
	@Transactional
	@Query(value = "DELETE from system_interface", nativeQuery = true)
	void deleteAllInterface();


}
