package io.sclera.workorders.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.workorders.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/ticket")
public class TicketController {
  @GetMapping("/getTicketCountByDeviceId")
  public Integer getTicketCountByDeviceId(@RequestParam String deviceId) {
    return Defaults.ZERO;
  }

  @GetMapping("/getOpenTicketStatus")
  public Boolean getOpenTicketStatus(@RequestParam String deviceId) {
    return Defaults.FALSE;
  }

  @GetMapping("/updateTicketAssigneeByUserEmail")
  public void updateTicketAssigneeByUserEmail(@RequestParam String email) {
    // no-op
  }
}
