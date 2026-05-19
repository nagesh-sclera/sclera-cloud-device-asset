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
@RequestMapping("/inspectionrecord")
public class InspectionRecordController {
  @GetMapping("/updateInspectionRecordStatus")
  public void updateInspectionRecordStatus(@RequestParam String a, @RequestParam String b, @RequestParam String id, @RequestParam boolean status) {
    // no-op
  }

  @GetMapping("/updateInspectionStatusOnDeviceArchive")
  public void updateInspectionStatusOnDeviceArchive(@RequestParam String ids) {
    // no-op
  }

  @GetMapping("/updateInspectionRecord")
  public void updateInspectionRecord(@RequestParam String email) {
    // no-op
  }
}
