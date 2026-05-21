package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduledjobrepo")
public class ScheduledJobRepoController {

    @GetMapping("/deleteByConditionId")
    public void deleteByConditionId(@RequestParam(required = false) String conditionId) {
        // no-op
    }
}
