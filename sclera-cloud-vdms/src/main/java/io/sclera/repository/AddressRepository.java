package io.sclera.repository;

import io.sclera.dto.AddressDTO;
import io.sclera.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

	@Query(nativeQuery = true)
	AddressDTO getAddressDetailsById(String id);
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO address(id ,address ,city ,country ,state ,zip) VALUES(?1,?2,?3,?4,?5,?6)" , nativeQuery = true)
	void addAddress(String id, String address, String city, String country, String state, String zip);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE address SET address = ?1 ,city = ?2 ,state = ?3 ,zip = ?4 ,country = ?5 WHERE id = ?6" , nativeQuery = true)
	void editAddress(String address ,String city ,String state ,String zip ,String country ,String id);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM address WHERE id = ?1" , nativeQuery = true)
	void deleteAddressById(String id);

}
