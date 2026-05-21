package io.sclera.client;

import io.dapr.client.DaprClient;
import io.sclera.Repository.RemoteAgentServerDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.stubs.RemoteAgentServerDetailsRepositoryStub}
 * and {@code io.sclera.Repository.RemoteAgentServerDetailsRepositoryImpl}.
 */
@Component
@Primary
public class RemoteAgentServerDetailsRepoClient implements RemoteAgentServerDetailsRepository {

    private static final Logger log = LoggerFactory.getLogger(RemoteAgentServerDetailsRepoClient.class);

    private final DaprClient dapr;

    public RemoteAgentServerDetailsRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    // No methods yet — interface is empty; add overrides when methods are added to the interface.
}
