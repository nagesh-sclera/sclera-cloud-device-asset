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
@RequestMapping("/globalinspectionrecord")
public class GlobalInspectionRecordController {
  @PostMapping("/updateGlobalInspectionRelationDeviceAndIsRemoved")
  public void updateGlobalInspectionRelationDeviceAndIsRemoved(@RequestBody String ids) {
    // no-op
  }

  @PostMapping("/deleteGlobalInspectionRelationInBatch")
  public void deleteGlobalInspectionRelationInBatch(@RequestBody String ids) {
    // no-op
  }

  @PostMapping("/updateGlobalInspectionByDeviceId")
  public void updateGlobalInspectionByDeviceId(@RequestParam(required=false) String primaryDeviceId, @RequestParam(required=false) String existingDeviceId) {
    // no-op
  }

  @PostMapping("/updateGlobalInspectionRelationLocationAndIsRemoved")
  public void updateGlobalInspectionRelationLocationAndIsRemoved(@RequestBody String locationIds) {
    // no-op
  }

  @PostMapping("/updateGlobalInspectionRecord")
  public void updateGlobalInspectionRecord(@RequestParam(required=false) String email) {
    // no-op
  }
}
