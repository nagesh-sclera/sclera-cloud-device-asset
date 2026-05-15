package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigInteger;

/** STUB: replace with remote call to AP-C5 */
@Service
public class AlertService {
    public void sendDeviceConditionsAlertInfo(DeviceAlertDTO deviceAlert, AlertProfileDTO alertProfile, BigInteger timestamp) {}
    public void sendSensorAlertInfo(Object sensorAlert, AlertProfileDTO alertProfile, BigInteger timestamp) {}
    public void sendDownloadEmail(JSONObject body, MultipartFile file, String type, String vdmsId) {}
}
