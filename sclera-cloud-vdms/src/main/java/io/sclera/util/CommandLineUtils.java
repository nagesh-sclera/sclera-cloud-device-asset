package io.sclera.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class CommandLineUtils {


    public static ConcurrentHashMap<String, Object> execCmd(String[] cmd) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();
        try {
            Process process = processBuilder.start();
            try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                concurrentHashMap.put("result", stdIn.lines().collect(Collectors.joining("\n")));
                log.info(concurrentHashMap.get("result") + "result of command");
            }
            try (BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                concurrentHashMap.put("error", stdErr.lines().collect(Collectors.joining("\n")));
                log.info(concurrentHashMap.get("error") + "error of command");
            }

            if (concurrentHashMap.get("error") != null && concurrentHashMap.get("error").toString().length() > 0) {
                concurrentHashMap.put("success", false);
            } else {
                concurrentHashMap.put("success", true);
            }
            return concurrentHashMap;

        } catch (Exception e) {
            log.error("Exception:" + e.getMessage());
            concurrentHashMap.put("success", false);
            return concurrentHashMap;
        }
    }
}
