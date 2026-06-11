package io.sclera.it;

import io.sclera.Repository.DeviceIPAddressRepository;
import io.sclera.dto.touchscreen.DeviceIPAddressDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-ip-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-ip-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceIPAddressRepositoryIT extends PostgresJpaIT {

    @Autowired DeviceIPAddressRepository repo;

    @Test
    void getIPAddressByDeviceId_returnsProjection() {
        List<DeviceIPAddressDTO> result = repo.getIPAddressByDeviceId("dev1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIp_address()).isEqualTo("10.0.0.5");
        assertThat(result.get(0).getIp_conflict_status()).isEqualTo(0);
    }

    @Test
    void deleteIPAddressByDeviceId_removesRows() {
        repo.deleteIPAddressByDeviceId("dev1");
        assertThat(repo.getIPAddressByDeviceId("dev1")).isEmpty();
    }
}
