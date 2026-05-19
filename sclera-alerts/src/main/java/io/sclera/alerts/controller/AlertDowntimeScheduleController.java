package io.sclera.alerts.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.alerts.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/alertdowntimeschedule")
public class AlertDowntimeScheduleController {
  @GetMapping("/checkAlertDowntime")
  public Boolean checkAlertDowntime(@RequestParam String deviceId, @RequestParam String alertProfileId) {
    return Defaults.FALSE;
  }
}
