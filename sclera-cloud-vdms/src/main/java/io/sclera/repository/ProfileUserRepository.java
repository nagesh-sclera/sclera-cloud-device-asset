package io.sclera.repository;

import io.sclera.dto.ProfileUserDTO;
import io.sclera.model.ProfileUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProfileUserRepository extends JpaRepository<ProfileUser, String> {

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile_user WHERE profile_id = ?1" ,nativeQuery = true)
	void deleteProfileUsersByProfileId(String profile_id);

	
	@Query(nativeQuery = true)
	Set<ProfileUserDTO> getProfileUsersByProfileId(String profile_id);

}
