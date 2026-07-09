package io.sclera.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import io.sclera.dto.FloorDTO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

/** Service contract for {@link io.sclera.service.FloorService}. */
public interface FloorService {

    String getServer_floor_images_url();

    void setServer_floor_images_url(String server_floor_images_url);

    String getServer_floor_images_absolute_path();

    void setServer_floor_images_absolute_path(String server_floor_images_absolute_path);

    void upsertFloorsByBuildingId(Set<FloorDTO> floors, String building_id);

    void updateFloorByFloorId(FloorDTO floordto, String building_id);

    String addFloorByBuildingId(FloorDTO floordto, String building_id);

    void syncFloorToADCServer(String building_id, FloorDTO floordto);

    boolean compareIds(Set<String> floor_ids, String floor_id);

    String getHashOfImage(String imageurl, String extension) throws IOException, NoSuchAlgorithmException;

    byte[] getBytesArrayByImageUrl(String image_url) throws IOException;

    String getExtensionByUrl(String url);

    String addFileToServer(byte[] image, String directory, String file_name, String file_extension) throws IOException;

    void removeFileFromServer(String absolute_path, String file_name, String file_extension);

    void deleteUnlikedFloors();

    Set<FloorDTO> upsertFloorsByBuildingId(String username, String vdms_id, String building_id, Set<FloorDTO> floors, HttpServletRequest httpServletRequest);

    void deleteFloorImageByFloorId(String username, String vdms_id, String floor_id, String clear_path, HttpServletRequest httpServletRequest);

    void upsertFloorByBuildingIdsFromBackend(String building_id, FloorDTO floor);

    String updatePathByFloorId(String username, String floor_id, String path, HttpServletRequest httpServletRequest);

    String getFloorPathByFloorId(String username, String vdms_id, String floor_id);

    Integer addFloorImageByFloorId(String username, String vdms_id, MultipartFile floor_image, String floor_dto, HttpServletRequest httpServletRequest);

    String addFloorImagesToServer(byte[] image, String directory, String file_name, String file_extension) throws IOException;

    String getImageExtensionByImageUrl(String image_url);

    Set<FloorDTO> getFloorsByBuildingId(String username, String vdms_id, String building_id, String field, String field_id);

    FloorDTO getFloorByFloorId(String username, String vdms_id, String floor_id);

    FloorDTO getFloorDetailsByFloorId(String username, String vdms_id, String floor_id);

    String getFileNameByImageUrl(String image_url);

    FloorDTO getFloorByLocationId(String location_id);

    Set<FloorDTO> getFloorsDetailsByBuildingId(String building_id);

    void deleteFloorsByBuildingId(String vdmsid, String building_id, String username, HttpServletRequest httpServletRequest);

    void deleteFloorByFloorId(String floor_id, String local_image_url, String username, HttpServletRequest httpServletRequest);

    void syncDeleteFloorToADC(String floorId, String buildingId);

    void deleteFloorsByIds(String username, String vdms_id, Set<String> floor_ids, HttpServletRequest httpServletRequest);

    String deleteFloorMapsForFloors(String vdms_id, Set<String> floor_ids);

    void removeFileFromServer(String absolute_path, String file_name);

    String upsertFloorImageByBuildingFromBackend(FloorDTO floor);

    void addFloorMapZoomLevels(String floor_id, String min_zoom, String max_zoom);

    void updateZoomLevels(List<FloorDTO> floors);

    String removeTimestampFromFileName(String fileName);

    FloorDTO mapInitialPositionToCoordinates(String initial_position, String floor_id);

    String addLocalFloorImagesToServer(byte[] image, String directory, String file_name) throws IOException;

    List<FloorDTO> updateFloorImages(String vdms_id, List<FloorDTO> floorImages);

    List<FloorDTO> updateAllFloors(String vdms_id, List<FloorDTO> floorsList);

    List<FloorDTO> syncFloorMapsTiles(String vdms_id, List<FloorDTO> floors_response);

    List<FloorDTO> getBatchFloorsByPagination(Set<String> floorIds);

    void deleteFloorsByIdsOnSync(String username, String vdmsId, Set<String> floorIds);

    void processFloorDeletions(String username, String vdmsId, List<FloorDTO> floors);

    List<FloorDTO> getFloorsByBuildingIds(Set<String> buildingIds);
}
