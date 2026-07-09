package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.auth.dto.TenantDTO;
import io.sclera.dto.FloorDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

/** Service contract for the matching service class. */
public interface WebClientService {
    byte[] getImageBytesByUrl(String link);
    TenantDTO getAllTenants(String issuer);
    JSONArray multiEditDigitalTwin(String vdmsId, MultipartFile file, String username, Set<String> deviceIds);
    String deleteFloorMapsByImageUrl(String vdmsId, List<FloorDTO> floors);
    List<FloorDTO> uploadFloorImages(String vdmsId, List<FloorDTO> floors);
    List<FloorDTO> addFloorImages(String vdmsId, MultipartFile file, List<FloorDTO> floors);
    List<FloorDTO> syncFloorMapImageByFloorId(String vdmsId, List<FloorDTO> floors);
    List<FloorDTO> syncFloorMapTilesFolder(String vdmsId, List<FloorDTO> floors);
}
