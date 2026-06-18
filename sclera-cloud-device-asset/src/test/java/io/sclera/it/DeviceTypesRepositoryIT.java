package io.sclera.it;

import io.sclera.Repository.DeviceTypesRepository;
import io.sclera.dto.DeviceTypesDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-types-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-types-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceTypesRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceTypesRepository deviceTypesRepository;

    @PersistenceContext
    EntityManager em;

    // ---- getAllDeviceTypes ----

    @Test
    void getAllDeviceTypes_returnsAllRows() {
        List<DeviceTypesDTO> result = deviceTypesRepository.getAllDeviceTypes();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(DeviceTypesDTO::getId)
                .containsExactlyInAnyOrder("dt-001", "dt-002", "dt-003");
    }

    @Test
    void getAllDeviceTypes_fieldsPopulated() {
        List<DeviceTypesDTO> result = deviceTypesRepository.getAllDeviceTypes();
        DeviceTypesDTO camera = result.stream()
                .filter(d -> "dt-001".equals(d.getId()))
                .findFirst().orElseThrow();
        assertThat(camera.getName()).isEqualTo("Camera");
        assertThat(camera.getUpdatedTimestamp()).isEqualTo(BigInteger.valueOf(1700000001000L));
        // getAllDeviceTypes uses 3-arg ctor; oldName not populated
        assertThat(camera.getOldName()).isNull();
    }

    // ---- findMaxUpdatedTimestamp ----

    @Test
    void findMaxUpdatedTimestamp_returnsHighestTimestamp() {
        BigInteger max = deviceTypesRepository.findMaxUpdatedTimestamp();
        assertThat(max).isEqualTo(BigInteger.valueOf(1700000003000L));
    }

    // ---- getAllUpdatedDeviceTypes ----

    @Test
    void getAllUpdatedDeviceTypes_returnsOnlyRenamedTypes() {
        List<DeviceTypesDTO> result = deviceTypesRepository.getAllUpdatedDeviceTypes("any-vdms");
        assertThat(result).hasSize(1);
        DeviceTypesDTO dto = result.get(0);
        assertThat(dto.getName()).isEqualTo("SmartSwitch");
        assertThat(dto.getOldName()).isEqualTo("Switch");
        // 2-arg ctor: id and updatedTimestamp not populated
        assertThat(dto.getId()).isNull();
    }

    @Test
    void getAllUpdatedDeviceTypes_nullOldName_excluded() {
        List<DeviceTypesDTO> result = deviceTypesRepository.getAllUpdatedDeviceTypes("any-vdms");
        assertThat(result).extracting(DeviceTypesDTO::getName)
                .doesNotContain("Camera", "Sensor");
    }

    // ---- deleteOldName ----

    @Test
    void deleteOldName_clearsMatchingOldName() {
        deviceTypesRepository.deleteOldName("Switch");
        em.flush();
        em.clear();

        String oldName = (String) em.createQuery(
                "SELECT dt.oldName FROM DeviceTypes dt WHERE dt.id = 'dt-002'")
                .getSingleResult();
        assertThat(oldName).isNull();
    }

    @Test
    void deleteOldName_doesNotAffectNonMatchingRows() {
        deviceTypesRepository.deleteOldName("NonExistent");
        em.flush();
        em.clear();

        String oldName = (String) em.createQuery(
                "SELECT dt.oldName FROM DeviceTypes dt WHERE dt.id = 'dt-002'")
                .getSingleResult();
        assertThat(oldName).isEqualTo("Switch");
    }

    // ---- upsert (native — verify PG compatibility) ----

    @Test
    void upsert_insertsNewRow() {
        deviceTypesRepository.upsert("dt-upsert", "NewType", BigInteger.valueOf(1700000099000L));
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT dt.id, dt.name, dt.updatedTimestamp FROM DeviceTypes dt WHERE dt.id = 'dt-upsert'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("dt-upsert");
        assertThat(row[1]).isEqualTo("NewType");
        assertThat(row[2]).isEqualTo(BigInteger.valueOf(1700000099000L));
    }

    @Test
    void upsert_updatesExistingRow() {
        // Insert first
        deviceTypesRepository.upsert("dt-001", "CameraUpdated", BigInteger.valueOf(1700000010000L));
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT dt.name, dt.updatedTimestamp FROM DeviceTypes dt WHERE dt.id = 'dt-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("CameraUpdated");
        assertThat(row[1]).isEqualTo(BigInteger.valueOf(1700000010000L));
    }
}
