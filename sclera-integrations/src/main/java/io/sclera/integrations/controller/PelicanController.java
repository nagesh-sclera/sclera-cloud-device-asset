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
@RequestMapping("/pelican")
public class PelicanController {
  @GetMapping("/getDeviceIdByPelicanSensorId")
  public String getDeviceIdByPelicanSensorId(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getPelicanSensorCountByDeviceId")
  public Integer getPelicanSensorCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getPelicanSensorAlertStatusByDeviceId")
  public Boolean getPelicanSensorAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDevicePelicanSensors")
  public Set<String> getDevicePelicanSensors(@RequestParam String a, @RequestParam String b, @RequestParam String c) {
    return Defaults.emptySet();
  }

  @GetMapping("/getPelicanSensorsByDeviceId")
  public List<String> getPelicanSensorsByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/listpelicanDevicesAlertMessagesByDeviceIds")
  public List<String> listpelicanDevicesAlertMessagesByDeviceIds(@RequestParam String ids) {
    return Defaults.emptyList();
  }

  @GetMapping("/updatePelicanSensorDeviceId")
  public void updatePelicanSensorDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }
}
