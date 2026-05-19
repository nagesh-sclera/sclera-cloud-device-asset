package io.sclera.workorders.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.workorders.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/mydevices")
public class MyDevicesController {
  @GetMapping("/startMyDevicesService")
  public void startMyDevicesService() {
    // no-op
  }

  @GetMapping("/upsertMyDevicesCompany")
  public void upsertMyDevicesCompany(@RequestParam String username, @RequestParam String vdmsid, @RequestParam String myDevicesCompany) {
    // no-op
  }

  @GetMapping("/updateMyDevicesEventData")
  public void updateMyDevicesEventData(@RequestParam String myDevicesEventData) {
    // no-op
  }

  @GetMapping("/getMyDevicesCompanies")
  public List<String> getMyDevicesCompanies(@RequestParam String vdmsId, @RequestParam Integer page, @RequestParam Integer size) {
    return Defaults.emptyList();
  }

  @GetMapping("/getMyDevicesSensors")
  public List<String> getMyDevicesSensors(@RequestParam String vdmsId, @RequestParam String companyId, @RequestParam Integer page, @RequestParam Integer size) {
    return Defaults.emptyList();
  }

  @GetMapping("/deleteMyDevicesCompany")
  public void deleteMyDevicesCompany(@RequestParam String username, @RequestParam String vdmsid, @RequestParam String id) {
    // no-op
  }

  @GetMapping("/deleteMyDevicesSensor")
  public void deleteMyDevicesSensor(@RequestParam String id) {
    // no-op
  }

  @GetMapping("/getDeviceIdByMyDevicesSensorId")
  public String getDeviceIdByMyDevicesSensorId(@RequestParam String sensorId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getMyDevicesSensorCountByDeviceId")
  public Integer getMyDevicesSensorCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getMyDevicesSensorAlertStatusByDeviceId")
  public Boolean getMyDevicesSensorAlertStatusByDeviceId(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/getDeviceMyDevicesSensors")
  public Set<String> getDeviceMyDevicesSensors(@RequestParam String vdmsId, @RequestParam String companyId, @RequestParam String deviceId) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMydevicesSensorsByDeviceId")
  public List<String> getMydevicesSensorsByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/listmydevicesDeviceAlertMessagesByDeviceIds")
  public String listmydevicesDeviceAlertMessagesByDeviceIds(@RequestParam String deviceIds) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/updateMyDevicesSensorDeviceId")
  public void updateMyDevicesSensorDeviceId(@RequestParam String oldDeviceId, @RequestParam String newDeviceId, @RequestParam String sensorIds) {
    // no-op
  }

  @GetMapping("/getAllMyDevicesCompanies")
  public List<String> getAllMyDevicesCompanies(@RequestParam String username, @RequestParam String vdmsId) {
    return Defaults.emptyList();
  }

  @GetMapping("/getMyDevicesCompaniesPagination")
  public Set<String> getMyDevicesCompaniesPagination(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String searchkey, @RequestParam Integer pageno, @RequestParam Integer pagesize) {
    return Defaults.emptySet();
  }

  @GetMapping("/getMyDevicesSensor")
  public String getMyDevicesSensor(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String sensorId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/updateMyDevicesSensors")
  public void updateMyDevicesSensors(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String sensors) {
    // no-op
  }

  @GetMapping("/deleteMyDevicesSensors")
  public void deleteMyDevicesSensors(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String sensors) {
    // no-op
  }

  @GetMapping("/updateDeviceMyDevicesSensors")
  public void updateDeviceMyDevicesSensors(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String sensors) {
    // no-op
  }

  @GetMapping("/deleteDeviceMyDevicesSensors")
  public void deleteDeviceMyDevicesSensors(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String sensors) {
    // no-op
  }

  @GetMapping("/getAllMyDevicesSensors")
  public List<String> getAllMyDevicesSensors(@RequestParam String username, @RequestParam String vdmsId) {
    return Defaults.emptyList();
  }

  @GetMapping("/getAllMyDevicesSensorsByPagination")
  public List<String> getAllMyDevicesSensorsByPagination(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String searchkey, @RequestParam Integer pageno, @RequestParam Integer pagesize) {
    return Defaults.emptyList();
  }

  @GetMapping("/getMyDevicesSensorsByPagination")
  public Set<String> getMyDevicesSensorsByPagination(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String companyId, @RequestParam String searchkey, @RequestParam Integer pageno, @RequestParam Integer pagesize) {
    return Defaults.emptySet();
  }

  @GetMapping("/updateMyDevicesSensorAttributes")
  public void updateMyDevicesSensorAttributes(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String attrs) {
    // no-op
  }
}
