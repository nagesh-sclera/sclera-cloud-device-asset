package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import io.sclera.dto.BuildingDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.models.Building;

/**
 * Manages persistence and querying of {@link Building} entities.
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, String>{

	/**
	 * Returns the building ids belonging to the given VDMS.
	 *
	 * @param vdms_id the VDMS identifier
	 * @return the matching building ids
	 */
	@Query("SELECT b.id FROM Building b WHERE b.vdms.id = ?1")
	Set<String> getBuildingIdsByVdmsId(String vdms_id);

	/**
	 * Inserts a new building for the given VDMS.
	 *
	 * @param building_id the building identifier
	 * @param name the building name
	 * @param vdms_id the owning VDMS identifier
	 * @param updated_timestamp the updated timestamp
	 * @return the number of rows affected
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: already valid PostgreSQL; entity save() would merge-SELECT the eager vdms graph (assigned @Id) and change insert->upsert semantics.
	@Query(value = "INSERT INTO building(id,name,vdms_id,updated_timestamp) VALUES(?1,?2,?3,?4)" , nativeQuery = true)
	int addBuildingByVdmsId(String building_id, String name, String vdms_id, BigInteger updated_timestamp);

	/**
	 * Updates the name and timestamp of the building with the given id.
	 *
	 * @param name the new building name
	 * @param building_id the building identifier
	 * @param updated_timestamp the updated timestamp
	 * @return the number of rows affected
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Building b SET b.name = ?1, b.updated_timestamp = ?3 WHERE b.id = ?2")
	int updateBuildingByBuildingId(String name, String building_id, BigInteger updated_timestamp);

	/**
	 * Returns the ids of buildings that are not tagged to any floor.
	 *
	 * @return the unlinked building ids
	 */
	@Query("SELECT b.id FROM Building b WHERE b.id NOT IN (SELECT f.building.id FROM Floor f WHERE f.building IS NOT NULL)")
	Set<String> getUnlinkedBuildingIds();

	/***************************************** new Building changes *******************************/

	/**
	 * Inserts a building, updating its name, code, and timestamp on id conflict.
	 *
	 * @param id the building identifier
	 * @param name the building name
	 * @param vdms_id the owning VDMS identifier
	 * @param code the building code
	 * @param updated_timestamp the updated timestamp
	 * @return the number of rows affected
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: already valid PostgreSQL; entity save() would merge-SELECT the eager vdms graph (assigned @Id).
	// PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
	@Query(value = "INSERT INTO building(id, name, vdms_id, code, updated_timestamp) VALUES(?1, ?2, ?3, ?4, ?5) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, code = EXCLUDED.code, updated_timestamp = EXCLUDED.updated_timestamp", nativeQuery = true)
	int upsertBuildingsByVdmsId(String id, String name, String vdms_id, String code, BigInteger updated_timestamp);

	/**
	 * Returns the building containing the given floor.
	 *
	 * @param floor_id the floor identifier
	 * @return the matching building projection
	 */
	@Query("SELECT new io.sclera.dto.BuildingDTO(b.id, b.name, b.vdms.id, b.code) FROM Building b JOIN b.floor f WHERE f.id = ?1")
	BuildingDTO getBuildingByFloorId(String floor_id);

	/**
	 * Returns the buildings belonging to the given VDMS.
	 *
	 * @param vdms_id the VDMS identifier
	 * @return the set of matching building projections
	 */
	@Query("SELECT new io.sclera.dto.BuildingDTO(b.id, b.name, b.vdms.id, b.code) FROM Building b WHERE b.vdms.id = ?1")
	Set<BuildingDTO> getBuildingsByVdmsId(String vdms_id);

	/**
	 * Returns the buildings belonging to the given VDMS for the ADC view.
	 *
	 * @param vdms_id the VDMS identifier
	 * @return the list of matching building projections
	 */
	@Query("SELECT new io.sclera.dto.BuildingDTO(b.id, b.name, b.vdms.id, b.code, b.id) FROM Building b WHERE b.vdms.id = ?1")
	List<BuildingDTO> getBuildingsByVdmsIdADC(String vdms_id);

	/**
	 * Returns the details of the building with the given id.
	 *
	 * @param building_id the building identifier
	 * @return the matching building projection
	 */
	@Query("SELECT new io.sclera.dto.BuildingDTO(b.id, b.name, b.vdms.id, b.code) FROM Building b WHERE b.id = ?1")
	BuildingDTO getBuildingDetailsByBuildingId(String building_id);

	/**
	 * Returns a page of buildings among the given building ids.
	 *
	 * @param buildingIds the building identifiers to match
	 * @param pageable    the page/size (use {@link org.springframework.data.domain.PageRequest#of(int, int)})
	 * @return the matching page of building projections
	 */
	@Query("SELECT new io.sclera.dto.BuildingDTO(b.id, b.name, b.vdms.id, b.code) FROM Building b WHERE b.id IN ?1")
    List<BuildingDTO> getBatchBuildingsByPagination(Set<String> buildingIds, Pageable pageable);

    /***************************************** new Building changes *******************************/

}
