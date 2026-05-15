package io.sclera.utils;

import io.sclera.dto.touchscreen.RemoteAccessSessionDTO;
import io.sclera.service.touchscreen.RemoteAccessSessionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RemoteAccessProcessParser {

    @Autowired
    Utils utils;

    public StringBuilder data = new StringBuilder();
    private String email;
    List<String> dockerProcess = new ArrayList<>();
    List<String> hostProcess = new ArrayList<>();
    List<ProcessData> finalProcessList = new ArrayList<>();

    public void formatData() {
        String[] splitData = this.data.toString().split("\n");
        System.out.println(Arrays.toString(splitData));
        for (var sd : splitData) {
            if (sd.contains("isDockerProcess")) {
                this.dockerProcess.add(sd);
            } else {
                this.hostProcess.add(sd);
            }
        }
        this.parseProcess(this.dockerProcess);
        this.parseProcess(this.hostProcess);
    }

    public void fillData(String cmdOutput) {
        this.data.append(cmdOutput);
    }

    public void parseProcess(List<String> processList) {
        Map<Integer, ProcessData> processMap = new HashMap<>();
        for (String s : processList) {
            ProcessData processData = new ProcessData();
            Arrays.asList(s.split("--"))
                    .forEach(proc -> {
                        if (proc.contains("local-port")) {
                            processData.setLocalPort(Integer.parseInt(proc.split("=")[1].trim()));
                        } else if (proc.contains("remote-port")) {
                            processData.setRemotePort(Integer.parseInt(proc.split("=")[1].trim()));
                        } else if (proc.contains("remote-host")) {
                            processData.setRemoteHost(proc.split("=")[1].trim());
                        } else if (proc.contains("isDockerProcess")) {
                            processData.setDockerProcess(true);
                        }
                    });

            if (!processData.ifEmpty()) {
                if (processMap.containsKey(processData.getLocalPort())) {
                    var exObj = processMap.get(processData.getLocalPort());
                    if (processData.getLocalPort().equals(exObj.getLocalPort()) && exObj.getRemoteHost().contains(this.getScleraBridgeIp().substring(0,this.getScleraBridgeIp().lastIndexOf('.')))) {
                        processData.setProcessAlive(true);
                        processMap.put(processData.getLocalPort(), processData);
                    }
                } else {
                    processData.setProcessAlive(!processData.isDockerProcess());
                    processMap.put(processData.getLocalPort(), processData);
                }
            }
        }

        finalProcessList.addAll(processMap.values());
        System.out.println("FINAL PROCESS!!!!" + (finalProcessList));
    }

    public List<ProcessData> getFinalProcessList() {
        return this.finalProcessList
                .stream()
                .filter(ProcessData::isProcessAlive)
                .collect(Collectors.toList());
    }

    public void performRemoteAccessCleanup(String vdms_id, RemoteAccessSessionService remoteAccessSessionService) {
        StringBuilder cmd = new StringBuilder("ps -e -o command | less | grep tcptunnel | grep -v \"127.0.0.1\" | grep -v \"grep\" | grep -v \"docker\"");
        System.out.println(cmd.toString());
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"bash" ,"-c", cmd.toString()});
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String error = stdErr.lines().collect(Collectors.joining("\n"));
            String result = stdIn.lines().collect(Collectors.joining("\n"));
            System.out.println("ERROR FROM COMMAND: "+error);
            System.out.println("RESULT FROM COMMAND: "+result);
            this.fillData(result);
        } catch (Exception e) {
            System.out.println(e);
        }

        this.formatData();

        List<ProcessData> aliveProcesses = this.getFinalProcessList();
        List<RemoteAccessSessionDTO> remoteAccessSessionDTOList = remoteAccessSessionService.getAllRemoteAccessSessions();

        if (remoteAccessSessionDTOList != null) {
            for (RemoteAccessSessionDTO remoteAccessSessionDTO : remoteAccessSessionDTOList) {
                for (ProcessData aliveProcess : aliveProcesses) {
                    System.out.println(remoteAccessSessionDTO.getPublic_port()+" "+aliveProcess.getLocalPort());
                    if (remoteAccessSessionDTO.getPublic_port().equals(aliveProcess.getLocalPort())) {
                        remoteAccessSessionDTO.setIsAlive(true);
                    }
                }
            }

            remoteAccessSessionDTOList.forEach(remoteAccessSessionDTO -> {
                if (!remoteAccessSessionDTO.getIsAlive()) {
                    remoteAccessSessionService.stopRemoteAccess(remoteAccessSessionDTO.getEmail(), vdms_id, remoteAccessSessionDTO.getNetwork_name(), remoteAccessSessionDTO, remoteAccessSessionDTO.getIp_address());
                }
            });
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public ConcurrentHashMap<String, Object> execCmd(String[] cmd) {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();
        try {
            Process process = processBuilder.start();
            try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                concurrentHashMap.put("result", stdIn.lines().collect(Collectors.joining("\n")));
            }
            return concurrentHashMap;
        } catch (Exception e) {
            System.out.println(e);
            concurrentHashMap.put("success", false);
            return concurrentHashMap;
        }
    }

    public String getScleraBridgeIp() {
        System.out.println("getScleraBridgeIp start");
        String cmd = "getent hosts scleravdmsnetworkgateway | awk {'print $1'}";
        var result = this.execCmd(new String[]{"bash", "-c", cmd});
        System.out.println("getScleraBridgeIp exit");
        return result.get("result").toString();
    }
}
