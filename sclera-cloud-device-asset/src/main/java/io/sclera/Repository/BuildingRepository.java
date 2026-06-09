package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import io.sclera.dto.BuildingDTO;
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
	@Query(value = "SELECT id FROM building WHERE vdms_id = ?1" , nativeQuery = true)
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
	@Modifying
	@Transactional
	@Query(value = "UPDATE building SET name = ?1 ,updated_timestamp = ?3  WHERE id = ?2" , nativeQuery = true)
	int updateBuildingByBuildingId(String name, String building_id, BigInteger updated_timestamp);

	/**
	 * Returns the ids of buildings that are not tagged to any floor.
	 *
	 * @return the unlinked building ids
	 */
	//get building ids not tagged to a floor
	@Query(value = "SELECT id FROM building b WHERE id NOT IN (SELECT f.building_id FROM floor f WHERE b.id = f.building_id)",nativeQuery = true)
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
	// PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
	@Query(value = "INSERT INTO building(id, name, vdms_id, code, updated_timestamp) VALUES(?1, ?2, ?3, ?4, ?5) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, code = EXCLUDED.code, updated_timestamp = EXCLUDED.updated_timestamp", nativeQuery = true)
	int upsertBuildingsByVdmsId(String id, String name, String vdms_id, String code, BigInteger updated_timestamp);

	/**
	 * Returns the building containing the given floor.
	 *
	 * @param floor_id the floor identifier
	 * @return the matching building projection
	 */
	@Query(nativeQuery = true)
	BuildingDTO getBuildingByFloorId(String floor_id);

	/**
	 * Returns the buildings belonging to the given VDMS.
	 *
	 * @param vdms_id the VDMS identifier
	 * @return the set of matching building projections
	 */
	@Query(nativeQuery = true)
	Set<BuildingDTO> getBuildingsByVdmsId(String vdms_id);

	/**
	 * Returns the buildings belonging to the given VDMS for the ADC view.
	 *
	 * @param vdms_id the VDMS identifier
	 * @return the list of matching building projections
	 */
	@Query(nativeQuery = true)
	List<BuildingDTO> getBuildingsByVdmsIdADC(String vdms_id);

	/**
	 * Returns the details of the building with the given id.
	 *
	 * @param building_id the building identifier
	 * @return the matching building projection
	 */
	@Query(nativeQuery = true)
	BuildingDTO getBuildingDetailsByBuildingId(String building_id);

	/**
	 * Returns a page of buildings among the given building ids.
	 *
	 * @param buildingIds the building identifiers to match
	 * @param pageSize the maximum number of buildings to return
	 * @param offset the number of buildings to skip
	 * @return the matching page of building projections
	 */
	@Query(nativeQuery = true)
    List<BuildingDTO> getBatchBuildingsByPagination(Set<String> buildingIds, int pageSize, int offset);

    /***************************************** new Building changes *******************************/

}
