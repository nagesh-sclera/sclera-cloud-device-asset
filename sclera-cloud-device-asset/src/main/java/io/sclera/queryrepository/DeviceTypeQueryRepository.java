package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class DeviceTypeQueryRepository {

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
