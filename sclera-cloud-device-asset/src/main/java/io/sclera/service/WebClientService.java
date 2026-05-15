package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.auth.dto.TenantDTO;
import io.sclera.dto.FloorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class WebClientService {

    private static final Logger log = LoggerFactory.getLogger(WebClientService.class);

    public byte[] getImageBytesByUrl(String link) { return new byte[1024]; }
    public TenantDTO getAllTenants(String issuer) { log.warn("STUB: getAllTenants called with issuer={}", issuer); return null; }
    public JSONArray multiEditDigitalTwin(String vdmsId, MultipartFile file, String username, Set<String> deviceIds) { return new JSONArray(); }
    public String deleteFloorMapsByImageUrl(String vdmsId, List<FloorDTO> floors) { return null; }
    public List<FloorDTO> uploadFloorImages(String vdmsId, List<FloorDTO> floors) { log.warn("STUB: uploadFloorImages"); return Collections.emptyList(); }
    public List<FloorDTO> addFloorImages(String vdmsId, MultipartFile file, List<FloorDTO> floors) { log.warn("STUB: addFloorImages"); return Collections.emptyList(); }
    public List<FloorDTO> syncFloorMapImageByFloorId(String vdmsId, List<FloorDTO> floors) { log.warn("STUB: syncFloorMapImageByFloorId"); return Collections.emptyList(); }
    public List<FloorDTO> syncFloorMapTilesFolder(String vdmsId, List<FloorDTO> floors) { log.warn("STUB: syncFloorMapTilesFolder"); return Collections.emptyList(); }
}
