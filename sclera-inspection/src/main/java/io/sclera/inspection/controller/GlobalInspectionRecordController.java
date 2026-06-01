package io.sclera.inspection.controller;

import io.sclera.inspection.service.GlobalInspectionRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/globalInspectionRecord")
public class GlobalInspectionRecordController {

  @Autowired
  GlobalInspectionRelationService globalInspectionRelationService;

  @PostMapping("/updateGlobalInspectionRelationDeviceAndIsRemoved")
  public void updateGlobalInspectionRelationDeviceAndIsRemoved(@RequestBody List<String> ids) {
    globalInspectionRelationService.updateDeviceAndIsRemoved(ids);
  }

  @PostMapping("/deleteGlobalInspectionRelationInBatch")
  public void deleteGlobalInspectionRelationInBatch(@RequestBody List<String> ids) {
    globalInspectionRelationService.deleteInBatch(ids);
  }

  @PostMapping("/updateGlobalInspectionByDeviceId")
  public void updateGlobalInspectionByDeviceId(@RequestParam(required=false) String primaryDeviceId, @RequestParam(required=false) String existingDeviceId) {
    globalInspectionRelationService.updateGlobalInspectionByDeviceId(primaryDeviceId, existingDeviceId);
  }

  @PostMapping("/updateGlobalInspectionRelationLocationAndIsRemoved")
  public void updateGlobalInspectionRelationLocationAndIsRemoved(@RequestBody List<String> locationIds) {
    globalInspectionRelationService.updateLocationAndIsRemoved(locationIds);
  }

  @PostMapping("/updateGlobalInspectionRecord")
  public void updateGlobalInspectionRecord(@RequestParam(required=false) String email) {
    globalInspectionRelationService.updateGlobalInspectionRecord(email);
  }
}