package io.sclera.utils;

//import io.sclera.service.touchscreen.VdmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Component
public class RemoteAccessSessionUtils {
    private static final Logger log = LoggerFactory.getLogger(RemoteAccessSessionUtils.class);

    @Autowired
    private Utils settingsUtils;

//    @Autowired
//    private VdmsService vdmsService;

    public Boolean startHostRemoteAccess(Integer public_port, Integer private_port, String ip_address) {
        String cmd = "tcptunnel --local-port=" + public_port + " --remote-port=" + private_port + " --remote-host=" + ip_address + " --stay-alive --fork &";
        log.info("CMD: {}", cmd);
        return settingsUtils.execRemoteAccessCmd(cmd);
    }

    public Boolean startGuestRemoteAccess(Integer public_port, Integer private_port, String ip_address, String network_name, String docker_internal_ip) {
        log.info("docker_internal_ip: {}", docker_internal_ip);
        String cmd1 = "tcptunnel --local-port=" + public_port + " --remote-port=" + public_port + " --remote-host=" + docker_internal_ip + " --stay-alive --fork isDockerProcess";
        String cmd2 = "docker exec " + network_name + " tcptunnel --local-port=" + public_port + " --remote-port=" + private_port + " --remote-host=" + ip_address + " --stay-alive --fork isDockerProcess";
        log.info("CMD_1: {}", cmd1);
        log.info("CMD_2: {}", cmd2);
        return settingsUtils.execRemoteAccessCmd(cmd1) && settingsUtils.execRemoteAccessCmd(cmd2);
    }

    public boolean stopHostRemoteAccess(Integer public_port) {
        String cmd = "kill -9 $(lsof -t -i:" + public_port + ")";
        String[] cmdArray = {"bash","-c",cmd};
        return Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray).get("status"));
    }

    public boolean stopGuestRemoteAccess(String network_name, Integer public_port) {
        log.info("public port inside stop guest remote access ------------ {}", public_port);
        String cmd1 = "docker exec " + network_name + " /bin/bash -c 'kill -9 $(lsof -t -i:" + public_port + ")'";
        log.info("command 1 : ------------------ {}", cmd1);
        String cmd2 = "kill -9 $(lsof -t -i:" + public_port + ")";
        log.info("command 2 : ------------------ {}", cmd2);
        String[] cmdArray1 = {"bash","-c",cmd1};
        log.info("command Array 1 : ------------------ {}", Arrays.toString(cmdArray1));
        String[] cmdArray2 = {"bash","-c",cmd2};
        log.info("command Array 2 : ------------------ {}", Arrays.toString(cmdArray2));
        Boolean data =  Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray1).get("status")) && Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray2).get("status"));
        log.info("boolean response : ::====== {}", data);
        return data;
    }

    public boolean stopGuestRemoteAccessMasterToSlave(Integer public_port) {
        String cmd = "kill -9 $(lsof -t -i:" + public_port + ")";
        String[] cmdArray = {"bash","-c",cmd};
        return Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray).get("status"));
    }

    public boolean bindMacWithIp(String network_name, String ip_address, String mac_address) {
        String cmd = "docker exec " + network_name + " arp -s " + ip_address + " " + mac_address + "";
        String[] cmdArray = {"bash","-c",cmd};
        return Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray).get("status"));
    }

    public boolean unbindMacWithIp(String network_name, String ip_address) {
        String cmd = "docker exec " + network_name + " arp --del " + ip_address + "";
        String[] cmdArray = {"bash","-c",cmd};
        return Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray).get("output"));
    }

    public boolean flushAllIpMacBinding(String network_name) {
        String cmd = "docker exec " + network_name + " ip -s -s neigh flush all";
        String[] cmdArray = {"bash","-c",cmd};
        return Boolean.parseBoolean(settingsUtils.execPipedCmd(cmdArray).get("output"));
    }

    public Boolean runTcpTunnel(Integer public_port, Integer private_port, String ip_address) {
        String command = "tcptunnel --local-port=" + public_port + " --remote-port=" + private_port + " --remote-host=" + ip_address + " --fork --stay-alive";
        log.info("TCP command is : {}", command);
        return settingsUtils.execRemoteAccessCmd(command);
    }
}
