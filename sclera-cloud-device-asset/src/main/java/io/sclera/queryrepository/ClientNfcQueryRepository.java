package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class ClientNfcQueryRepository {

    public String getQueryForUpsertClientNfc() {
        return "INSERT INTO client_nfc (id, batch_id, created_by, creation_time, device_id, location_id, nfc_id, uuid, vdms_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?, false) "
                + "ON DUPLICATE KEY UPDATE "
                + "batch_id = VALUES(batch_id), "
                + "created_by = VALUES(created_by), "
                + "creation_time = VALUES(creation_time), "
                + "device_id = VALUES(device_id), "
                + "location_id = VALUES(location_id), "
                + "nfc_id = VALUES(nfc_id), "
                + "uuid = VALUES(uuid), "
                + "vdms_id = VALUES(vdms_id), "
                + "is_deleted = false;";
    }

}
