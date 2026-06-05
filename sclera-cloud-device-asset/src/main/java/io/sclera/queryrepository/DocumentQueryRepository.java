package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting and querying {@code document} records and their device associations.
 */
@Component
public class DocumentQueryRepository {

    /**
     * Returns the SQL for upserting a document, updating its mutable columns on id conflict.
     *
     * @return the parameterized document upsert SQL statement
     */
    public String getQueryForUpsertDocument() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        // PG-port: original MySQL `encrypted_type = VALUES(link)` was a copy-paste bug (assigned the
        //   varchar `link` into the integer `encrypted_type`; MySQL coerced non-numeric->0). Per app-team
        //   decision (2026-05-27) the bug is fixed: update encrypted_type from its own EXCLUDED value.
        return "INSERT INTO document (id , name, category , description, link, created_email, created_timestamp, encrypted_type,source_type) VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name , category = EXCLUDED.category, description = EXCLUDED.description, link = EXCLUDED.link, encrypted_type = EXCLUDED.encrypted_type ";
    }

    /**
     * Returns the SQL for tagging a document to a device by upserting a {@code device_document} link.
     *
     * @return the parameterized document-to-device tag SQL statement
     */
    public String getQueryForTagDocument() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (document_id, device_id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO device_document (document_id , device_id) VALUES (?,?) " +
                "ON CONFLICT (document_id, device_id) DO UPDATE SET document_id = EXCLUDED.document_id, device_id = EXCLUDED.device_id ";
    }

    /**
     * Returns the SQL for refreshing a device's document and media counts from its association tables.
     *
     * @return the parameterized device count-update SQL statement
     */
    public String getCountUpdateQuery() {
        return "UPDATE device SET document_count = (SELECT COUNT(*) FROM device_document WHERE device_id = ?), media_count = (SELECT COUNT(*) FROM device_media WHERE device_id = ?)  WHERE id = ? ";
    }
}
