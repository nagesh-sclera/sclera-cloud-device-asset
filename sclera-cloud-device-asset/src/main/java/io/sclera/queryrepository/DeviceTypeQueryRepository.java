package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

/**
 * Supplies native SQL statements for persisting and querying {@code device_types} records.
 */
@Component
public class DeviceTypeQueryRepository {

    /**
     * Returns the SQL for batch-upserting device types, updating name and timestamp on id conflict.
     *
     * @return the parameterized upsert SQL statement
     */
    public String getQueryForUpsertDeviceTypesInBatch() {
        // PG-port: ON DUPLICATE KEY UPDATE -> ON CONFLICT (id) DO UPDATE SET ... (VALUES()->EXCLUDED)
        return "INSERT INTO device_types (id, name, updated_timestamp) \n" +
                "VALUES (?,?,?) \n" +
                "ON CONFLICT (id) DO UPDATE SET \n" +
                "    old_name = device_types.name, " +
                "    name = EXCLUDED.name,\n" +
                "    updated_timestamp = EXCLUDED.updated_timestamp\n";
    }


}
