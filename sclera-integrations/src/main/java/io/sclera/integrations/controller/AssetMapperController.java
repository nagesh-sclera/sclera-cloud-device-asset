package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.integrations.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/assetmapper")
public class AssetMapperController {
  @GetMapping("/updateDeviceTypeForAllAsset")
  public void updateDeviceTypeForAllAsset(@RequestParam String deviceTypes) {
    // no-op
  }

  @GetMapping("/updatePropertyServiceLocations")
  public void updatePropertyServiceLocations(@RequestParam String locationId) {
    // no-op
  }

  @GetMapping("/upsertPropertyServiceDetails")
  public String upsertPropertyServiceDetails(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String dto) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/addPropertyServiceLocations")
  public void addPropertyServiceLocations(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String serviceId, @RequestParam String locations) {
    // no-op
  }

  @GetMapping("/multiUpdatePropertyServiceResponse")
  public void multiUpdatePropertyServiceResponse(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String responses) {
    // no-op
  }

  @GetMapping("/getPropertyServices")
  public Set<String> getPropertyServices(@RequestParam String username, @RequestParam String vdmsId) {
    return Defaults.emptySet();
  }

  @GetMapping("/getPropertyServiceLocationsById")
  public Set<String> getPropertyServiceLocationsById(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String serviceId) {
    return Defaults.emptySet();
  }

  @GetMapping("/deletePropertyServiceRequests")
  public void deletePropertyServiceRequests(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String requests) {
    // no-op
  }

  @GetMapping("/deletePropertyServiceLocations")
  public void deletePropertyServiceLocations(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String serviceId, @RequestParam String locations) {
    // no-op
  }

  @GetMapping("/deletePropertyService")
  public void deletePropertyService(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String serviceId) {
    // no-op
  }

  @GetMapping("/getZoneMap")
  public Set<String> getZoneMap(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String buildingId, @RequestParam String floorId, @RequestParam String locationId, @RequestParam String serviceId) {
    return Defaults.emptySet();
  }

  @GetMapping("/syncServiceValue")
  public void syncServiceValue(@RequestParam String vdmsId) {
    // no-op
  }
}
