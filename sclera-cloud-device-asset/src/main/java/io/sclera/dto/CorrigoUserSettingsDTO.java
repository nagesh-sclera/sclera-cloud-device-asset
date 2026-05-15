package io.sclera.dto;

public class CorrigoUserSettingsDTO {

    private String id;
    private String client_id;
    private String client_secret;
    private String credential_type;
    private String oauth_token;


    private String corrigo_user_id;


    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }


    public String getCredential_type() {
        return credential_type;
    }

    public void setCredential_type(String credential_type) {
        this.credential_type = credential_type;
    }

    public String getOauth_token() {
        return oauth_token;
    }

    public void setOauth_token(String oauth_token) {
        this.oauth_token = oauth_token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorrigo_user_id() {
        return corrigo_user_id;
    }

    public void setCorrigo_user_id(String corrigo_user_id) {
        this.corrigo_user_id = corrigo_user_id;
    }

//    public CorrigoUserSettingsDTO(String id, String client_id, String client_secret, String credential_type, String oauth_token) {
//        this.id = id;
//        this.client_id = client_id;
//        this.client_secret = client_secret;
//        this.credential_type = credential_type;
//        this.oauth_token = oauth_token;
//    }

    public CorrigoUserSettingsDTO(String id, String client_id, String client_secret, String credential_type, String oauth_token, String corrigo_user_id) {
        this.id = id;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.credential_type = credential_type;
        this.oauth_token = oauth_token;
        this.corrigo_user_id = corrigo_user_id;
    }


    public CorrigoUserSettingsDTO() {
        // Empty constructor
    }

//    @Override
//    public String toString() {
//        return "CorrigoUserSettingsDTO{" +
//                "id='" + id + '\'' +
//                ", client_id='" + client_id + '\'' +
//                ", client_secret='" + client_secret + '\'' +
//                ", credential_type='" + credential_type + '\'' +
//                ", oauth_token='" + oauth_token + '\'' +
//                '}';
//    }

    @Override
    public String toString() {
        return "CorrigoUserSettingsDTO{" +
                "id='" + id + '\'' +
                ", client_id='" + client_id + '\'' +
                ", client_secret='" + client_secret + '\'' +
                ", credential_type='" + credential_type + '\'' +
                ", oauth_token='" + oauth_token + '\'' +
                ", corrigo_user_id='" + corrigo_user_id + '\'' +
                '}';
    }
}
