package io.sclera.inspection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/updateRecordChecklistDeviceAndIsRemoved")
  public void updateRecordChecklistDeviceAndIsRemoved(@RequestBody String ids) {
    // no-op
  }

  @PostMapping("/deleteRecordChecklistInBatch")
  public void deleteRecordChecklistInBatch(@RequestBody String ids) {
    // no-op
  }

  @PostMapping("/deleteAllRecordChecklistByDeviceId")
  public List<String> deleteAllRecordChecklistByDeviceId(@RequestParam(required=false) String deviceId) {
    return Defaults.emptyList();
  }

  @PostMapping("/deleteAllRecordChecklistImagesByUrls")
  public void deleteAllRecordChecklistImagesByUrls(@RequestBody String urls) {
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

  @PostMapping("/updateRecordChecklistByDeviceId")
  public void updateRecordChecklistByDeviceId(@RequestBody String ids, @RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }

  @PostMapping("/getAllRecordChecklistByBuildings")
  public Set<String> getAllRecordChecklistByBuildings(@RequestBody String buildingIds, @RequestParam(required=false) String floorIds, @RequestParam(required=false) String locationIds) {
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

  @PostMapping("/updateRecordChecklistLocationAndIsRemoved")
  public void updateRecordChecklistLocationAndIsRemoved(@RequestBody String locationIds) {
    // no-op
  }

  @PostMapping("/deleteRecordChecklistByLocationId")
  public void deleteRecordChecklistByLocationId(@RequestParam(required=false) String locationId) {
    // no-op
  }

  @PostMapping("/deleteAllRecordChecklistByLocationId")
  public List<String> deleteAllRecordChecklistByLocationId(@RequestParam(required=false) String locationId) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateRecordChecklist")
  public void updateRecordChecklist(@RequestParam(required=false) String email) {
    // no-op
  }
}
