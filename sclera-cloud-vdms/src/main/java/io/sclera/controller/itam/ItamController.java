package io.sclera.controller.itam;

import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.ItamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/itam")
public class ItamController {

    @Autowired
    private ItamService itamService;

    @PutMapping(value = "/vdms/{vdmsId}/syncManagedSoftwaresByVdmsId")
    public ResponseEntity<ResponseDTO> syncManagedSoftwaresByVdmsId(@PathVariable String vdmsId, @RequestBody JSONObject jsonObject,HttpServletRequest httpServletRequest) {
        return itamService.syncManagedSoftwaresByVdmsId(vdmsId,jsonObject,httpServletRequest);
    }

    @PostMapping(value = "/statusFailedAlert")
    public void statusFailedAlert(@RequestBody JSONObject jsonObject) {
        itamService.statusFailedAlert(jsonObject);
    }
}
