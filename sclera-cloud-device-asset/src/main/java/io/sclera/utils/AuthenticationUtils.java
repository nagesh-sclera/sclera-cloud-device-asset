package io.sclera.utils;


import org.springframework.stereotype.Component;

@Component
public class AuthenticationUtils {

    private String vdms_id;
    private String access_token;
    private String refresh_token;
    private String devuid;
    private String public_key;

    public String getVdms_id() {
        return vdms_id;
    }

    public void setVdms_id(String vdms_id) {
        this.vdms_id = vdms_id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getDevuid() {
        return devuid;
    }

    public void setDevuid(String devuid) {
        this.devuid = devuid;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }
}
