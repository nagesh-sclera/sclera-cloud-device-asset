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
@RequestMapping("/workordertemplate")
public class WorkorderTemplateController {
  @GetMapping("/getWorkOrderTemplateComment")
  public String getWorkOrderTemplateComment(@RequestParam String templateId) {
    return Defaults.NULL_STRING;
  }
}
