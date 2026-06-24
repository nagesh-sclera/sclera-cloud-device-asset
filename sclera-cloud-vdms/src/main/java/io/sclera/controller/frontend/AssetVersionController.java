package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VersionDTO;
import io.sclera.service.AssetVersionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assetVersion")
public class AssetVersionController {

    @Autowired
    private AssetVersionService assetVersionService;

    @GetMapping
    private VersionDTO getAssetVersion(HttpServletRequest httpServletRequest) {
        return assetVersionService.getAssetVersion(httpServletRequest);
    }

    @PutMapping
    private ResponseEntity<ResponseDTO> updateAssetVersion(@RequestBody String assetVersion, HttpServletRequest httpServletRequest){
        return assetVersionService.updateAssetVersion(assetVersion, httpServletRequest);
    }
}
