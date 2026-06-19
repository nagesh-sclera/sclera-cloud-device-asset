package io.sclera.workorder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maximo connection configuration row, one per VDMS.
 *
 * Cross-service refactor changes vs. the monolith:
 *   - {@code @ManyToOne Vdms vdms} → {@code @Column(name = "vdms_id") String vdmsId}
 *     (the relationship now lives across the Dapr boundary; the FK column stays)
 *   - {@code javax.persistence.*} → {@code jakarta.persistence.*} (Spring Boot 3)
 *   - Getters/setters added explicitly (the monolith relied on field-level access only;
 *     accessors enable Jackson reads in tests and Spring Data projection)
 *   - Read query logic moved out of this entity into {@code MaximoConfigurationRepository}
 *     as JPQL constructor projections (the {@code vdms_id} UNIQUE constraint makes the
 *     previous {@code LIMIT 1} single-result guard redundant). This class now carries
 *     only mapping metadata.
 *
 * NOT changed:
 *   - Column names, types, nullability
 *   - Field set (besides the FK swap)
 */
@Entity
@Table(name = "maximo_configuration")
public class MaximoConfiguration {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "server_url", nullable = false, length = 512)
    private String serverUrl;

    @Column(name = "auth_url", nullable = false, length = 512)
    private String authUrl;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret", length = 512)
    private String clientSecret;

    /** Foreign-key column to a VDMS row owned by vdms-service. */
    @Column(name = "vdms_id", nullable = false, length = 64)
    private String vdmsId;

    @Column(name = "sites", columnDefinition = "TEXT")
    private String sites;

    public MaximoConfiguration() {
        // for JPA
    }

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

    public String getVdmsId() { return vdmsId; }
    public void setVdmsId(String vdmsId) { this.vdmsId = vdmsId; }

    public String getSites() { return sites; }
    public void setSites(String sites) { this.sites = sites; }
}
