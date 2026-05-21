package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.Repository.ScheduledJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.stubs.ScheduledJobRepositoryStub}
 * and {@code io.sclera.Repository.ScheduledJobRepositoryImpl}.
 */
@Component
@Primary
public class ScheduledJobRepoClient implements ScheduledJobRepository {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobRepoClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public ScheduledJobRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    @Override
    public void deleteByConditionId(String conditionId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("conditionId", conditionId);
            dapr.invokeMethod(APP_ID, "scheduledjobrepo/deleteByConditionId", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("ScheduledJobRepoClient.deleteByConditionId failed; swallowing", e);
        }
    }
}
