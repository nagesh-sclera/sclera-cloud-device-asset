package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.LorawanService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/lorawan")
public class LorawanController {
  @Autowired
  LorawanService lorawanService;

  @GetMapping("/getDeviceIdByLorawanSensorId")
  public String getDeviceIdByLorawanSensorId(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getLorawanSensorCountByDeviceId")
  public Integer getLorawanSensorCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getLorawanSensorAlertStatusByDeviceId")
  public Boolean getLorawanSensorAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceLorawanSensors")
  public Set<String> getDeviceLorawanSensors(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getLorawanSensorsByDeviceId")
  public List<String> getLorawanSensorsByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @PostMapping("/listLorawanDevicesAlertMessagesByDeviceIds")
  public List<String> listLorawanDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateLorawanSensorDeviceId")
  public void updateLorawanSensorDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
