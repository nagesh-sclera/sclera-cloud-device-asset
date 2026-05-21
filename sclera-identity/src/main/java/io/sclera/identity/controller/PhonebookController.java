package io.sclera.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/addPhoneBookByDeviceId")
  public void addPhoneBookByDeviceId(@RequestBody String d, @RequestParam(required=false) String a, @RequestParam(required=false) String b, @RequestParam(required=false) String c, @RequestParam(required=false) String e) {
    // no-op
  }

  @GetMapping("/getPhoneAddressById")
  public String getPhoneAddressById(@RequestParam String id) {
    return Defaults.NULL_STRING;
  }
}
