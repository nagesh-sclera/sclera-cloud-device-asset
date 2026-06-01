package io.sclera.inspection.controller;

import io.sclera.inspection.service.ChecklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkListTemplate")
public class CheckListTemplateController {

  @Autowired
  ChecklistService checklistService;

  @GetMapping("/getCheckListTemplatesCountByDeviceId")
  public Integer getCheckListTemplatesCountByDeviceId(@RequestParam String deviceId) {
    return (int) checklistService.getCheckListTemplatesCountByDeviceId(deviceId);
  }
}