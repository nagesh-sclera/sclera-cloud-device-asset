package io.sclera.interfaces;

import io.sclera.dto.AssetFieldDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/** Service contract for the matching service class. */
public interface AssetFieldServiceInterface {
    List<AssetFieldDTO> getAssetFields(HttpServletRequest httpServletRequest);
}
