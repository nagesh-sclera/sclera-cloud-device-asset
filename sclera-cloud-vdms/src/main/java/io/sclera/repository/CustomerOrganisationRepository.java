package io.sclera.repository;

import io.sclera.dto.CustomerOrganisationDto;
import io.sclera.model.Customer_Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface CustomerOrganisationRepository  extends JpaRepository<Customer_Organisation, String>{

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO customer_organisation(id,company_name,is_enterprise) VALUE(?1,?2,?3)" , nativeQuery = true)
	void addCustomerOrganisationById(String customer_org_id ,String company_name ,Integer is_enterprise);

	@Modifying
	@Transactional
	@Query(value = "SELECT id FROM customer_organisation" ,nativeQuery = true)
	List<String> getAllOrganisationIds();

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM customer_organisation WHERE id = ?1" ,nativeQuery = true)
	void deleteCustomerOrganisationById(String customer_org_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE customer_organisation SET company_name = ?1 WHERE id = ?2" ,nativeQuery = true)
	void updateCompanyNameByOrganisationId(String company_name, String organisation_id);

	@Query(value = "SELECT is_enterprise FROM customer_organisation WHERE id = ?1 " ,nativeQuery = true)
	Integer getEnterpriseInfoById(String customerOrgId);

	@Query(nativeQuery = true)
	List<CustomerOrganisationDto> getAllOrgIdAndCompanyName();

    @Query(value = "SELECT company_name  FROM customer_organisation WHERE id = ?1 " ,nativeQuery = true)
    String getOrgNameByOrgId(String orgId);
}
