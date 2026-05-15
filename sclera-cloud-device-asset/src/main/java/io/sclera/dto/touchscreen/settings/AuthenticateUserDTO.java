package io.sclera.dto.touchscreen.settings;

public class AuthenticateUserDTO {
    private String email;
    private String password;
    private String vdms_id;
    private String vdmsPassword;

    public AuthenticateUserDTO() {
    }

    public AuthenticateUserDTO(String email, String password, String vdms_id) {
        this.email = email;
        this.password = password;
        this.vdms_id = vdms_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public String getVdmsPassword() {
        return vdmsPassword;
    }

    public void setVdmsPassword(String vdmsPassword) {
        this.vdmsPassword = vdmsPassword;
    }

    @Override
    public String toString() {
        return "AuthenticateUserDTO{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", vdms_id='" + vdms_id + '\'' +
                '}';
    }
}
