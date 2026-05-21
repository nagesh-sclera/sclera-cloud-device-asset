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
@RequestMapping("/daintree")
public class DaintreeController {
  @GetMapping("/getDeviceIdByDaintreeDeviceId")
  public String getDeviceIdByDaintreeDeviceId(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getDeviceDaintreeDevicesCountByDeviceId")
  public Integer getDeviceDaintreeDevicesCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getDaintreeAlertStatusByDeviceId")
  public Boolean getDaintreeAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDaintreeDevicesByDeviceId")
  public Set<String> getDaintreeDevicesByDeviceId(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @PostMapping("/listDaintreeDevicesAlertMessagesByDeviceIds")
  public List<String> listDaintreeDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateDaintreeDeviceByDeviceId")
  public void updateDaintreeDeviceByDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }

  @GetMapping("/getDaintreeConfigurations")
  public List<String> getDaintreeConfigurations(@RequestParam String vdmsId) {
    return Defaults.emptyList();
  }
}
