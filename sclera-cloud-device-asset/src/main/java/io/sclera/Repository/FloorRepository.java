package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import io.sclera.dto.FloorDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.models.Floor;

@Repository
public interface FloorRepository extends JpaRepository<Floor, String> {
	
	@Query(value = "SELECT id FROM floor WHERE building_id = ?1" , nativeQuery = true)
	Set<String> getFloorIdsByBuildingId(String building_id);
	
	@Query(value = "SELECT image_url FROM floor WHERE id = ?1" ,nativeQuery = true)
	String getImageUrlById(String floor_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET name = ?1 ,initial_position = ?2 ,image_url = ?3, angle = ?5 WHERE id = ?4" , nativeQuery = true)
	void updateFloorByFloorId(String name, String initial_position, String image_url, String floor_id, String angle);
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO floor(id,name,initial_position,image_url,building_id,angle) VALUE(?1,?2,?3,?4,?5,?6)" , nativeQuery = true)
	void addFloorByBuildingId(String floor_id, String name, String initial_position ,String image_url ,String building_id, String angle);
	
	//Get floor ids not tagged to a location
	@Query(value = "SELECT id FROM floor f WHERE id NOT IN (SELECT l.floor_id FROM location l WHERE f.id = l.floor_id)" , nativeQuery = true)
	Set<String> getUnlinkedFloorIds();


	/*************************************************************** new Floor changes ******************************************************************/

	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET name = ?1 ,initial_position = ?2 ,image_url = ?3, angle = ?5, updated_timestamp = ?6 WHERE id = ?4" , nativeQuery = true)
	int updateFloorByFloorId(String name, String initial_position, String image_url, String floor_id, Integer angle, BigInteger timestamp);


	@Modifying
	@Transactional
	@Query(value = "INSERT INTO floor(id,name,initial_position,image_url,building_id,angle,updated_timestamp) VALUE(?1,?2,?3,?4,?5,?6,?7)" , nativeQuery = true)
	int addFloorByBuildingId(String floor_id, String name, String initial_position ,String image_url ,String building_id, Integer angle, BigInteger timestamp);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO floor(id,name,initial_position ,angle ,building_id, updated_timestamp) VALUES(?1,?2,?3,?4,?5,?6) ON DUPLICATE KEY UPDATE name = ?2, updated_timestamp = ?6 " , nativeQuery = true)
	int upsertFloorByBuildingId(String floor_id, String name, String initial_position, Integer angle, String building_id, BigInteger timestamp);


	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET initial_position = ?1 , angle = ?2 WHERE building_id = ?3" ,nativeQuery = true)
	void updateFloorOrientationsByBuildingId(String initial_position, Integer angle, String building_id);


	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET image_url = ?1 ,initial_position = ?2 ,angle = ?3 WHERE id = ?4" ,nativeQuery = true)
	void addFloorImageUrlById(String updated_image_url, String initial_position, Integer angle, String floor_id);


	@Query(nativeQuery = true)
	Set<FloorDTO> getFloorsByBuildingId(String building_id);


	@Query(nativeQuery = true)
	List<FloorDTO> getFloorsByBuildingIds(List<String> buildingIds);

	@Query(nativeQuery = true)
	FloorDTO getFloorByLocationId(String location_id);

	@Query(nativeQuery = true)
	Set<FloorDTO> getFloorsDetailsByBuildingId(String building_id);

	@Query(nativeQuery = true)
	FloorDTO getFloorById(String floor_id);

	@Query(value = "SELECT path FROM floor WHERE id = ?1" , nativeQuery = true)
	String getFloorPathByFloorId(String floor_id);
	
	@Query(nativeQuery = true)
	FloorDTO getFloor(String floor_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET path = ?1 WHERE id = ?2" , nativeQuery = true)
	void updatePathByFloorId(String path, String floor_id);


	@Modifying
	@Transactional
	@Query(value = "INSERT INTO floor(id,name,initial_position ,angle ,building_id, image_url, path, updated_timestamp) VALUES(?1,?2,?3,?4,?5,?6,?7,?8) ON DUPLICATE KEY UPDATE name = ?2, initial_position = ?3, angle = ?4, building_id = ?5, image_url = ?6, path = ?7, updated_timestamp = ?8 " , nativeQuery = true)
	int upsertFloorByBuildingIdsFromBackend(String floor_id, String name, String initial_position, Integer angle, String building_id, String image_url, String path, BigInteger timestamp);

	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET min_zoom = ?1, max_zoom = ?2  WHERE id = ?3" , nativeQuery = true)
	void updateFloorMapZoomLevels(String min_zoom, String max_zoom, String floor_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET local_image_url = ?1 WHERE id = ?2" , nativeQuery = true)
	void updateLocalImageUrl(String local_image_url, String floor_id);

	//delete after sync is done
	@Modifying
	@Transactional
	@Query(value = "UPDATE floor SET local_image_url = ?1, image_url = ?2 WHERE id = ?3" , nativeQuery = true)
	void updateImageUrls(String local_image_url, String image_url, String floor_id);

	@Query(nativeQuery = true)
	List<FloorDTO> getBatchFloorsByPagination(Set<String> floorIds, int pageSize, int offset);

	@Query(nativeQuery = true)
	List<FloorDTO> getFloorIdsByBuildingIds(Set<String> buildingIds, int pageSize, int offset);
	/*************************************************************** new Floor changes ******************************************************************/


}
