package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.NfcDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class NfcService {
    public JSONArray getDeviceIdsTaggedToNfc(String nfcId) { return new JSONArray(); }
    public Set<NfcDTO> getNfcsByDeviceIds(Set<String> deviceIds) { return Collections.emptySet(); }
    public Integer getQrNfcCountByDeviceId(String deviceId) { return 0; }

    public JSONArray getLocationIdsTaggedToNfc(String nfcId) {
        StubLog.warn("NfcService", "getLocationIdsTaggedToNfc", "AP-C1edge");
        return new JSONArray();
    }
    public Set<NfcDTO> getNfcsByLocationIds(Set<String> locationIds) {
        StubLog.warn("NfcService", "getNfcsByLocationIds", "AP-C1edge");
        return Collections.emptySet();
    }
    public void syncAllNfc(String vdmsId) {
        StubLog.warn("NfcService", "syncAllNfc", "AP-C1edge");
    }
    public void syncNfc(String vdmsId) {
        StubLog.warn("NfcService", "syncNfc", "AP-C1edge");
    }
}
