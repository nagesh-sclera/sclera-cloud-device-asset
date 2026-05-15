package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import io.sclera.dto.BuildingDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.models.Building;

@Repository
public interface BuildingRepository extends JpaRepository<Building, String>{
	
	@Query(value = "SELECT id FROM building WHERE vdms_id = ?1" , nativeQuery = true)
	Set<String> getBuildingIdsByVdmsId(String vdms_id);
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO building(id,name,vdms_id,updated_timestamp) VALUE(?1,?2,?3,?4)" , nativeQuery = true)
	int addBuildingByVdmsId(String building_id, String name, String vdms_id, BigInteger updated_timestamp);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE building SET name = ?1 ,updated_timestamp = ?3  WHERE id = ?2" , nativeQuery = true)
	int updateBuildingByBuildingId(String name, String building_id, BigInteger updated_timestamp);
	
	//get building ids not tagged to a floor
	@Query(value = "SELECT id FROM building b WHERE id NOT IN (SELECT f.building_id FROM floor f WHERE b.id = f.building_id)",nativeQuery = true)
	Set<String> getUnlinkedBuildingIds();

	/***************************************** new Building changes *******************************/

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO building(id, name, vdms_id, code, updated_timestamp) VALUES(?1, ?2, ?3, ?4, ?5) ON DUPLICATE KEY UPDATE name = ?2, code = ?4, updated_timestamp = ?5 ", nativeQuery = true)
	int upsertBuildingsByVdmsId(String id, String name, String vdms_id, String code, BigInteger updated_timestamp);

	@Query(nativeQuery = true)
	BuildingDTO getBuildingByFloorId(String floor_id);

	@Query(nativeQuery = true)
	Set<BuildingDTO> getBuildingsByVdmsId(String vdms_id);

	@Query(nativeQuery = true)
	List<BuildingDTO> getBuildingsByVdmsIdADC(String vdms_id);

	@Query(nativeQuery = true)
	BuildingDTO getBuildingDetailsByBuildingId(String building_id);

	@Query(nativeQuery = true)
    List<BuildingDTO> getBatchBuildingsByPagination(Set<String> buildingIds, int pageSize, int offset);

    /***************************************** new Building changes *******************************/

}
