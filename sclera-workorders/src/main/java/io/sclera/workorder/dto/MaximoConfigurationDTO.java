package io.sclera.workorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Configuration payload for the Maximo connection per VDMS.
 *
 * <p>Used as both the read projection (5-arg ctor, no secrets) and the write
 * request body (7-arg ctor, with secrets). Bean Validation constraints apply
 * only to inbound write requests annotated with {@code @Valid}; they do not
 * affect the read projection's serialization.
 *
 * <p>{@code clientId} and {@code clientSecret} are intentionally <b>not</b> required so
 * credential-preserving updates remain valid: when either is null the server keeps the
 * value already stored (the {@code COALESCE}/keep-on-null rule in
 * {@code MaximoConfigurationRepository.upsertMaximoConfiguration}). The frontend still
 * enforces Client ID on create. Making {@code clientId} {@code @NotBlank} here would
 * reject the edit flow, which sends a null clientId to retain the stored one.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaximoConfigurationDTO {
    private String id;

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    @NotBlank(message = "serverUrl is required")
    @Size(max = 1000, message = "serverUrl must be at most 1000 characters")
    @Pattern(regexp = "^https?://.+", message = "serverUrl must be a valid http(s) URL")
    private String serverUrl;

    @NotBlank(message = "authUrl is required")
    @Size(max = 1000, message = "authUrl must be at most 1000 characters")
    @Pattern(regexp = "^https?://.+", message = "authUrl must be a valid http(s) URL")
    private String authUrl;

    @Size(max = 255, message = "clientId must be at most 255 characters")
    private String clientId;

    @Size(max = 512, message = "clientSecret must be at most 512 characters")
    private String clientSecret;

    @Size(max = 20000, message = "sites payload is too large")
    private String sites;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getSites() { return sites; }
    public void setSites(String sites) { this.sites = sites; }

    public MaximoConfigurationDTO() {
    }

    public MaximoConfigurationDTO(String id, String name, String serverUrl, String authUrl, String sites) {
        this.id = id;
        this.name = name;
        this.serverUrl = serverUrl;
        this.authUrl = authUrl;
        this.sites = sites;
    }

    public MaximoConfigurationDTO(String id, String name, String serverUrl, String authUrl,
                                  String clientId, String clientSecret, String sites) {
        this.id = id;
        this.name = name;
        this.serverUrl = serverUrl;
        this.authUrl = authUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sites = sites;
    }

    @Override
    public String toString() {
        return "MaximoConfigurationDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", authUrl='" + authUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + (clientSecret == null ? "null" : "***") + '\'' +
                ", sites='" + sites + '\'' +
                '}';
    }
}
