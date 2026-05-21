package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.integrations.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/disruptive")
public class DisruptiveController {
  @GetMapping("/getDeviceIdByDisruptiveSensorId")
  public String getDeviceIdByDisruptiveSensorId(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getDisruptiveSensorCountByDeviceId")
  public Integer getDisruptiveSensorCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getDisruptiveSensorAlertStatusByDeviceId")
  public Boolean getDisruptiveSensorAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceDisruptiveSensors")
  public Set<String> getDeviceDisruptiveSensors(@RequestParam String a, @RequestParam String b, @RequestParam String c) {
    return Defaults.emptySet();
  }

  @PostMapping("/listDisruptiveDevicesAlertMessagesByDeviceIds")
  public List<String> listDisruptiveDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateDisruptiveSensorDeviceId")
  public void updateDisruptiveSensorDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
