package io.sclera.workorder.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.dto.VdmsDetailsDTO;

import java.util.List;

/**
 * Maximo integration: per-VDMS configuration management, OAuth token handling,
 * and work-order queries against the configured Maximo OSLC API.
 */
public interface MaximoService {

    /**
     * @param vdmsId the VDMS scope
     * @return the configured Maximo server URL for the VDMS
     */
    String getServerUrl(String vdmsId);

    /**
     * Caches the Maximo OAuth token for a VDMS.
     *
     * @param token  the token to cache
     * @param vdmsId the VDMS scope
     */
    void updateToken(String token, String vdmsId);

    /**
     * Returns a valid Maximo OAuth token for the VDMS, creating one if needed.
     *
     * @param forceCreate when {@code true}, bypasses the cache and re-authenticates
     * @param vdmsId      the VDMS scope
     * @return a bearer token
     */
    String getToken(Boolean forceCreate, String vdmsId);

    /**
     * Creates or updates the Maximo configuration for a user + VDMS (idempotent upsert).
     *
     * @param userName               acting user
     * @param vdmsId                 the VDMS scope
     * @param maximoConfigurationDTO the configuration to persist
     * @return a status string
     */
    String upsertMaximoConfiguration(String userName, String vdmsId, MaximoConfigurationDTO maximoConfigurationDTO);

    /**
     * @param vdmsId the VDMS scope
     * @return the configuration (without secrets), or {@code null} if none exists
     */
    MaximoConfigurationDTO getMaximoConfigurationByVdmsId(String vdmsId);

    /**
     * Deletes a Maximo configuration.
     *
     * @param userName acting user
     * @param id       the configuration id
     * @param vdmsId   the VDMS scope
     */
    void deleteMaximoConfiguration(String userName, String id, String vdmsId);

    /**
     * Fetches Maximo work orders matching the filter (with 401-retry and per-record resilience).
     *
     * @param vdmsId      the VDMS scope
     * @param workOrderId a specific work-order number, or {@code all}
     * @param pageno      1-based page number
     * @param pagesize    page size
     * @param maximoDTO   filter criteria
     * @return the matching work orders
     */
    List<MaximoDTO> getMaximoWorkOrders(String vdmsId, String workOrderId, Integer pageno, Integer pagesize, MaximoDTO maximoDTO);

    /**
     * Like {@link #getMaximoWorkOrders} but returns only the work-order numbers.
     *
     * @return the matching work-order ids
     */
    List<String> getMaximoWorkOrderId(String vdmsId, String workOrderId, Integer pageno, Integer pagesize, MaximoDTO maximoDTO);

    /**
     * Validates connectivity/credentials for a (possibly partial) configuration, backfilling
     * missing fields from the stored config.
     *
     * @return a status string describing the probe result
     */
    String checkConfigurationStatus(String userName, String vdmsId, MaximoConfigurationDTO maximoConfigurationDTO);

    /**
     * @return the catalog of available Maximo sites
     */
    JSONArray getMaximoSites();

    /**
     * Cross-service: fetches VDMS details from vdms-service via Dapr invocation.
     *
     * @param vdmsId the VDMS id
     * @return the VDMS details
     */
    VdmsDetailsDTO getVdmsDetailsForMaximoConfig(String vdmsId);
}
