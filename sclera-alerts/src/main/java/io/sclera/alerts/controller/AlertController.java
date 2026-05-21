package io.sclera.alerts.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.alerts.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/alert")
public class AlertController {
  @PostMapping("/sendDeviceConditionsAlertInfo")
  public void sendDeviceConditionsAlertInfo(@RequestBody String deviceAlert, @RequestParam(required=false) String alertProfile, @RequestParam(required=false) String timestamp) {
    // no-op
  }

  @PostMapping("/sendSensorAlertInfo")
  public void sendSensorAlertInfo(@RequestBody String sensorAlert, @RequestParam(required=false) String alertProfile, @RequestParam(required=false) String timestamp) {
    // no-op
  }

  @PostMapping("/sendDownloadEmail")
  public void sendDownloadEmail(@RequestBody String body, @RequestParam(required=false) String file, @RequestParam(required=false) String type, @RequestParam(required=false) String vdmsId) {
    // no-op
  }

  @GetMapping("/getAllCallFlowRules")
  public List<String> getAllCallFlowRules(@RequestParam Integer offset, @RequestParam Integer pagesize, @RequestParam String searchkey) {
    return Defaults.emptyList();
  }

  @PostMapping("/deleteById")
  public void deleteById(@RequestParam(required=false) String id) {
    // no-op
  }

  @GetMapping("/checkCallFlowByDeviceid")
  public String checkCallFlowByDeviceid(@RequestParam String deviceId) {
    return Defaults.NULL_STRING;
  }

  @PostMapping("/upsertAiCallFlow")
  public void upsertAiCallFlow(@RequestBody String createdAt, @RequestParam(required=false) String updatedAt, @RequestParam(required=false) String id, @RequestParam(required=false) String name, @RequestParam(required=false) String createdBy, @RequestParam(required=false) String updatedBy, @RequestParam(required=false) String deviceId) {
    // no-op
  }

  @GetMapping("/getCallFlowByDeviceId")
  public List<String> getCallFlowByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @PostMapping("/upsertCallFlowRuleCondition")
  public void upsertCallFlowRuleCondition(@RequestParam(required=false) String id, @RequestParam(required=false) String criteria, @RequestParam(required=false) String actionType, @RequestParam(required=false) String actionValue, @RequestParam(required=false) String actionMessage, @RequestParam(required=false) String callFlowRuleId) {
    // no-op
  }

  @GetMapping("/getCallFlowRuleConditionsByCallFlowRuleId")
  public List<String> getCallFlowRuleConditionsByCallFlowRuleId(@RequestParam String callFlowRuleId) {
    return Defaults.emptyList();
  }

  @GetMapping("/getCallFlowRuleConditionByRuleIdAndCriteria")
  public List<String> getCallFlowRuleConditionByRuleIdAndCriteria(@RequestParam String ruleId, @RequestParam String criteria) {
    return Defaults.emptyList();
  }

  @PostMapping("/deleteCallFlowRuleConditionById")
  public void deleteCallFlowRuleConditionById(@RequestBody String ids) {
    // no-op
  }
}
