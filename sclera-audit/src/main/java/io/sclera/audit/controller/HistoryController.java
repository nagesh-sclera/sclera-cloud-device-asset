package io.sclera.audit.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/history")
public class HistoryController {
  @org.springframework.web.bind.annotation.GetMapping("/insertDeviceStatusHistory")
  public void insertDeviceStatusHistory(@org.springframework.web.bind.annotation.RequestParam Integer alarm, @org.springframework.web.bind.annotation.RequestParam String ipAddress, @org.springframework.web.bind.annotation.RequestParam String o, @org.springframework.web.bind.annotation.RequestParam String o1, @org.springframework.web.bind.annotation.RequestParam String finalDeviceId) {
    // no-op
  }

  @org.springframework.web.bind.annotation.GetMapping("/addHistory")
  public void addHistory(@org.springframework.web.bind.annotation.RequestParam String historyDTO) {
    // no-op
  }

  @org.springframework.web.bind.annotation.GetMapping("/addHistoryWithTimestamp")
  public void addHistoryWithTimestamp(@org.springframework.web.bind.annotation.RequestParam String historyDTO) {
    // no-op
  }

  @org.springframework.web.bind.annotation.GetMapping("/updateHistoryDeviceId")
  public void updateHistoryDeviceId(@org.springframework.web.bind.annotation.RequestParam String oldId, @org.springframework.web.bind.annotation.RequestParam String newId) {
    // no-op
  }
}
