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
@RequestMapping("/mqtt")
public class MqttController {
  @GetMapping("/getAllMqttDevices")
  public Set<String> getAllMqttDevices(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMqttDeviceCountByDeviceId")
  public Integer getMqttDeviceCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }
}
