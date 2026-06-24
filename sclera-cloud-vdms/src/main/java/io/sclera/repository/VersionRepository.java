package io.sclera.repository;

import io.sclera.dto.VersionDTO;
import io.sclera.model.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface VersionRepository extends JpaRepository<Version, String> {


	@Query(nativeQuery = true)
	VersionDTO getVdmsVersion();

	@Modifying
	@Transactional
	@Query(value = "UPDATE version SET version = ?1" ,nativeQuery = true)
	void updateVdmsVersion(String version);
}
