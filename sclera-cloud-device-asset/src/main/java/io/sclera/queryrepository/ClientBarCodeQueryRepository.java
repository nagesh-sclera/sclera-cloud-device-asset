package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class ClientBarCodeQueryRepository {


    public String getQueryForUpsertClientBarCode() {
        return "INSERT INTO client_bar_code (id, added_at, added_by, client_bar_code_id, device_id, location_id, updated_at, updated_by, vdms_id, batch_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?, false) "
                + "ON DUPLICATE KEY UPDATE "
                + "added_at = VALUES(added_at), "
                + "added_by = VALUES(added_by), "
                + "client_bar_code_id = VALUES(client_bar_code_id), "
                + "device_id = VALUES(device_id), "
                + "location_id = VALUES(location_id), "
                + "updated_at = VALUES(updated_at), "
                + "updated_by = VALUES(updated_by), "
                + "vdms_id = VALUES(vdms_id), "
                + "batch_id = VALUES(batch_id), "
                + "is_deleted = false;";
    }

}
