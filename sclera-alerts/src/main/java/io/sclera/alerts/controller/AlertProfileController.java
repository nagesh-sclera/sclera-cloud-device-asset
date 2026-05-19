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
@RequestMapping("/alertprofile")
public class AlertProfileController {
  @GetMapping("/getAlertProfileById")
  public String getAlertProfileById(@RequestParam String alertProfileId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getAlertProfileDetailsById")
  public String getAlertProfileDetailsById(@RequestParam String a, @RequestParam String b, @RequestParam String deviceId) {
    return Defaults.NULL_STRING;
  }
}
