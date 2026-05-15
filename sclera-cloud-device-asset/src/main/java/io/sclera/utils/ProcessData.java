package io.sclera.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class ProcessData {
    String sessionId;
    Integer localPort;
    Integer remotePort;
    String remoteHost;
    boolean isDockerProcess;
    boolean isProcessAlive;

    public ProcessData() {
        this.isDockerProcess = false;
        this.isProcessAlive = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public boolean isDockerProcess() {
        return isDockerProcess;
    }

    public void setDockerProcess(boolean dockerProcess) {
        isDockerProcess = dockerProcess;
    }

    public boolean isProcessAlive() {
        return isProcessAlive;
    }

    public void setProcessAlive(boolean processAlive) {
        isProcessAlive = processAlive;
    }

    public boolean ifEmpty() {
        return !(this.localPort != null && this.remotePort != null && this.remoteHost != null);
    }

    @Override
    public String toString() {
        return "ProcessData{" +
                "localPort=" + localPort +
                ", remotePort=" + remotePort +
                ", remoteHost='" + remoteHost + '\'' +
                ", isDockerProcess=" + isDockerProcess +
                ", isProcessAlive=" + isProcessAlive +
                '}';
    }
}
