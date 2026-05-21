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
@RequestMapping("/knx")
public class KNXController {
  @GetMapping("/getDeviceIdByKNXGroupAddress")
  public String getDeviceIdByKNXGroupAddress(@RequestParam String a, @RequestParam String b) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getKNXGroupCountByDeviceId")
  public Integer getKNXGroupCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getKNXGroupAlertStatusByDeviceId")
  public Boolean getKNXGroupAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceKNXGroups")
  public Set<String> getDeviceKNXGroups(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getKNXGroupsByDeviceAddress")
  public List<String> getKNXGroupsByDeviceAddress(@RequestParam String addr) {
    return Defaults.emptyList();
  }

  @PostMapping("/listKNXDevicesAlertMessagesByDeviceIds")
  public List<String> listKNXDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateKnxGroupDeviceId")
  public void updateKnxGroupDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
