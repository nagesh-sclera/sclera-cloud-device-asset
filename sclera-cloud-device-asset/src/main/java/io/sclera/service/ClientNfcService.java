package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class ClientNfcService {
    public JSONArray getDeviceIdsTaggedToClientNfc(String id) { return new JSONArray(); }
    public Integer getClientNfcCountByDeviceId(String deviceId) { return 0; }

    public JSONArray getLocationIdsTaggedToClientNfc(String id) {
        StubLog.warn("ClientNfcService", "getLocationIdsTaggedToClientNfc", "AP-C1edge");
        return new JSONArray();
    }
    public void syncAllClientNfc(String vdmsId) {
        StubLog.warn("ClientNfcService", "syncAllClientNfc", "AP-C1edge");
    }
    public void syncClientNfc(String vdmsId) {
        StubLog.warn("ClientNfcService", "syncClientNfc", "AP-C1edge");
    }
}
