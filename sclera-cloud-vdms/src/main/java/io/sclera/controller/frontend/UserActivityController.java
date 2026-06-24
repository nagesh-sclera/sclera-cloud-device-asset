package io.sclera.controller.frontend;


import com.alibaba.fastjson2.JSONObject;
import io.sclera.service.UserActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/vdms")
public class UserActivityController {

    @Autowired
    private UserActivityService userActivityService;

    @PostMapping("/user/{username}/getUserActionLogs")
    public ResponseEntity<?> getQrCodeAndNfcLogs(@PathVariable String username,
                                                 @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                 @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pagesize,
                                                 @RequestParam(defaultValue = "all") String searchkey,
                                                 @RequestParam(value = "loggedInUser") String loggedInUser,
                                                 @RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return userActivityService.getQrCodeAndNfcLogs(username, pageno, pagesize, searchkey, loggedInUser, jsonObject, httpServletRequest);
    }

    @PostMapping("/getQrCodeCount")
    public ResponseEntity<?> getQrCodeCount(@RequestParam(value = "loggedInUser") String loggedInUser,
                                            @RequestParam(defaultValue = "all") String searchkey,
                                            @RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return userActivityService.getCount(jsonObject, searchkey, httpServletRequest);
    }

    @PostMapping("/getNfcCount")
    public ResponseEntity<?> getNfcCount(@RequestParam(value = "loggedInUser") String loggedInUser,
                                         @RequestParam(defaultValue = "all") String searchkey,
                                         @RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return userActivityService.getCount(jsonObject, searchkey, httpServletRequest);
    }
}
