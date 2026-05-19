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
@RequestMapping("/polylens")
public class PolyLensController {
  @GetMapping("/getAllPolyLensDevices")
  public Set<String> getAllPolyLensDevices(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/updatePolyLensDeviceId")
  public void updatePolyLensDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }

  @GetMapping("/getPolyLensDeviceCountByDeviceId")
  public Integer getPolyLensDeviceCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }
}
