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
@RequestMapping("/corrigo")
public class CorrigoController {
  @GetMapping("/getCorrigoConfigurationDetails")
  public String getCorrigoConfigurationDetails() {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/updateCorrigoAssets")
  public void updateCorrigoAssets(@RequestParam String username, @RequestParam String vdmsid, @RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam String searchKey, @RequestParam String config) {
    // no-op
  }

  @GetMapping("/getWorkordersByAssetIdForBot")
  public String getWorkordersByAssetIdForBot(@RequestParam String device) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/corrigoUrlSync")
  public void corrigoUrlSync(@RequestParam String url, @RequestParam String vdmsId) {
    // no-op
  }

  @GetMapping("/updateCorrigoCredentialsFromCloud")
  public void updateCorrigoCredentialsFromCloud(@RequestParam String vdmsId) {
    // no-op
  }

  @GetMapping("/updateCorrigoCredentialsMigration")
  public void updateCorrigoCredentialsMigration(@RequestParam String vdmsId) {
    // no-op
  }
}
