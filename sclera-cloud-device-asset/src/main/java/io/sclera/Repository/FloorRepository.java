package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import io.sclera.dto.FloorDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.models.Floor;

/**
 * Spring Data repository for {@code Floor} entities, providing persistence and
 * querying of floors and their building associations.
 */
@Repository
public interface FloorRepository extends JpaRepository<Floor, String> {

	/**
	 * Returns the identifiers of all floors belonging to the given building.
	 *
	 * @param building_id the building identifier
	 * @return the matching floor identifiers
	 */
	@Query(value = "SELECT id FROM floor WHERE building_id = ?1" , nativeQuery = true)
	Set<String> getFloorIdsByBuildingId(String building_id);

	/**
	 * Returns the image URL of the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the floor image URL
	 */
	@Query(value = "SELECT image_url FROM floor WHERE id = ?1" ,nativeQuery = true)
	String getImageUrlById(String floor_id);

	/**
	 * Updates the name, initial position, image URL, and angle of the given floor.
	 *
	 * @param name the new floor name
	 * @param initial_position the new initial position
	 * @param image_url the new image URL
	 * @param floor_id the floor identifier
	 * @param angle the new angle
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET name = ?1 ,initial_position = ?2 ,image_url = ?3, angle = ?5 WHERE id = ?4" , nativeQuery = true)
	void updateFloorByFloorId(String name, String initial_position, String image_url, String floor_id, String angle);

	/**
	 * Inserts a new floor for the given building.
	 *
	 * @param floor_id the floor identifier
	 * @param name the floor name
	 * @param initial_position the initial position
	 * @param image_url the image URL
	 * @param building_id the owning building identifier
	 * @param angle the angle
	 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO floor(id,name,initial_position,image_url,building_id,angle) VALUES(?1,?2,?3,?4,?5,?6)" , nativeQuery = true)
	void addFloorByBuildingId(String floor_id, String name, String initial_position ,String image_url ,String building_id, String angle);

	/**
	 * Returns the identifiers of floors that are not tagged to any location.
	 *
	 * @return the unlinked floor identifiers
	 */
	//Get floor ids not tagged to a location
	@Query(value = "SELECT id FROM floor f WHERE id NOT IN (SELECT l.floor_id FROM location l WHERE f.id = l.floor_id)" , nativeQuery = true)
	Set<String> getUnlinkedFloorIds();


	/*************************************************************** new Floor changes ******************************************************************/

	/**
	 * Updates the name, initial position, image URL, angle, and update timestamp of the given floor.
	 *
	 * @param name the new floor name
	 * @param initial_position the new initial position
	 * @param image_url the new image URL
	 * @param floor_id the floor identifier
	 * @param angle the new angle
	 * @param timestamp the update timestamp
	 * @return the number of rows updated
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET name = ?1 ,initial_position = ?2 ,image_url = ?3, angle = ?5, updated_timestamp = ?6 WHERE id = ?4" , nativeQuery = true)
	int updateFloorByFloorId(String name, String initial_position, String image_url, String floor_id, Integer angle, BigInteger timestamp);


	/**
	 * Inserts a new floor for the given building with an update timestamp.
	 *
	 * @param floor_id the floor identifier
	 * @param name the floor name
	 * @param initial_position the initial position
	 * @param image_url the image URL
	 * @param building_id the owning building identifier
	 * @param angle the angle
	 * @param timestamp the update timestamp
	 * @return the number of rows inserted
	 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO floor(id,name,initial_position,image_url,building_id,angle,updated_timestamp) VALUES(?1,?2,?3,?4,?5,?6,?7)" , nativeQuery = true)
	int addFloorByBuildingId(String floor_id, String name, String initial_position ,String image_url ,String building_id, Integer angle, BigInteger timestamp);

	/**
	 * Inserts a floor for the given building, or updates its name and timestamp on identifier conflict.
	 *
	 * @param floor_id the floor identifier
	 * @param name the floor name
	 * @param initial_position the initial position
	 * @param angle the angle
	 * @param building_id the owning building identifier
	 * @param timestamp the update timestamp
	 * @return the number of rows affected
	 */
	@Modifying
	@Transactional
	// PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
	@Query(value = "INSERT INTO floor(id,name,initial_position ,angle ,building_id, updated_timestamp) VALUES(?1,?2,?3,?4,?5,?6) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, updated_timestamp = EXCLUDED.updated_timestamp" , nativeQuery = true)
	int upsertFloorByBuildingId(String floor_id, String name, String initial_position, Integer angle, String building_id, BigInteger timestamp);


	/**
	 * Updates the initial position and angle of all floors in the given building.
	 *
	 * @param initial_position the new initial position
	 * @param angle the new angle
	 * @param building_id the building identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET initial_position = ?1 , angle = ?2 WHERE building_id = ?3" ,nativeQuery = true)
	void updateFloorOrientationsByBuildingId(String initial_position, Integer angle, String building_id);


	/**
	 * Updates the image URL, initial position, and angle of the given floor.
	 *
	 * @param updated_image_url the new image URL
	 * @param initial_position the new initial position
	 * @param angle the new angle
	 * @param floor_id the floor identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET image_url = ?1 ,initial_position = ?2 ,angle = ?3 WHERE id = ?4" ,nativeQuery = true)
	void addFloorImageUrlById(String updated_image_url, String initial_position, Integer angle, String floor_id);


	/**
	 * Returns the floors of the given building.
	 *
	 * @param building_id the building identifier
	 * @return the matching floors
	 */
	@Query(nativeQuery = true)
	Set<FloorDTO> getFloorsByBuildingId(String building_id);


	/**
	 * Returns the floors belonging to any of the given buildings.
	 *
	 * @param buildingIds the building identifiers
	 * @return the matching floors
	 */
	@Query(nativeQuery = true)
	List<FloorDTO> getFloorsByBuildingIds(List<String> buildingIds);

	/**
	 * Returns the floor that contains the given location.
	 *
	 * @param location_id the location identifier
	 * @return the matching floor
	 */
	@Query(nativeQuery = true)
	FloorDTO getFloorByLocationId(String location_id);

	/**
	 * Returns the detailed floor records of the given building.
	 *
	 * @param building_id the building identifier
	 * @return the matching floor details
	 */
	@Query(nativeQuery = true)
	Set<FloorDTO> getFloorsDetailsByBuildingId(String building_id);

	/**
	 * Returns the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the matching floor
	 */
	@Query(nativeQuery = true)
	FloorDTO getFloorById(String floor_id);

	/**
	 * Returns the path of the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the floor path
	 */
	@Query(value = "SELECT path FROM floor WHERE id = ?1" , nativeQuery = true)
	String getFloorPathByFloorId(String floor_id);

	/**
	 * Returns the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the matching floor
	 */
	@Query(nativeQuery = true)
	FloorDTO getFloor(String floor_id);

	/**
	 * Updates the path of the floor with the given identifier.
	 *
	 * @param path the new path
	 * @param floor_id the floor identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET path = ?1 WHERE id = ?2" , nativeQuery = true)
	void updatePathByFloorId(String path, String floor_id);


	/**
	 * Inserts a floor from the backend, or updates all of its fields on identifier conflict.
	 *
	 * @param floor_id the floor identifier
	 * @param name the floor name
	 * @param initial_position the initial position
	 * @param angle the angle
	 * @param building_id the owning building identifier
	 * @param image_url the image URL
	 * @param path the floor path
	 * @param timestamp the update timestamp
	 * @return the number of rows affected
	 */
	@Modifying
	@Transactional
	// PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
	@Query(value = "INSERT INTO floor(id,name,initial_position ,angle ,building_id, image_url, path, updated_timestamp) VALUES(?1,?2,?3,?4,?5,?6,?7,?8) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, initial_position = EXCLUDED.initial_position, angle = EXCLUDED.angle, building_id = EXCLUDED.building_id, image_url = EXCLUDED.image_url, path = EXCLUDED.path, updated_timestamp = EXCLUDED.updated_timestamp" , nativeQuery = true)
	int upsertFloorByBuildingIdsFromBackend(String floor_id, String name, String initial_position, Integer angle, String building_id, String image_url, String path, BigInteger timestamp);

	/**
	 * Updates the minimum and maximum map zoom levels of the given floor.
	 *
	 * @param min_zoom the new minimum zoom level
	 * @param max_zoom the new maximum zoom level
	 * @param floor_id the floor identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET min_zoom = ?1, max_zoom = ?2  WHERE id = ?3" , nativeQuery = true)
	void updateFloorMapZoomLevels(String min_zoom, String max_zoom, String floor_id);

	/**
	 * Updates the local image URL of the given floor.
	 *
	 * @param local_image_url the new local image URL
	 * @param floor_id the floor identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET local_image_url = ?1 WHERE id = ?2" , nativeQuery = true)
	void updateLocalImageUrl(String local_image_url, String floor_id);

	/**
	 * Updates both the local image URL and the image URL of the given floor.
	 *
	 * @param local_image_url the new local image URL
	 * @param image_url the new image URL
	 * @param floor_id the floor identifier
	 */
	//delete after sync is done
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET local_image_url = ?1, image_url = ?2 WHERE id = ?3" , nativeQuery = true)
	void updateImageUrls(String local_image_url, String image_url, String floor_id);

	/**
	 * Returns a paginated batch of floors matching the given identifiers.
	 *
	 * @param floorIds the floor identifiers
	 * @param pageSize the maximum number of rows to return
	 * @param offset the starting row offset
	 * @return the matching floors for the page
	 */
	@Query(nativeQuery = true)
	List<FloorDTO> getBatchFloorsByPagination(Set<String> floorIds, int pageSize, int offset);

	/**
	 * Returns a paginated list of floors belonging to the given buildings.
	 *
	 * @param buildingIds the building identifiers
	 * @param pageSize the maximum number of rows to return
	 * @param offset the starting row offset
	 * @return the matching floors for the page
	 */
	@Query(nativeQuery = true)
	List<FloorDTO> getFloorIdsByBuildingIds(Set<String> buildingIds, int pageSize, int offset);
	/*************************************************************** new Floor changes ******************************************************************/


}
