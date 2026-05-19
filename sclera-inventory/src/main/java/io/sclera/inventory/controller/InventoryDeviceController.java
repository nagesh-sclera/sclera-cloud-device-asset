package io.sclera.inventory.controller;

import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/upsertInventoryDevices")
  public Set<String> upsertInventoryDevices(@RequestParam String stockedOutItems, @RequestParam String vdmsId, @RequestParam String email, @RequestParam String dto) {
    return Defaults.emptySet();
  }

  @GetMapping("/deleteByDeviceId")
  public void deleteByDeviceId(@RequestParam String deviceId) {
    // no-op
  }
}
