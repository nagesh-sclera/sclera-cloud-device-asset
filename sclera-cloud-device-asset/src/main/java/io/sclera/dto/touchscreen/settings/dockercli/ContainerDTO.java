package io.sclera.dto.touchscreen.settings.dockercli;

import io.sclera.utils.Utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerDTO {

  private String containerName;
  private List<String> env;
  private Boolean privileged;
  private String image;
  private List<String> dns;
  private String networkMode;
  private String restartPolicy;
  private String logConfigMode;
  private String logConfigMaxSize;
  private String ipV4Address;
  private Boolean openStdin;

  private List <ConcurrentHashMap<String, String>> extraHost;

  public List<ConcurrentHashMap<String, String>> getExtraHost() {
    return extraHost;
  }

  public void setExtraHost(List<ConcurrentHashMap<String, String>> extraHost) {
    this.extraHost = extraHost;
  }

  public ContainerDTO() {

  }

  public ContainerDTO(String containerName, List<String> env, Boolean privileged, String image, List<String> dns, String networkMode,
                      String restartPolicy, String logConfigMode, String logConfigMaxSize, String ipV4Address, Boolean openStdin, List <ConcurrentHashMap<String, String>> extraHost) {
    this.containerName = containerName;
    this.env = env;
    this.privileged = true;
    this.image = image;
    this.dns = dns;
    this.networkMode = networkMode;
    this.restartPolicy = restartPolicy;
    this.logConfigMode = logConfigMode;
    this.logConfigMaxSize = logConfigMaxSize;
    this.ipV4Address = ipV4Address;
    this.openStdin = openStdin;
    this.extraHost = extraHost;
  }

  public List<String> getEnv() {
    return env;
  }

  public void setEnv(List<String> env) {
    this.env = env;
  }

  public Boolean getPrivileged() {
    return privileged;
  }

  public void setPrivileged(Boolean privileged) {
    this.privileged = privileged;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public List<String> getDns() {
    return dns;
  }

  public void setDns(List<String> dns) {
    this.dns = dns;
  }

  public String getNetworkMode() {
    return networkMode;
  }

  public void setNetworkMode(String networkMode) {
    this.networkMode = networkMode;
  }

  public String getRestartPolicy() {
    return restartPolicy;
  }

  public void setRestartPolicy(String restartPolicy) {
    this.restartPolicy = restartPolicy;
  }

  public String getLogConfigMode() {
    return logConfigMode;
  }

  public void setLogConfigMode(String logConfigMode) {
    this.logConfigMode = logConfigMode;
  }

  public String getLogConfigMaxSize() {
    return logConfigMaxSize;
  }

  public void setLogConfigMaxSize(String logConfigMaxSize) {
    this.logConfigMaxSize = logConfigMaxSize;
  }

  public String getIpV4Address() {
    return ipV4Address;
  }

  public void setIpV4Address(String ipV4Address) {
    this.ipV4Address = ipV4Address;
  }

  public Boolean getOpenStdin() {
    return openStdin;
  }

  public void setOpenStdin(Boolean openStdin) {
    this.openStdin = openStdin;
  }

  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  public String toString() {
    Utils utils = new Utils();
    return "{" +
            "\"Env\":" + env + "," +
            "\"Image\":\"" + image + "\"," +
            "\"HostConfig\": {" +
            "\"ExtraHosts\": [\"scleravdmsnetworkgateway:" + utils.getScleraBridgeIp() + "\"]," +
            "\"PortBindings\": {}," +
            "\"DnsOptions\": []," +
            "\"DnsSearch\": []," +
            "\"Devices\": []," +
            "\"BlkioWeightDevice\": []," +
            "\"Privileged\":" + privileged + "," +
            "\"Dns\":" + dns + "," +
            "\"NetworkMode\":\"" + networkMode + "\"," +
            "\"RestartPolicy\": {" +
            "\"Name\":\"" + restartPolicy + "\"" +
            "}," +
            "\"LogConfig\": {" +
            "\"Type\": \"json-file\"," +
            "\"Config\": {" +
            "\"mode\":\"" + logConfigMode + "\"," +
            "\"max-size\":\"" + logConfigMaxSize + "\"" +
            "}" +
            "}" +
            "}," +
            "\"OpenStdin\":" + openStdin + "," +
            "\"NetworkingConfig\": " +
            (networkMode.equalsIgnoreCase("host") ?
                    null :
                    "{" +
                            "\"EndpointsConfig\": {" +
                            "\"" + networkMode + "\": {" +
                            "\"IPAMConfig\": {" +
                            "\"IPv4Address\":\"" + ipV4Address + "\"" +
                            "}," +
                            "\"IPAddress\":\"" + ipV4Address + "\"" +
                            "}" +
                            "}")
            + "}" +
            "}";
  }
}
