package io.sclera.inspection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/updateDeviceGlobalChecklistDeviceId")
  public void updateDeviceGlobalChecklistDeviceId(@RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }

  @PostMapping("/deleteGlobalChecklistByDeviceId")
  public void deleteGlobalChecklistByDeviceId(@RequestParam(required=false) String deviceId) {
    // no-op
  }
}
