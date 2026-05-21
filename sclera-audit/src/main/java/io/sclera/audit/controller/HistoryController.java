package io.sclera.audit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/insertDeviceStatusHistory")
  public void insertDeviceStatusHistory(@RequestBody String o, @RequestParam(required=false) String o1, @RequestParam(required=false) Integer alarm, @RequestParam(required=false) String ipAddress, @RequestParam(required=false) String finalDeviceId) {
    // no-op
  }

  @PostMapping("/addHistory")
  public void addHistory(@RequestBody String historyDTO) {
    // no-op
  }

  @PostMapping("/addHistoryWithTimestamp")
  public void addHistoryWithTimestamp(@RequestBody String historyDTO) {
    // no-op
  }

  @PostMapping("/updateHistoryDeviceId")
  public void updateHistoryDeviceId(@RequestParam(required=false) String oldId, @RequestParam(required=false) String newId) {
    // no-op
  }
}
