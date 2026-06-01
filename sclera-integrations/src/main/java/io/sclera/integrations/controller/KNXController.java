package io.sclera.integrations.controller;

import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.KNXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Path prefix is "kNX" (camelCase k+N+X) to match KNXClient's Dapr method names
 * ("kNX/getDeviceIdByKNXGroupAddress" etc.).
 */
@RestController
@RequestMapping("/kNX")
public class KNXController {

  @Autowired
  KNXService knxService;

  @GetMapping("/getDeviceIdByKNXGroupAddress")
  public String getDeviceIdByKNXGroupAddress(@RequestParam String a, @RequestParam String b) {
    return Defaults.NULL_STRING; // TODO: lookup by group address
  }

  @GetMapping("/getKNXGroupCountByDeviceId")
  public Integer getKNXGroupCountByDeviceId(@RequestParam String deviceId) {
    return (int) knxService.listAllGroups().stream().filter(g -> deviceId.equals(g.getDevice_id())).count();
  }

  @GetMapping("/getKNXGroupAlertStatusByDeviceId")
  public Boolean getKNXGroupAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE; // TODO: requires Conditions (out of scope)
  }

  @GetMapping("/getDeviceKNXGroups")
  public Set<String> getDeviceKNXGroups(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet(); // TODO: param semantics unclear
  }

  @GetMapping("/getKNXGroupsByDeviceAddress")
  public List<String> getKNXGroupsByDeviceAddress(@RequestParam String addr) {
    return Defaults.emptyList(); // TODO: business logic
  }

  @PostMapping("/listKNXDevicesAlertMessagesByDeviceIds")
  public List<String> listKNXDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
    return Defaults.emptyList(); // TODO: requires Conditions (out of scope)
  }

  @PostMapping("/updateKnxGroupDeviceId")
  public void updateKnxGroupDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // TODO: bulk update device_id on knx_group
  }
}
