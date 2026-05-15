package io.sclera.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
@Repository
@Primary
public class ScheduledJobRepositoryImpl implements ScheduledJobRepository {
    private static final Logger log = LoggerFactory.getLogger(ScheduledJobRepositoryImpl.class);
    @Override public void deleteByConditionId(String conditionId) { log.warn("[STUB] ScheduledJobRepository.deleteByConditionId"); }
}
