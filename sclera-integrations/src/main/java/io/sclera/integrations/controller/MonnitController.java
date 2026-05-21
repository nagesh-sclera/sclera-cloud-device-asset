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
@RequestMapping("/monnit")
public class MonnitController {
  @GetMapping("/getDeviceIdByMonnitSensorId")
  public String getDeviceIdByMonnitSensorId(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getMonnitCountByDeviceId")
  public Integer getMonnitCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getMonnitAlertStatusByDeviceId")
  public Boolean getMonnitAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceMonnitSensors")
  public Set<String> getDeviceMonnitSensors(@RequestParam String a, @RequestParam String b, @RequestParam String c) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMonnitSensorsByDeviceId")
  public List<String> getMonnitSensorsByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @PostMapping("/listmonnitDevicesAlertMessagesByDeviceIds")
  public List<String> listmonnitDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateMonnitSensorDeviceId")
  public void updateMonnitSensorDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
