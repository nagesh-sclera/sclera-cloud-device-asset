package io.sclera.it;

import io.sclera.Repository.DeviceRepository;
import io.sclera.dto.DeviceDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting integration test for the multi-join DeviceDTO projection pagination queries
 * now implemented with JPA Criteria in DeviceRepositoryImpl (getNetworkParentDeviceByPagination /
 * getAllParentDeviceByPagination). Driven against the device-search fixture on a PG 16 Testcontainer.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceParentDeviceProjectionIT extends PostgresJpaIT {

    @Autowired
    DeviceRepository repo;

    private Set<String> ids(Set<DeviceDTO> r) {
        return r.stream().map(DeviceDTO::getId).collect(Collectors.toSet());
    }

    @Test
    void getAllParentDevices_noSearch_returnsEveryDevice() {
        assertThat(ids(repo.getAllParentDeviceByPagination("null", 50, 0)))
                .containsExactlyInAnyOrder("dsx1", "dsx2", "dsx3", "dsx4", "dsx5", "dsx9");
    }

    @Test
    void getAllParentDevices_search_matchesConcatHaystack() {
        assertThat(ids(repo.getAllParentDeviceByPagination("Foreign", 50, 0))).containsExactly("dsx9");
    }

    @Test
    void getAllParentDevices_projectionUsesUserDataFallback() {
        Set<DeviceDTO> r = repo.getAllParentDeviceByPagination("My-Alpha", 50, 0);
        assertThat(r).hasSize(1);
        DeviceDTO dto = r.iterator().next();
        assertThat(dto.getId()).isEqualTo("dsx1");
        // user_data_name ('My-Alpha') overrides display_name ('Alpha Device')
        assertThat(dto.getDisplay_name()).isEqualTo("My-Alpha");
    }

    @Test
    void getNetworkParentDevices_dockerScope_excludesForeignVdms() {
        assertThat(ids(repo.getNetworkParentDeviceByPagination(
                Set.of("dock1"), Set.of("all"), "null", 50, 0, Set.of("all"))))
                .containsExactlyInAnyOrder("dsx1", "dsx2", "dsx3", "dsx4", "dsx5");
    }

    @Test
    void getNetworkParentDevices_ipVirtualType_excludesNonIp() {
        // vdts=ip -> virtual_device_type IS NULL/0/1 ; dsx5 (vdt=5) excluded.
        assertThat(ids(repo.getNetworkParentDeviceByPagination(
                Set.of("dock1"), Set.of("all"), "null", 50, 0, Set.of("ip"))))
                .containsExactlyInAnyOrder("dsx1", "dsx2", "dsx3", "dsx4");
    }

    @Test
    void getNetworkParentDevices_typeFilter_acrossAllDockers() {
        assertThat(ids(repo.getNetworkParentDeviceByPagination(
                Set.of("all"), Set.of("router"), "null", 50, 0, Set.of("all"))))
                .containsExactlyInAnyOrder("dsx1", "dsx4", "dsx9");
    }

    @Test
    void getNetworkParentDevices_search() {
        assertThat(ids(repo.getNetworkParentDeviceByPagination(
                Set.of("dock1"), Set.of("all"), "Beta", 50, 0, Set.of("all"))))
                .containsExactly("dsx2");
    }
}
