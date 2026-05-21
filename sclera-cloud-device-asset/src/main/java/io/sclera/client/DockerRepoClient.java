package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.Repository.DockerRepository;
import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.stubs.DockerRepositoryStub}
 * and {@code io.sclera.Repository.DockerRepositoryImpl}.
 */
@Component
@Primary
public class DockerRepoClient implements DockerRepository {

    private static final Logger log = LoggerFactory.getLogger(DockerRepoClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public DockerRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    @Override
    public List<DockerDTO> getAllNetworksByNetworkOrigin(Integer networkOrigin) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("networkOrigin", String.valueOf(networkOrigin));
            dapr.invokeMethod(APP_ID, "dockerrepo/getAllNetworksByNetworkOrigin", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("DockerRepoClient.getAllNetworksByNetworkOrigin failed; returning default", e);
        }
        return Collections.emptyList();
    }
}
