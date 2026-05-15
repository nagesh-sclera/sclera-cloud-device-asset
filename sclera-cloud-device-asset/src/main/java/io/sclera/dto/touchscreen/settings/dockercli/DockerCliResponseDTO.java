package io.sclera.dto.touchscreen.settings.dockercli;

import java.util.List;

public class DockerCliResponseDTO {

  private String id;
  private List<String> warnings;
  private String message;

  public DockerCliResponseDTO() {

  }

  public DockerCliResponseDTO(String id, List<String> warnings, String message) {
    this.id = id;
    this.warnings = warnings;
    this.message = message;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "Response{" +
        "id='" + id + '\'' +
        ", warnings=" + warnings +
        ", message='" + message + '\'' +
        '}';
  }
}
