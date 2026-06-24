package io.sclera.repository;

import io.sclera.model.VdmsProfile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface VdmsProfileRepository extends JpaRepository<VdmsProfile, String>{


	@Modifying
	@Transactional
	@Query(value = "INSERT INTO vdms_profile(id,vdms_id,customer_org_id,vendor_org_id,profile_id) VALUE(?1,?2,?3,?4,?5)" ,nativeQuery = true)
	void tagProfileToVdmsByOrganisationId(String id, String vdms_id ,String customer_org_id, String vendor_org_id ,String profile_id);


	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_profile WHERE vdms_id = ?1" ,nativeQuery = true)
	void deleteVdmsProfileByVdmsId(String vdms_id);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM vdms_profile WHERE profile_id = ?1" ,nativeQuery = true)
	void deleteVdmsProfileByProfileId(String profile_id);

	
	@Query(value = "SELECT profile_id FROM vdms_profile WHERE vdms_id = ?1 AND vendor_org_id = ?2" ,nativeQuery = true)
	String getProfileIdByVendorOrganisationIdAndVdmsId(String vdms_id, String vendor_org_id);

	@Query(value = "SELECT COUNT(id) FROM vdms_profile WHERE vdms_id = ?1 AND customer_org_id = ?2" ,nativeQuery = true)
	int checkIfPrimaryProfileIsTaggedToVdmsByCustomerOrgId(String vdms_id, String organisation_id);

	@Query(value = "SELECT COUNT(id) FROM vdms_profile WHERE vdms_id = ?1 AND vendor_org_id = ?2" ,nativeQuery = true)
	int checkIfPrimaryProfileIsTaggedToVdmsByVendorOrgId(String vdms_id, String organisation_id);

	@Modifying
	@Transactional
	@Query(value = "DELETE from vdms_profile WHERE vdms_id = ?1 AND customer_org_id = ?2" ,nativeQuery = true)
	void deleteVdmsProfileByVdmsIdAndCustomerOrganisationId(String vdms_id, String customer_org_id);
}
