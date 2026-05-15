package io.sclera.queryrepository;

import org.springframework.stereotype.Component;

@Component
public class DeviceTypeQueryRepository {

    public String getQueryForUpsertDeviceTypesInBatch() {
        return "INSERT INTO device_types (id, name, updated_timestamp) \n" +
                "VALUES (?,?,?) \n" +
                "ON DUPLICATE KEY UPDATE \n" +
                "    old_name = name, " +
                "    name = VALUES(name),\n" +
                "    updated_timestamp = VALUES(updated_timestamp);\n";
    }


}
