package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class DocumentQueryRepository {

    public String getQueryForUpsertDocument() {
        return "INSERT INTO document (id , name, category , description, link, created_email, created_timestamp, encrypted_type,source_type) VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name) , category = VALUES(category), description = VALUES(description), link = VALUES(link), encrypted_type = VALUES(link) ";
    }

    public String getQueryForTagDocument() {
        return "INSERT INTO device_document (document_id , device_id) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE document_id = VALUES(document_id), device_id = VALUES(device_id) ";
    }

    public String getCountUpdateQuery() {
        return "UPDATE device SET document_count = (SELECT COUNT(*) FROM device_document WHERE device_id = ?), media_count = (SELECT COUNT(*) FROM device_media WHERE device_id = ?)  WHERE id = ? ";
    }
}
