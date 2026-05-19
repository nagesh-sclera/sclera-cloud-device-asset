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
@RequestMapping("/globalchecklist")
public class GlobalChecklistController {
  @GetMapping("/updateDeviceGlobalChecklistDeviceId")
  public void updateDeviceGlobalChecklistDeviceId(@RequestParam String oldId, @RequestParam String newId) {
    // no-op
  }

  @GetMapping("/deleteGlobalChecklistByDeviceId")
  public void deleteGlobalChecklistByDeviceId(@RequestParam String deviceId) {
    // no-op
  }
}
