package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.auth.dto.TenantDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.interfaces.WebClientServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class WebClientService implements WebClientServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(WebClientService.class);

    /**
     * Fetches the bytes of the image at the given link. Stub returns an empty buffer.
     */
    public byte[] getImageBytesByUrl(String link) { return new byte[1024]; }
    /**
     * Returns all tenants for the given issuer. Stub logs a warning and returns null.
     */
    public TenantDTO getAllTenants(String issuer) { log.warn("STUB: getAllTenants called with issuer={}", issuer); return null; }
    /**
     * Applies a multi-device digital-twin edit using the uploaded file. Stub returns an empty array.
     */
    public JSONArray multiEditDigitalTwin(String vdmsId, MultipartFile file, String username, Set<String> deviceIds) { return new JSONArray(); }
    /**
     * Deletes the cloud floor map images for the given floors. Stub returns null.
     */
    public String deleteFloorMapsByImageUrl(String vdmsId, List<FloorDTO> floors) { return null; }
    /**
     * Uploads floor map images for the given floors. Stub logs a warning and returns an empty list.
     */
    public List<FloorDTO> uploadFloorImages(String vdmsId, List<FloorDTO> floors) { log.warn("STUB: uploadFloorImages"); return Collections.emptyList(); }
    /**
     * Adds a floor map image from the uploaded file for the given floors. Stub logs a warning and
     * returns an empty list.
     */
    public List<FloorDTO> addFloorImages(String vdmsId, MultipartFile file, List<FloorDTO> floors) { log.warn("STUB: addFloorImages"); return Collections.emptyList(); }
    /**
     * Syncs floor map images by floor id. Stub logs a warning and returns an empty list.
     */
    public List<FloorDTO> syncFloorMapImageByFloorId(String vdmsId, List<FloorDTO> floors) { log.warn("STUB: syncFloorMapImageByFloorId"); return Collections.emptyList(); }
    /**
     * Syncs the floor map tiles folder for the given floors. Stub logs a warning and returns an
     * empty list.
     */
    public List<FloorDTO> syncFloorMapTilesFolder(String vdmsId, List<FloorDTO> floors) { log.warn("STUB: syncFloorMapTilesFolder"); return Collections.emptyList(); }
}
