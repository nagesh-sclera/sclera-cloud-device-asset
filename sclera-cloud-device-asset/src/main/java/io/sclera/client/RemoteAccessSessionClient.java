package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.touchscreen.RemoteAccessSessionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Thin Dapr client delegating to sclera-edge (AP-C1edge).
 * Replaces {@code io.sclera.service.touchscreen.RemoteAccessSessionService}.
 */
@Component
public class RemoteAccessSessionClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteAccessSessionClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public RemoteAccessSessionClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public List<RemoteAccessSessionDTO> getAllRemoteAccessSessions() {
        try {
            dapr.invokeMethod(APP_ID, "remoteaccesssession/getAllRemoteAccessSessions", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RemoteAccessSessionClient.getAllRemoteAccessSessions failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public void stopRemoteAccess(String email, String vdmsId, String networkName,
                                 RemoteAccessSessionDTO dto, String ipAddress) {
        try {
            dapr.invokeMethod(APP_ID, "remoteaccesssession/stopRemoteAccess", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RemoteAccessSessionClient.stopRemoteAccess failed; swallowing", e);
        }
    }
}
