package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface JobInstanceRepository
        extends JpaRepository<JobInstanceEntity, JobInstanceId> {

    List<JobInstanceEntity> findByJobName(String jobName);
    List<JobInstanceEntity> findByVdmsId(String vdmsId);
    List<JobInstanceEntity> findByStateAndSnoozeUntilLessThanEqual(
            JobInstanceState state, Instant cutoff);
}
