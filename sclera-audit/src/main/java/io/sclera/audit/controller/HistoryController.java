package io.sclera.audit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.audit.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/history")
public class HistoryController {
  @GetMapping("/insertDeviceStatusHistory")
  public void insertDeviceStatusHistory(@RequestParam Integer alarm, @RequestParam String ipAddress, @RequestParam String o, @RequestParam String o1, @RequestParam String finalDeviceId) {
    // no-op
  }

  @GetMapping("/addHistory")
  public void addHistory(@RequestParam String historyDTO) {
    // no-op
  }

  @GetMapping("/addHistoryWithTimestamp")
  public void addHistoryWithTimestamp(@RequestParam String historyDTO) {
    // no-op
  }

  @GetMapping("/updateHistoryDeviceId")
  public void updateHistoryDeviceId(@RequestParam String oldId, @RequestParam String newId) {
    // no-op
  }
}
