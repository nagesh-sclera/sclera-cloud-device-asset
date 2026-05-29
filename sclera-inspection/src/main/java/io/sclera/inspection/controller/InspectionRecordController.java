package io.sclera.inspection.controller;

import io.sclera.inspection.service.InspectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/inspectionRecord")
public class InspectionRecordController {

  @Autowired
  InspectionRecordService inspectionRecordService;

  @PostMapping("/updateInspectionRecordStatus")
  public void updateInspectionRecordStatus(@RequestParam(required=false) String a, @RequestParam(required=false) String b, @RequestParam(required=false) String id, @RequestParam(required=false) boolean status) {
    inspectionRecordService.updateInspectionRecordStatus(id, status);
  }

  @PostMapping("/updateInspectionStatusOnDeviceArchive")
  public void updateInspectionStatusOnDeviceArchive(@RequestBody List<String> deviceIds) {
    inspectionRecordService.updateInspectionStatusOnDeviceArchive(deviceIds);
  }

  @PostMapping("/updateInspectionRecord")
  public void updateInspectionRecord(@RequestParam(required=false) String email) {
    inspectionRecordService.updateInspectionRecord(email);
  }
}