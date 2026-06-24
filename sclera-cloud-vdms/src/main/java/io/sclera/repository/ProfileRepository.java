package io.sclera.repository;

import io.sclera.model.Profile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String>{


	@Query(value = "SELECT id FROM profile WHERE customer_org_id = ?1 AND is_primary = true" ,nativeQuery = true)
	String checkFavouriteByCustomerOrganisationId(String organisation_id);

	@Query(value = "SELECT id FROM profile WHERE vendor_org_id = ?1 AND is_primary = true" ,nativeQuery = true)
	String checkFavouriteByVendorOrgansiationId(String organisation_id);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile WHERE id  = ?1" ,nativeQuery = true)
	void deleteProfileByProfileId(String profile_id, HttpServletRequest httpServletRequest);


	@Query(value = "SELECT id FROM profile WHERE customer_org_id = ?1" ,nativeQuery = true)
	Set<String> getAllProfilesByCustomerOrganisationId(String organisation_id);

}
