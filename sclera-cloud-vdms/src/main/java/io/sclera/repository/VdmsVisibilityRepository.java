package io.sclera.repository;

import io.sclera.dto.ExternalClientUserDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.model.Vdms_Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface VdmsVisibilityRepository extends JpaRepository<Vdms_Visibility, String> {

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO vdms_visibility(id,vdms_id,property_name,email) VALUE(?1,?2,?3,?4)" ,nativeQuery = true)
	void addVdmsVisibilityByEmail(String id, String vdms_id, String property_name ,String email);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_visibility WHERE vdms_id = ?1" ,nativeQuery = true)
	void deleteVdmsVisibilityByVdmsId(String vdms_id);

	@Query(nativeQuery = true)
	Set<VdmsDTO> getVisibleVdmsByEmail(String email);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_visibility WHERE email = ?1" ,nativeQuery = true)
	void deleteVisibleVdmsByEmail(String email);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE vdms_visibility SET property_name = ?1 WHERE vdms_id = ?2" ,nativeQuery = true)
	void editPropertyNameByVdmsId(String property_name, String vdms_id);

	@Query(value = "SELECT COUNT(id) FROM vdms_visibility WHERE vdms_id = ?1 AND property_name = ?2 AND email = ?3" ,nativeQuery = true)
    Integer checkVdmsVisibilityByVdmsIdAndPropertyName(String vdms_id, String property_name, String vendor);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_visibility WHERE email = ?1 AND vdms_id = ?2" ,nativeQuery = true)
	void deleteVisibleVdmsByEmailAndVdmsId(String email, String vdms_id);

	@Query(value = "SELECT email FROM vdms_visibility WHERE email = ?1 AND vdms_id = ?2" ,nativeQuery = true)
    String checkVisibleVdmsByEmailAndVdmsId(String email, String vdmsId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_visibility WHERE email IN (SELECT email FROM user WHERE customer_org_id = ?1) AND vdms_id = ?2", nativeQuery = true)
	void deleteVisibleVdmsByCustomerOrganisationIdAndVdmsId(String organisation_id, String vdms_id);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO vdms_visibility(id, email,full_access) VALUES(?1,?2,?3)", nativeQuery = true)
	void addVdmsFullAccessVisibilityByEmail(String id, String email, Integer fullAccess);

	@Query(value = "SELECT full_access FROM vdms_visibility WHERE email=?1 AND property_name IS NULL AND  vdms_id IS NULL", nativeQuery = true)
	Integer getVdmsFullAccessByEmail(String userEmail);

	@Query(nativeQuery = true)
	List<ExternalClientUserDTO> getVisibleVdmsInfoByEmail(String email);

	@Query(value = "SELECT vdms_id FROM vdms_visibility WHERE email = ?1",nativeQuery = true)
	List<String> getVisibleVdmsIdsByEmail(String email);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_visibility WHERE email = ?1 AND vdms_id IN ?2" ,nativeQuery = true)
	void deleteVisibleVdmsByEmailAndVdmsIds(String email, List<String> vdmsRelatedToOrg);

	@Query(value = "SELECT DISTINCT email FROM vdms_visibility WHERE vdms_id IN (?1)", nativeQuery = true)
	List<String> getEmailsByVdmsIds(List<String> vdmsIds);

	@Query(value = "SELECT email,full_access FROM vdms_visibility WHERE email IN ?1 AND property_name IS NULL AND  vdms_id IS NULL", nativeQuery = true)
	List<Object[]> getFullAccessByEmails(List<String> emails);

	@Query(nativeQuery = true)
	List<ExternalClientUserDTO> getVisibleVdmsByEmailList(List<String> emailList);
}
