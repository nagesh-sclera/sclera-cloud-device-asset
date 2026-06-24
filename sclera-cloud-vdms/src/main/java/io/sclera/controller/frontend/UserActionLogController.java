package io.sclera.controller.frontend;

import com.alibaba.fastjson2.JSONObject;
import io.sclera.service.UserActionLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/vdms")
public class UserActionLogController {


    @Autowired
    private UserActionLogService userActionLogService;


    @PostMapping("/user/{username}/getAllUserActionLogs")
    public ResponseEntity<?> getAllUserActionLogs(@PathVariable String username,
                                                  @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pagesize,
                                                  @RequestParam(defaultValue = "all") String searchkey,
                                                  @RequestParam(value = "loggedInUser") String loggedInUser,
                                                  @RequestBody JSONObject jsonObject,
                                                  HttpServletRequest httpServletRequest) {
        return userActionLogService.getAllUserActionLogs(username, pageno, pagesize, searchkey, loggedInUser, jsonObject, httpServletRequest);
    }


    @GetMapping("/userActionLogs/getAllTypes")
    public ResponseEntity<?> getAllTypes(@RequestParam(value = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return userActionLogService.getAllTypes(loggedInUser, httpServletRequest);
    }

    @PostMapping("/user/{username}/getUserActionLogsCount")
    public ResponseEntity<?> getUserActionLogsCount(@PathVariable String username,
                                                    @RequestParam(defaultValue = "all") String searchkey,
                                                    @RequestParam(value = "loggedInUser") String loggedInUser,
                                                    @RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return userActionLogService.getUserActionLogsCount(username, searchkey, loggedInUser, jsonObject, httpServletRequest);
    }
}
