package io.sclera.controller.inventory;


import io.sclera.service.InventoryAlertService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryAlertController {

    @Autowired
    private InventoryAlertService inventoryAlertService;

    @PostMapping(value = "/sendInventoryReport")
    public ResponseEntity<?> sendInventoryReport(@RequestParam(name = "file", required = false) List<MultipartFile> files,
                                                 @RequestParam(name = "fileType") String fileType,
                                                 @RequestParam(name = "body") String body,
                                                 HttpServletRequest httpServletRequest) throws IOException {
        return inventoryAlertService.sendInventoryReport(files, fileType, body, httpServletRequest);
    }
}
