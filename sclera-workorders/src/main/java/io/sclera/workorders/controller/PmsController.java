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
@RequestMapping("/pms")
public class PmsController {
  @GetMapping("/getLocationIdsByRoomStatus")
  public Set<String> getLocationIdsByRoomStatus(@RequestParam String vdmsId, @RequestParam String status) {
    return Defaults.emptySet();
  }

  @PostMapping("/getPmsAttributesByLocationIds")
  public Set<String> getPmsAttributesByLocationIds(@RequestBody String locationIds) {
    return Defaults.emptySet();
  }

  @PostMapping("/updatePmsAttributesByLocationId")
  public void updatePmsAttributesByLocationId(@RequestParam(required=false) String locationId) {
    // no-op
  }
}
