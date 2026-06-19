package io.sclera.workorder.jobs;

import io.micrometer.core.instrument.MeterRegistry;
import io.sclera.workorder.config.DaprProperties;
import io.sclera.workorder.config.JobsProperties;
import io.sclera.workorder.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link JobService}: runs cron-triggered jobs with retry, dead-letter routing,
 * metrics, and a per-run correlation id.
 *
 * <p>Add a new job by registering a case in {@link #dispatch(String)}. Job bodies must be
 * idempotent. The reference job {@code status-refresh} is a safe read (counts tickets and
 * records a metric) — replace/extend with real device-sync, sensor-poll, etc.
 */
@Service
public class JobServiceImpl implements JobService {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
    private static final String MDC_KEY = "requestId";

    private final JobsProperties props;
    private final DaprProperties dapr;
    private final RestClient daprRestClient;
    private final MeterRegistry meterRegistry;
    private final TicketRepository ticketRepository;

    public JobServiceImpl(JobsProperties props, DaprProperties dapr, RestClient daprRestClient,
                          MeterRegistry meterRegistry, TicketRepository ticketRepository) {
        this.props = props;
        this.dapr = dapr;
        this.daprRestClient = daprRestClient;
        this.meterRegistry = meterRegistry;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public boolean run(String jobName) {
        if (!props.isEnabled()) {
            log.info("Jobs disabled — skipping '{}'", jobName);
            return true;
        }
        String runId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_KEY, runId);
        long start = System.currentTimeMillis();
        try {
            for (int attempt = 1; attempt <= props.getMaxAttempts(); attempt++) {
                try {
                    log.info("Job '{}' attempt {}/{} starting", jobName, attempt, props.getMaxAttempts());
                    dispatch(jobName);
                    meterRegistry.counter("jobs.runs", "job", jobName, "outcome", "success").increment();
                    log.info("Job '{}' succeeded in {} ms (attempt {})", jobName, System.currentTimeMillis() - start, attempt);
                    return true;
                } catch (Exception e) {
                    log.warn("Job '{}' attempt {}/{} failed: {}", jobName, attempt, props.getMaxAttempts(), e.getMessage());
                    if (attempt < props.getMaxAttempts()) {
                        sleep(props.getRetryDelayMs());
                    } else {
                        meterRegistry.counter("jobs.runs", "job", jobName, "outcome", "failure").increment();
                        log.error("Job '{}' failed after {} attempts; routing to DLQ", jobName, props.getMaxAttempts(), e);
                        publishToDlq(jobName, runId, e.getMessage());
                        return false;
                    }
                }
            }
            return false;
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /** Routes a job name to its implementation. Register new jobs here. */
    private void dispatch(String jobName) {
        switch (jobName) {
            case "status-refresh" -> runStatusRefresh();
            // case "device-sync" -> runDeviceSync();   // add real jobs here
            default -> throw new IllegalArgumentException("Unknown job: " + jobName);
        }
    }

    /** Reference job — safe, idempotent read that records the current ticket count as a metric. */
    private void runStatusRefresh() {
        long total = ticketRepository.count();
        meterRegistry.gauge("jobs.status_refresh.ticket_count", total);
        log.info("status-refresh: {} tickets currently tracked", total);
    }

    private void publishToDlq(String jobName, String runId, String error) {
        try {
            Map<String, Object> event = Map.of(
                    "job", jobName,
                    "requestId", runId,
                    "error", error == null ? "unknown" : error,
                    "timestamp", BigInteger.valueOf(System.currentTimeMillis()));
            daprRestClient.post()
                    .uri("/publish/{pubsub}/{topic}", dapr.getPubsubName(), props.getDlqTopic())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Job '{}' run {} published to DLQ topic '{}'", jobName, runId, props.getDlqTopic());
        } catch (Exception e) {
            log.error("Failed to publish job '{}' to DLQ topic '{}': {}", jobName, props.getDlqTopic(), e.getMessage());
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
