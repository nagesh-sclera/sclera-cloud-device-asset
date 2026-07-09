package io.sclera.service;

import io.sclera.dto.AssetFieldDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/** Service contract for the matching service class. */
public interface AssetFieldService {
    List<AssetFieldDTO> getAssetFields(HttpServletRequest httpServletRequest);
}
