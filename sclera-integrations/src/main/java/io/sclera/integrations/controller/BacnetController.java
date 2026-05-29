package io.sclera.integrations.controller;

import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.BacnetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/bacnet")
public class BacnetController {

  @Autowired
  BacnetService bacnetService;

  @GetMapping("/getDeviceIdByBacnetObjectId")
  public String getDeviceIdByBacnetObjectId(@RequestParam String bacnetDeviceId, @RequestParam String bacnetObjectId) {
    return bacnetService.getDeviceIdByBacnetObjectId(bacnetDeviceId, bacnetObjectId);
  }

  @GetMapping("/getBacnetObjectCountByDeviceId")
  public Integer getBacnetObjectCountByDeviceId(@RequestParam String deviceId) {
    return (int) bacnetService.getBacnetObjectCountByDeviceId(deviceId);
  }

  @GetMapping("/getBacnetObjectAlertStatusByDeviceId")
  public Boolean getBacnetObjectAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE; // TODO: requires Conditions table (out of scope)
  }

  @GetMapping("/getDeviceBacnetObjects")
  public Set<String> getDeviceBacnetObjects(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet(); // TODO: param semantics unclear
  }

  @GetMapping("/getBacnetObjectsByDeviceId")
  public List<String> getBacnetObjectsByDeviceId(@RequestParam String deviceId) {
    return bacnetService.getBacnetObjectIdsByDeviceId(deviceId);
  }

  @PostMapping("/listBacnetDevicesAlertMessagesByDeviceIds")
  public List<String> listBacnetDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList(); // TODO: requires Conditions table (out of scope)
  }

  @GetMapping("/getBacnetDeviceIdForAdvanceExcelExport")
  public List<String> getBacnetDeviceIdForAdvanceExcelExport(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String deviceId) {
    return Defaults.emptyList(); // TODO: export business logic
  }

  @PostMapping("/updateBacnetObjectDeviceId")
  public void updateBacnetObjectDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // TODO: bulk update device_id on bacnet_object
  }
}
