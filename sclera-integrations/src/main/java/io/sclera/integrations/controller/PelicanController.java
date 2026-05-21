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

  @PostMapping("/listpelicanDevicesAlertMessagesByDeviceIds")
  public List<String> listpelicanDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList();
  }

  @PostMapping("/updatePelicanSensorDeviceId")
  public void updatePelicanSensorDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
