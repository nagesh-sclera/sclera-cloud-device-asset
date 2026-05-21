package io.sclera.workorders.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping("/upsertMyDevicesCompany")
  public void upsertMyDevicesCompany(@RequestBody String myDevicesCompany, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsid) {
    // no-op
  }

  @PostMapping("/updateMyDevicesEventData")
  public void updateMyDevicesEventData(@RequestBody String myDevicesEventData) {
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

  @PostMapping("/deleteMyDevicesCompany")
  public void deleteMyDevicesCompany(@RequestParam(required=false) String username, @RequestParam(required=false) String vdmsid, @RequestParam(required=false) String id) {
    // no-op
  }

  @PostMapping("/deleteMyDevicesSensor")
  public void deleteMyDevicesSensor(@RequestParam(required=false) String id) {
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

  @PostMapping("/listmydevicesDeviceAlertMessagesByDeviceIds")
  public String listmydevicesDeviceAlertMessagesByDeviceIds(@RequestBody String deviceIds) {
    return Defaults.NULL_STRING;
  }

  @PostMapping("/updateMyDevicesSensorDeviceId")
  public void updateMyDevicesSensorDeviceId(@RequestBody String sensorIds, @RequestParam(required=false) String oldDeviceId, @RequestParam(required=false) String newDeviceId) {
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

  @PostMapping("/updateMyDevicesSensors")
  public void updateMyDevicesSensors(@RequestBody String sensors, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @PostMapping("/deleteMyDevicesSensors")
  public void deleteMyDevicesSensors(@RequestBody String sensors, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @PostMapping("/updateDeviceMyDevicesSensors")
  public void updateDeviceMyDevicesSensors(@RequestBody String sensors, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @PostMapping("/deleteDeviceMyDevicesSensors")
  public void deleteDeviceMyDevicesSensors(@RequestBody String sensors, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
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

  @PostMapping("/updateMyDevicesSensorAttributes")
  public void updateMyDevicesSensorAttributes(@RequestBody String attrs, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
    // no-op
  }
}
