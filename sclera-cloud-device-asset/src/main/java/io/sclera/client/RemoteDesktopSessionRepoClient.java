package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.Repository.RemoteDesktopSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.stubs.RemoteDesktopSessionRepositoryStub}
 * and {@code io.sclera.Repository.RemoteDesktopSessionRepositoryImpl}.
 */
@Component
@Primary
public class RemoteDesktopSessionRepoClient implements RemoteDesktopSessionRepository {

    private static final Logger log = LoggerFactory.getLogger(RemoteDesktopSessionRepoClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public RemoteDesktopSessionRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    @Override
    public void deleteByDeviceId(String deviceId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("deviceId", deviceId);
            dapr.invokeMethod(APP_ID, "remotedesktopsessionrepo/deleteByDeviceId", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RemoteDesktopSessionRepoClient.deleteByDeviceId failed; swallowing", e);
        }
    }
}
