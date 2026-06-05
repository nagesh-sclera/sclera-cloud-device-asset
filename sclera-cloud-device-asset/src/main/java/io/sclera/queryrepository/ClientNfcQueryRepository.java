package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting client NFC records.
 */
@Component
public class ClientNfcQueryRepository {

    /**
     * Returns the native upsert SQL that inserts a client NFC row or updates it on primary-key conflict.
     *
     * @return the parameterized {@code INSERT ... ON CONFLICT (id) DO UPDATE} statement for {@code client_nfc}
     */
    public String getQueryForUpsertClientNfc() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO client_nfc (id, batch_id, created_by, creation_time, device_id, location_id, nfc_id, uuid, vdms_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?, false) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "batch_id = EXCLUDED.batch_id, "
                + "created_by = EXCLUDED.created_by, "
                + "creation_time = EXCLUDED.creation_time, "
                + "device_id = EXCLUDED.device_id, "
                + "location_id = EXCLUDED.location_id, "
                + "nfc_id = EXCLUDED.nfc_id, "
                + "uuid = EXCLUDED.uuid, "
                + "vdms_id = EXCLUDED.vdms_id, "
                + "is_deleted = false";
    }

}
