package io.sclera.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.BuildingDTO;
import jakarta.servlet.http.HttpServletRequest;

/** Service contract for {@link io.sclera.service.BuildingService}. */
public interface BuildingService {
    void upsertBuildingByVdmsId(BuildingDTO buildingdto, String vdms_id);

    void updateBuildingByBuildingId(BuildingDTO buildingdto);

    void syncBuildingToADCServer(BuildingDTO buildingDTO);

    String addBuildingByVdmsId(BuildingDTO buildingdto, String vdms_id);

    boolean compareIds(Set<String> building_ids, String building_id);

    void deleteUnlinkedBuildings();

    void synclocationbyId(String location_id, String vdms_id);

    Set<BuildingDTO> upsertBuildingsByVdmsId(String username, String vdms_id, Set<BuildingDTO> buildings, HttpServletRequest httpServletRequest);

    BuildingDTO getBuildingByLocationId(String username, String vdms_id, String location_id);

    Set<BuildingDTO> getBuildingsByVdmsId(String vdms_id, String field, String field_id);

    void deleteBuildingsByIdsSync(String username, String vdmsid, Set<String> buildingIds);

    List<BuildingDTO> getBatchBuildingsByPagination(Set<String> buildingIds);

    void deleteBuildingsByIds(String username, String vdmsid, Set<String> building_ids, HttpServletRequest httpServletRequest);

    void syncDeleteBuildingToADC(String buildingId);

    Map<String, Object> syncLocationsFromBackend(HttpServletRequest httpServletRequest);

    Set<FloorDTO> syncFloorMaps(String vdms_id);

    List<FloorDTO> updateFloorMaps(String vdms_id, List<FloorDTO> floorDTOS);

    List<FloorDTO> syncFloorMapsTiles();
}
