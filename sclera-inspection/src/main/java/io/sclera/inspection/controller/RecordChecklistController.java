package io.sclera.inspection.controller;

import io.sclera.inspection.defaults.Defaults;
import io.sclera.inspection.service.RecordChecklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/recordChecklist")
public class RecordChecklistController {

  @Autowired
  RecordChecklistService recordChecklistService;

  @PostMapping("/updateRecordChecklistDeviceAndIsRemoved")
  public void updateRecordChecklistDeviceAndIsRemoved(@RequestBody List<String> ids) {
    recordChecklistService.updateDeviceAndIsRemoved(ids);
  }

  @PostMapping("/deleteRecordChecklistInBatch")
  public void deleteRecordChecklistInBatch(@RequestBody List<String> ids) {
    recordChecklistService.deleteInBatch(ids);
  }

  @PostMapping("/deleteAllRecordChecklistByDeviceId")
  public List<String> deleteAllRecordChecklistByDeviceId(@RequestParam(required=false) String deviceId) {
    return recordChecklistService.deleteAllByDeviceId(deviceId);
  }

  @PostMapping("/deleteAllRecordChecklistImagesByUrls")
  public void deleteAllRecordChecklistImagesByUrls(@RequestBody List<String> urls) {
    recordChecklistService.deleteImagesByUrls(urls);
  }

  @GetMapping("/getRecordChecklistStatusByDeviceId")
  public String getRecordChecklistStatusByDeviceId(@RequestParam String deviceId, @RequestParam(required=false) String x) {
    String s = recordChecklistService.getStatusByDeviceId(deviceId, x);
    return s != null ? s : Defaults.NULL_STRING;
  }

  @GetMapping("/getChecklistStatusCountDeviceId")
  public Integer getChecklistStatusCountDeviceId(@RequestParam String a, @RequestParam(required=false) String b, @RequestParam(required=false) String c) {
    return (int) recordChecklistService.getStatusCountByDeviceId(a, b);
  }

  @PostMapping("/updateRecordChecklistByDeviceId")
  public void updateRecordChecklistByDeviceId(@RequestBody List<String> ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    recordChecklistService.updateRecordChecklistByDeviceId(ids, oldId, newId);
  }

  @PostMapping("/getAllRecordChecklistByBuildings")
  public Set<String> getAllRecordChecklistByBuildings(@RequestBody List<String> buildingIds, @RequestParam(required=false) List<String> floorIds, @RequestParam(required=false) List<String> locationIds) {
    return recordChecklistService.getAllByBuildings(buildingIds, floorIds, locationIds);
  }

  @GetMapping("/getRecordChecklistStatusByLocationId")
  public String getRecordChecklistStatusByLocationId(@RequestParam String locationId, @RequestParam(required=false) String status) {
    String s = recordChecklistService.getStatusByLocationId(locationId, status);
    return s != null ? s : Defaults.NULL_STRING;
  }

  @GetMapping("/getChecklistStatusCountLocationId")
  public Integer getChecklistStatusCountLocationId(@RequestParam String locationId, @RequestParam(required=false) String a, @RequestParam(required=false) String b) {
    return (int) recordChecklistService.getStatusCountByLocationId(locationId, a);
  }

  @PostMapping("/updateRecordChecklistLocationAndIsRemoved")
  public void updateRecordChecklistLocationAndIsRemoved(@RequestBody List<String> locationIds) {
    recordChecklistService.updateLocationAndIsRemoved(locationIds);
  }

  @PostMapping("/deleteRecordChecklistByLocationId")
  public void deleteRecordChecklistByLocationId(@RequestParam(required=false) String locationId) {
    recordChecklistService.deleteByLocationId(locationId);
  }

  @PostMapping("/deleteAllRecordChecklistByLocationId")
  public List<String> deleteAllRecordChecklistByLocationId(@RequestParam(required=false) String locationId) {
    return recordChecklistService.deleteAllByLocationId(locationId);
  }

  @PostMapping("/updateRecordChecklist")
  public void updateRecordChecklist(@RequestParam(required=false) String email) {
    recordChecklistService.updateRecordChecklist(email);
  }
}