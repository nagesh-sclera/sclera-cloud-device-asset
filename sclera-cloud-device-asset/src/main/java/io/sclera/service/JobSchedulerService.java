package io.sclera.service;

import io.sclera.dto.ScheduledJobDTO;
import java.util.Set;

/** Service contract for {@link io.sclera.service.JobSchedulerService}. */
public interface JobSchedulerService {
    String createScheduledJob(ScheduledJobDTO dto);

    void addScheduledJob(Set<ScheduledJobDTO> dtos);

    void deleteScheduledJob(Set<String> jobIds);

    ScheduledJobDTO getScheduledJobByConditionId(String conditionId);
}
