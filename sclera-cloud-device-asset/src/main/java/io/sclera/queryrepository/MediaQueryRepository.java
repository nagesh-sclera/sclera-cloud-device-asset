package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Provides SQL statements for persisting media records and their device associations.
 */
@Component
public class MediaQueryRepository {

    /**
     * Returns the SQL statement that inserts a media record or updates it on id conflict.
     *
     * @return the parameterized upsert SQL for a media record
     */
    public String getQueryForUpsertMedia() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO media (id , name, category , description, link, created_email, created_timestamp, extension,source_type) VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name , category = EXCLUDED.category, description = EXCLUDED.description, link = EXCLUDED.link  ";
    }

    /**
     * Returns the SQL statement that links a media record to a device, upserting on the device/media key conflict.
     *
     * @return the parameterized upsert SQL for a device-media association
     */
    public String getQueryForTagDocument() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (device_id, media_id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO device_media (media_id , device_id) VALUES (?,?) " +
                "ON CONFLICT (media_id, device_id) DO UPDATE SET media_id = EXCLUDED.media_id, device_id = EXCLUDED.device_id ";
    }
}
