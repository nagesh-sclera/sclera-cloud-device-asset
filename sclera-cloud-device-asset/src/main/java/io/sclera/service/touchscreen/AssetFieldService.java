package io.sclera.service.touchscreen;

import io.sclera.Repository.AssetFieldRepository;
import io.sclera.dto.AssetFieldDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class AssetFieldService {
    private static final Logger log = LoggerFactory.getLogger(AssetFieldService.class);

    private final AssetFieldRepository assetFieldRepository;

    public AssetFieldService(AssetFieldRepository assetFieldRepository) {
        this.assetFieldRepository = assetFieldRepository;
    }

    public List<AssetFieldDTO> getAssetFields(HttpServletRequest httpServletRequest) {
        log.info("Fetching asset fields for touchscreen");
        return assetFieldRepository.getAllAssetFields();
    }
}
