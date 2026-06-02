package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, String> {
    List<JobEntity> findByState(JobState state);
}
