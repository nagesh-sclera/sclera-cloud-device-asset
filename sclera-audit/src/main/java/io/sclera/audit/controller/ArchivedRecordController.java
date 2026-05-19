package io.sclera.audit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.audit.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/archivedrecord")
public class ArchivedRecordController {
  @GetMapping("/batchUpdateArchivedRecords")
  public void batchUpdateArchivedRecords(@RequestParam String logs) {
    // no-op
  }
}
