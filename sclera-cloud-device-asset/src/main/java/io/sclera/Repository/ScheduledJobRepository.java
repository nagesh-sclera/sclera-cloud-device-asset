package io.sclera.Repository;

import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface ScheduledJobRepository {
    /**
     * Deletes the scheduled jobs associated with the given condition.
     *
     * @param conditionId the condition identifier
     */
    void deleteByConditionId(String conditionId);
}
