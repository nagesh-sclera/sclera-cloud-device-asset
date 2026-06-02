package io.sclera.scheduler.service;

import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HousekeepingService {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingService.class);

    private final JobRunRepository runs;

    @Value("${scheduler.orphan-timeout-seconds}") private long orphanTimeoutSeconds;
    @Value("${scheduler.history-retention-days}") private long retentionDays;

    public HousekeepingService(JobRunRepository runs) {
        this.runs = runs;
    }

    /** Every 5 minutes: FIRED runs with no result past the timeout become FAILED. */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void reapOrphans() {
        Instant cutoff = Instant.now().minusSeconds(orphanTimeoutSeconds);
        List<JobRunEntity> orphans = runs.findByStatusAndFiredAtBefore(RunStatus.FIRED, cutoff);
        for (JobRunEntity r : orphans) {
            r.setStatus(RunStatus.FAILED);
            r.setFinishedAt(Instant.now());
            r.setError("timed out: no result received");
        }
        if (!orphans.isEmpty()) log.warn("Reaped {} orphan runs", orphans.size());
    }

    /** Daily: delete run history older than the retention window. */
    @Scheduled(fixedDelay = 86_400_000)
    @Transactional
    public void pruneHistory() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = runs.deleteByFiredAtBefore(cutoff);
        if (deleted > 0) log.info("Pruned {} old run rows", deleted);
    }
}
