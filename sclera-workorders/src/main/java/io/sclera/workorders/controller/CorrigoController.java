package io.sclera.workorders.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping("/updateCorrigoAssets")
  public void updateCorrigoAssets(@RequestBody String config, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsid, @RequestParam(required=false) Integer pageNo, @RequestParam(required=false) Integer pageSize, @RequestParam(required=false) String searchKey) {
    // no-op
  }

  @PostMapping("/getWorkordersByAssetIdForBot")
  public String getWorkordersByAssetIdForBot(@RequestBody String device) {
    return Defaults.NULL_STRING;
  }

  @PostMapping("/corrigoUrlSync")
  public void corrigoUrlSync(@RequestBody String url, @RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @PostMapping("/updateCorrigoCredentialsFromCloud")
  public void updateCorrigoCredentialsFromCloud(@RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @PostMapping("/updateCorrigoCredentialsMigration")
  public void updateCorrigoCredentialsMigration(@RequestParam(required=false) String vdmsId) {
    // no-op
  }
}
