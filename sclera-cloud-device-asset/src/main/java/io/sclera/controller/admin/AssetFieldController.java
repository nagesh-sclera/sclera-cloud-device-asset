package io.sclera.controller.admin;

import io.sclera.dto.AssetFieldDTO;
import io.sclera.service.touchscreen.AssetFieldService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user/{username}/vdms/{vdms_id}/asset-fields")
public class AssetFieldController {

    private final AssetFieldService assetFieldService;

    public AssetFieldController(AssetFieldService assetFieldService) {
        this.assetFieldService = assetFieldService;
    }

    @GetMapping
    public List<AssetFieldDTO> getAllAssetFields(@PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return assetFieldService.getAssetFields(httpServletRequest);
    }
}
