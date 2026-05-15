package io.sclera.dto.touchscreen.settings.dockercli;

public class NetworkDTO {

  private String name;
  private String driver;
  private String subnet;
  private String gateway;
  private String parent;

  public NetworkDTO() {

  }

  public NetworkDTO(String name, String driver, String subnet, String gateway, String parent) {
    this.name = name;
    this.driver = driver;
    this.subnet = subnet;
    this.gateway = gateway;
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getSubnet() {
    return subnet;
  }

  public void setSubnet(String subnet) {
    this.subnet = subnet;
  }

  public String getGateway() {
    return gateway;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return "{" +
        "\"Name\":\"" + name + "\"," +
        "\"Driver\":\"" + driver + "\"," +
        "\"IPAM\": {" +
        "\"config\":[" +
        "{" +
        "\"Subnet\":\"" + subnet + "\"," +
        "\"Gateway\":\"" + gateway + "\"" +
        "}" +
        "]" +
        "}," +
        "\"Options\": {" +
        "\"parent\":\"" + parent + "\"" +
        "}" +
        "}";
  }
}
