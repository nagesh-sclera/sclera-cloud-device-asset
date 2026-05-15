//package io.sclera.utils;
//
//import io.sclera.Repository.VdmsRepository;
//import io.sclera.dto.CorrigoConfigurationDTO;
//import io.sclera.dto.CorrigoUserSettingsDTO;
//import io.sclera.service.EssentialService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.PostConstruct;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class CorrigoUserUtils {
//    private static final Logger log = LoggerFactory.getLogger(CorrigoUserUtils.class);
//
//    // private + volatile allows controlled reset while guaranteeing visibility across threads
//    private volatile ConcurrentHashMap<String, CorrigoUserSettingsDTO> corrigo_credentials = new ConcurrentHashMap<>();
//
//    @Autowired
//    EssentialService essentialService;  // example of another service that could be used in reloadAllCredentials()
//
//    @Autowired
//    VdmsRepository vdmsRepository;
//
//    @PostConstruct
//    public void init() {
//        ensureInitialized();
//        log.info("CorrigoUserUtils initialized – credentials cache ready.");
//    }
//
//    /**
//     * Central guard that detects the null-map condition, logs a full stack trace
//     * for root-cause diagnosis, and instantly resets the map.
//     * This guarantees seamless continuation for all callers.
//     */
//    private void ensureInitialized() {
//        if (corrigo_credentials == null) {
//            corrigo_credentials = new ConcurrentHashMap<>();
//            log.error("CRITICAL: Corrigo credentials hashmap was null! Resetting to new ConcurrentHashMap. "
//                    + "Full stack trace for diagnosis:", new Exception("Hashmap-null detection"));
//            reloadAllCredentials();
//            return;
//        }
//        if(corrigo_credentials.isEmpty()){
//            log.error("CRITICAL: Corrigo credentials hashmap was EMPTY! Reloading HashMap. "
//                    + "Full stack trace for diagnosis:", new Exception("Hashmap-empty detection"));
//                reloadAllCredentials();
//        }// attempt seamless repopulation (placeholder below)
//    }
//
//    /**
//     * Placeholder for full reload from persistent storage.
//     * Implement this method (e.g., via CorrigoService or a repository) to restore
//     * all credentials automatically when the map is reset.
//     */
//    private void reloadAllCredentials() {
//        log.info("Initiating full reload of Corrigo credentials after map reset.");
//        // TODO: replace with actual load logic
//        // Example:
//        try {
//            String vdmsId = vdmsRepository.getVDMSId();
//            essentialService.syncUserData(vdmsId);
//            log.info("Successfully reloaded {} Corrigo user credentials.", corrigo_credentials.size());
//        } catch (Exception e) {
//            log.error("Failed to reload credentials after map reset", e);
//        }
//
//    }
//
//    public CorrigoUserSettingsDTO getCredentials(String email) {
//        ensureInitialized();
//        try {
//            CorrigoUserSettingsDTO credentials = corrigo_credentials.get(email);
//            if (credentials == null) {
//                log.warn("Corrigo credentials missing for username: {}.", email);
//                return new CorrigoUserSettingsDTO();
//            }
//            return credentials;
//        } catch (Exception e) {
//            log.error("Exception while fetching Corrigo credentials for username: {}", email, e);
//            return null;
//        }
//    }
//
//    public void setCorrigoCredentials(String email, CorrigoUserSettingsDTO credentials) {
//        log.info("Setting Corrigo credentials for email: {}, credentials null: {}", email, credentials == null);
//        if (credentials != null) {
//            corrigo_credentials.put(email, credentials);
//        }
//    }
//
//    public void setCorrigoCredentialsForSync(String email, CorrigoUserSettingsDTO credentials) {
//        log.info("Setting Corrigo credentials (sync) for email: {}, credentials null: {}", email, credentials == null);
//        if (credentials != null) {
//            corrigo_credentials.put(email, credentials);
//        }
//    }
//
//    public void deleteCorrigoCredentials(String email) {
//        ensureInitialized();
//        log.info("Removing Corrigo credentials for email: {}", email);
//        corrigo_credentials.remove(email);
//    }
//
//    public void setOauth_token(String email, String token) {
//        ensureInitialized();
//        corrigo_credentials.computeIfPresent(email, (k, dto) -> {
//            dto.setOauth_token(token);
//            return dto;
//        });
//    }
//
//    public String getOauth_token(String email) {
//        CorrigoUserSettingsDTO dto = getCredentials(email);
//        return (dto != null) ? dto.getOauth_token() : null;
//    }
//
//    public String getClientId(String email) {
//        CorrigoUserSettingsDTO dto = getCredentials(email);
//        return (dto != null) ? dto.getClient_id() : null;
//    }
//
//    public String getClientSecret(String email) {
//        CorrigoUserSettingsDTO dto = getCredentials(email);
//        return (dto != null) ? dto.getClient_secret() : null;
//    }
//
//    public String getUserEmployeeId(String email) {
//        ensureInitialized();
//        try {
//            CorrigoUserSettingsDTO dto = corrigo_credentials.get(email);
//            return (dto != null) ? dto.getCorrigo_user_id() : null;
//        } catch (Exception e) {
//            log.error("Error retrieving employee ID for user: {}", email, e);
//            return null;
//        }
//    }
//
//    public Map<String, CorrigoUserSettingsDTO> getCorrigo_credentials() {
//        ensureInitialized();
//        log.info("Returning snapshot of corrigo credentials hashmap");
//        return new HashMap<>(corrigo_credentials);   // defensive copy – prevents external mutation
//    }
//
//    public void syncCorrigoData(Map<String, CorrigoUserSettingsDTO> corrigo_credentialsParam,
//                                CorrigoConfigurationDTO corrigoConfigurationDTO) {
////        ensureInitialized(); - need to analyse better if this check is needed ?
//        log.info("syncCorrigoData – upsert Corrigo Configuration");
//        try {
//            for (Map.Entry<String, CorrigoUserSettingsDTO> entry : corrigo_credentialsParam.entrySet()) {
//                CorrigoUserSettingsDTO dto = entry.getValue();
//                if ("default".equals(dto.getCredential_type())) {
//                    dto.setClient_id(corrigoConfigurationDTO.getClient_id());
//                    dto.setClient_secret(corrigoConfigurationDTO.getClient_secret());
//                    this.setCorrigoCredentialsForSync(entry.getKey(), dto);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error in syncCorrigoData", e);
//        }
//    }
//
//    public void syncCorrigoDataForDeleteConfiguration(Map<String, CorrigoUserSettingsDTO> corrigo_credentialsParam) {
////        ensureInitialized(); - need to analyse better if this check is needed ?
//        log.info("syncCorrigoDataForDeleteConfiguration – delete Corrigo Configuration");
//        try {
//            for (Map.Entry<String, CorrigoUserSettingsDTO> entry : corrigo_credentialsParam.entrySet()) {
//                CorrigoUserSettingsDTO dto = entry.getValue();
//                if ("default".equals(dto.getCredential_type())) {
//                    dto.setClient_id(null);
//                    dto.setClient_secret(null);
//                    dto.setOauth_token(null);
//                    this.setCorrigoCredentials(entry.getKey(), dto);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error in syncCorrigoDataForDeleteConfiguration", e);
//        }
//    }
//
//}