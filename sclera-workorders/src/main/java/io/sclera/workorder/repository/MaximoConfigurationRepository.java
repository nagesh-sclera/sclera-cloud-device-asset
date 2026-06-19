package io.sclera.workorder.repository;

import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.entity.MaximoConfiguration;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@code maximo_configuration}.
 *
 * The two read methods were moved off the {@link MaximoConfiguration} entity
 * (formerly {@code @NamedNativeQuery} + {@code @SqlResultSetMapping}) and rewritten
 * as JPQL constructor projections. The previous {@code LIMIT 1} single-result guard
 * is redundant given the {@code UNIQUE (vdms_id)} constraint, so behavior is unchanged.
 *
 * The add-or-update {@code upsertMaximoConfiguration} (formerly native
 * {@code INSERT ... ON CONFLICT ... COALESCE}) is now a {@code save()}-based upsert
 * (default method below), preserving the same per-column rules (overwrite incl. null
 * for the non-secret columns; keep the stored secret on null).
 */
@Repository
public interface MaximoConfigurationRepository extends JpaRepository<MaximoConfiguration, String> {

    /** Returns DTO WITHOUT secrets (5-arg projection). Safe for read APIs. */
    @Query("SELECT new io.sclera.workorder.dto.MaximoConfigurationDTO(" +
            "m.id, m.name, m.serverUrl, m.authUrl, m.sites) " +
            "FROM MaximoConfiguration m WHERE m.vdmsId = ?1")
    MaximoConfigurationDTO getMaximoConfigurationByVdmsId(String vdmsId);

    /** Returns DTO WITH secrets (7-arg projection). Used internally for token generation. */
    @Query("SELECT new io.sclera.workorder.dto.MaximoConfigurationDTO(" +
            "m.id, m.name, m.serverUrl, m.authUrl, m.clientId, m.clientSecret, m.sites) " +
            "FROM MaximoConfiguration m WHERE m.vdmsId = ?1")
    MaximoConfigurationDTO getMaximoConfigByVdmsId(String vdmsId);

    /**
     * Add-or-update a Maximo configuration via {@code save()} — replaces the former native
     * {@code INSERT … ON CONFLICT … COALESCE}, with the same per-column rules:
     * overwrite {@code name, server_url, auth_url, sites, vdms_id} (incl. null), but keep the
     * stored {@code client_id} / {@code client_secret} when the incoming value is null.
     */
    default void upsertMaximoConfiguration(String id, String name, String serverUrl, String authUrl,
                                           String clientId, String clientSecret, String sites, String vdmsId) {
        MaximoConfiguration cfg = findById(id).orElseGet(MaximoConfiguration::new);
        cfg.setId(id);
        cfg.setName(name);
        cfg.setServerUrl(serverUrl);
        cfg.setAuthUrl(authUrl);
        cfg.setSites(sites);
        cfg.setVdmsId(vdmsId);
        if (clientId != null) {
            cfg.setClientId(clientId);
        }
        if (clientSecret != null) {
            cfg.setClientSecret(clientSecret);
        }
        save(cfg);
    }

    @Modifying
    @Transactional
    @Query("DELETE FROM MaximoConfiguration m WHERE m.id = ?1")
    void deleteMaximoConfiguration(String id);
}
