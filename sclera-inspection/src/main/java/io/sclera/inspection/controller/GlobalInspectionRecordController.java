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
@RequestMapping("/globalinspectionrecord")
public class GlobalInspectionRecordController {
  @GetMapping("/updateGlobalInspectionRelationDeviceAndIsRemoved")
  public void updateGlobalInspectionRelationDeviceAndIsRemoved(@RequestParam String ids) {
    // no-op
  }

  @GetMapping("/deleteGlobalInspectionRelationInBatch")
  public void deleteGlobalInspectionRelationInBatch(@RequestParam String ids) {
    // no-op
  }

  @GetMapping("/updateGlobalInspectionByDeviceId")
  public void updateGlobalInspectionByDeviceId(@RequestParam String primaryDeviceId, @RequestParam String existingDeviceId) {
    // no-op
  }

  @GetMapping("/updateGlobalInspectionRelationLocationAndIsRemoved")
  public void updateGlobalInspectionRelationLocationAndIsRemoved(@RequestParam String locationIds) {
    // no-op
  }

  @GetMapping("/updateGlobalInspectionRecord")
  public void updateGlobalInspectionRecord(@RequestParam String email) {
    // no-op
  }
}
