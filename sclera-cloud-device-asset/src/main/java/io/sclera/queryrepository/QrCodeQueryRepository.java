package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting QR code records.
 */
@Component
public class QrCodeQueryRepository {

    /**
     * Returns the native upsert SQL that inserts a QR code row or updates it on primary-key conflict.
     *
     * @return the parameterized {@code INSERT ... ON CONFLICT (id) DO UPDATE} statement for {@code qr_code}
     */
    public String getQueryForUpsertQrCodesInBatch() {
        return "INSERT INTO qr_code (id, image_url, location_id, vdms_id, device_id, created_by, creation_time, batch_id, qr_code_link, updated_time, updated_by, is_deleted) "
             + "VALUES (?,?,?,?,?,?,?,?,?,?,?, false) "
             + "ON CONFLICT (id) DO UPDATE SET "
             + "image_url = EXCLUDED.image_url, location_id = EXCLUDED.location_id, vdms_id = EXCLUDED.vdms_id, "
             + "device_id = EXCLUDED.device_id, created_by = EXCLUDED.created_by, creation_time = EXCLUDED.creation_time, "
             + "batch_id = EXCLUDED.batch_id, qr_code_link = EXCLUDED.qr_code_link, updated_time = EXCLUDED.updated_time, "
             + "updated_by = EXCLUDED.updated_by, is_deleted = false";
    }

}
