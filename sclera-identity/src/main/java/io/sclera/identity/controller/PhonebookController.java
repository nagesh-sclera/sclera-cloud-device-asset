package io.sclera.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.identity.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/phonebook")
public class PhonebookController {
  @GetMapping("/addPhoneBookByDeviceId")
  public void addPhoneBookByDeviceId(@RequestParam String a, @RequestParam String b, @RequestParam String c, @RequestParam String d, @RequestParam String e) {
    // no-op
  }

  @GetMapping("/getPhoneAddressById")
  public String getPhoneAddressById(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }
}
