package io.sclera.controller.inventory;


import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/organisation/{orgId}/getVdmsListByOrganisationId")
    public ResponseEntity<ResponseDTO>getVdmsListByOrganisationId(@PathVariable String orgId) {
        return inventoryService.getVdmsListByOrganisationId(orgId);
    }

    @PostMapping("/vdms/{vdmsId}/socket")
    public void socket(@RequestBody JSONObject jsonObject, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        inventoryService.socket(vdmsId, jsonObject,httpServletRequest);
    }
}
