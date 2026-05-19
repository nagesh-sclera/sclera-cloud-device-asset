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
@RequestMapping("/bacnet")
public class BacnetController {
  @GetMapping("/getDeviceIdByBacnetObjectId")
  public String getDeviceIdByBacnetObjectId(@RequestParam String bacnetDeviceId, @RequestParam String bacnetObjectId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getBacnetObjectCountByDeviceId")
  public Integer getBacnetObjectCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getBacnetObjectAlertStatusByDeviceId")
  public Boolean getBacnetObjectAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceBacnetObjects")
  public Set<String> getDeviceBacnetObjects(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getBacnetObjectsByDeviceId")
  public List<String> getBacnetObjectsByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/listBacnetDevicesAlertMessagesByDeviceIds")
  public List<String> listBacnetDevicesAlertMessagesByDeviceIds(@RequestParam String ids) {
    return Defaults.emptyList();
  }

  @GetMapping("/getBacnetDeviceIdForAdvanceExcelExport")
  public List<String> getBacnetDeviceIdForAdvanceExcelExport(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/updateBacnetObjectDeviceId")
  public void updateBacnetObjectDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }
}
