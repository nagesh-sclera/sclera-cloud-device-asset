package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.metamodel.model.convert.spi.JpaAttributeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VlanDTO;
import io.sclera.models.Address;
import io.sclera.models.System_interface;


@Repository
public interface SystemInterfaceRepository  extends JpaRepository<System_interface, String> {

	
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO system_interface(interface_name, status) VALUE (?1 , ?2)"
			+ "ON DUPLICATE KEY UPDATE status = ?2", nativeQuery = true)
	void upsertInterfaceStatus(String interface_name, String interface_status);

	
	
	@Transactional
	@Query(value = "SELECT status FROM system_interface WHERE interface_name = ?1", nativeQuery = true)
	String getInterfaceStatus(String interface_name);



	@Modifying
	@Transactional
	@Query(nativeQuery = true)
	List<DockerInfoDto> getInterfaceStatusList();
	
	
	@Transactional
	@Query(nativeQuery = true)
	VlanDTO getVlanDiscoverPidByInterfaceName(String interface_name);
	
	
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE system_interface SET pid = ?1, timestamp = ?2 WHERE interface_name = ?3", nativeQuery = true)
	void updateVlanDiscoverPidByInterfaceName(String pid, BigInteger timestamp, String interface_name);



	@Modifying
	@Transactional
	@Query(value = "DELETE from system_interface", nativeQuery = true)
	void deleteAllInterface();
	

}
