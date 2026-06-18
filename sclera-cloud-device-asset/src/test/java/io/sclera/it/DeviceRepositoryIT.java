package io.sclera.it;

import io.sclera.Repository.DeviceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceRepository deviceRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(deviceRepository).isNotNull();
    }

    @Test
    void getAllDeviceCount_returnsTotal() {
        assertThat(deviceRepository.getAllDeviceCount()).isEqualTo(2);
    }

    @Test
    void getDeviceStatus_returnsStatus() {
        assertThat(deviceRepository.getDeviceStatus("d1")).isEqualTo(1);
    }

    @Test
    void updateDeviceSnmpCount_updatesCount() {
        deviceRepository.updateDeviceSnmpCount("d1", 5);
        em.flush();
        em.clear();
        Integer count = em.createQuery(
                "SELECT d.snmp_count FROM Device d WHERE d.id = 'd1'", Integer.class)
                .getSingleResult();
        assertThat(count).isEqualTo(5);
    }

    @Test
    void getParentDeviceNameById_usesUserDataNameWhenPresent() {
        assertThat(deviceRepository.getParentDeviceNameById("d1")).isEqualTo("MyDevice");
    }

    @Test
    void getParentDeviceNameById_fallsBackToDisplayName() {
        assertThat(deviceRepository.getParentDeviceNameById("d2")).isEqualTo("Device2");
    }

    @Test
    void getDeviceNameById_usesUserDataName() {
        assertThat(deviceRepository.getDeviceNameById("d1")).isEqualTo("MyDevice");
    }

    @Test
    void getDeviceNameById_fallsBackToDisplayName() {
        assertThat(deviceRepository.getDeviceNameById("d2")).isEqualTo("Device2");
    }

    @Test
    void getAllDeviceCount_afterInsert_incrementsCount() {
        // Use a native INSERT to avoid triggering eager-loading of Phonebook/Docker relations
        em.createNativeQuery("INSERT INTO device(id, display_name) VALUES ('d3', 'Device3')").executeUpdate();
        em.flush();
        em.clear();
        assertThat(deviceRepository.getAllDeviceCount()).isEqualTo(3);
    }

    @Test
    void getSubsystemParentId_returnsParent() {
        assertThat(deviceRepository.getSubsystemParentId("d2")).isEqualTo("d1");
    }

    @Test
    void getDevicesBySubSystemParentId_returnsChildren() {
        assertThat(deviceRepository.getDevicesBySubSystemParentId("d1")).contains("d2");
    }

    @Test
    void updateSubsystemParentDevice_setsParent() {
        deviceRepository.updateSubsystemParentDevice("d1", "d2");
        em.flush();
        em.clear();
        String parent = em.createQuery(
                "SELECT d.subsystem_parent_id FROM Device d WHERE d.id = 'd1'", String.class)
                .getSingleResult();
        assertThat(parent).isEqualTo("d2");
    }

    @Test
    void updateDeviceAssetStatus_updatesFields() {
        deviceRepository.updateDeviceAssetStatus(2, "d1", "user@test.com", java.math.BigInteger.valueOf(9999L));
        em.flush();
        em.clear();
        Integer status = em.createQuery(
                "SELECT d.asset_match_status FROM Device d WHERE d.id = 'd1'", Integer.class)
                .getSingleResult();
        assertThat(status).isEqualTo(2);
    }
}
