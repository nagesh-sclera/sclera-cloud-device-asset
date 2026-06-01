package io.sclera.integrations.controller;

import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.DatahoistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * Routes match cloud-device-asset's DatahoistClient Dapr method names
 * (path prefix /datahoist).
 */
@RestController
@RequestMapping("/datahoist")
public class DatahoistController {

  @Autowired
  DatahoistService datahoistService;

  /** Mirrors the deleted DataHoistService#getDataHoistDeviceById stub. */
  @GetMapping("/getDataHoistDeviceById")
  public Set<Map<String, Object>> getDataHoistDeviceById(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String vdmsId,
      @RequestParam(required = false) String deviceId) {
    return Defaults.emptySet(); // TODO: business logic
  }
}
