package io.sclera.inspection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.inspection.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/checklisttemplate")
public class CheckListTemplateController {
  @GetMapping("/getCheckListTemplatesCountByDeviceId")
  public Integer getCheckListTemplatesCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }
}
