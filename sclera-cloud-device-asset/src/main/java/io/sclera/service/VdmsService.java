package io.sclera.service;

import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;

/** Service contract for the matching service class. */
public interface VdmsService {

    void startVdmsService();

    String getVDMSId();

    void upsertWeatherData(VdmsDetailsDTO vdms_details);

    VdmsDetailsDTO getWeatherData();

    String getVdmsDetailsId();

    String getDeviceFieldsList();

    String getMergedDeviceFieldsList(StringBuilder file, VdmsDetailsDTO vdmsDetailsDTO);

    VdmsDetailsDTO getVdmsDeviceCustomFields();

    void upsertVdmsDeviceCustomFields(VdmsDetailsDTO vdms_details);

    void updateVdmsLayoutData(String vdms_id, VdmsDetailsDTO vdms_details);

    VdmsDetailsDTO getVdmsLayoutData();

    void upsertCorrigoLayoutData(String vdms_id, VdmsDetailsDTO vdms_details);

    VdmsConfigurationDTO getConfiguration();

    void upsertDeviceCustomFields(String username, String vdms_id, com.alibaba.fastjson.JSONArray deviceCustomFieldsList);

    VdmsDetailsDTO getDeviceCustomFields(String username, String vdms_id);

    VdmsDTO getVDMSDetails();

    void updateCustomerOrgIdByVdmsId(String vdms_id, String customer_org_id);

    void getVdmsAccessToken(String vdms_id, String password);

    String getVDMSPassword();

    String getCustomerOrgIdByVdmsId(String vdms_id);

    Integer getIsMaster();
}
