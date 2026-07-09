package io.sclera.service.impl;
import io.sclera.service.*;

import io.sclera.dto.ScheduledJobDTO;
import io.sclera.service.JobSchedulerService;
import org.springframework.stereotype.Service;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class JobSchedulerServiceImpl implements JobSchedulerService {
    /**
     * Creates a scheduled job and returns its id. Stub returns null.
     */
    public String createScheduledJob(ScheduledJobDTO dto) { return null; }
    /**
     * Registers the given scheduled jobs. Stub is a no-op.
     */
    public void addScheduledJob(Set<ScheduledJobDTO> dtos) {}
    /**
     * Deletes the scheduled jobs with the given ids. Stub is a no-op.
     */
    public void deleteScheduledJob(Set<String> jobIds) {}
    /**
     * Returns the scheduled job associated with the given condition id. Stub returns null.
     */
    public ScheduledJobDTO getScheduledJobByConditionId(String conditionId) { return null; }
}
