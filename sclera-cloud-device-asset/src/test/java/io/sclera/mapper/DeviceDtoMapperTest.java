package io.sclera.mapper;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.projection.DeviceDetailRow;
import io.sclera.models.Device;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoMapperTest {

    private final DeviceDtoMapper mapper = org.mapstruct.factory.Mappers.getMapper(DeviceDtoMapper.class);

    private DeviceDetailRow row(Device d) {
        return new DeviceDetailRow(d,
                "docker-1", "vdms-9",
                "Room 101", "loc-1", "Floor 1", "flr-1", "Tower A", "bld-1",
                "lv-1", "gv-1", "o1", "o2", "o3",
                "tech@x.com",
                "obs-1", "assignee@x.com", 1, 0, 1, 0,
                "trk-7");
    }

    @Test
    void mapsScalars_associations_joins_andConversions() {
        Device d = new Device();
        d.setId("dev-1");
        d.setDisplay_name("Boiler-1");
        d.setType("hvac");                 // -> type AND system_type
        d.setSnmp_count(3);
        d.setMac_address("AA:BB:CC");
        d.setLast_seen_on(new BigInteger("1699999999")); // BigInteger -> String
        d.setAlarm(5);                      // Integer -> String

        DeviceDTO dto = mapper.toDto(row(d));

        assertThat(dto.getId()).isEqualTo("dev-1");
        assertThat(dto.getDisplay_name()).isEqualTo("Boiler-1");
        assertThat(dto.getType()).isEqualTo("hvac");
        assertThat(dto.getSystem_type()).isEqualTo("hvac");
        assertThat(dto.getSnmp_count()).isEqualTo(3);
        assertThat(dto.getLast_seen_on()).isEqualTo("1699999999");  // converted
        assertThat(dto.getAlarm()).isEqualTo("5");                  // converted
        assertThat(dto.getDocker_name()).isEqualTo("docker-1");
        assertThat(dto.getVdms_id()).isEqualTo("vdms-9");
        assertThat(dto.getLocation()).isEqualTo("Room 101");
        assertThat(dto.getBuilding()).isEqualTo("Tower A");
        assertThat(dto.getLocal_vendor_id()).isEqualTo("lv-1");
        assertThat(dto.getAssigned_user_email()).isEqualTo("tech@x.com");
        assertThat(dto.getInventory_tracking_id()).isEqualTo("trk-7");
    }

    @Test
    void nullConversions_stayNull() {
        Device d = new Device();
        d.setId("dev-2");                  // last_seen_on / alarm left null
        DeviceDTO dto = mapper.toDto(row(d));
        assertThat(dto.getLast_seen_on()).isNull();
        assertThat(dto.getAlarm()).isNull();
    }
}
