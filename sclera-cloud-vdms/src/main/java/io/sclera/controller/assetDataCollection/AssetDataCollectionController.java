package io.sclera.controller.assetDataCollection;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.AssetDataCollectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/assetDataCollection")
@AllArgsConstructor
public class AssetDataCollectionController {

    private AssetDataCollectionService assetDataCollectionService;

    @GetMapping(value = "/vdms/{vdmsId}/getIsMultiTenantCheckByVdmsId")
    public ResponseEntity<ResponseDTO>getIsMultiTenantCheckByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest){
        return assetDataCollectionService.getIsMultiTenantCheckByVdmsId(vdmsId, httpServletRequest);
    }
}
