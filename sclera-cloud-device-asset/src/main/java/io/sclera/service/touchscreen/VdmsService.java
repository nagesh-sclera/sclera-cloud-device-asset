package io.sclera.service.touchscreen;
import io.sclera.client.APICallClient;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.*;

import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;

import io.sclera.client.CorrigoClient;
import io.sclera.client.IntegrationClient;
import io.sclera.client.RabbitmqClient;
import io.sclera.service.*;
import io.sclera.interfaces.VdmsServiceInterface;

import io.sclera.utils.AuthenticationUtils;
import io.sclera.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages VDMS (touchscreen device) identity, configuration, weather, layout, and
 * device-field metadata for the asset-management service.
 *
 * <p>Reads and persists VDMS state through {@link VdmsRepository},
 * {@link VdmsDetailsRepository}, {@link VdmsconfigurationRepository},
 * {@link RemoteAgentServerDetailsRepository}, and {@link DockerRepository}.
 * Coordinates with {@link APICallClient} for VDMS access tokens,
 * {@link RabbitmqClient}, {@link CorrigoClient}, and {@link IntegrationClient} for
 * external integrations, and {@link DeviceService} for device operations. Uses
 * {@link Utils} and {@link AuthenticationUtils} for shared helpers and authentication
 * context.
 */
@Service
public class VdmsService implements VdmsServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(VdmsService.class);

    @Autowired
    VdmsRepository vdmsRepository;

    @Autowired
    VdmsDetailsRepository vdmsDetailsRepository;

    @Autowired
    VdmsconfigurationRepository vdmsconfigurationRepository;

    @Autowired
    APICallClient apiCallService;

    @Autowired
    Utils utils;

    @Autowired
    AuthenticationUtils authenticationUtils;

    @Autowired
    RabbitmqClient rabbitmqService;

    @Autowired
    CorrigoClient corrigoService;

    @Autowired
    IntegrationClient integrationService;


    @Autowired
    DeviceService deviceService;


    @Autowired
    RemoteAgentServerDetailsRepository remoteAgentServerDetailsRepository;

    @Autowired
    DockerRepository dockerRepository;

    /**
     * Bootstraps the VDMS session by resolving the VDMS id, storing it in the
     * authentication context, and requesting an access token using the stored password.
     */
    public void startVdmsService() {

        try {
            String vdmsId = vdmsRepository.getVDMSId();
            authenticationUtils.setVdms_id(vdmsId);
            String password = this.getVDMSPassword();
            if (password != null) {
                this.getVdmsAccessToken(vdmsId, password);
            }
        } catch (Exception e) {
            log.error("Error in starting vdms service: {}", e.getMessage());
        }

    }


    /**
     * Returns the VDMS id.
     *
     * @return the VDMS id
     */
    //get vdms id
    public String getVDMSId() {
        return vdmsRepository.getVDMSId();
    }

    //Get current set timezone of the device
//    public String getCurrentTimeZone() {
//        try {
//            return vdmsRepository.getCurrentTimeZone();
//        } catch (Exception e) {
//            log.error("error getting current set timezone", e);
//        }
//        return null;
//    }

    //Update VDMS timezone
//    public void updateTimeZone(String timezone) {
//        try {
//            vdmsRepository.updateTimeZone(timezone);
//        } catch (Exception e) {
//            log.error("Error updating vdms timezone", e);
//        }
//
//    }

    /**
     * Inserts or updates VDMS weather data by city, zip code, or geolocation.
     *
     * @param vdms_details the VDMS details carrying the weather fields to persist
     */
    //update vdms weather data by city, zipcode or geolocation
    public void upsertWeatherData(VdmsDetailsDTO vdms_details) {
        vdmsDetailsRepository.upsertWeatherData(vdms_details.getId(), vdms_details.getWeather_city(), vdms_details.getWeather_country_code(),
                vdms_details.getWeather_latitude(), vdms_details.getWeather_longitude(), vdms_details.getWeather_data(),
                vdms_details.getWeather_zip_code(), vdms_details.getWeather_units(), vdms_details.getVdms_id());
    }

    /**
     * Returns the stored VDMS weather details.
     *
     * @return the VDMS weather details
     */
    //get weather details
    public VdmsDetailsDTO getWeatherData() {
        return vdmsDetailsRepository.getWeatherData();
    }

    /**
     * Returns the VDMS details record id.
     *
     * @return the VDMS details id
     */
    //get vdms details id
    public String getVdmsDetailsId() {
        return vdmsDetailsRepository.getVdmsDetailsId();
    }


    /**
     * Reads the bundled base device-fields definition and merges it with the
     * VDMS-specific custom device fields.
     *
     * @return the merged device-fields list as a JSON string, or {@code null} if the
     *         file cannot be read
     */
    public String getDeviceFieldsList() {
        try {

            //Read from file
            StringBuilder stringBuilder = new StringBuilder();
//		      File file = new File("/home/sclera/device_fields.json");
//			File file = ResourceUtils.getFile("classpath:device_fields.json");
            InputStream inputStreamReader = getClass().getResourceAsStream("/device_fields.json");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader))) {
                stringBuilder.append(bufferedReader.lines().collect(Collectors.joining("\n")));
            }
            return this.getMergedDeviceFieldsList(stringBuilder, this.getVdmsDeviceCustomFields());

        } catch (Exception e) {
            log.error("Error reading device fields file: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Merges the base device-fields JSON with any custom device fields, skipping
     * columns already present in the base definition.
     *
     * @param file           the base device-fields JSON content
     * @param vdmsDetailsDTO the VDMS details supplying custom device fields, may be
     *                       {@code null}
     * @return the merged device-fields list as a JSON string, or {@code null} on error
     */
    public String getMergedDeviceFieldsList(StringBuilder file, VdmsDetailsDTO vdmsDetailsDTO) {
        JSONArray result = null;
        Set<String> existingColumns = new HashSet<>();

        try {
            // Parse device_fields.json and collect columns
            result = new JSONArray(file.toString());
            for (int i = 0; i < result.length(); i++) {
                JSONObject obj = result.getJSONObject(i);
                String column = obj.optString("column", "").trim().toLowerCase();
                existingColumns.add(column);
            }
        } catch (JSONException e) {
            log.error("Error parsing base device fields", e);
        }

        try {
            if (vdmsDetailsDTO != null && vdmsDetailsDTO.getDevice_custom_fields() != null &&
                    !(vdmsDetailsDTO.getDevice_custom_fields().equalsIgnoreCase(""))) {
                JSONArray otherDetails = new JSONArray(vdmsDetailsDTO.getDevice_custom_fields());

                for (int i = 0; i < otherDetails.length(); i++) {
                    JSONObject obj = otherDetails.getJSONObject(i);
                    String column = obj.optString("column", "").trim().toLowerCase();
                    if (!existingColumns.contains(column)) {
                        result.put(obj);
                        existingColumns.add(column);
                    }
                }
            }
            return result.toString();

        } catch (Exception e) {
            log.error("Error merging custom fields", e);
        }
        return null;
    }

    /**
     * Returns the VDMS details holding the device custom fields.
     *
     * @return the VDMS device custom fields details
     */
    public VdmsDetailsDTO getVdmsDeviceCustomFields() {
        return vdmsDetailsRepository.getVdmsDeviceCustomFields();
    }

    /**
     * Inserts or updates the VDMS device custom fields, generating an id when absent.
     *
     * @param vdms_details the VDMS details carrying the custom fields to persist
     */
    public void upsertVdmsDeviceCustomFields(VdmsDetailsDTO vdms_details) {

        if (vdms_details.getId() == null) {
            vdms_details.setId(Generators.timeBasedGenerator().generate().toString());
        }

        vdmsDetailsRepository.upsertVdmsDeviceCustomFields(vdms_details.getId(), vdms_details.getDevice_custom_fields(), vdms_details.getVdms_id());
    }


    /**
     * Inserts or updates the VDMS layout data, reusing the existing details id or
     * generating a new one.
     *
     * @param vdms_id      the VDMS id to associate with the layout data
     * @param vdms_details the VDMS details carrying the layout data to persist
     */
    public void updateVdmsLayoutData(String vdms_id, VdmsDetailsDTO vdms_details) {

        String id = this.getVdmsDetailsId();

        if (id == null) {
            vdms_details.setId(Generators.timeBasedGenerator().generate().toString());
        } else {
            vdms_details.setId(id);
        }
        vdmsDetailsRepository.upsertVdmsLayoutData(vdms_details.getId(), vdms_details.getLayout_data(), vdms_id);

    }


    /**
     * Returns the stored VDMS layout data.
     *
     * @return the VDMS layout data details
     */
    public VdmsDetailsDTO getVdmsLayoutData() {
        return vdmsDetailsRepository.getVdmsLayoutData();
    }

//    public void updatePropertyDetails(PropertyAddressDTO propertyAddress) {
//        vdmsRepository.updatePropertyDetails(propertyAddress.getProperty_name(), propertyAddress.getAddress(),
//                propertyAddress.getCity(), propertyAddress.getCountry(), propertyAddress.getState(), propertyAddress.getZip(),
//                propertyAddress.getLatitude(), propertyAddress.getLongitude(), propertyAddress.getImage_url(),
//                propertyAddress.getActivation_timestamp(), propertyAddress.getDeployment_type(), propertyAddress.getRegion());
//    }

    /**
     * Inserts or updates the Corrigo layout data, reusing the existing details id or
     * generating a new one.
     *
     * @param vdms_id      the VDMS id to associate with the layout data
     * @param vdms_details the VDMS details carrying the Corrigo layout data to persist
     */
    public void upsertCorrigoLayoutData(String vdms_id, VdmsDetailsDTO vdms_details) {
        String id = this.getVdmsDetailsId();

        if (id == null) {
            vdms_details.setId(Generators.timeBasedGenerator().generate().toString());
        } else {
            vdms_details.setId(id);
        }
        vdmsDetailsRepository.upsertCorrigoLayoutData(vdms_details.getId(), vdms_details.getCorrigo_layout_data(), vdms_id);
    }

    /**
     * Returns the VDMS configuration.
     *
     * @return the VDMS configuration
     */
    public VdmsConfigurationDTO getConfiguration() {
        return vdmsconfigurationRepository.getConfiguration();
    }

//    public void activateAgent(String vdmsConfigurationId, VdmsDTO vdmsDTO, String organisation_id, String password) {
//        try {
//            vdmsRepository.activateAgent(
//                    vdmsDTO.getId(),
//                    vdmsConfigurationId,
//                    "Activated",
//                    organisation_id,
//                    vdmsDTO.getProperty_name(),
//                    vdmsDTO.getCity(),
//                    vdmsDTO.getState(),
//                    vdmsDTO.getCountry(),
//                    vdmsDTO.getAddress(),
//                    vdmsDTO.getIs_block(),
//                    vdmsDTO.getCreation_timestamp(),
//                    vdmsDTO.getLast_seen(),
//                    vdmsDTO.getBlock_timestamp(),
//                    vdmsDTO.getZip(),
//                    vdmsDTO.getLongitude(),
//                    vdmsDTO.getLatitude(),
//                    password,
//                    vdmsDTO.getActivation_timestamp(),
//                    vdmsDTO.getDeployment_type(),
//                    vdmsDTO.getRegion()
//            );
//        } catch (Exception e) {
//            log.error("Error activating agent: {}", e.getMessage());
//        }

    /**
     * Merges the supplied device custom fields into the stored VDMS custom fields,
     * adding only columns not already present, and persists the result.
     *
     * @param username               the requesting user's name
     * @param vdms_id                the VDMS id used when creating a new details record
     * @param deviceCustomFieldsList the device custom fields to merge, may be
     *                               {@code null}
     */
    public void upsertDeviceCustomFields(String username, String vdms_id, com.alibaba.fastjson.JSONArray deviceCustomFieldsList) {
        if (deviceCustomFieldsList != null) {
            VdmsDetailsDTO vdmsDetailsDTO = this.getVdmsDeviceCustomFields();

            if (vdmsDetailsDTO != null) {
                if (vdmsDetailsDTO.getDevice_custom_fields() != null) {
                    com.alibaba.fastjson.JSONArray vdmsDeviceCustomFields = com.alibaba.fastjson.JSONArray.parseArray(vdmsDetailsDTO.getDevice_custom_fields());

                    for (int i = 0; i < deviceCustomFieldsList.size(); i++) {
                        int flag = 0;
                        for (int j = 0; j < vdmsDeviceCustomFields.size(); j++) {
                            if (vdmsDeviceCustomFields.getJSONObject(j).getString("column").
                                    equalsIgnoreCase(deviceCustomFieldsList.getJSONObject(i).getString("column"))) {
                                flag = 1;
                            }
                        }
                        if (flag == 0) {
                            vdmsDeviceCustomFields.add(deviceCustomFieldsList.getJSONObject(i));
                        }
                    }
                    vdmsDetailsDTO.setDevice_custom_fields(vdmsDeviceCustomFields.toString());
                } else {
                    vdmsDetailsDTO.setDevice_custom_fields(deviceCustomFieldsList.toString());
                }
            } else {
                vdmsDetailsDTO = new VdmsDetailsDTO();
                vdmsDetailsDTO.setId(Generators.timeBasedGenerator().generate().toString());
                vdmsDetailsDTO.setDevice_custom_fields(deviceCustomFieldsList.toString());
                vdmsDetailsDTO.setVdms_id(vdms_id);
            }
            this.upsertVdmsDeviceCustomFields(vdmsDetailsDTO);
        }
    }

    /**
     * Returns the VDMS device custom fields for the given user and VDMS.
     *
     * @param username the requesting user's name
     * @param vdms_id  the VDMS id
     * @return the VDMS device custom fields details
     */
    public VdmsDetailsDTO getDeviceCustomFields(String username, String vdms_id) {
        return this.getVdmsDeviceCustomFields();
    }

    /**
     * Returns the VDMS details.
     *
     * @return the VDMS details
     */
    public VdmsDTO getVDMSDetails() {
        return vdmsRepository.getVdmsDetails();
    }

    /**
     * Updates the customer organisation id associated with the given VDMS.
     *
     * @param vdms_id         the VDMS id
     * @param customer_org_id the customer organisation id to set
     */
    public void updateCustomerOrgIdByVdmsId(String vdms_id, String customer_org_id) {
        vdmsRepository.updateCustomerOrgIdByVdmsId(vdms_id, customer_org_id);
    }


    private com.alibaba.fastjson.JSONObject getSystemHealth() {
        String[] cmd = {"bash", "-c", "cd scripts/vdms_health_scripts && ./system-health "};
        log.info("cmd : {}", cmd);
        String interfaceResult = utils.execPipedCmd(cmd).get("output");
        log.info("result : {}", interfaceResult);
        com.alibaba.fastjson.JSONObject result = com.alibaba.fastjson.JSONObject
                .parseObject(interfaceResult);

        log.info("response : {}", result);
        return result;
    }

    /**
     * Requests a VDMS access token for the given credentials via {@link APICallClient}.
     *
     * @param vdms_id  the VDMS id
     * @param password the VDMS password
     */
    public void getVdmsAccessToken(String vdms_id, String password) {
        try {
            apiCallService.getVdmsAccessToken(vdms_id, password);
        } catch (Exception e) {
            log.error("Error getting vdms access token: {}", e.getMessage());
        }
    }



    /**
     * Returns the stored VDMS password.
     *
     * @return the VDMS password
     */
    public String getVDMSPassword() {
        return vdmsRepository.getVDMSPassword();
    }



    /**
     * Returns the customer organisation id associated with the given VDMS.
     *
     * @param vdms_id the VDMS id
     * @return the customer organisation id
     */
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        return vdmsRepository.getCustomerOrgIdByVdmsId(vdms_id);
    }

    /**
     * Returns the master flag for the VDMS.
     *
     * @return the master indicator
     */
    public Integer getIsMaster() {
        return vdmsRepository.getIsMaster();
    }
}