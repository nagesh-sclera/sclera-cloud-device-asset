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
@RequestMapping("/inspectionrecord")
public class InspectionRecordController {
  @PostMapping("/updateInspectionRecordStatus")
  public void updateInspectionRecordStatus(@RequestParam(required=false) String a, @RequestParam(required=false) String b, @RequestParam(required=false) String id, @RequestParam(required=false) boolean status) {
    // no-op
  }

  @PostMapping("/updateInspectionStatusOnDeviceArchive")
  public void updateInspectionStatusOnDeviceArchive(@RequestBody String ids) {
    // no-op
  }

  @PostMapping("/updateInspectionRecord")
  public void updateInspectionRecord(@RequestParam(required=false) String email) {
    // no-op
  }
}
