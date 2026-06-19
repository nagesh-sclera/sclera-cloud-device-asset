package io.sclera.utils;

import io.sclera.dto.touchscreen.RemoteAccessSessionDTO;
import io.sclera.client.RemoteAccessSessionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Parses the output of running tcptunnel processes to identify live remote-access
 * port forwards and reconcile them against persisted remote-access sessions.
 */
public class RemoteAccessProcessParser {

    private static final Logger log = LoggerFactory.getLogger(RemoteAccessProcessParser.class);

    @Autowired
    Utils utils;

    public StringBuilder data = new StringBuilder();
    private String email;
    List<String> dockerProcess = new ArrayList<>();
    List<String> hostProcess = new ArrayList<>();
    List<ProcessData> finalProcessList = new ArrayList<>();

    /**
     * Splits the accumulated command output into docker and host process lines and
     * parses each group.
     */
    public void formatData() {
        String[] splitData = this.data.toString().split("\n");
        log.debug("{}", Arrays.toString(splitData));
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

    /**
     * Appends raw command output to the internal data buffer.
     */
    public void fillData(String cmdOutput) {
        this.data.append(cmdOutput);
    }

    /**
     * Parses each process line into a ProcessData entry keyed by local port,
     * marking entries alive and adding them to the final process list.
     */
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
        log.debug("{}", "FINAL PROCESS!!!!" + (finalProcessList));
    }

    /**
     * Returns the parsed processes that are marked alive.
     */
    public List<ProcessData> getFinalProcessList() {
        return this.finalProcessList
                .stream()
                .filter(ProcessData::isProcessAlive)
                .collect(Collectors.toList());
    }

    /**
     * Discovers live tcptunnel processes and stops any persisted remote-access
     * sessions whose public port has no matching live process.
     */
    public void performRemoteAccessCleanup(String vdms_id, RemoteAccessSessionClient remoteAccessSessionService) {
        StringBuilder cmd = new StringBuilder("ps -e -o command | less | grep tcptunnel | grep -v \"127.0.0.1\" | grep -v \"grep\" | grep -v \"docker\"");
        log.debug("{}", cmd.toString());
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"bash" ,"-c", cmd.toString()});
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String error = stdErr.lines().collect(Collectors.joining("\n"));
            String result = stdIn.lines().collect(Collectors.joining("\n"));
            log.debug("{}", "ERROR FROM COMMAND: "+error);
            log.debug("{}", "RESULT FROM COMMAND: "+result);
            this.fillData(result);
        } catch (Exception e) {
            log.debug("{}", e);
        }

        this.formatData();

        List<ProcessData> aliveProcesses = this.getFinalProcessList();
        List<RemoteAccessSessionDTO> remoteAccessSessionDTOList = remoteAccessSessionService.getAllRemoteAccessSessions();

        if (remoteAccessSessionDTOList != null) {
            for (RemoteAccessSessionDTO remoteAccessSessionDTO : remoteAccessSessionDTOList) {
                for (ProcessData aliveProcess : aliveProcesses) {
                    log.debug("{}", remoteAccessSessionDTO.getPublic_port()+" "+aliveProcess.getLocalPort());
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


    /**
     * Runs the given command and returns a map containing its stdout under "result",
     * or "success" false on failure.
     */
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
            log.debug("{}", e);
            concurrentHashMap.put("success", false);
            return concurrentHashMap;
        }
    }

    /**
     * Resolves and returns the IP address of the Sclera VDMS network gateway bridge.
     */
    public String getScleraBridgeIp() {
        log.debug("{}", "getScleraBridgeIp start");
        String cmd = "getent hosts scleravdmsnetworkgateway | awk {'print $1'}";
        var result = this.execCmd(new String[]{"bash", "-c", cmd});
        log.debug("{}", "getScleraBridgeIp exit");
        return result.get("result").toString();
    }
}
