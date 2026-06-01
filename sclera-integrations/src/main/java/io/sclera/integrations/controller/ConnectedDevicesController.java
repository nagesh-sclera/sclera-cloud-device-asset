package io.sclera.integrations.controller;

import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.ConnectedDevicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Routes match the Dapr method names invoked by cloud-device-asset's ConnectedDevicesClient
 * (path prefix /connectedDevices).
 */
@RestController
@RequestMapping("/connectedDevices")
public class ConnectedDevicesController {

  @Autowired
  ConnectedDevicesService connectedDevicesService;

  @GetMapping("/addConnectedDevices")
  public void addConnectedDevices(@RequestParam(required = false) Map<String, String> q) {
    /* TODO: marshal payload -> ConnectedDevices, then connectedDevicesService.save */
  }

  @GetMapping("/getConnectedDevicesSpecifications")
  public List<Map<String, Object>> getConnectedDevicesSpecifications(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList(); // TODO: join with specifications
  }

  @GetMapping("/getConnectedSpecificationsByDeviceId")
  public Set<String> getConnectedSpecificationsByDeviceId(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptySet();
  }

  @GetMapping("/getAllInputConnectedSpecifications")
  public List<Map<String, Object>> getAllInputConnectedSpecifications(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList();
  }

  @GetMapping("/getAllOutputConnectedSpecifications")
  public List<Map<String, Object>> getAllOutputConnectedSpecifications(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList();
  }

  @GetMapping("/untagPowerSource")
  public void untagPowerSource(@RequestParam(required = false) Map<String, String> q) {
    /* TODO */
  }

  @GetMapping("/untagDevice")
  public void untagDevice(@RequestParam(required = false) Map<String, String> q) {
    /* TODO */
  }

  @GetMapping("/untagPowerSourceByDeviceId")
  public void untagPowerSourceByDeviceId(@RequestParam(required = false) Map<String, String> q) {
    /* TODO */
  }

  @GetMapping("/getPowerSourceTopologyForDevice")
  public List<Map<String, Object>> getPowerSourceTopologyForDevice() {
    return Defaults.emptyList(); // TODO: topology query
  }

  @GetMapping("/getAllConnectedDevicesForLoadCalculation")
  public List<Map<String, Object>> getAllConnectedDevicesForLoadCalculation(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList(); // TODO: load-calculation query
  }

  @GetMapping("/getPowerSourceTopologyConnectionsCount")
  public Integer getPowerSourceTopologyConnectionsCount() {
    return Defaults.ZERO;
  }

  @GetMapping("/getPowerSourceTopologyByPagination")
  public List<Map<String, Object>> getPowerSourceTopologyByPagination(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList(); // TODO: paginated topology
  }

  @GetMapping("/deleteConnectedDevicesBySpecificationId")
  public void deleteConnectedDevicesBySpecificationId(@RequestParam(required = false) Map<String, String> q) {
    String specId = q != null ? q.get("specifications_id") : null;
    if (specId != null) connectedDevicesService.deleteBySpecificationsId(specId);
  }
}
