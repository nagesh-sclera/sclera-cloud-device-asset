package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting client bar code records.
 */
@Component
public class ClientBarCodeQueryRepository {


    /**
     * Returns the native upsert SQL that inserts a client bar code row or updates it on primary-key conflict.
     *
     * @return the parameterized {@code INSERT ... ON CONFLICT (id) DO UPDATE} statement for {@code client_bar_code}
     */
    public String getQueryForUpsertClientBarCode() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO client_bar_code (id, added_at, added_by, client_bar_code_id, device_id, location_id, updated_at, updated_by, vdms_id, batch_id, is_deleted) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?, false) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "added_at = EXCLUDED.added_at, "
                + "added_by = EXCLUDED.added_by, "
                + "client_bar_code_id = EXCLUDED.client_bar_code_id, "
                + "device_id = EXCLUDED.device_id, "
                + "location_id = EXCLUDED.location_id, "
                + "updated_at = EXCLUDED.updated_at, "
                + "updated_by = EXCLUDED.updated_by, "
                + "vdms_id = EXCLUDED.vdms_id, "
                + "batch_id = EXCLUDED.batch_id, "
                + "is_deleted = false";
    }

}
