package io.sclera.audit.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/syslog")
public class SyslogController {
  @org.springframework.web.bind.annotation.GetMapping("/getSyslogExcludeDeviceIds")
  public String getSyslogExcludeDeviceIds(@org.springframework.web.bind.annotation.RequestParam String a, @org.springframework.web.bind.annotation.RequestParam String b, @org.springframework.web.bind.annotation.RequestParam String c, @org.springframework.web.bind.annotation.RequestParam String d) {
    return io.sclera.audit.defaults.Defaults.NULL_STRING;
  }

  @org.springframework.web.bind.annotation.GetMapping("/deleteByDeviceId")
  public void deleteByDeviceId(@org.springframework.web.bind.annotation.RequestParam String deviceId) {
    // no-op
  }
}
