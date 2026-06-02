package io.sclera.scheduler.domain;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

public interface JobRunRepository extends JpaRepository<JobRunEntity, java.util.UUID> {

    List<JobRunEntity> findByJobNameOrderByFiredAtDesc(String jobName, Limit limit);

    List<JobRunEntity> findByStatusAndFiredAtBefore(RunStatus status, Instant before);

    // Derived delete: needs an active transaction. Annotated here so it is safe
    // regardless of whether the caller opened one.
    @Transactional
    long deleteByFiredAtBefore(Instant before);
}
