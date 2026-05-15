package io.sclera.stubs;

import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// TODO: replace with remote call to sclera-vdms-service
@Component
public class VdmsRepositoryStub implements VdmsRepository {

    private static final Logger log = LoggerFactory.getLogger(VdmsRepositoryStub.class);

    @Override
    public String getVDMSId() {
        log.warn("STUB CALL: class={} method=getVDMSId target_microservice=sclera-vdms-service args_hash=none", getClass().getName());
        return null;
    }

    @Override
    public VdmsDTO getVdmsDetails() {
        return null;
    }

    @Override
    public void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId) {

    }

    @Override
    public String getVDMSPassword() {
        return "";
    }

    @Override
    public Integer getIsMaster() {
        return 0;
    }

    @Override
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        log.warn("STUB CALL: class={} method=getCustomerOrgIdByVdmsId target_microservice=sclera-vdms-service args_hash={}", getClass().getName(), vdms_id != null ? vdms_id.hashCode() : 0);
        return null;
    }

    @Override
    public VdmsDTO getSyncDetailsForADC() {
        log.warn("STUB CALL: class={} method=getSyncDetailsForADC target_microservice=sclera-vdms-service args_hash=none", getClass().getName());
        return null;
    }
}