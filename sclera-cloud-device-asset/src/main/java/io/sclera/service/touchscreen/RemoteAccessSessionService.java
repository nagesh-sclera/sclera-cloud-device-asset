package io.sclera.service.touchscreen;
import io.sclera.dto.touchscreen.RemoteAccessSessionDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
/** STUB: edge-only remote access */
@Service
public class RemoteAccessSessionService {
    public List<RemoteAccessSessionDTO> getAllRemoteAccessSessions() { StubLog.warn("RemoteAccessSessionService","getAllRemoteAccessSessions","edge-D"); return Collections.emptyList(); }
    public void stopRemoteAccess(String email, String vdmsId, String networkName, RemoteAccessSessionDTO dto, String ipAddress) { StubLog.warn("RemoteAccessSessionService","stopRemoteAccess","edge-D"); }
}
