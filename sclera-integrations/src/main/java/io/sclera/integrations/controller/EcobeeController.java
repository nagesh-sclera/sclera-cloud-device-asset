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
@RequestMapping("/ecobee")
public class EcobeeController {
  @GetMapping("/getEcobeeDevicesByDeviceId")
  public Set<String> getEcobeeDevicesByDeviceId(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getEcobeeSensorCountByDeviceId")
  public Integer getEcobeeSensorCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getEcobeeSensorAlertStatusByDeviceId")
  public Boolean getEcobeeSensorAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceIdByEcobeeSensorId")
  public String getDeviceIdByEcobeeSensorId(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/updateEcobeeSensorDeviceId")
  public void updateEcobeeSensorDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }
}
