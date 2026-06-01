package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.ModbusService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/modbus")
public class ModbusController {
  @Autowired
  ModbusService modbusService;

  @GetMapping("/getDeviceModbusRegisters")
  public Set<String> getDeviceModbusRegisters(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.emptySet();
  }

  @GetMapping("/getModbusRegistersByDeviceId")
  public List<String> getModbusRegistersByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @PostMapping("/listModbusDevicesAlertMessagesByDeviceIds")
  public List<String> listModbusDevicesAlertMessagesByDeviceIds(@RequestBody String ids) {
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

  @PostMapping("/updateModbusRegisterDeviceId")
  public void updateModbusRegisterDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
