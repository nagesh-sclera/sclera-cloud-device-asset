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
@RequestMapping("/modbus")
public class ModbusController {
  @GetMapping("/getDeviceModbusRegisters")
  public Set<String> getDeviceModbusRegisters(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getModbusRegistersByDeviceId")
  public List<String> getModbusRegistersByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/listModbusDevicesAlertMessagesByDeviceIds")
  public List<String> listModbusDevicesAlertMessagesByDeviceIds(@RequestParam String ids) {
    return Defaults.emptyList();
  }

  @GetMapping("/getDeviceIdByModbusRegisterId")
  public String getDeviceIdByModbusRegisterId(@RequestParam String modbusRegisterId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getModbusRegisterAlertStatusByDeviceId")
  public Boolean getModbusRegisterAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getModbusRegistersCountByDeviceId")
  public Integer getModbusRegistersCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/updateModbusRegisterDeviceId")
  public void updateModbusRegisterDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }
}
