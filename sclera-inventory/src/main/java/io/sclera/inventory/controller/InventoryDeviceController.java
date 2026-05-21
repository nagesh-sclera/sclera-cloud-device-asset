package io.sclera.inventory.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.inventory.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/inventorydevice")
public class InventoryDeviceController {
  @GetMapping("/retireInventoryDevice")
  public void retireInventoryDevice(@RequestParam String vdmsId, @RequestParam String deviceId, @RequestParam String username, @RequestParam String description, @RequestParam String inventoryTrackingId) {
    // no-op
  }

  @PostMapping("/upsertInventoryDevices")
  public Set<String> upsertInventoryDevices(@RequestBody String stockedOutItems, @RequestParam(required=false) String dto, @RequestParam(required=false) String vdmsId, @RequestParam(required=false) String email) {
    return Defaults.emptySet();
  }

  @PostMapping("/deleteByDeviceId")
  public void deleteByDeviceId(@RequestParam(required=false) String deviceId) {
    // no-op
  }
}
