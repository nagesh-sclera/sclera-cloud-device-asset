package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.integrations.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/siemens")
public class SiemensController {
  @GetMapping("/getBacnetDeviceIdForAdvanceExcelExport")
  public List<String> getBacnetDeviceIdForAdvanceExcelExport(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/getSiemensDeviceIdForAdvanceExcelExport")
  public List<String> getSiemensDeviceIdForAdvanceExcelExport(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/getSiemensBmsData")
  public List<String> getSiemensBmsData(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/updateSiemensDeviceId")
  public void updateSiemensDeviceId(@RequestParam String oldId, @RequestParam String newId) {
    // no-op
  }
}
