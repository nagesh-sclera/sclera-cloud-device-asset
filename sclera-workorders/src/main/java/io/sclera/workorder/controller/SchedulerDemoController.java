package io.sclera.workorder.controller;

import io.sclera.workorder.client.SchedulerClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Demo: asks the central scheduler to fire the workorder-owned catalog job once after
 * {@code seconds}. Served at /api/v1/workorder-service/demo/schedule-onetime. When the
 * trigger comes back on {@code scheduler.trigger}, {@code SchedulerTriggerSubscriber}
 * prints it (the event's owner is "workorder").
 */
@RestController
@RequestMapping("/demo")
public class SchedulerDemoController {

    /** The workorder-owned recurring job registered in the scheduler catalog (jobs.yaml). */
    static final String WORKORDER_JOB = "workorderTicketSync";

    private final SchedulerClient scheduler;

    public SchedulerDemoController(SchedulerClient scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/schedule-onetime")
    public Map<String, String> scheduleOneTime(@RequestParam(defaultValue = "15") long seconds) {
        Instant dueAt = Instant.now().plusSeconds(seconds);
        scheduler.scheduleOnce(WORKORDER_JOB, dueAt);
        return Map.of("scheduledJob", WORKORDER_JOB, "dueAt", dueAt.toString());
    }
}
