package io.sclera.stubs;

import io.sclera.Repository.ScheduledJobRepository;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobRepositoryStub implements ScheduledJobRepository {

    @Override
    public void deleteByConditionId(String conditionId) {
    }
}
