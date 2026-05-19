package io.sclera.alerts.controller;

import org.springframework.web.bind.annotation.GetMapping;
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
  @GetMapping("/sendDeviceConditionsAlertInfo")
  public void sendDeviceConditionsAlertInfo(@RequestParam String deviceAlert, @RequestParam String alertProfile, @RequestParam String timestamp) {
    // no-op
  }

  @GetMapping("/sendSensorAlertInfo")
  public void sendSensorAlertInfo(@RequestParam String sensorAlert, @RequestParam String alertProfile, @RequestParam String timestamp) {
    // no-op
  }

  @GetMapping("/sendDownloadEmail")
  public void sendDownloadEmail(@RequestParam String body, @RequestParam String file, @RequestParam String type, @RequestParam String vdmsId) {
    // no-op
  }

  @GetMapping("/getAllCallFlowRules")
  public List<String> getAllCallFlowRules(@RequestParam Integer offset, @RequestParam Integer pagesize, @RequestParam String searchkey) {
    return Defaults.emptyList();
  }

  @GetMapping("/deleteById")
  public void deleteById(@RequestParam String id) {
    // no-op
  }

  @GetMapping("/checkCallFlowByDeviceid")
  public String checkCallFlowByDeviceid(@RequestParam String deviceId) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/upsertAiCallFlow")
  public void upsertAiCallFlow(@RequestParam String id, @RequestParam String name, @RequestParam String createdBy, @RequestParam String createdAt, @RequestParam String updatedBy, @RequestParam String updatedAt, @RequestParam String deviceId) {
    // no-op
  }

  @GetMapping("/getCallFlowByDeviceId")
  public List<String> getCallFlowByDeviceId(@RequestParam String deviceId) {
    return Defaults.emptyList();
  }

  @GetMapping("/upsertCallFlowRuleCondition")
  public void upsertCallFlowRuleCondition(@RequestParam String id, @RequestParam String criteria, @RequestParam String actionType, @RequestParam String actionValue, @RequestParam String actionMessage, @RequestParam String callFlowRuleId) {
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

  @GetMapping("/deleteCallFlowRuleConditionById")
  public void deleteCallFlowRuleConditionById(@RequestParam String ids) {
    // no-op
  }
}
