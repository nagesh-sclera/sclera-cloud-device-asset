package io.sclera.dto.touchscreen.settings.dockercli;

public class ConnectorDTO {

  private String container;
  private String ipV4Address;

  public ConnectorDTO() {

  }

  public ConnectorDTO(String container, String ipV4Address) {
    this.container = container;
    this.ipV4Address = ipV4Address;
  }

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  public String getIpV4Address() {
    return ipV4Address;
  }

  public void setIpV4Address(String ipV4Address) {
    this.ipV4Address = ipV4Address;
  }

  @Override
  public String toString() {
    return "{" +
        "\"Container\":\"" + container + "\"," +
        "\"EndpointConfig\":{" +
        "\"IPAMConfig\": {" +
        "\"IPv4Address\":\"" + ipV4Address + "\"" +
        "}" +
        "}" +
        "}";
  }
}
