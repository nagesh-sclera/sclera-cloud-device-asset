package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.UserActionLogDTO;
import io.sclera.repository.UserActionLogRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
public class UserActionLogService {

    @Autowired
    private UserActionLogRepository userActionLogRepository;

    @Autowired
    private WebClientService webClientService;

    public void addUserActionLog(String email, String type, String action, String message, String status) {
        String id = Generators.timeBasedGenerator().generate().toString();
        Long created_timestamp = System.currentTimeMillis();
        userActionLogRepository.addUserActionLog(id, email, type, action, created_timestamp, message, status);
    }

    public ResponseEntity<?> getAllUserActionLogs(String username, int pageno, int pagesize, String
            searchkey, String loggedInUser, JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        log.info("Payload:LoggedInUser:{},PageNo:{},PageSize:{},SearchKey:{}", loggedInUser, pageno, pagesize, searchkey);
        int offset = pagesize * (pageno - 1);
        String key = searchkey.replaceAll("[ -.! _+#~`@$%^&*()=;:<>?,/{}|\\\\\\\\ ]", "");

        String ip = httpServletRequest.getHeader("x-real-ip");
        if (ip == null) {
            ip = "50.194.123.216";
        }
        JSONObject timeZone = webClientService.sendIpApi(ip);
        ZoneId istZone = ZoneId.of(timeZone.getString("timezone"));

        Instant startInstant = Instant.ofEpochMilli(jsonObject.getLongValue("start_date"));
        Instant endInstant = Instant.ofEpochMilli(jsonObject.getLongValue("end_date"));

        ZonedDateTime zonedStartTime = startInstant.atZone(istZone);
        ZonedDateTime zonedEndTime = endInstant.atZone(istZone);

        long start = zonedStartTime.toInstant().toEpochMilli();
        long end = zonedEndTime.toInstant().toEpochMilli();

        List<UserActionLogDTO> userActionLogDTO = userActionLogRepository.getAllUserActionLog(
                jsonObject.getString("email"),
                jsonObject.getString("status"),
                jsonObject.getString("action"),
                jsonObject.getString("type"),
                start,
                end,
                key, offset, pagesize);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(userActionLogDTO, 200, true);
        log.info("Fetching User Action Logs. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void deleteUserActionLogsByEmail(String child) {
        userActionLogRepository.deleteUserLogsByEmail(child);
    }

    public ResponseEntity<?> getAllTypes(String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: LoggedInUser: {}", loggedInUser);
        List<String> types = userActionLogRepository.getAllTypes();
        log.info("Types:" + types);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(types, 200, true);
        log.info("Successfully Fetching ALL Types From User Action Logs.Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getUserActionLogsCount(String username, String searchkey, String loggedInUser, JSONObject jsonObject,
                                                    HttpServletRequest httpServletRequest) {
        log.info("Payload:LoggedInUser:{},SearchKey:{}", loggedInUser, searchkey);
        String key = searchkey.replaceAll("[ -.! _+#~`@$%^&*()=;:<>?,/{}|\\\\\\\\ ]", "");

        String ip = httpServletRequest.getHeader("x-real-ip");
        if (ip == null) {
            ip = "50.194.123.216";
        }
        JSONObject timeZone = webClientService.sendIpApi(ip);
        ZoneId istZone = ZoneId.of(timeZone.getString("timezone"));

        Instant startInstant = Instant.ofEpochMilli(jsonObject.getLongValue("start_date"));
        Instant endInstant = Instant.ofEpochMilli(jsonObject.getLongValue("end_date"));

        ZonedDateTime zonedStartTime = startInstant.atZone(istZone);
        ZonedDateTime zonedEndTime = endInstant.atZone(istZone);

        long start = zonedStartTime.toInstant().toEpochMilli();
        long end = zonedEndTime.toInstant().toEpochMilli();

        log.info("Payload:1:{},2:{},3:{},4:{},5:{},6:{},7:{}", jsonObject.getString("email"),
                jsonObject.getString("status"),
                jsonObject.getString("action"),
                jsonObject.getString("type"),
                start,
                end,
                key);
        Integer count = userActionLogRepository.getUserActionLogsCount(
                jsonObject.getString("email"),
                jsonObject.getString("status"),
                jsonObject.getString("action"),
                jsonObject.getString("type"),
                start,
                end,
                key);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("count", count);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject1, 200, true);
        log.info("Fetching User Action Logs count. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
    public Integer getCount(long sixMonthsAgo) {
        return userActionLogRepository.getCount(sixMonthsAgo);
    }

    public Integer getAllCount() {
        return userActionLogRepository.getAllCount();
    }

    public void deleteByLimit(long sixMonthsAgo, int i) {
        userActionLogRepository.deleteByLimit(sixMonthsAgo, i);
    }
}