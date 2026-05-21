package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to vdms-service.
 * Replaces {@code io.sclera.stubs.VdmsRepositoryStub}.
 */
@Component
@Primary
public class VdmsRepoClient implements VdmsRepository {

    private static final Logger log = LoggerFactory.getLogger(VdmsRepoClient.class);
    private static final String APP_ID = "vdms-service";

    private final DaprClient dapr;

    public VdmsRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    @Override
    public String getVDMSId() {
        try {
            return dapr.invokeMethod(APP_ID, "vdms/getVDMSId", null, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.getVDMSId failed; returning null", e);
        }
        return null;
    }

    @Override
    public VdmsDTO getVdmsDetails() {
        try {
            return dapr.invokeMethod(APP_ID, "vdms/getVdmsDetails", null, HttpExtension.GET, VdmsDTO.class).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.getVdmsDetails failed; returning null", e);
        }
        return null;
    }

    @Override
    public void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            p.put("customerOrgId", customerOrgId);
            dapr.invokeMethod(APP_ID, "vdms/updateCustomerOrgIdByVdmsId", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.updateCustomerOrgIdByVdmsId failed; swallowing", e);
        }
    }

    @Override
    public String getVDMSPassword() {
        try {
            return dapr.invokeMethod(APP_ID, "vdms/getVDMSPassword", null, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.getVDMSPassword failed; returning empty string", e);
        }
        return "";
    }

    @Override
    public Integer getIsMaster() {
        try {
            return dapr.invokeMethod(APP_ID, "vdms/getIsMaster", null, HttpExtension.GET, Integer.class).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.getIsMaster failed; returning 0", e);
        }
        return 0;
    }

    @Override
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdms_id);
            return dapr.invokeMethod(APP_ID, "vdms/getCustomerOrgIdByVdmsId", p, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.getCustomerOrgIdByVdmsId failed; returning null", e);
        }
        return null;
    }

    @Override
    public VdmsDTO getSyncDetailsForADC() {
        try {
            return dapr.invokeMethod(APP_ID, "vdms/getSyncDetailsForADC", null, HttpExtension.GET, VdmsDTO.class).block();
        } catch (Exception e) {
            log.warn("VdmsRepoClient.getSyncDetailsForADC failed; returning null", e);
        }
        return null;
    }
}
