package io.sclera.service;

import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceDTO;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: non-AP-C1 service */
@Service
public class CorrigoService {
    private static final Logger log = LoggerFactory.getLogger(CorrigoService.class);

    public CorrigoConfigurationDTO getCorrigoConfigurationDetails() {
        log.warn("STUB: getCorrigoConfigurationDetails called");
        return null;
    }

    public void updateCorrigoAssets(String username, String vdmsid, Integer pageNo, Integer pageSize, String searchKey, CorrigoConfigurationDTO config) {
        log.warn("STUB: updateCorrigoAssets called");
    }

    public JSONArray getWorkordersByAssetIdForBot(DeviceDTO device) {
        log.warn("STUB: getWorkordersByAssetIdForBot called");
        return null;
    }

    public void corrigoUrlSync(Object url, String vdmsId) {
        log.warn("STUB: corrigoUrlSync called");
    }

    public void updateCorrigoCredentialsFromCloud(String vdmsId) {
        log.warn("STUB: updateCorrigoCredentialsFromCloud called");
    }

    public void updateCorrigoCredentialsMigration(String vdmsId) {
        log.warn("STUB: updateCorrigoCredentialsMigration called");
    }
}
