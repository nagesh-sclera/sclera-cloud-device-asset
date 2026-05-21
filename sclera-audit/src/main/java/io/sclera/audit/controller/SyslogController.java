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
@RequestMapping("/syslog")
public class SyslogController {
  @GetMapping("/getSyslogExcludeDeviceIds")
  public String getSyslogExcludeDeviceIds(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d) {
    return Defaults.NULL_STRING;
  }

  @PostMapping("/deleteByDeviceId")
  public void deleteByDeviceId(@RequestParam(required=false) String deviceId) {
    // no-op
  }
}
