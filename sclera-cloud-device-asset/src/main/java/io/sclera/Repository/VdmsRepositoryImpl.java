package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.models.Vdms;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Local, database-backed implementation of {@link VdmsRepository}. Reads the
 * {@code vdms} table by id via {@link VdmsJpaRepository}, replacing the remote
 * single-VDMS Dapr delegation in {@code VdmsRepoClient}. Marked {@link Primary}
 * so it is the injected {@code VdmsRepository}.
 */
@Repository
@Primary
public class VdmsRepositoryImpl implements VdmsRepository {

    private final VdmsJpaRepository jpa;

    public VdmsRepositoryImpl(VdmsJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public String findSingleVdmsId() {
        List<String> ids = jpa.findAllIds();
        return ids.size() == 1 ? ids.get(0) : null;
    }

    @Override
    public String getVDMSId() {
        return findSingleVdmsId();
    }

    @Override
    public VdmsDTO getVdmsDetails() {
        String id = findSingleVdmsId();
        return id == null ? null : getVdmsDetails(id);
    }

    @Override
    public VdmsDTO getVdmsDetails(String vdmsId) {
        return jpa.findById(vdmsId).map(this::toDto).orElse(null);
    }

    @Override
    public String getVDMSPassword() {
        String id = findSingleVdmsId();
        return id == null ? null : getVDMSPassword(id);
    }

    @Override
    public String getVDMSPassword(String vdmsId) {
        return jpa.findById(vdmsId).map(Vdms::getPassword).orElse(null);
    }

    @Override
    public Integer getIsMaster() {
        String id = findSingleVdmsId();
        return id == null ? 0 : getIsMaster(id);
    }

    @Override
    public Integer getIsMaster(String vdmsId) {
        return jpa.findById(vdmsId).map(Vdms::getIs_master).orElse(0);
    }

    @Override
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        return jpa.findById(vdms_id).map(Vdms::getCustomer_org_id).orElse(null);
    }

    @Override
    public void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId) {
        jpa.findById(vdmsId).ifPresent(v -> {
            v.setCustomer_org_id(customerOrgId);
            jpa.save(v);
        });
    }

    @Override
    public VdmsDTO getSyncDetailsForADC() {
        String id = findSingleVdmsId();
        if (id == null) return null;
        return jpa.findById(id).map(v -> {
            VdmsDTO dto = new VdmsDTO();
            dto.setId(v.getId());
            dto.setCustomer_org_id(v.getCustomer_org_id());
            dto.setAdc_configuration_id(v.getAdc_configuration_id());
            dto.setZip(v.getZip());
            return dto;
        }).orElse(null);
    }

    private VdmsDTO toDto(Vdms v) {
        VdmsDTO dto = new VdmsDTO();
        dto.setId(v.getId());
        dto.setProperty_name(v.getProperty_name());
        dto.setAddress(v.getAddress());
        dto.setCity(v.getCity());
        dto.setCountry(v.getCountry());
        dto.setState(v.getState());
        dto.setZip(v.getZip());
        dto.setTimezone(v.getTimezone());
        dto.setImage_url(v.getImage_url());
        dto.setLatitude(v.getLatitude());
        dto.setLongitude(v.getLongitude());
        dto.setActivation_timestamp(v.getActivation_timestamp());
        dto.setDeployment_type(v.getDeployment_type());
        dto.setRegion(v.getRegion());
        return dto;
    }
}
