package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.UserActionLogDTO;
import io.sclera.dto.UserActivityDTO;
import io.sclera.repository.UserActionLogRepository;
import io.sclera.repository.UserActivityRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@Slf4j
public class UserActivityService {

    @Autowired
    private UserActionLogRepository userActionLogRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;


    public void addUserActivityLogs(String email, String type, String subType, String action, String status, String message, String primaryId,
                                    String vdmsId) {
        String id = Generators.timeBasedGenerator().generate().toString();
        Long created_timestamp = System.currentTimeMillis();
        userActivityRepository.addUserActivityLogs(id, email, type, subType, action, status, message, primaryId, vdmsId, created_timestamp);
    }

    public ResponseEntity<?> getQrCodeAndNfcLogs(String userName, int pageNo, int pageSize, String searchkey, String loggedInUser, JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        log.info("Payload:LoggedInUser:{},PageNo:{},PageSize:{},SearchKey:{}", loggedInUser, pageNo, pageSize, searchkey);
        int offset = pageSize * (pageNo - 1);
        String key = searchkey.replaceAll("[ -.! _+#~`@$%^&*()=;:<>?,/{}|\\\\\\\\ ]", "");

        List<UserActivityDTO> userActivityLog = userActivityRepository.getUserActivityLog(jsonObject.getString("email"),
                jsonObject.getString("type"),
                jsonObject.getString("action"),
                jsonObject.getString("status"),
                jsonObject.getString("vdmsid"),
                jsonObject.getString("sub_type"),
                jsonObject.getBigInteger("start_date"),
                jsonObject.getBigInteger("end_date"),
                key, pageSize, offset
        );
        ResponseDTO responseDTO = ScleraUtils.generatePayload(userActivityLog, 200, true);
        log.info("Fetching User Action Logs. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public List<UserActivityDTO> getAllQrCodeAndNfcData(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        log.info("PayLoad:" + jsonObject);
        return userActivityRepository.getAllQrCodeAndNfcData(
                jsonObject.getJSONArray("type"),
                jsonObject.getBigInteger("start_date"),
                jsonObject.getBigInteger("end_date"),
                jsonObject.getString("vdmsid"));
    }

    public ResponseEntity<?> getCount(JSONObject jsonObject, String searchkey, HttpServletRequest
            httpServletRequest) {
        log.info("PayLoad:Email:{},Type:{},startDate:{},endDate:{},status:{},action:{},vdmsId:{},SearchKey:{}", jsonObject.getString("email"),
                jsonObject.getString("type"), jsonObject.getBigInteger("start_date"), jsonObject.getBigInteger("end_date"), jsonObject.getString("status")
                , jsonObject.getString("action"), jsonObject.getString("vdmsid"), searchkey);

        String key = searchkey.replaceAll("[ -.! _+#~`@$%^&*()=;:<>?,/{}|\\\\\\\\ ]", "");

        if (jsonObject.getString("type").equals("nfc")) {
            UserActivityDTO userActivityDTO = userActivityRepository.getNfcCount(jsonObject.getString("email"),
                    jsonObject.getString("type"),
                    jsonObject.getString("action"),
                    jsonObject.getBigInteger("start_date"),
                    jsonObject.getBigInteger("end_date"),
                    jsonObject.getString("status"),
                    jsonObject.getString("vdmsid"),
                    key,
                    jsonObject.getString("sub_type"));
            ResponseDTO responseDTO = ScleraUtils.generatePayload(userActivityDTO, 200, true);
            log.info("Successfully Fetching NFC User Logs Count");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            UserActivityDTO UserActivityDTO = userActivityRepository.getQrCodeCount(jsonObject.getString("email"),
                    jsonObject.getString("type"),
                    jsonObject.getString("action"),
                    jsonObject.getBigInteger("start_date"),
                    jsonObject.getBigInteger("end_date"),
                    jsonObject.getString("status"),
                    jsonObject.getString("vdmsid"),
                    key,
                    jsonObject.getString("sub_type"));
            ResponseDTO responseDTO = ScleraUtils.generatePayload(UserActivityDTO, 200, true);
            log.info("Successfully Fetching Qr-Code User Logs Count");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }
}
