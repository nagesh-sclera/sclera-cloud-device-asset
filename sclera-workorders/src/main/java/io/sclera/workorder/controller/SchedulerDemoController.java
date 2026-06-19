package io.sclera.workorder.controller;

import io.sclera.workorder.client.SchedulerClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Demo: schedules a one-time job (via the central scheduler) that fires after {@code seconds}.
 * Served at /api/v1/workorder-service/demo/schedule-onetime. The fire is printed by
 * {@code SchedulerTriggerSubscriber} when the trigger comes back.
 */
@RestController
@RequestMapping("/demo")
public class SchedulerDemoController {

    private final SchedulerClient scheduler;

    public SchedulerDemoController(SchedulerClient scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/schedule-onetime")
    public Map<String, String> scheduleOneTime(@RequestParam(defaultValue = "15") long seconds) {
        String name = "demoWorkorderOnce-" + UUID.randomUUID().toString().substring(0, 8);
        Instant dueAt = Instant.now().plusSeconds(seconds);
        scheduler.scheduleOneTime(name, "workorder", dueAt);
        return Map.of("scheduledJob", name, "dueAt", dueAt.toString());
    }
}
