package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface JobInstanceRepository
        extends JpaRepository<JobInstanceEntity, JobInstanceId> {

    List<JobInstanceEntity> findByJobName(String jobName);
    List<JobInstanceEntity> findByVdmsId(String vdmsId);
    long countByVdmsId(String vdmsId);
    List<JobInstanceEntity> findByStateAndSnoozeUntilLessThanEqual(
            JobInstanceState state, Instant cutoff);

    @org.springframework.data.jpa.repository.Query(
        "select i.jobName as jobName, i.state as state, count(i) as cnt "
      + "from JobInstanceEntity i group by i.jobName, i.state")
    java.util.List<JobInstanceStateCount> countByJobNameAndState();
}
