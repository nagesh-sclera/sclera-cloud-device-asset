package io.sclera.inspection.controller;

import io.sclera.inspection.service.GlobalChecklistConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/globalChecklistConditions")
public class GlobalChecklistConditionsController {

  @Autowired
  GlobalChecklistConditionsService globalChecklistConditionsService;

  @PostMapping("/updateGlobalChecklistConditionsDeviceAndIsRemoved")
  public void updateGlobalChecklistConditionsDeviceAndIsRemoved(@RequestBody List<String> ids) {
    globalChecklistConditionsService.updateDeviceAndIsRemoved(ids);
  }

  @PostMapping("/updateGlobalChecklistConditionsLocationAndIsRemoved")
  public void updateGlobalChecklistConditionsLocationAndIsRemoved(@RequestBody List<String> locationIds) {
    globalChecklistConditionsService.updateLocationAndIsRemoved(locationIds);
  }
}