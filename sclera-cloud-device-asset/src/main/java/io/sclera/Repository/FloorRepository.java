package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import io.sclera.dto.FloorDTO;
import org.springframework.data.domain.Pageable;
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
	@Query("SELECT f.id FROM Floor f WHERE f.building.id = ?1")
	Set<String> getFloorIdsByBuildingId(String building_id);

	/**
	 * Returns the image URL of the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the floor image URL
	 */
	@Query("SELECT f.image_url FROM Floor f WHERE f.id = ?1")
	String getImageUrlById(String floor_id);

	/**
	 * NOT CONVERTED — stays native: String angle param into Integer column;
	 * JPQL bulk UPDATE rejects the type mismatch (and this duplicates the Integer-angle overload).
	 *
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
	 * NOT CONVERTED — stays native: plain INSERT already valid PostgreSQL;
	 * entity save() is worse (assigned @Id -> merge -> SELECT eagerly loads @ManyToOne building graph).
	 *
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
	@Query("SELECT f.id FROM Floor f WHERE f.id NOT IN (SELECT l.floor.id FROM Location l WHERE l.floor IS NOT NULL)")
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
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.name = ?1, f.initial_position = ?2, f.image_url = ?3, f.angle = ?5, f.updatedTimestamp = ?6 WHERE f.id = ?4")
	int updateFloorByFloorId(String name, String initial_position, String image_url, String floor_id, Integer angle, BigInteger timestamp);


	/**
	 * NOT CONVERTED — stays native: plain INSERT already valid PostgreSQL;
	 * entity save() is worse (assigned @Id -> merge -> SELECT eagerly loads @ManyToOne building graph).
	 *
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
	 * NOT CONVERTED — stays native: INSERT ... ON CONFLICT already valid PostgreSQL;
	 * not worth an entity save() that merge-SELECTs the eager building graph.
	 *
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
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.initial_position = ?1, f.angle = ?2 WHERE f.building.id = ?3")
	void updateFloorOrientationsByBuildingId(String initial_position, Integer angle, String building_id);


	/**
	 * Updates the image URL, initial position, and angle of the given floor.
	 *
	 * @param updated_image_url the new image URL
	 * @param initial_position the new initial position
	 * @param angle the new angle
	 * @param floor_id the floor identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.image_url = ?1, f.initial_position = ?2, f.angle = ?3 WHERE f.id = ?4")
	void addFloorImageUrlById(String updated_image_url, String initial_position, Integer angle, String floor_id);


	/**
	 * Returns the floors of the given building.
	 * Supports 'all' as a wildcard to return floors from any building.
	 *
	 * @param building_id the building identifier (or 'all')
	 * @return the matching floors
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name) FROM Floor f WHERE ('all' = ?1 OR f.building.id = ?1) ORDER BY f.name, f.id")
	Set<FloorDTO> getFloorsByBuildingId(String building_id);


	/**
	 * Returns the floors belonging to any of the given buildings.
	 *
	 * @param buildingIds the building identifiers
	 * @return the matching floors
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name, f.building.id) FROM Floor f WHERE f.building.id IN ?1")
	List<FloorDTO> getFloorsByBuildingIds(List<String> buildingIds);

	/**
	 * Returns the floor that contains the given location.
	 *
	 * @param location_id the location identifier
	 * @return the matching floor
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name) FROM Floor f JOIN f.location l WHERE l.id = ?1")
	FloorDTO getFloorByLocationId(String location_id);

	/**
	 * Returns the detailed floor records of the given building.
	 *
	 * @param building_id the building identifier
	 * @return the matching floor details
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name, f.initial_position, f.image_url, f.building.id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url) FROM Floor f WHERE f.building.id = ?1")
	Set<FloorDTO> getFloorsDetailsByBuildingId(String building_id);

	/**
	 * Returns the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the matching floor
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name, f.initial_position, f.image_url, f.building.id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url) FROM Floor f WHERE f.id = ?1")
	FloorDTO getFloorById(String floor_id);

	/**
	 * Returns the path of the floor with the given identifier.
	 *
	 * @param floor_id the floor identifier
	 * @return the floor path
	 */
	@Query("SELECT f.path FROM Floor f WHERE f.id = ?1")
	String getFloorPathByFloorId(String floor_id);

	/**
	 * Returns the floor with the given identifier (id and name only).
	 *
	 * @param floor_id the floor identifier
	 * @return the matching floor
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name) FROM Floor f WHERE f.id = ?1")
	FloorDTO getFloor(String floor_id);

	/**
	 * Updates the path of the floor with the given identifier.
	 *
	 * @param path the new path
	 * @param floor_id the floor identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.path = ?1 WHERE f.id = ?2")
	void updatePathByFloorId(String path, String floor_id);


	/**
	 * NOT CONVERTED — stays native: INSERT ... ON CONFLICT already valid PostgreSQL;
	 * not worth an entity save() that merge-SELECTs the eager building graph.
	 *
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
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.min_zoom = ?1, f.max_zoom = ?2 WHERE f.id = ?3")
	void updateFloorMapZoomLevels(String min_zoom, String max_zoom, String floor_id);

	/**
	 * Updates the local image URL of the given floor.
	 *
	 * @param local_image_url the new local image URL
	 * @param floor_id the floor identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.local_image_url = ?1 WHERE f.id = ?2")
	void updateLocalImageUrl(String local_image_url, String floor_id);

	/**
	 * Updates both the local image URL and the image URL of the given floor.
	 *
	 * @param local_image_url the new local image URL
	 * @param image_url the new image URL
	 * @param floor_id the floor identifier
	 */
	//delete after sync is done
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Floor f SET f.local_image_url = ?1, f.image_url = ?2 WHERE f.id = ?3")
	void updateImageUrls(String local_image_url, String image_url, String floor_id);

	/**
	 * Returns a paginated batch of floors matching the given identifiers.
	 *
	 * @param floorIds the floor identifiers
	 * @param pageable pagination (page size and offset via {@code PageRequest.of(offset/pageSize, pageSize)})
	 * @return the matching floors for the page
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name, f.initial_position, f.image_url, f.building.id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url) FROM Floor f WHERE f.id IN ?1")
	List<FloorDTO> getBatchFloorsByPagination(Set<String> floorIds, Pageable pageable);

	/**
	 * Returns a paginated list of floors belonging to the given buildings.
	 *
	 * @param buildingIds the building identifiers
	 * @param pageable pagination (page size and offset via {@code PageRequest.of(offset/pageSize, pageSize)})
	 * @return the matching floors for the page
	 */
	@Query("SELECT new io.sclera.dto.FloorDTO(f.id, f.name, f.initial_position, f.image_url, f.building.id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url) FROM Floor f WHERE f.building.id IN ?1")
	List<FloorDTO> getFloorIdsByBuildingIds(Set<String> buildingIds, Pageable pageable);
	/*************************************************************** new Floor changes ******************************************************************/


}
