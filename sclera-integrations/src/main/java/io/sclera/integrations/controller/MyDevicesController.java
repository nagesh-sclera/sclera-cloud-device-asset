package io.sclera.integrations.controller;

import io.sclera.integrations.defaults.Defaults;
import io.sclera.integrations.service.MyDevicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MyDevices ownership lives here. Routes match the Dapr method names invoked by
 * cloud-device-asset's MyDevicesClient (path prefix /myDevices, see APP_ID + method).
 * Trivial methods delegate to MyDevicesService; complex ones return Defaults with TODOs.
 */
@RestController
@RequestMapping("/myDevices")
public class MyDevicesController {

  @Autowired
  MyDevicesService myDevicesService;

  @GetMapping("/startMyDevicesService")
  public void startMyDevicesService() { /* no-op */ }

  @PostMapping("/upsertMyDevicesCompany")
  public void upsertMyDevicesCompany(@RequestBody Map<String, Object> payload) { /* TODO: marshal payload -> MyDevicesCompany, then myDevicesService.upsertCompany */ }

  @PostMapping("/updateMyDevicesEventData")
  public void updateMyDevicesEventData() { /* TODO */ }

  @GetMapping("/getMyDevicesCompanies")
  public List<Map<String, Object>> getMyDevicesCompanies(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList();
  }

  @GetMapping("/getMyDevicesSensors")
  public List<Map<String, Object>> getMyDevicesSensors(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList();
  }

  @PostMapping("/deleteMyDevicesCompany")
  public void deleteMyDevicesCompany(@RequestBody Map<String, String> payload) {
    String id = payload != null ? payload.get("my_devices_company_id") : null;
    if (id != null) myDevicesService.deleteCompanyById(id);
  }

  @PostMapping("/deleteMyDevicesSensor")
  public void deleteMyDevicesSensor(@RequestBody Map<String, String> payload) {
    String id = payload != null ? payload.get("my_devices_sensor_id") : null;
    if (id != null) myDevicesService.deleteSensorById(id);
  }

  @GetMapping("/getDeviceIdByMyDevicesSensorId")
  public String getDeviceIdByMyDevicesSensorId(@RequestParam String myDevicesSensorId) {
    String result = myDevicesService.getDeviceIdByMyDevicesSensorId(myDevicesSensorId);
    return result != null ? result : Defaults.NULL_STRING;
  }

  @GetMapping("/getMyDevicesSensorCountByDeviceId")
  public Integer getMyDevicesSensorCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO; // TODO: add countByDeviceId to MyDevicesSensorRepository
  }

  @GetMapping("/getMyDevicesSensorAlertStatusByDeviceId")
  public Boolean getMyDevicesSensorAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE; // TODO: requires Conditions table (out of scope)
  }

  @GetMapping("/getDeviceMyDevicesSensors")
  public Set<Map<String, Object>> getDeviceMyDevicesSensors(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMydevicesSensorsByDeviceId")
  public Set<Map<String, Object>> getMydevicesSensorsByDeviceId(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptySet();
  }

  @PostMapping("/listmydevicesDeviceAlertMessagesByDeviceIds")
  public List<Map<String, Object>> listmydevicesDeviceAlertMessagesByDeviceIds(@RequestBody(required = false) String body) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateMyDevicesSensorDeviceId")
  public void updateMyDevicesSensorDeviceId(@RequestBody(required = false) Map<String, Object> payload) { /* TODO */ }

  @GetMapping("/getAllMyDevicesCompanies")
  public List<Map<String, Object>> getAllMyDevicesCompanies(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList();
  }

  @GetMapping("/getMyDevicesCompaniesPagination")
  public Set<Map<String, Object>> getMyDevicesCompaniesPagination(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMyDevicesSensor")
  public Map<String, Object> getMyDevicesSensor(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyMap();
  }

  @PostMapping("/updateMyDevicesSensors")
  public void updateMyDevicesSensors(@RequestBody(required = false) Map<String, Object> payload) { /* TODO */ }

  @PostMapping("/deleteMyDevicesSensors")
  public void deleteMyDevicesSensors(@RequestBody(required = false) Map<String, Object> payload) { /* TODO */ }

  @PostMapping("/updateDeviceMyDevicesSensors")
  public void updateDeviceMyDevicesSensors(@RequestBody(required = false) Map<String, Object> payload) { /* TODO */ }

  @PostMapping("/deleteDeviceMyDevicesSensors")
  public void deleteDeviceMyDevicesSensors(@RequestBody(required = false) Map<String, Object> payload) { /* TODO */ }

  @GetMapping("/getAllMyDevicesSensors")
  public List<Map<String, Object>> getAllMyDevicesSensors(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptyList();
  }

  @GetMapping("/getAllMyDevicesSensorsByPagination")
  public Set<Map<String, Object>> getAllMyDevicesSensorsByPagination(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMyDevicesSensorsByPagination")
  public Set<Map<String, Object>> getMyDevicesSensorsByPagination(@RequestParam(required = false) Map<String, String> q) {
    return Defaults.emptySet();
  }

  @PostMapping("/updateMyDevicesSensorAttributes")
  public void updateMyDevicesSensorAttributes(@RequestBody(required = false) Map<String, Object> payload) { /* TODO */ }
}
