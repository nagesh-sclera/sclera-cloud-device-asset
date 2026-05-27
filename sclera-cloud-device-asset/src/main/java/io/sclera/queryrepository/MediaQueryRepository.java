package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class MediaQueryRepository {

    public String getQueryForUpsertMedia() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO media (id , name, category , description, link, created_email, created_timestamp, extension,source_type) VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name , category = EXCLUDED.category, description = EXCLUDED.description, link = EXCLUDED.link  ";
    }

    public String getQueryForTagDocument() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (device_id, media_id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO device_media (media_id , device_id) VALUES (?,?) " +
                "ON CONFLICT (media_id, device_id) DO UPDATE SET media_id = EXCLUDED.media_id, device_id = EXCLUDED.device_id ";
    }
}
