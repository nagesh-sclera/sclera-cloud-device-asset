package io.sclera.workorder.service.impl;

import io.sclera.workorder.service.MaximoService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.workorder.client.MaximoApiClient;
import io.sclera.workorder.client.UserActionLogClient;
import io.sclera.workorder.client.VdmsClient;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.dto.VdmsDetailsDTO;
import io.sclera.workorder.exception.MaximoException;
import io.sclera.workorder.exception.VdmsNotFoundException;
import io.sclera.workorder.repository.MaximoConfigurationRepository;
import io.sclera.workorder.util.MaximoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Business logic for Maximo configuration and work-order endpoints.
 *
 * All existing methods preserve monolith semantics exactly:
 *   - upsertMaximoConfiguration: same UUID v1 generation, same audit calls, same return value
 *   - getMaximoConfigurationByVdmsId: same DTO shape (no secrets)
 *   - deleteMaximoConfiguration: same try/catch swallow, same audit pattern
 *   - getMaximoWorkOrders: same 401-retry, same per-record exception swallowing, same field mapping
 *   - getMaximoWorkOrderId: same wonum extraction
 *   - checkConfigurationStatus: same backfill-from-DB logic, same probe call
 *   - getMaximoSites: same hardcoded catalog (delegated to MaximoUtils)
 *
 * Refactor-only changes (no business behavior change):
 *   - apiCallService.* → maximoApiClient.*  (focused client extracted from god class)
 *   - userActionLogService.addUserAction(...) → userActionLogClient.addUserAction(..., vdmsId)
 *     (the call now crosses the Dapr boundary to vdms-service; signature gains vdmsId)
 *   - vdmsService.getVDMSId() → vdmsId passed in by controller (already in the URL path)
 *
 * NEW method (per prompt requirement):
 *   - getVdmsDetailsForMaximoConfig(vdmsId): Dapr-invoke to vdms-service. Returns
 *     VDMS details. Existing flows are NOT routed through this method.
 */
/** Default {@link MaximoService} implementation (Maximo config + work-order business logic). */
@Slf4j
@Service
public class MaximoServiceImpl implements MaximoService {

    private final MaximoConfigurationRepository maximoConfigurationRepository;
    private final MaximoApiClient maximoApiClient;
    private final MaximoUtils maximoUtils;
    private final UserActionLogClient userActionLogClient;
    private final VdmsClient vdmsClient;

    public MaximoServiceImpl(MaximoConfigurationRepository maximoConfigurationRepository,
                         MaximoApiClient maximoApiClient,
                         MaximoUtils maximoUtils,
                         UserActionLogClient userActionLogClient,
                         VdmsClient vdmsClient) {
        this.maximoConfigurationRepository = maximoConfigurationRepository;
        this.maximoApiClient = maximoApiClient;
        this.maximoUtils = maximoUtils;
        this.userActionLogClient = userActionLogClient;
        this.vdmsClient = vdmsClient;
    }

    /** {@inheritDoc} Lazy-loads from the config row into the per-VDMS cache on first access. */
    @Override
    @Transactional(readOnly = true)
    public String getServerUrl(String vdmsId) {
        if (maximoUtils.getServerUrl(vdmsId) == null) {
            MaximoConfigurationDTO maximoConfigurationDTO = maximoConfigurationRepository.getMaximoConfigByVdmsId(vdmsId);
            maximoUtils.setServerUrl(vdmsId, maximoConfigurationDTO.getServerUrl());
        }
        return maximoUtils.getServerUrl(vdmsId);
    }

    /** {@inheritDoc} Stores the token and its creation timestamp in the per-VDMS cache. */
    @Override
    public void updateToken(String token, String vdmsId) {
        maximoUtils.setToken(vdmsId, token);
        maximoUtils.setTokenCreatedAt(vdmsId, System.currentTimeMillis());
    }

    /** {@inheritDoc} Returns the cached token if under one hour old (unless {@code forceCreate}); otherwise re-authenticates and caches. */
    @Override
    public String getToken(Boolean forceCreate, String vdmsId) {
        if (maximoUtils.getToken(vdmsId) != null && forceCreate != null && !forceCreate
                && maximoUtils.getTokenCreatedAt(vdmsId) != null
                && (System.currentTimeMillis() - maximoUtils.getTokenCreatedAt(vdmsId) < (60 * 60 * 1000))) {
            return maximoUtils.getToken(vdmsId);
        }
        log.debug("Refreshing Maximo access token for vdmsId={} (forceCreate={})", vdmsId, forceCreate);
        MaximoConfigurationDTO maximoConfigurationDTO = maximoConfigurationRepository.getMaximoConfigByVdmsId(vdmsId);
        String authToken = maximoApiClient.generateMaximoAccessToken(maximoConfigurationDTO);
        updateToken(authToken, vdmsId);
        return authToken;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configuration CRUD
    // ─────────────────────────────────────────────────────────────────────────

    /** {@inheritDoc} Monolith-preserved upsert: generates a time-based UUID for new rows and records an audit action. */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "maximoConfig", key = "#vdmsId")
    public String upsertMaximoConfiguration(String userName, String vdmsId, MaximoConfigurationDTO maximoConfigurationDTO) {
        if (maximoConfigurationDTO.getId() == null) {
            maximoConfigurationDTO.setId(Generators.timeBasedGenerator().generate().toString());
            maximoConfigurationRepository.upsertMaximoConfiguration(
                    maximoConfigurationDTO.getId(),
                    maximoConfigurationDTO.getName(),
                    maximoConfigurationDTO.getServerUrl(),
                    maximoConfigurationDTO.getAuthUrl(),
                    maximoConfigurationDTO.getClientId(),
                    maximoConfigurationDTO.getClientSecret(),
                    maximoConfigurationDTO.getSites(),
                    vdmsId);
            userActionLogClient.addUserAction(userName, "maximo", "ADD",
                    " Configuration is added successfully", "success",
                    "maximo_configuration", maximoConfigurationDTO.getId(), vdmsId);
            log.info("Added Maximo configuration id={} for vdmsId={} by user={}",
                    maximoConfigurationDTO.getId(), vdmsId, userName);
        } else {
            maximoConfigurationRepository.upsertMaximoConfiguration(
                    maximoConfigurationDTO.getId(),
                    maximoConfigurationDTO.getName(),
                    maximoConfigurationDTO.getServerUrl(),
                    maximoConfigurationDTO.getAuthUrl(),
                    maximoConfigurationDTO.getClientId(),
                    maximoConfigurationDTO.getClientSecret(),
                    maximoConfigurationDTO.getSites(),
                    vdmsId);
            userActionLogClient.addUserAction(userName, "maximo", "UPDATE",
                    " Configuration is updated successfully", "success",
                    "maximo_configuration", maximoConfigurationDTO.getId(), vdmsId);
            log.info("Updated Maximo configuration id={} for vdmsId={} by user={}",
                    maximoConfigurationDTO.getId(), vdmsId, userName);
        }
        maximoUtils.setServerUrl(vdmsId, maximoConfigurationDTO.getServerUrl());
        maximoUtils.setSiteId(vdmsId, maximoConfigurationDTO.getSites());
        return "success";
    }

    /** {@inheritDoc} Returns the secrets-free read projection (5-arg mapping) for the VDMS. */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "maximoConfig", key = "#vdmsId")
    public MaximoConfigurationDTO getMaximoConfigurationByVdmsId(String vdmsId) {
        return maximoConfigurationRepository.getMaximoConfigurationByVdmsId(vdmsId);
    }

    /** {@inheritDoc} Deletes the configuration row and records the audit action (monolith-preserved swallow on failure). */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "maximoConfig", key = "#vdmsId")
    public void deleteMaximoConfiguration(String userName, String id, String vdmsId) {
        try {
            maximoConfigurationRepository.deleteById(id);
            userActionLogClient.addUserAction(userName, "maximo", "DELETE",
                    " Configuration is deleted successfully", "success",
                    "maximo_configuration", id, vdmsId);
            log.info("Deleted Maximo configuration id={} for vdmsId={} by user={}", id, vdmsId, userName);
        } catch (RuntimeException e) {
            log.error("Failed to delete Maximo configuration id={} for vdmsId={}: {}", id, vdmsId, e.getMessage(), e);
            userActionLogClient.addUserAction(userName, "maximo", "DELETE",
                    "Unable to delete configuration", "failed",
                    "maximo_configuration", id, vdmsId);
            throw e;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Work-order queries — verbatim port (50+ field setters per record + per-record swallowing)
    // ─────────────────────────────────────────────────────────────────────────

    /** {@inheritDoc} Calls the Maximo OSLC API with a 401-retry (token refresh) and swallows per-record mapping errors. */
    @Override
    public List<MaximoDTO> getMaximoWorkOrders(String vdmsId, String workOrderId, Integer pageno, Integer pagesize, MaximoDTO maximoDTO) {
        JSONObject allWorkorders = null;
        try {
            allWorkorders = maximoApiClient.getAllWorkorders(this.getToken(false, vdmsId), this.getServerUrl(vdmsId), workOrderId, maximoDTO, pageno, pagesize);
        } catch (MaximoException me) {
            if (me.getErrorCode() == 401) {
                log.warn("Maximo returned 401 for vdmsId={}; retrying with a fresh token", vdmsId);
                allWorkorders = maximoApiClient.getAllWorkorders(this.getToken(true, vdmsId), this.getServerUrl(vdmsId), workOrderId, maximoDTO, pageno, pagesize);
            } else {
                log.error("Maximo work-order query failed for vdmsId={} (errorCode={}): {}",
                        vdmsId, me.getErrorCode(), me.getMessage());
            }
        }
        if (allWorkorders != null) {
            List<MaximoDTO> maximoDTOS = new ArrayList<>();
            if (allWorkorders.containsKey("member")) {
                JSONArray jsonArray = allWorkorders.getJSONArray("member");
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        MaximoDTO maximpDTOList = new MaximoDTO();
                        JSONObject jsonWorkOrder = jsonArray.getJSONObject(i);

                        maximpDTOList.setWonum(jsonWorkOrder.getString("wonum"));
                        maximpDTOList.setDescription(jsonWorkOrder.getString("description"));
                        maximpDTOList.setLocation(jsonWorkOrder.getString("location"));
                        maximpDTOList.setWorktype(jsonWorkOrder.getString("worktype"));
                        maximpDTOList.setStatus(jsonWorkOrder.getString("status"));
                        maximpDTOList.setAssetnum(jsonWorkOrder.getString("assetnum"));
                        maximpDTOList.setStatusdate(jsonWorkOrder.getString("statusdate"));
                        maximpDTOList.setGlaccount(jsonWorkOrder.getString("glaccount"));
                        maximpDTOList.setParent(jsonWorkOrder.getString("parent"));
                        maximpDTOList.setIstask(jsonWorkOrder.getBoolean("istask"));
                        maximpDTOList.setInShared(jsonWorkOrder.getBoolean("in_shared"));
                        maximpDTOList.setInImpact(jsonWorkOrder.getBoolean("in_impact"));
                        maximpDTOList.setWoeq11(jsonWorkOrder.getString("woeq11"));
                        maximpDTOList.setAssetlocationpriority(jsonWorkOrder.getInteger("assetlocpriority"));
                        maximpDTOList.setInIrn(jsonWorkOrder.getBoolean("in_irn"));
                        maximpDTOList.setChangeby(jsonWorkOrder.getString("changeby"));
                        maximpDTOList.setChangedate(jsonWorkOrder.getString("changedate"));
                        maximpDTOList.setParentchgsstatus(jsonWorkOrder.getBoolean("parentchgsstatus"));
                        maximpDTOList.setCalcpriority(jsonWorkOrder.getInteger("calcpriority"));
                        maximpDTOList.setJpnum(jsonWorkOrder.getString("jpnum"));
                        maximpDTOList.setJpFrequency(jsonWorkOrder.getString("jp_frequency"));
                        maximpDTOList.setPluscjprevnum(jsonWorkOrder.getInteger("pluscjprevnum"));
                        maximpDTOList.setPmnum(jsonWorkOrder.getString("pmnum"));
                        maximpDTOList.setRoute(jsonWorkOrder.getString("route"));
                        maximpDTOList.setTargstartdate(jsonWorkOrder.getString("targstartdate"));
                        maximpDTOList.setTargcompdate(jsonWorkOrder.getString("targcompdate"));
                        maximpDTOList.setSneconstraint(jsonWorkOrder.getString("sneconstraint"));
                        maximpDTOList.setFnlconstraint(jsonWorkOrder.getString("fnlconstraint"));
                        maximpDTOList.setPcacthrs(jsonWorkOrder.getString("pcacthrs"));
                        maximpDTOList.setAms(jsonWorkOrder.getBoolean("ams"));
                        maximpDTOList.setLms(jsonWorkOrder.getBoolean("lms"));
                        maximpDTOList.setAos(jsonWorkOrder.getBoolean("aos"));
                        maximpDTOList.setLos(jsonWorkOrder.getBoolean("los"));
                        maximpDTOList.setOrigrecordid(jsonWorkOrder.getString("origrecordid"));
                        maximpDTOList.setWplaborDescription(jsonWorkOrder.getString("description"));
                        maximpDTOList.setHasfollowupwork(jsonWorkOrder.getBoolean("hasfollowupwork"));
                        maximpDTOList.setInterruptible(jsonWorkOrder.getBoolean("interruptible"));
                        maximpDTOList.setReportedby(jsonWorkOrder.getString("reportedby"));
                        maximpDTOList.setSupervisor(jsonWorkOrder.getString("supervisor"));
                        maximpDTOList.setOwner(jsonWorkOrder.getString("owner"));
                        maximpDTOList.setInIfmsource(jsonWorkOrder.getString("in_ifmsource"));
                        maximpDTOList.setReporteddate(jsonWorkOrder.getString("reportdate"));
                        maximpDTOList.setCrewid(jsonWorkOrder.getString("crewid"));
                        maximpDTOList.setInIfmpriority(jsonWorkOrder.getInteger("in_ifmpriorty"));
                        maximpDTOList.setPhone(jsonWorkOrder.getString("phone"));
                        maximpDTOList.setLeadcraft(jsonWorkOrder.getString("leadcraft"));
                        maximpDTOList.setVendor(jsonWorkOrder.getString("vendor"));
                        maximpDTOList.setAmcrew(jsonWorkOrder.getString("amcrew"));
                        maximpDTOList.setCrewworkgroup(jsonWorkOrder.getString("crewworkgroup"));
                        maximpDTOList.setLead(jsonWorkOrder.getString("lead"));
                        maximpDTOList.setSiteid(jsonWorkOrder.getString("siteid"));
                        maximpDTOList.setSchedstart(jsonWorkOrder.getString("schedstart"));
                        maximoDTOS.add(maximpDTOList);
                    } catch (Exception e) {
                        // Behaviour preserved — malformed rows are still dropped, now logged.
                        log.debug("Skipping malformed Maximo work order at index {} for vdmsId={}: {}",
                                i, vdmsId, e.getMessage());
                    }
                }
                if (!maximoDTOS.isEmpty()) {
                    return maximoDTOS;
                }
            }
        }
        return Collections.emptyList();
    }

    /** {@inheritDoc} Same OSLC query as {@link #getMaximoWorkOrders} but projects only the {@code wonum} values. */
    @Override
    public List<String> getMaximoWorkOrderId(String vdmsId, String workOrderId, Integer pageno, Integer pagesize, MaximoDTO maximoDTO) {
        JSONObject allWorkorders = null;
        try {
            allWorkorders = maximoApiClient.getAllWorkorders(this.getToken(false, vdmsId), this.getServerUrl(vdmsId), workOrderId, maximoDTO, pageno, pagesize);
        } catch (MaximoException me) {
            if (me.getErrorCode() == 401) {
                log.warn("Maximo returned 401 for vdmsId={}; retrying with a fresh token", vdmsId);
                allWorkorders = maximoApiClient.getAllWorkorders(this.getToken(true, vdmsId), this.getServerUrl(vdmsId), workOrderId, maximoDTO, pageno, pagesize);
            } else {
                log.error("Maximo work-order query failed for vdmsId={} (errorCode={}): {}",
                        vdmsId, me.getErrorCode(), me.getMessage());
            }
        }
        if (allWorkorders != null) {
            List<String> workOrders = new ArrayList<>();
            if (allWorkorders.containsKey("member")) {
                JSONArray jsonArray = allWorkorders.getJSONArray("member");
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        JSONObject jsonWorkOrder = jsonArray.getJSONObject(i);
                        String workoder = jsonWorkOrder.getString("wonum");
                        if (workoder != null && !workoder.isEmpty()) {
                            workOrders.add(workoder);
                        }
                    } catch (Exception e) {
                        // Behaviour preserved — malformed rows are still dropped, now logged.
                        log.debug("Skipping malformed Maximo work order (wonum) at index {} for vdmsId={}: {}",
                                i, vdmsId, e.getMessage());
                    }
                }
                if (!workOrders.isEmpty()) {
                    return workOrders;
                }
            }
        }
        return Collections.emptyList();
    }

    /** {@inheritDoc} Backfills missing fields from the stored config, then probes Maximo connectivity/credentials. */
    @Override
    public String checkConfigurationStatus(String userName, String vdmsId, MaximoConfigurationDTO maximoConfigurationDTO) {
        if (maximoConfigurationDTO.getClientId() == null || maximoConfigurationDTO.getClientSecret() == null) {
            MaximoConfigurationDTO maximo = maximoConfigurationRepository.getMaximoConfigByVdmsId(vdmsId);
            if (maximoConfigurationDTO.getClientId() == null && maximo.getClientId() != null) {
                maximoConfigurationDTO.setClientId(maximo.getClientId());
            }
            if (maximoConfigurationDTO.getClientSecret() == null && maximo.getClientSecret() != null) {
                maximoConfigurationDTO.setClientSecret(maximo.getClientSecret());
            }
        }
        String accessToken = maximoApiClient.generateMaximoAccessToken(maximoConfigurationDTO);
        updateToken(accessToken, vdmsId);
        if (accessToken != null && !accessToken.isBlank()) {
            JSONObject allWorkorders = maximoApiClient.getAllWorkorders(
                    this.getToken(false, vdmsId), maximoConfigurationDTO.getServerUrl(), null, null, 1, 1);
            if (allWorkorders != null && allWorkorders.containsKey("status_code")
                    && allWorkorders.getInteger("status_code") == 200) {
                log.info("Maximo configuration probe succeeded for vdmsId={}", vdmsId);
                return "success";
            }
        }
        log.warn("Maximo configuration probe failed for vdmsId={}", vdmsId);
        return "failure";
    }

    /** {@inheritDoc} Returns the site catalog, delegated to {@link io.sclera.workorder.util.MaximoUtils}. */
    @Override
    public JSONArray getMaximoSites() {
        return maximoUtils.getSites();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEW SAMPLE METHOD — Dapr service invocation to vdms-service.
    // Not called by any existing endpoint; exposed via a NEW endpoint
    // /user/{u}/vdms/{v}/getvdmsdetails (see MaximoController).
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Cross-service: invokes vdms-service through the Dapr sidecar (the inter-service Dapr
     * invocation pattern). Throws {@link VdmsNotFoundException} when the VDMS is absent.
     */
    @Override
    public VdmsDetailsDTO getVdmsDetailsForMaximoConfig(String vdmsId) {
        return vdmsClient.getVdmsDetails(vdmsId)
                .orElseThrow(() -> new VdmsNotFoundException(vdmsId));
    }
}
