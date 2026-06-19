package io.sclera.workorder.jobs;

/**
 * Executes named background jobs triggered by the Dapr cron binding.
 *
 * <p>Implementations wrap each run with: a correlation id, retry with a fixed delay
 * (per {@link io.sclera.workorder.config.JobsProperties}), dead-letter publishing on
 * terminal failure, and metrics. Jobs must be <b>idempotent</b> — a tick may be
 * retried or (rarely) delivered more than once.
 */
public interface JobService {

    /**
     * Runs the named job to completion (including retries), publishing to the dead-letter
     * topic if all attempts fail. Never throws — the cron trigger always gets an ack.
     *
     * @param jobName the job identifier (the cron binding's logical name, e.g. {@code status-refresh})
     * @return {@code true} if the job succeeded within the retry budget, {@code false} otherwise
     */
    boolean run(String jobName);
}
