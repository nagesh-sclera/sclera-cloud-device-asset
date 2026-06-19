package io.sclera.workorder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for scheduled background jobs, bound from {@code jobs.*}.
 *
 * <p>Jobs are triggered by a Dapr <b>cron input-binding</b> (the schedule lives in the
 * component YAML, e.g. {@code components-docker/scheduler.yaml}, and is env-overridable).
 * This class controls the in-app execution policy: retry, dead-letter routing, and an
 * on/off switch. All values are overridable via environment variables (relaxed binding),
 * e.g. {@code JOBS_MAX_ATTEMPTS}.
 */
@ConfigurationProperties(prefix = "jobs")
public class JobsProperties {

    /** Master switch — when false, triggers are accepted but skipped (logged). */
    private boolean enabled = true;

    /** Total attempts per run (1 = no retry). */
    private int maxAttempts = 3;

    /** Fixed delay between retry attempts, in milliseconds. */
    private long retryDelayMs = 1000L;

    /** Dapr pub/sub topic that terminally-failed job runs are published to. */
    private String dlqTopic = "jobs-dlq";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }

    public String getDlqTopic() { return dlqTopic; }
    public void setDlqTopic(String dlqTopic) { this.dlqTopic = dlqTopic; }
}
