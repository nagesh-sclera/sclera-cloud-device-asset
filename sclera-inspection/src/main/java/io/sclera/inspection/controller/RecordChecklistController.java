package io.sclera.inspection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.inspection.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/recordchecklist")
public class RecordChecklistController {
  @GetMapping("/updateRecordChecklistDeviceAndIsRemoved")
  public void updateRecordChecklistDeviceAndIsRemoved(@RequestParam String ids) {
    // no-op
  }

  @GetMapping("/deleteRecordChecklistInBatch")
  public void deleteRecordChecklistInBatch(@RequestParam String ids) {
    // no-op
  }

  @GetMapping("/deleteAllRecordChecklistByDeviceId")
  public List<String> deleteAllRecordChecklistByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/deleteAllRecordChecklistImagesByUrls")
  public void deleteAllRecordChecklistImagesByUrls(@RequestParam String urls) {
    // no-op
  }

  @GetMapping("/getRecordChecklistStatusByDeviceId")
  public String getRecordChecklistStatusByDeviceId(@RequestParam String deviceId, @RequestParam String x) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getChecklistStatusCountDeviceId")
  public Integer getChecklistStatusCountDeviceId(@RequestParam String a, @RequestParam String b, @RequestParam String c) {
    return Defaults.ZERO;
  }

  @GetMapping("/updateRecordChecklistByDeviceId")
  public void updateRecordChecklistByDeviceId(@RequestParam String oldId, @RequestParam String newId, @RequestParam String ids) {
    // no-op
  }

  @GetMapping("/getAllRecordChecklistByBuildings")
  public Set<String> getAllRecordChecklistByBuildings(@RequestParam String buildingIds, @RequestParam String floorIds, @RequestParam String locationIds) {
    return Defaults.emptySet();
  }

  @GetMapping("/getRecordChecklistStatusByLocationId")
  public String getRecordChecklistStatusByLocationId(@RequestParam String locationId, @RequestParam String status) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getChecklistStatusCountLocationId")
  public Integer getChecklistStatusCountLocationId(@RequestParam String locationId, @RequestParam String a, @RequestParam String b) {
    return Defaults.ZERO;
  }

  @GetMapping("/updateRecordChecklistLocationAndIsRemoved")
  public void updateRecordChecklistLocationAndIsRemoved(@RequestParam String locationIds) {
    // no-op
  }

  @GetMapping("/deleteRecordChecklistByLocationId")
  public void deleteRecordChecklistByLocationId(@RequestParam String locationId) {
    // no-op
  }

  @GetMapping("/deleteAllRecordChecklistByLocationId")
  public List<String> deleteAllRecordChecklistByLocationId(@RequestParam String locationId) {
    return Defaults.emptyList();
  }

  @GetMapping("/updateRecordChecklist")
  public void updateRecordChecklist(@RequestParam String email) {
    // no-op
  }
}
