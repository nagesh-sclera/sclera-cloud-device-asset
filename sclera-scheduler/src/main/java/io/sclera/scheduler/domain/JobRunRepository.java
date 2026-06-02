package io.sclera.scheduler.domain;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface JobRunRepository extends JpaRepository<JobRunEntity, java.util.UUID> {

    List<JobRunEntity> findByJobNameOrderByFiredAtDesc(String jobName, Limit limit);

    List<JobRunEntity> findByStatusAndFiredAtBefore(RunStatus status, Instant before);

    long deleteByFiredAtBefore(Instant before);
}
