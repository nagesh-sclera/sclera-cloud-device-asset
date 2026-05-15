package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.RemoteAgentServerDetailsDTO;
import io.sclera.utils.StubLog;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class RemoteDesktopSessionService {

    public ResponseEntity<?> updateRemoteConnectFlag(JSONObject json) {
        StubLog.warn("RemoteDesktopSessionService", "updateRemoteConnectFlag", "edge-D");
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<?> getRemoteConnectInfo(String deviceId, String username) {
        StubLog.warn("RemoteDesktopSessionService", "getRemoteConnectInfo", "edge-D");
        return ResponseEntity.ok(null);
    }

    public RemoteAgentServerDetailsDTO getRemoteSessionDetails(String id) {
        StubLog.warn("RemoteDesktopSessionService", "getRemoteSessionDetails", "edge-D");
        return null;
    }

    public void updateAcknowledge(JSONObject json) {
        StubLog.warn("RemoteDesktopSessionService", "updateAcknowledge", "edge-D");
    }
}
