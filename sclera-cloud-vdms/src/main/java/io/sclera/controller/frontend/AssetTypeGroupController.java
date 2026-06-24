package io.sclera.controller.frontend;


import io.sclera.service.AssetTypeGroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/assetTypeGroup")
public class AssetTypeGroupController {

    @Autowired
    private AssetTypeGroupService assetTypeGroupService;

    @GetMapping
    public ResponseEntity<?> getAllAssetTypeGroup(@RequestParam(required = false, defaultValue = "all") String key,
                                                  @RequestParam(required = false, defaultValue = "creation_timestamp") String sort,
                                                  @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pagesize,
                                                  @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return assetTypeGroupService.getAllAssetTypeGroup(key, sort, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addAssetTypeGroup")
    public ResponseEntity<?> addAssetTypeGroupByCategoryId(@RequestBody List<String> body,
                                                           @RequestParam(name = "loggedInUser") String loggedInUser,
                                                           HttpServletRequest httpServletRequest) throws IOException {
        return assetTypeGroupService.addAssetTypeGroup( body, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAssetTypeGroup(@RequestBody List<String> assetTypeGroupNames, @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                                 HttpServletRequest httpServletRequest) {
        return assetTypeGroupService.deleteAssetTypeGroup(assetTypeGroupNames, loggedInUser, httpServletRequest);
    }

}
