package io.sclera.audit.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/archivedrecord")
public class ArchivedRecordController {
  @org.springframework.web.bind.annotation.GetMapping("/batchUpdateArchivedRecords")
  public void batchUpdateArchivedRecords(@org.springframework.web.bind.annotation.RequestParam String logs) {
    // no-op
  }
}
