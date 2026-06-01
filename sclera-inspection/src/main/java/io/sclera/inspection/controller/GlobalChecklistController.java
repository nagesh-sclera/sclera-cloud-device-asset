package io.sclera.inspection.controller;

import io.sclera.inspection.service.GlobalChecklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/globalChecklist")
public class GlobalChecklistController {

  @Autowired
  GlobalChecklistService globalChecklistService;

  @PostMapping("/updateDeviceGlobalChecklistDeviceId")
  public void updateDeviceGlobalChecklistDeviceId(@RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    globalChecklistService.updateDeviceGlobalChecklistDeviceId(oldId, newId);
  }

  @PostMapping("/deleteGlobalChecklistByDeviceId")
  public void deleteGlobalChecklistByDeviceId(@RequestParam(required=false) String deviceId) {
    if (deviceId != null) globalChecklistService.deleteByDeviceId(deviceId);
  }
}