package io.sclera.service;


import io.sclera.util.CommandLineUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CommandLineService {


    public void execCommand(String floorId, String[] cmd, HttpServletRequest httpServletRequest) {
        log.info("Payload: FloorId: {}, Endpoint: {}", floorId, httpServletRequest.getRequestURI());
        ConcurrentHashMap<String, Object> response = CommandLineUtils.execCmd(cmd);
        log.info("MAP RESPONSE: {}", response);
    }
}
