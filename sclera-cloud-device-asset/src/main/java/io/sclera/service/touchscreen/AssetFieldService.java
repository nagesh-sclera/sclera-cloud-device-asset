package io.sclera.service.touchscreen;

import io.sclera.Repository.AssetFieldRepository;
import io.sclera.dto.AssetFieldDTO;
import io.sclera.interfaces.AssetFieldServiceInterface;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Provides asset field definitions for the touchscreen interface.
 *
 * <p>Delegates persistence access to {@link AssetFieldRepository} to retrieve
 * the configured asset fields as {@link AssetFieldDTO} instances.
 */
@Service
public class AssetFieldService implements AssetFieldServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(AssetFieldService.class);

    private final AssetFieldRepository assetFieldRepository;

    public AssetFieldService(AssetFieldRepository assetFieldRepository) {
        this.assetFieldRepository = assetFieldRepository;
    }

    /**
     * Retrieves all asset fields available to the touchscreen.
     *
     * @param httpServletRequest the incoming HTTP request
     * @return the list of asset fields as {@link AssetFieldDTO} instances
     */
    public List<AssetFieldDTO> getAssetFields(HttpServletRequest httpServletRequest) {
        log.info("Fetching asset fields for touchscreen");
        return assetFieldRepository.getAllAssetFields();
    }
}
