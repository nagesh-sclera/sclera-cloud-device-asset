package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.QrCodeDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C1edge */
@Service
public class QrCodeService {
    public JSONArray getDeviceIdsTaggedToQrCode(String qrCodeId) { return new JSONArray(); }
    public Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String> deviceIds) { return Collections.emptySet(); }
    public Integer getQrCodeCountByDeviceId(String deviceId) { return 0; }
    public BigInteger getMaxUpdatedQrCodeTimeStamp(String deviceId) { return null; }

    public JSONArray getLocationIdsTaggedToQrCode(String qrCodeId) {
        StubLog.warn("QrCodeService", "getLocationIdsTaggedToQrCode", "AP-C1edge");
        return new JSONArray();
    }
    public Set<QrCodeDTO> getQrCodesByLocationIds(Set<String> locationIds) {
        StubLog.warn("QrCodeService", "getQrCodesByLocationIds", "AP-C1edge");
        return Collections.emptySet();
    }
    public Set<QrCodeDTO> syncQrCodes(String vdmsId) {
        StubLog.warn("QrCodeService", "syncQrCodes", "AP-C1edge"); return Collections.emptySet();
    }
    public Set<QrCodeDTO> getQrCodeDetailsByIds(Set<String> ids) {
        StubLog.warn("QrCodeService", "getQrCodeDetailsByIds", "AP-C1edge"); return Collections.emptySet();
    }
    public void upsertQrCodesInBatch(Set<QrCodeDTO> dtos) {
        StubLog.warn("QrCodeService", "upsertQrCodesInBatch", "AP-C1edge");
    }
    public Set<QrCodeDTO> getClientQrCodeDetailsByIds(Set<String> ids) {
        StubLog.warn("QrCodeService", "getClientQrCodeDetailsByIds", "AP-C1edge"); return Collections.emptySet();
    }
    public void syncAlQrCodes(String vdmsId) {
        StubLog.warn("QrCodeService", "syncAlQrCodes", "AP-C1edge");
    }
}
