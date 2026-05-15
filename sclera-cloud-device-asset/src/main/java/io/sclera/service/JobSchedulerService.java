package io.sclera.service;

import io.sclera.dto.ScheduledJobDTO;
import org.springframework.stereotype.Service;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class JobSchedulerService {
    public String createScheduledJob(ScheduledJobDTO dto) { return null; }
    public void addScheduledJob(Set<ScheduledJobDTO> dtos) {}
    public void deleteScheduledJob(Set<String> jobIds) {}
    public ScheduledJobDTO getScheduledJobByConditionId(String conditionId) { return null; }
}
