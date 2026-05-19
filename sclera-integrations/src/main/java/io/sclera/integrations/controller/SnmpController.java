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

  @GetMapping("/deleteGlobalSnmpByDeviceId")
  public void deleteGlobalSnmpByDeviceId(@RequestParam String deviceId) {
    // no-op
  }

  @GetMapping("/upsertGlobalSnmpByDeviceId")
  public void upsertGlobalSnmpByDeviceId(@RequestParam String snmpSet, @RequestParam String deviceId) {
    // no-op
  }

  @GetMapping("/updateSnmpObjectDeviceId")
  public void updateSnmpObjectDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }

  @GetMapping("/getAllNetworkSnmpDeviceData")
  public Object getAllNetworkSnmpDeviceData(@RequestParam String deviceId) {
    return Defaults.NULL_STRING;
  }
}
