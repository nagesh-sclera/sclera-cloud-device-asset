package io.sclera.dto.touchscreen.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkConditionsResponseDTO {

  private String message;
  private Integer status;
  private Boolean success;

  public NetworkConditionsResponseDTO() {
  }

  public NetworkConditionsResponseDTO(String message, HttpStatus status, Boolean success) {
    this.message = message;
    this.status = status.value();
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getStatus() {
    return status;
  }

//  public HttpStatus getStatus(){
//    return HttpStatus.valueOf(this.status);
//  }

//  public void setStatus(HttpStatus status) {
//    this.status = status.value();
//  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }
}

