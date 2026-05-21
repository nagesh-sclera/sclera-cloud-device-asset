package io.sclera.integrations.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/updateDeviceTypeForAllAsset")
  public void updateDeviceTypeForAllAsset(@RequestBody String deviceTypes) {
    // no-op
  }

  @PostMapping("/updatePropertyServiceLocations")
  public void updatePropertyServiceLocations(@RequestParam(required=false) String locationId) {
    // no-op
  }

  @PostMapping("/upsertPropertyServiceDetails")
  public String upsertPropertyServiceDetails(@RequestBody String dto, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
    return Defaults.NULL_STRING;
  }

  @PostMapping("/addPropertyServiceLocations")
  public void addPropertyServiceLocations(@RequestBody String locations, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId, @RequestParam(required=false) String serviceId) {
    // no-op
  }

  @PostMapping("/multiUpdatePropertyServiceResponse")
  public void multiUpdatePropertyServiceResponse(@RequestBody String responses, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
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

  @PostMapping("/deletePropertyServiceRequests")
  public void deletePropertyServiceRequests(@RequestBody String requests, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @PostMapping("/deletePropertyServiceLocations")
  public void deletePropertyServiceLocations(@RequestBody String locations, @RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId, @RequestParam(required=false) String serviceId) {
    // no-op
  }

  @PostMapping("/deletePropertyService")
  public void deletePropertyService(@RequestParam(required=false) String username, @RequestParam(required=false) String vdmsId, @RequestParam(required=false) String serviceId) {
    // no-op
  }

  @GetMapping("/getZoneMap")
  public Set<String> getZoneMap(@RequestParam String username, @RequestParam String vdmsId, @RequestParam String buildingId, @RequestParam String floorId, @RequestParam String locationId, @RequestParam String serviceId) {
    return Defaults.emptySet();
  }

  @PostMapping("/syncServiceValue")
  public void syncServiceValue(@RequestParam(required=false) String vdmsId) {
    // no-op
  }
}
