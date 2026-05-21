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
@RequestMapping("/snmp")
public class SnmpController {
  @GetMapping("/getSnmpDeviceAlertStatusByDeviceId")
  public Boolean getSnmpDeviceAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceIdBySnmpDeviceId")
  public String getDeviceIdBySnmpDeviceId(@RequestParam String snmpDeviceId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getSnmpDeviceCountByDeviceAndSnmpConfiguration")
  public Integer getSnmpDeviceCountByDeviceAndSnmpConfiguration(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getSnmpObjectCountByDeviceId")
  public Integer getSnmpObjectCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getDeviceIdBySnmpObjectId")
  public String getDeviceIdBySnmpObjectId(@RequestParam String a, @RequestParam String b) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getSnmpObjectAlertStatusByDeviceId")
  public Boolean getSnmpObjectAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceSnmpObjects")
  public Set<String> getDeviceSnmpObjects(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getSnmpDevicesByDeviceId")
  public List<String> getSnmpDevicesByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @PostMapping("/deleteGlobalSnmpByDeviceId")
  public void deleteGlobalSnmpByDeviceId(@RequestParam(required=false) String deviceId) {
    // no-op
  }

  @PostMapping("/upsertGlobalSnmpByDeviceId")
  public void upsertGlobalSnmpByDeviceId(@RequestBody String snmpSet, @RequestParam(required=false) String deviceId) {
    // no-op
  }

  @PostMapping("/updateSnmpObjectDeviceId")
  public void updateSnmpObjectDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }

  @GetMapping("/getAllNetworkSnmpDeviceData")
  public Object getAllNetworkSnmpDeviceData(@RequestParam String deviceId) {
    return Defaults.NULL_STRING;
  }
}
