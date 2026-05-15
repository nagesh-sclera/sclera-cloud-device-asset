package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class MediaQueryRepository {

    public String getQueryForUpsertMedia() {
        return "INSERT INTO media (id , name, category , description, link, created_email, created_timestamp, extension,source_type) VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name) , category = VALUES(category), description = VALUES(description), link = VALUES(link)  ";
    }

    public String getQueryForTagDocument() {
        return "INSERT INTO device_media (media_id , device_id) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE media_id = VALUES(media_id), device_id = VALUES(device_id) ";
    }
}
